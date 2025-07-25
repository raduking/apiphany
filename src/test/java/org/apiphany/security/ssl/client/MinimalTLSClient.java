package org.apiphany.security.ssl.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt64;
import org.apiphany.lang.BinaryRepresentable;
import org.apiphany.lang.Bytes;
import org.apiphany.lang.Hex;
import org.apiphany.lang.Strings;
import org.apiphany.security.ssl.DeterministicSecureRandom;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.tls.AdditionalAuthenticatedData;
import org.apiphany.security.tls.Alert;
import org.apiphany.security.tls.AlertDescription;
import org.apiphany.security.tls.AlertLevel;
import org.apiphany.security.tls.ApplicationData;
import org.apiphany.security.tls.Certificates;
import org.apiphany.security.tls.ChangeCipherSpec;
import org.apiphany.security.tls.CipherSuite;
import org.apiphany.security.tls.ClientHello;
import org.apiphany.security.tls.ClientKeyExchange;
import org.apiphany.security.tls.ECDHEPublicKey;
import org.apiphany.security.tls.Encrypted;
import org.apiphany.security.tls.Finished;
import org.apiphany.security.tls.Handshake;
import org.apiphany.security.tls.KeyExchangeAlgorithm;
import org.apiphany.security.tls.NamedCurve;
import org.apiphany.security.tls.RSAEncryptedPreMaster;
import org.apiphany.security.tls.Record;
import org.apiphany.security.tls.RecordContentType;
import org.apiphany.security.tls.ServerHello;
import org.apiphany.security.tls.ServerHelloDone;
import org.apiphany.security.tls.ServerKeyExchange;
import org.apiphany.security.tls.SignatureAlgorithm;
import org.apiphany.security.tls.TLSKeyExchange;
import org.apiphany.security.tls.Version;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ThrowingBiFunction;
import org.morphix.lang.function.ThrowingConsumer;
import org.morphix.lang.function.ThrowingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal TLS Client implementation using simple {@link Socket} connection.
 *
 * @see <a target="_blank" href="https://tls12.xargs.org/">The Illustrated TLS 1.2 Connection</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class MinimalTLSClient implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MinimalTLSClient.class);

	public static final Duration DEFAULT_SOCKET_TIMEOUT = Duration.ofSeconds(1);

	public static final List<NamedCurve> SUPPORTED_NAMED_CURVES = List.of(
			NamedCurve.X25519
	);

	public static final List<CipherSuite> SUPPORTED_CIPHER_SUITES = List.of(
			CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
			CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384
	);

	private final String host;
	private final int port;
	private final Duration socketTimeout = DEFAULT_SOCKET_TIMEOUT;
	private final SSLProtocol sslProtocol = SSLProtocol.TLS_1_2;

	private Socket tcpSocket;
	private DataOutputStream out;
	private InputStream in;

	private List<CipherSuite> cipherSuites;

	private byte[] clientRandom;
	private byte[] serverRandom;

	private byte[] preMasterSecret;
	private byte[] masterSecret;

	private long clientSequenceNumber = 0;
	private long serverSequenceNumber = 0;

	private KeyPair clientKeyPair;
	private PublicKey serverPublicKey;

	private ExchangeKeys exchangeKeys;

	private final List<Handshake> handshakeMessages = new ArrayList<>();

	public MinimalTLSClient(final String host, final int port, final KeyPair clientKeyPair, final List<CipherSuite> cipherSuites) {
		this.host = host;
		this.port = port;
		this.clientKeyPair = clientKeyPair;
		this.cipherSuites = cipherSuites;
	}

	@Override
	public void close() throws Exception {
		for (Closeable closeable : Arrays.asList(in, out, tcpSocket)) {
			try {
				Nullables.whenNotNull(closeable, ThrowingConsumer.unchecked(Closeable::close));
			} catch (Exception e) {
				LOGGER.error("Error while closing", e);
			}
		}
	}

	public void connect() throws IOException {
		tcpSocket = new Socket(host, port);
		tcpSocket.setSoTimeout((int) socketTimeout.toMillis());
		out = new DataOutputStream(tcpSocket.getOutputStream());
		in = tcpSocket.getInputStream();

		LOGGER.debug("TCP connection established.");
		LOGGER.debug("Connected to: {}:{}, local port: {}",
				tcpSocket.getInetAddress(), tcpSocket.getPort(), tcpSocket.getLocalPort());
	}

	public void sendRecord(final byte[] bytes) throws IOException {
		out.write(bytes);
		out.flush();
	}

	public byte[] performHandshake() throws Exception {
		connect();

		// 1. Send Client Hello
		Record clientHelloRecord = sendClientHello();

		ClientHello clientHello = clientHelloRecord.getHandshake().get(ClientHello.class);
		this.clientRandom = clientHello.getClientRandom().getRandom();
		LOGGER.debug("Client random:\n{}", Hex.dump(clientRandom));

		// 2. Receive Server Hello
		Record serverHelloRecord = Record.from(in);
		accumulateHandshakes(serverHelloRecord.getFragments(Handshake.class));

		// 2a. Server Hello
		ServerHello serverHello = serverHelloRecord.getHandshake(ServerHello.class);
		this.serverRandom = serverHello.getServerRandom().toByteArray();
		LOGGER.debug("Server random:\n{}", Hex.dump(serverRandom));
		LOGGER.debug("Received Server Hello:\n{}", Hex.dump(serverHello.toByteArray()));
		CipherSuite serverCipherSuite = serverHello.getCipherSuite();

		// 2b. Server Certificates
		if (!serverHelloRecord.hasHandshake(Certificates.class)) {
			serverHelloRecord = Record.from(in);
			accumulateHandshakes(serverHelloRecord.getFragments(Handshake.class));
		}
		Certificates certificates = serverHelloRecord.getHandshake(Certificates.class);
		X509Certificate x509Certificate = parseCertificate(certificates.getList().getFirst().getData().toByteArray());
		LOGGER.debug("Received Server Certificate:\n{}", Hex.dump(certificates.toByteArray()));
		LOGGER.debug("Received Server X509Certificate: {}", x509Certificate);

		// 2b. Server Key Exchange (RSA cipher suites don't have a server key exchange)
		ServerKeyExchange serverKeyExchange = null;
		if (KeyExchangeAlgorithm.ECDHE == serverCipherSuite.keyExchange()) {
			if (!serverHelloRecord.hasHandshake(ServerKeyExchange.class)) {
				serverHelloRecord = Record.from(in);
				accumulateHandshakes(serverHelloRecord.getFragments(Handshake.class));
			}
			serverKeyExchange = serverHelloRecord.getHandshake(ServerKeyExchange.class);
			LOGGER.debug("Received Server Key Exchange:\n{}", Hex.dump(serverKeyExchange.toByteArray()));
		}

		// 2b. Server Hello Done
		if (!serverHelloRecord.hasHandshake(ServerHelloDone.class)) {
			serverHelloRecord = Record.from(in);
			accumulateHandshakes(serverHelloRecord.getFragments(Handshake.class));
		}
		ServerHelloDone serverHelloDone = serverHelloRecord.getHandshake(ServerHelloDone.class);
		LOGGER.debug("Received Server Hello Done:\n{}", Hex.dump(serverHelloDone.toByteArray()));

		// 3. Generate Client Key Exchange
		TLSKeyExchange tlsKeyExchange;
		switch (serverCipherSuite.keyExchange()) {
			case ECDHE -> {
				byte[] leServerPublic = Objects.requireNonNull(serverKeyExchange).getPublicKey().getValue().toByteArray();
				LOGGER.debug("Server public key (raw bytes from key exchange):\n{}", Hex.dump(leServerPublic));
				X25519Keys keys = new X25519Keys();
				byte[] clientPublicBytes = getClientPublicBytes(serverKeyExchange, keys);
				tlsKeyExchange = new ECDHEPublicKey(clientPublicBytes);
				LOGGER.debug("Server public key ({}):\n{}", serverPublicKey.getClass(), serverPublicKey);
				LOGGER.debug("Keys match: {}", keys.verifyKeyMatch(keys.toRawByteArray(serverPublicKey), serverPublicKey));
				// Compute shared secret
				this.preMasterSecret = keys.getSharedSecret(clientKeyPair.getPrivate(), serverPublicKey);
				LOGGER.debug("Pre Master Secret:\n{}", Hex.dump(preMasterSecret));
			}
			case RSA -> {
				this.serverPublicKey = x509Certificate.getPublicKey();
				this.preMasterSecret =
						Bytes.concatenate(new Version(SSLProtocol.TLS_1_2).toByteArray(), DeterministicSecureRandom.generateLinear(46));
				// Encrypt pre-master with server's RSA key
				Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				rsa.init(Cipher.ENCRYPT_MODE, serverPublicKey);
				byte[] encryptedPreMaster = rsa.doFinal(preMasterSecret);
				tlsKeyExchange = new RSAEncryptedPreMaster(encryptedPreMaster);
			}
			default -> throw new SSLException("Unsupported cipher suite: " + serverCipherSuite);
		}

		// 4. Send Client Key Exchange
		Record clientKeyExchangeRecord = new Record(sslProtocol, new ClientKeyExchange(tlsKeyExchange));
		byte[] clientKeyExchangeBytes = clientKeyExchangeRecord.toByteArray();

		sendRecord(clientKeyExchangeBytes);
		accumulateHandshakes(clientKeyExchangeRecord.getFragments(Handshake.class));
		LOGGER.debug("Sent Client Key Exchange:\n{}", Hex.dump(clientKeyExchangeBytes));

		// 5. Derive Master Secret and Keys
		this.masterSecret = PseudoRandomFunction.apply(preMasterSecret, "master secret", Bytes.concatenate(clientRandom, serverRandom), 48);
		LOGGER.debug("Master secret:\n{}", Hex.dump(masterSecret));
		byte[] keyBlock = PseudoRandomFunction.apply(masterSecret, "key expansion",
				Bytes.concatenate(serverRandom, clientRandom), (ExchangeKeys.KEY_LENGTH + ExchangeKeys.IV_LENGTH) * 2);
		// Extract keys
		exchangeKeys = ExchangeKeys.from(keyBlock, ExchangeKeys.Type.AEAD);

		// 6. Send Client Change Cipher Spec
		Record changeCypherSpecRecord = new Record(sslProtocol, new ChangeCipherSpec());
		sendRecord(changeCypherSpecRecord.toByteArray());
		LOGGER.debug("Sent Client Change Cipher Spec:\n{}", Hex.dump(changeCypherSpecRecord.toByteArray()));

		// 7. Send Finished
		byte[] handshakeBytes = getConcatenatedHandshakeMessages();
		LOGGER.debug("Concatenated handshake message content types:\n{}", getConcatenatedHandshakeMessageTypes());
		LOGGER.debug("Handshake transcript ({} bytes):\n{}", handshakeBytes.length, Hex.dump(handshakeBytes));

		byte[] handshakeHash = sha("SHA-384", handshakeBytes);
		LOGGER.debug("Handshake hash:\n{}", Hex.dump(handshakeHash));

		byte[] clientVerifyData = PseudoRandomFunction.apply(masterSecret, "client finished", handshakeHash, 12);
		LOGGER.debug("Computed Client verify data:\n{}", Hex.dump(clientVerifyData));
		Handshake clientFinishedHandshake = new Handshake(new Finished(clientVerifyData));

		Encrypted encrypted = encrypt(clientFinishedHandshake, RecordContentType.HANDSHAKE, exchangeKeys);
		Record clientFinished = new Record(SSLProtocol.TLS_1_2, encrypted);

		byte[] clientFinishedBytes = clientFinished.toByteArray();
		sendRecord(clientFinishedBytes);
		LOGGER.debug("Sent Client Finished Message:\n{}", Hex.dump(clientFinishedBytes));

		// 8. Receive ChangeCipherSpec and Finished
		Record serverChangeCipherSpec = Record.from(in); // type 0x14
		LOGGER.debug("Received Change Cipher Spec:\n{}", Hex.dump(serverChangeCipherSpec.toByteArray()));

		// 9. Receive Server Finished Record
		Record serverFinishedRecord = Record.from(in, ThrowingBiFunction.unchecked((is, total) -> Encrypted.from(is, total, 8)));
		LOGGER.debug("Received Server Finished Record:\n{}", Hex.dump(serverFinishedRecord.toByteArray()));

		// 10. Decrypt finished
		byte[] decrypted = decrypt(serverFinishedRecord, RecordContentType.HANDSHAKE, exchangeKeys);
		Handshake serverFinishedHandshake = Handshake.from(new ByteArrayInputStream(decrypted));
		LOGGER.debug("Received Server Finished (decrypted):\n{}", Hex.dump(serverFinishedHandshake.toByteArray()));

		// 11. Compute server verify data and validate
		accumulateHandshake(clientFinishedHandshake);
		handshakeBytes = getConcatenatedHandshakeMessages();
		handshakeHash = sha("SHA-384", handshakeBytes);

		LOGGER.debug("Handshake hash:\n{}", Hex.dump(handshakeHash));
		byte[] computedVerifyData = PseudoRandomFunction.apply(masterSecret, "server finished", handshakeHash, 12);
		LOGGER.debug("Computed Server verify data:\n{}", Hex.dump(computedVerifyData));
		byte[] serverVerifyData = serverFinishedHandshake.get(Finished.class).getVerifyData().toByteArray();
		LOGGER.debug("Received Server verify data:\n{}", Hex.dump(serverVerifyData));
		if (Arrays.equals(computedVerifyData, serverVerifyData)) {
			LOGGER.debug("Server Finished verification SUCCESS!");
		} else {
			throw new SecurityException("Server Finished verification FAILED!");
		}

		LOGGER.info("{} handshake complete!", SSLProtocol.TLS_1_2);
		return serverFinishedRecord.toByteArray();
	}

	public byte[] closeNotify() throws Exception {
		Alert closeAlert = new Alert(AlertLevel.WARNING, AlertDescription.CLOSE_NOTIFY);
		Encrypted encrypted = encrypt(closeAlert, RecordContentType.ALERT, exchangeKeys);

		Record closeAlertRecord = new Record(SSLProtocol.TLS_1_2, encrypted);
		byte[] closeAlertBytes = closeAlertRecord.toByteArray();
		sendRecord(closeAlertBytes);
		LOGGER.debug("Sent Client Close Notify:\n{}", Hex.dump(closeAlertBytes));

		return closeAlertBytes;
	}

	public String get(final String path) throws Exception {
		String request =
				"GET " + path + " HTTP/1.1\r\n" +
						"Host: " + host + "\r\n" +
						"Connection: close\r\n\r\n";

		byte[] requestBytes = request.getBytes(StandardCharsets.US_ASCII);
		Encrypted encrypted = encrypt(new BytesWrapper(requestBytes), RecordContentType.APPLICATION_DATA, exchangeKeys);
		Record requestRecord = new Record(SSLProtocol.TLS_1_2, new ApplicationData(encrypted));
		sendRecord(requestRecord.toByteArray());
		LOGGER.debug("Sent Application Data Record:\n{}", Hex.dump(requestRecord.toByteArray()));

		Record responseRecord = Record.from(in, ThrowingBiFunction.unchecked((is, total) -> Encrypted.from(is, total, 8)));
		LOGGER.debug("Received Application Data Record:\n{}", Hex.dump(responseRecord.toByteArray()));
		byte[] decrypted = decrypt(responseRecord, RecordContentType.APPLICATION_DATA, exchangeKeys);

		String response = new String(decrypted, StandardCharsets.US_ASCII);
		LOGGER.debug("Received HTTP GET Response:\n{}", response);

		HttpResponseParser httpResponseParser = new HttpResponseParser(response);
		int contentLength = Integer.parseInt(httpResponseParser.getHeader("content-length"));
		StringBuilder responseBuilder = new StringBuilder();
		String body = httpResponseParser.getBody();
		responseBuilder.append(body);

		int currentLength = body.length();
		while (contentLength > currentLength) {
			responseRecord = Record.from(in, ThrowingBiFunction.unchecked((is, total) -> Encrypted.from(is, total, 8)));
			LOGGER.debug("Received Application Data Record:\n{}", Hex.dump(responseRecord.toByteArray()));
			decrypted = decrypt(responseRecord, RecordContentType.APPLICATION_DATA, exchangeKeys);

			body = new String(decrypted, StandardCharsets.US_ASCII);
			LOGGER.debug("Received HTTP GET Content:\n{}", body);

			responseBuilder.append(body);
			currentLength += decrypted.length;
		}
		return responseBuilder.toString();
	}

	private Record sendClientHello() throws IOException {
		ClientHello clientHello =
				new ClientHello(new SecureRandom(), cipherSuites, List.of(host), SUPPORTED_NAMED_CURVES, SignatureAlgorithm.STRONG_ALGORITHMS);
		Record clientHelloRecord = new Record(SSLProtocol.TLS_1_0, clientHello);
		byte[] clientHelloBytes = clientHelloRecord.toByteArray();
		sendRecord(clientHelloBytes);
		accumulateHandshakes(clientHelloRecord.getFragments(Handshake.class));
		LOGGER.debug("Sent Client Hello:\n{}", Hex.dump(clientHelloBytes));
		return clientHelloRecord;
	}

	public X509Certificate parseCertificate(final byte[] certData) throws Exception {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certData));
	}

	public byte[] getClientPublicBytes(final ServerKeyExchange ske, final TLSKeysHandler keys) throws Exception {
		byte[] serverPubBytes = ske.getPublicKey().getValue().toByteArray();
		this.serverPublicKey = keys.getPublicKeyLE(serverPubBytes);
		if (null == clientKeyPair) {
			this.clientKeyPair = keys.generateKeyPair();
		}
		return keys.toRawByteArray(clientKeyPair.getPublic());
	}

	public Encrypted encrypt(final BinaryRepresentable tlsObject, final RecordContentType type, final ExchangeKeys keys) throws Exception {
		byte[] plaintext = tlsObject.toByteArray();
		LOGGER.debug("Plaintext:\n{}", Hex.dump(plaintext));

		long seq = this.clientSequenceNumber++;
		byte[] explicitNonce = UInt64.toByteArray(seq); // 8 bytes

		byte[] fixedIV = keys.getClientIV(); // 4 bytes
		byte[] fullIV = new byte[12];
		System.arraycopy(fixedIV, 0, fullIV, 0, 4);
		System.arraycopy(explicitNonce, 0, fullIV, 4, 8);

		short aadLength = (short) plaintext.length;
		AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData(seq, type, SSLProtocol.TLS_1_2, aadLength);
		LOGGER.debug("Encrypt AAD:\n{}", Hex.dump(aad.toByteArray()));

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec spec = new GCMParameterSpec(128, fullIV);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keys.getClientWriteKey(), "AES"), spec);
		cipher.updateAAD(aad.toByteArray());

		byte[] encrypted = cipher.doFinal(plaintext);
		LOGGER.debug("Encrypted (ciphertext + tag):\n{}", Hex.dump(encrypted));

		return new Encrypted(explicitNonce, encrypted);
	}

	public byte[] decrypt(final Record tlsRecord, final RecordContentType type, final ExchangeKeys keys) throws Exception {
		Encrypted encrypted = tlsRecord.getFragments(Encrypted.class).getFirst();

		long seq = this.serverSequenceNumber++;
		byte[] explicitNonce = encrypted.getNonce().toByteArray();

		byte[] fixedIV = keys.getServerIV(); // 4 bytes
		byte[] fullIV = new byte[12];
		System.arraycopy(fixedIV, 0, fullIV, 0, 4);
		System.arraycopy(explicitNonce, 0, fullIV, 4, 8);

		short aadLength = (short) (encrypted.getEncryptedData().toByteArray().length - 16);
		AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData(seq, type, SSLProtocol.TLS_1_2, aadLength);
		LOGGER.debug("Decrypt AAD:\n{}", Hex.dump(aad.toByteArray()));

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec spec = new GCMParameterSpec(128, fullIV);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keys.getServerWriteKey(), "AES"), spec);
		cipher.updateAAD(aad.toByteArray());

		byte[] decrypted = cipher.doFinal(encrypted.getEncryptedData().toByteArray());
		LOGGER.debug("Decrypted:\n{}", Hex.dump(decrypted));

		return decrypted;
	}

	public void accumulateHandshake(final Handshake handshake) {
		LOGGER.debug("Accumulate handshake: {}", handshake.getBody().getType());
		handshakeMessages.add(handshake);
	}

	public void accumulateHandshakes(final List<Handshake> handshakes) {
		for (Handshake handshake : handshakes) {
			accumulateHandshake(handshake);
		}
	}

	public byte[] getConcatenatedHandshakeMessages() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			for (Handshake handshake : handshakeMessages) {
				dos.write(handshake.toByteArray());
			}
		}).run();
		return bos.toByteArray();
	}

	public String getConcatenatedHandshakeMessageTypes() {
		StringBuilder stringBuilder = new StringBuilder();
		for (Handshake handshake : handshakeMessages) {
			stringBuilder.append(handshake.getHeader().getType());
			stringBuilder.append(Strings.EOL);
		}
		return stringBuilder.toString();
	}

	public static byte[] sha(final String algorithm, final byte[] input) throws Exception {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		return digest.digest(input);
	}
}
