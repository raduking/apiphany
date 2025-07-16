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
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;

import org.apiphany.lang.Hex;
import org.apiphany.lang.Strings;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ThrowingBiFunction;
import org.morphix.lang.function.ThrowingConsumer;
import org.morphix.lang.function.ThrowingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimalTLSClient implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MinimalTLSClient.class);

	private final String host;
	private final int port;

	private Socket tcpSocket;
	private DataOutputStream out;
	private InputStream in;

	private byte[] clientRandom;
	private byte[] serverRandom;

	private byte[] preMasterSecret;
	private byte[] masterSecret;

	private long clientSequenceNumber = 0;
	private long serverSequenceNumber = 0;

	private KeyPair clientKeyPair;
	private PublicKey serverPublicKey;

	private ExchangeKeys exchangeKeys;

	private final List<TLSHandshake> handshakeMessages = new ArrayList<>();

	public MinimalTLSClient(final String host, final int port, final KeyPair clientKeyPair) {
		this.host = host;
		this.port = port;
		this.clientKeyPair = clientKeyPair;
	}

	public MinimalTLSClient(final String host, final int port) {
		this(host, port, null);
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
		out = new DataOutputStream(tcpSocket.getOutputStream());
		in = tcpSocket.getInputStream();

		LOGGER.debug("TCP connection established.");
		LOGGER.debug("Connected to: {}:{}, local port: {}",
				tcpSocket.getInetAddress(), tcpSocket.getPort(), tcpSocket.getLocalPort());
	}

	public void sendTLSRecord(final byte[] bytes) throws IOException {
		out.write(bytes);
		out.flush();
	}

	public byte[] performHandshake() throws Exception {
		connect();

		// 1. Send Client Hello
		TLSRecord clientHelloRecord = sendClientHello();

		ClientHello clientHello = clientHelloRecord.getHandshake().get(ClientHello.class);
		this.clientRandom = clientHello.getClientRandom().getRandom();
		LOGGER.debug("Client random:\n{}", Hex.dump(clientRandom));

		// 2. Receive Server Hello
		TLSRecord serverHelloRecord = receiveServerHello();

		// 2a. Server Hello
		ServerHello serverHello = serverHelloRecord.getHandshake(ServerHello.class);
		this.serverRandom = serverHello.getServerRandom().toByteArray();
		LOGGER.debug("Server random:\n{}", Hex.dump(serverRandom));
		LOGGER.debug("Received Server Hello:\n{}", Hex.dump(serverHello.toByteArray()));

		// 2b. Server Certificates
		if (!serverHelloRecord.hasHandshake(Certificates.class)) {
			serverHelloRecord = TLSRecord.from(in);
			accumulateHandshakes(serverHelloRecord.getFragments(TLSHandshake.class));
		}
		Certificates certificates = serverHelloRecord.getHandshake(Certificates.class);
		X509Certificate x509Certificate = parseCertificate(certificates.getList().getFirst().getData().getBytes());
		LOGGER.debug("Received Server Certificate:\n{}", Hex.dump(certificates.toByteArray()));
		LOGGER.debug("Received Server X509Certificate: {}", x509Certificate);

		// 2b. Server Key Exchange
		if (!serverHelloRecord.hasHandshake(ServerKeyExchange.class)) {
			serverHelloRecord = TLSRecord.from(in);
			accumulateHandshakes(serverHelloRecord.getFragments(TLSHandshake.class));
		}
		ServerKeyExchange serverKeyExchange = serverHelloRecord.getHandshake(ServerKeyExchange.class);
		LOGGER.debug("Received Server Key Exchange:\n{}", Hex.dump(serverKeyExchange.toByteArray()));

		// 2b. Server Hello Done
		if (!serverHelloRecord.hasHandshake(ServerHelloDone.class)) {
			serverHelloRecord = TLSRecord.from(in);
			accumulateHandshakes(serverHelloRecord.getFragments(TLSHandshake.class));
		}
		ServerHelloDone serverHelloDone = serverHelloRecord.getHandshake(ServerHelloDone.class);
		LOGGER.debug("Received Server Hello Done:\n{}", Hex.dump(serverHelloDone.toByteArray()));

		// 3. Generate Client Key Exchange
		CipherSuiteName selectedCipher = serverHello.getCipherSuite().getCipher();
		byte[] clientPublic;
		if (serverKeyExchange.getCurveInfo().getName() == CurveName.X25519) {
			byte[] leServerPublic = serverKeyExchange.getPublicKey().getValue().getBytes();
			LOGGER.debug("Server public key (raw bytes from key exchange):\n{}", Hex.dump(leServerPublic));
			clientPublic = getX25519ClientPublicBytes(serverKeyExchange);
			LOGGER.debug("Server public key ({}):\n{}", serverPublicKey.getClass(), serverPublicKey);
			LOGGER.debug("Keys match: {}", X25519Keys.verifyKeyMatch(X25519Keys.toRawByteArray(serverPublicKey), serverPublicKey));
		} else {
			throw new SSLException("Unsupported cipher suite: " + selectedCipher);
		}
		// Compute shared secret
		this.preMasterSecret = X25519Keys.getECDHESharedSecret(clientKeyPair.getPrivate(), serverPublicKey);
		LOGGER.debug("Pre Master Secret:\n{}", Hex.dump(preMasterSecret));

		// 4. Send Client Key Exchange
		TLSRecord clientKeyExchangeRecord = new TLSRecord(SSLProtocol.TLS_1_2, new ClientKeyExchange(clientPublic));
		byte[] clientKeyExchangeBytes = clientKeyExchangeRecord.toByteArray();

		sendTLSRecord(clientKeyExchangeBytes);
		accumulateHandshakes(clientKeyExchangeRecord.getFragments(TLSHandshake.class));
		LOGGER.debug("Sent Client Key Exchange:\n{}", Hex.dump(clientKeyExchangeBytes));

		// 5. Derive Master Secret and Keys
		this.masterSecret = PseudoRandomFunction.apply(preMasterSecret, "master secret", Bytes.concatenate(clientRandom, serverRandom), 48);
		LOGGER.debug("Master secret:\n{}", Hex.dump(masterSecret));
		byte[] keyBlock = PseudoRandomFunction.apply(masterSecret, "key expansion",
				Bytes.concatenate(serverRandom, clientRandom), (ExchangeKeys.KEY_LENGTH + ExchangeKeys.IV_LENGTH) * 2);
		// Extract keys
		exchangeKeys = ExchangeKeys.from(keyBlock, ExchangeKeys.Type.AHEAD);

		// 6. Send Client Change Cipher Spec
		TLSRecord changeCypherSpecRecord = new TLSRecord(SSLProtocol.TLS_1_2, new ChangeCipherSpec());
		sendTLSRecord(changeCypherSpecRecord.toByteArray());
		LOGGER.debug("Sent Client Change Cipher Spec:\n{}", Hex.dump(changeCypherSpecRecord.toByteArray()));

		// 7. Send Finished
		byte[] handshakeBytes = getConcatenatedHandshakeMessages();
		LOGGER.debug("Concatenated handshake message content types:\n{}", getConcatenatedHandshakeMessageTypes());
		LOGGER.debug("Handshake transcript ({} bytes):\n{}", handshakeBytes.length, Hex.dump(handshakeBytes));

		byte[] handshakeHash = sha("SHA-384", handshakeBytes);
		LOGGER.debug("Handshake hash:\n{}", Hex.dump(handshakeHash));

		byte[] clientVerifyData = PseudoRandomFunction.apply(masterSecret, "client finished", handshakeHash, 12);
		LOGGER.debug("Computed Client verify data:\n{}", Hex.dump(clientVerifyData));
		TLSHandshake clientFinishedHandshake = new TLSHandshake(new Finished(clientVerifyData));

		Encrypted encrypted = encrypt(clientFinishedHandshake, RecordContentType.HANDSHAKE, exchangeKeys);
		TLSRecord clientFinished = new TLSRecord(SSLProtocol.TLS_1_2, encrypted);

		byte[] clientFinishedBytes = clientFinished.toByteArray();
		sendTLSRecord(clientFinishedBytes);
		LOGGER.debug("Sent Client Finished Message:\n{}", Hex.dump(clientFinishedBytes));

		// 8. Receive ChangeCipherSpec and Finished
		TLSRecord serverChangeCipherSpec = TLSRecord.from(in); // type 0x14
		LOGGER.debug("Received Change Cipher Spec:\n{}", Hex.dump(serverChangeCipherSpec.toByteArray()));

		// 9. Receive Server Finished Record
		TLSRecord serverFinishedRecord = TLSRecord.from(in, ThrowingBiFunction.unchecked((is, total) -> Encrypted.from(is, total, 8)));
		LOGGER.debug("Received Server Finished Record:\n{}", Hex.dump(serverFinishedRecord.toByteArray()));

		// 10. Decrypt finished
		byte[] decrypted = decrypt(serverFinishedRecord, RecordContentType.HANDSHAKE, exchangeKeys);
		TLSHandshake serverFinishedHandshake = TLSHandshake.from(new ByteArrayInputStream(decrypted));
		LOGGER.debug("Received Server Finished (decrypted):\n{}", Hex.dump(serverFinishedHandshake.toByteArray()));

		// 11. Compute server verify data and validate
		accumulateHandshake(clientFinishedHandshake);
		handshakeBytes = getConcatenatedHandshakeMessages();
		handshakeHash = sha("SHA-384", handshakeBytes);

		LOGGER.debug("Handshake hash:\n{}", Hex.dump(handshakeHash));
		byte[] computedVerifyData = PseudoRandomFunction.apply(masterSecret, "server finished", handshakeHash, 12);
		LOGGER.debug("Computed Server verify data:\n{}", Hex.dump(computedVerifyData));
		byte[] serverVerifyData = serverFinishedHandshake.get(Finished.class).getVerifyData().getBytes();
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

		TLSRecord closeAlertRecord = new TLSRecord(SSLProtocol.TLS_1_2, encrypted);
		byte[] closeAlertBytes = closeAlertRecord.toByteArray();
		sendTLSRecord(closeAlertBytes);
		LOGGER.debug("Sent Client Close Notify:\n{}", Hex.dump(closeAlertBytes));

		return closeAlertBytes;
	}

	public String get(final String path) throws Exception {
		String request =
				"GET " + path + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "Connection: close\r\n\r\n";

		byte[] requestBytes = request.getBytes(StandardCharsets.US_ASCII);
		Encrypted encrypted = encrypt(new BinaryData(requestBytes), RecordContentType.APPLICATION_DATA, exchangeKeys);
		TLSRecord requestRecord = new TLSRecord(SSLProtocol.TLS_1_2, new ApplicationData(encrypted));
		sendTLSRecord(requestRecord.toByteArray());
		LOGGER.debug("Sent Application Data Record: {}", Hex.dump(requestRecord.toByteArray()));

		TLSRecord responseRecord = TLSRecord.from(in, ThrowingBiFunction.unchecked((is, total) -> Encrypted.from(is, total, 8)));
		LOGGER.debug("Received Application Data Record:\n{}", Hex.dump(responseRecord.toByteArray()));
		byte[] decrypted = decrypt(responseRecord, RecordContentType.APPLICATION_DATA, exchangeKeys);

		String response = new String(decrypted, StandardCharsets.US_ASCII);
		LOGGER.debug("Received HTTP Response:\n{}", response);

		HttpResponseParser httpResponseParser = new HttpResponseParser(response);
		int contentLength = Integer.parseInt(httpResponseParser.getHeader("content-length"));
		if (contentLength > 0) {
			responseRecord = TLSRecord.from(in, ThrowingBiFunction.unchecked((is, total) -> Encrypted.from(is, total, 8)));
			LOGGER.debug("Received Application Data Record:\n{}", Hex.dump(responseRecord.toByteArray()));
			decrypted = decrypt(responseRecord, RecordContentType.APPLICATION_DATA, exchangeKeys);

			response = new String(decrypted, StandardCharsets.US_ASCII);
			LOGGER.debug("Received GET Response:\n{}", response);
		}
		return response;
	}

	private TLSRecord sendClientHello() throws IOException {
		TLSRecord clientHelloRecord = new TLSRecord(SSLProtocol.TLS_1_0,
				new ClientHello(List.of(host), new CipherSuites(CipherSuiteName.values()), List.of(CurveName.values())));
		byte[] clientHelloBytes = clientHelloRecord.toByteArray();
		sendTLSRecord(clientHelloBytes);
		accumulateHandshakes(clientHelloRecord.getFragments(TLSHandshake.class));
		LOGGER.debug("Sent Client Hello:\n{}", Hex.dump(clientHelloBytes));
		return clientHelloRecord;
	}

	private TLSRecord receiveServerHello() throws IOException {
		TLSRecord serverHelloRecord = TLSRecord.from(in);
		accumulateHandshakes(serverHelloRecord.getFragments(TLSHandshake.class));
		return serverHelloRecord;
	}

	public X509Certificate parseCertificate(final byte[] certData) throws Exception {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certData));
	}

	public byte[] getX25519ClientPublicBytes(final ServerKeyExchange ske) throws Exception {
		byte[] serverPubBytes = ske.getPublicKey().getValue().getBytes();
		this.serverPublicKey = X25519Keys.getPublicKeyLE(serverPubBytes);
		if (null == clientKeyPair) {
			this.clientKeyPair = X25519Keys.generateKeyPair();
		}
		return X25519Keys.toRawByteArray(clientKeyPair.getPublic());
	}

	public Encrypted encrypt(final TLSObject tlsObject, final RecordContentType type, final ExchangeKeys keys) throws Exception {
		byte[] plaintext = tlsObject.toByteArray();
		LOGGER.debug("Plaintext:\n{}", Hex.dump(plaintext));

		long seq = this.clientSequenceNumber++;
		byte[] explicitNonce = Int64.toByteArray(seq); // 8 bytes

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

	public byte[] decrypt(final TLSRecord tlsRecord, final RecordContentType type, final ExchangeKeys keys) throws Exception {
		Encrypted encrypted = tlsRecord.getFragments(Encrypted.class).getFirst();

		long seq = this.serverSequenceNumber++;
		byte[] explicitNonce = encrypted.getNonce().getBytes();

		byte[] fixedIV = keys.getServerIV(); // 4 bytes
		byte[] fullIV = new byte[12];
		System.arraycopy(fixedIV, 0, fullIV, 0, 4);
		System.arraycopy(explicitNonce, 0, fullIV, 4, 8);

		short aadLength = (short) (encrypted.getEncryptedData().getBytes().length - 16);
		AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData(seq, type, SSLProtocol.TLS_1_2, aadLength);
		LOGGER.debug("Decrypt AAD:\n{}", Hex.dump(aad.toByteArray()));

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec spec = new GCMParameterSpec(128, fullIV);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keys.getServerWriteKey(), "AES"), spec);
		cipher.updateAAD(aad.toByteArray());

		byte[] decrypted = cipher.doFinal(encrypted.getEncryptedData().getBytes());
		LOGGER.debug("Decrypted:\n{}", Hex.dump(decrypted));

		return decrypted;
	}

	public void accumulateHandshake(final TLSHandshake handshake) {
		LOGGER.debug("Accumulate handshake: {}", handshake.getBody().type());
		handshakeMessages.add(handshake);
	}

	public void accumulateHandshakes(final List<TLSHandshake> handshakes) {
		for (TLSHandshake handshake : handshakes) {
			accumulateHandshake(handshake);
		}
	}

	public byte[] getConcatenatedHandshakeMessages() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			for (TLSHandshake handshake : handshakeMessages) {
				dos.write(handshake.toByteArray());
			}
		}).run();
		return bos.toByteArray();
	}

	public String getConcatenatedHandshakeMessageTypes() {
		StringBuilder stringBuilder = new StringBuilder();
		for (TLSHandshake handshake : handshakeMessages) {
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
