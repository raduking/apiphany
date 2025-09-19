package org.apiphany.security.tls.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.net.ssl.SSLException;

import org.apiphany.http.HttpResponseParser;
import org.apiphany.io.BinaryRepresentable;
import org.apiphany.io.ByteBufferInputStream;
import org.apiphany.io.ByteSizeable;
import org.apiphany.io.BytesOrder;
import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt64;
import org.apiphany.lang.Bytes;
import org.apiphany.lang.Hex;
import org.apiphany.lang.Strings;
import org.apiphany.security.KeyExchangeHandler;
import org.apiphany.security.MessageDigestAlgorithm;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.tls.AdditionalAuthenticatedData;
import org.apiphany.security.tls.Alert;
import org.apiphany.security.tls.AlertDescription;
import org.apiphany.security.tls.AlertLevel;
import org.apiphany.security.tls.ApplicationData;
import org.apiphany.security.tls.BulkCipher;
import org.apiphany.security.tls.Certificates;
import org.apiphany.security.tls.ChangeCipherSpec;
import org.apiphany.security.tls.CipherSuite;
import org.apiphany.security.tls.CipherType;
import org.apiphany.security.tls.ClientHello;
import org.apiphany.security.tls.ClientKeyExchange;
import org.apiphany.security.tls.ECDHEPublicKey;
import org.apiphany.security.tls.Encrypted;
import org.apiphany.security.tls.EncryptedAlert;
import org.apiphany.security.tls.EncryptedHandshake;
import org.apiphany.security.tls.ExchangeKeys;
import org.apiphany.security.tls.ExchangeRandom;
import org.apiphany.security.tls.Finished;
import org.apiphany.security.tls.Handshake;
import org.apiphany.security.tls.KeyExchangeAlgorithm;
import org.apiphany.security.tls.NamedCurve;
import org.apiphany.security.tls.PRF;
import org.apiphany.security.tls.PRFLabel;
import org.apiphany.security.tls.RSAEncryptedPreMaster;
import org.apiphany.security.tls.Record;
import org.apiphany.security.tls.RecordContentType;
import org.apiphany.security.tls.ServerHello;
import org.apiphany.security.tls.ServerHelloDone;
import org.apiphany.security.tls.ServerKeyExchange;
import org.apiphany.security.tls.SignatureAlgorithm;
import org.apiphany.security.tls.TLSEncryptedObject;
import org.apiphany.security.tls.TLSKeyExchange;
import org.apiphany.security.tls.Version;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ThrowingConsumer;
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
			NamedCurve.X25519);

	public static final List<CipherSuite> SUPPORTED_CIPHER_SUITES = List.of(
			CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
			CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384);

	private final String host;
	private final int port;
	private final Duration socketTimeout = DEFAULT_SOCKET_TIMEOUT;
	private final SSLProtocol sslProtocol = SSLProtocol.TLS_1_2;

	private Socket tcpSocket;
	private OutputStream out;
	private InputStream in;

	private final List<CipherSuite> cipherSuites;

	private long clientSequenceNumber = 0;
	private long serverSequenceNumber = 0;

	private KeyPair clientKeyPair;
	private PublicKey serverPublicKey;

	private CipherSuite serverCipherSuite;
	private ExchangeKeys exchangeKeys;

	private final List<Handshake> handshakeMessages = new ArrayList<>();

	public MinimalTLSClient(final String host, final int port, final KeyPair clientKeyPair, final List<CipherSuite> cipherSuites) {
		this.host = host;
		this.port = port;
		this.clientKeyPair = clientKeyPair;
		for (CipherSuite cipherSuite : cipherSuites) {
			if (!SUPPORTED_CIPHER_SUITES.contains(cipherSuite)) {
				throw new IllegalArgumentException("Unsupported cipher suite: " + cipherSuite);
			}
		}
		this.cipherSuites = cipherSuites;
	}

	@Override
	public void close() throws Exception {
		for (Closeable closeable : List.of(in, out, tcpSocket)) {
			try {
				Nullables.whenNotNull(closeable, ThrowingConsumer.unchecked(Closeable::close));
			} catch (Exception e) {
				LOGGER.error("Error while closing", e);
			}
		}
	}

	public void connect() throws IOException {
		tcpSocket = new Socket(host, port);
		tcpSocket.setSoTimeout(Math.toIntExact(socketTimeout.toMillis()));
		out = tcpSocket.getOutputStream();
		in = tcpSocket.getInputStream();

		LOGGER.debug("TCP connection established.");
		LOGGER.debug("Connected to: {}:{}, local port: {}",
				tcpSocket.getInetAddress(), tcpSocket.getPort(), tcpSocket.getLocalPort());
	}

	public void sendRecord(final Record tlsRecord) throws IOException {
		byte[] bytes = tlsRecord.toByteArray();
		out.write(bytes);
		out.flush();
		LOGGER.debug("Sent TLS Record {}:{}", String.join(",", tlsRecord.getFragmentNames()), tlsRecord);
	}

	public Record receiveRecord() throws IOException {
		Record tlsRecord = Record.from(in);
		LOGGER.debug("Received TLS Record {}:{}", String.join(",", tlsRecord.getFragmentNames()), tlsRecord);
		return tlsRecord;
	}

	public byte[] performHandshake() throws Exception {
		connect();

		// 1. Send Client Hello
		ClientHello clientHello =
				new ClientHello(new SecureRandom(), cipherSuites, List.of(host), SUPPORTED_NAMED_CURVES, SignatureAlgorithm.STRONG_ALGORITHMS);
		Record clientHelloRecord = new Record(SSLProtocol.TLS_1_0, clientHello);
		sendRecord(clientHelloRecord);
		accumulateHandshakes(clientHelloRecord.getFragments(Handshake.class));

		byte[] clientRandom = clientHello.getClientRandom().getRandom();
		LOGGER.debug("Client random:\n{}", Hex.dump(clientRandom));

		// 2. Receive Server Hello
		Record tlsRecord = receiveRecord();
		accumulateHandshakes(tlsRecord.getFragments(Handshake.class));

		// 2a. Server Hello
		ServerHello serverHello = tlsRecord.getHandshake(ServerHello.class);
		byte[] serverRandom = serverHello.getServerRandom().toByteArray();
		LOGGER.debug("Server random:\n{}", Hex.dump(serverRandom));
		this.serverCipherSuite = serverHello.getCipherSuite();
		MessageDigestAlgorithm messageDigest = serverCipherSuite.messageDigest();
		String prfAlgorithm = messageDigest.hmacName();

		// 2b. Server Certificates
		if (tlsRecord.hasNoHandshake(Certificates.class)) {
			tlsRecord = receiveRecord();
			accumulateHandshakes(tlsRecord.getFragments(Handshake.class));
		}
		Certificates certificates = tlsRecord.getHandshake(Certificates.class);
		X509Certificate x509Certificate = certificates.getList().getFirst().toX509Certificate();
		LOGGER.debug("Received Server X509Certificate: {}", x509Certificate);

		// 2b. Server Key Exchange (RSA cipher suites don't have a server key exchange)
		ServerKeyExchange serverKeyExchange = null;
		if (KeyExchangeAlgorithm.ECDHE == serverCipherSuite.keyExchange()) {
			if (tlsRecord.hasNoHandshake(ServerKeyExchange.class)) {
				tlsRecord = receiveRecord();
				accumulateHandshakes(tlsRecord.getFragments(Handshake.class));
			}
			serverKeyExchange = tlsRecord.getHandshake(ServerKeyExchange.class);
		}

		// 2b. Server Hello Done
		if (tlsRecord.hasNoHandshake(ServerHelloDone.class)) {
			tlsRecord = receiveRecord();
			accumulateHandshakes(tlsRecord.getFragments(Handshake.class));
		}

		// 3. Generate Client Key Exchange
		TLSKeyExchange tlsKeyExchange;
		byte[] preMasterSecret;
		switch (serverCipherSuite.keyExchange()) {
			case ECDHE -> {
				byte[] serverPublicLittleEndian = Objects.requireNonNull(serverKeyExchange).getPublicKey().getValue().toByteArray();
				LOGGER.debug("Server public key (raw bytes from key exchange):\n{}", Hex.dump(serverPublicLittleEndian));
				X25519Keys keys = new X25519Keys();
				byte[] clientPublicBytes = getClientPublicBytes(serverKeyExchange, keys);
				LOGGER.debug("Server public key ({}):\n{}", serverPublicKey.getClass(), serverPublicKey);
				// Compute shared secret (pre master secret)
				preMasterSecret = keys.getSharedSecret(clientKeyPair.getPrivate(), serverPublicKey);
				tlsKeyExchange = new ECDHEPublicKey(clientPublicBytes);
			}
			case RSA -> {
				this.serverPublicKey = x509Certificate.getPublicKey();
				preMasterSecret = Bytes.concatenate(new Version(sslProtocol).toByteArray(), ExchangeRandom.generate(46));
				// Encrypt pre-master with server's RSA key
				Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				rsa.init(Cipher.ENCRYPT_MODE, serverPublicKey);
				byte[] encryptedPreMaster = rsa.doFinal(preMasterSecret);
				tlsKeyExchange = new RSAEncryptedPreMaster(encryptedPreMaster);
			}
			default -> throw new SSLException("Unsupported cipher suite: " + serverCipherSuite);
		}
		LOGGER.debug("Pre Master Secret:\n{}", Hex.dump(preMasterSecret));

		// 4. Send Client Key Exchange
		Record clientKeyExchangeRecord = new Record(sslProtocol, new ClientKeyExchange(tlsKeyExchange));
		sendRecord(clientKeyExchangeRecord);
		accumulateHandshakes(clientKeyExchangeRecord.getFragments(Handshake.class));

		// 5. Derive Master Secret and Keys
		byte[] masterSecret = PRF.apply(preMasterSecret, PRFLabel.MASTER_SECRET,
				Bytes.concatenate(clientRandom, serverRandom), SSLProtocol.TLS_1_2_MASTER_SECRET_LENGTH, prfAlgorithm);
		LOGGER.debug("Master secret:\n{}", Hex.dump(masterSecret));
		// Derive key block length dynamically based on server cipher suite
		int keyBlockLength = serverCipherSuite.totalKeyBlockLength();
		byte[] keyBlock = PRF.apply(masterSecret, PRFLabel.KEY_EXPANSION,
				Bytes.concatenate(serverRandom, clientRandom), keyBlockLength, prfAlgorithm);
		// Extract keys
		exchangeKeys = ExchangeKeys.from(keyBlock, serverCipherSuite);

		// 6. Send Client Change Cipher Spec
		Record changeCypherSpecRecord = new Record(sslProtocol, new ChangeCipherSpec());
		sendRecord(changeCypherSpecRecord);

		// 7. Send Finished
		byte[] handshakeBytes = getConcatenatedHandshakeMessages();
		LOGGER.debug("Concatenated handshake message content types:\n{}", getConcatenatedHandshakeMessageTypes());
		LOGGER.debug("Handshake transcript ({} bytes):\n{}", handshakeBytes.length, Hex.dump(handshakeBytes));

		byte[] handshakeHash = messageDigest.digest(handshakeBytes);
		LOGGER.debug("Handshake hash:\n{}", Hex.dump(handshakeHash));

		byte[] clientVerifyData = PRF.apply(masterSecret, PRFLabel.CLIENT_FINISHED, handshakeHash, 12, prfAlgorithm);
		LOGGER.debug("Computed Client verify data:\n{}", Hex.dump(clientVerifyData));
		Handshake clientFinishedHandshake = new Handshake(new Finished(clientVerifyData));

		Encrypted encrypted = encrypt(clientFinishedHandshake, RecordContentType.HANDSHAKE, exchangeKeys);
		Record clientFinished = new Record(sslProtocol, new EncryptedHandshake(encrypted));
		sendRecord(clientFinished);

		// 8. Receive ChangeCipherSpec and Finished
		@SuppressWarnings("unused")
		Record serverChangeCipherSpec = receiveRecord();

		// 9. Receive Server Finished Record
		Record serverFinishedRecord = Record.from(in, EncryptedHandshake::from);

		// 10. Decrypt finished
		byte[] decrypted = decrypt(serverFinishedRecord, exchangeKeys);
		@SuppressWarnings("resource")
		Handshake serverFinishedHandshake = Handshake.from(ByteBufferInputStream.of(decrypted));
		LOGGER.debug("Received Server Finished (decrypted):{}", serverFinishedHandshake);
		Finished serverFinished = serverFinishedHandshake.get(Finished.class);

		// 11. Compute server verify data and validate
		accumulateHandshake(clientFinishedHandshake);
		handshakeBytes = getConcatenatedHandshakeMessages();
		handshakeHash = messageDigest.digest(handshakeBytes);
		LOGGER.debug("Handshake hash:\n{}", Hex.dump(handshakeHash));

		byte[] computedVerifyData = PRF.apply(masterSecret, PRFLabel.SERVER_FINISHED, handshakeHash, 12, prfAlgorithm);
		LOGGER.debug("Computed Server verify data:\n{}", Hex.dump(computedVerifyData));
		byte[] serverVerifyData = serverFinished.getVerifyData().toByteArray();
		LOGGER.debug("Received Server verify data:\n{}", Hex.dump(serverVerifyData));
		if (Arrays.equals(computedVerifyData, serverVerifyData)) {
			LOGGER.debug("Server Finished verification SUCCESS!");
		} else {
			throw new SecurityException("Server Finished verification FAILED!");
		}

		LOGGER.info("{} handshake complete!", sslProtocol);
		return serverFinishedRecord.toByteArray();
	}

	public byte[] closeNotify() throws Exception {
		Alert closeAlert = new Alert(AlertLevel.WARNING, AlertDescription.CLOSE_NOTIFY);
		Encrypted encrypted = encrypt(closeAlert, RecordContentType.ALERT, exchangeKeys);

		Record closeAlertRecord = new Record(sslProtocol, new EncryptedAlert(encrypted));
		sendRecord(closeAlertRecord);

		return closeAlertRecord.toByteArray();
	}

	public String get(final String path) throws Exception {
		String request =
				"GET " + path + " HTTP/1.1\r\n" +
						"Host: " + host + "\r\n" +
						"Connection: close\r\n\r\n";

		sendApplicationData(request);

		String response = receiveApplicationData();
		HttpResponseParser parser = new HttpResponseParser(response);
		while (!parser.isComplete()) {
			response = receiveApplicationData();
			parser.appendData(response);
		}
		return parser.getBody();
	}

	private void sendApplicationData(final String request) throws Exception {
		LOGGER.debug("Sending request:\n{}", request);
		byte[] requestBytes = request.getBytes(StandardCharsets.US_ASCII);
		Encrypted encrypted = encrypt(new BytesWrapper(requestBytes), RecordContentType.APPLICATION_DATA, exchangeKeys);
		Record requestRecord = new Record(sslProtocol, new ApplicationData(encrypted));
		sendRecord(requestRecord);
	}

	private String receiveApplicationData() throws Exception {
		Record responseRecord = receiveRecord();
		LOGGER.debug("Received Application Data Record:{}", responseRecord);
		byte[] decrypted = decrypt(responseRecord, exchangeKeys);
		String content = new String(decrypted, StandardCharsets.US_ASCII);
		LOGGER.debug("Received content:\n{}", content);
		return content;
	}

	private static byte[] buildFullIV(final byte[] fixedIV, final byte[] explicitNonce, final BulkCipher bulkCipher) {
		if (bulkCipher.hasExplicitNonce()) {
			byte[] iv = new byte[bulkCipher.fullIVLength()];
			System.arraycopy(fixedIV, 0, iv, 0, bulkCipher.fixedIvLength());
			System.arraycopy(explicitNonce, 0, iv, bulkCipher.fixedIvLength(), bulkCipher.explicitNonceLength());
			return iv;
		}
		// No explicit nonce — the fixedIV (or a derived nonce) is the full IV.
		return fixedIV != null ? fixedIV.clone() : new byte[bulkCipher.fullIVLength()];
	}

	public Encrypted encrypt(final BinaryRepresentable tlsObject, final RecordContentType type, final ExchangeKeys keys) throws Exception {
		byte[] plaintext = tlsObject.toByteArray();
		LOGGER.debug("Plaintext:\n{}", Hex.dump(plaintext));

		BulkCipher bulkCipher = serverCipherSuite.bulkCipher();
		CipherType cipherType = bulkCipher.type();

		long seq = this.clientSequenceNumber++;
		byte[] explicitNonce = UInt64.toByteArray(seq);

		switch (cipherType) {
			case AEAD -> {
				byte[] fullIV = buildFullIV(keys.getClientIV(), explicitNonce, bulkCipher);

				short aadLength = (short) plaintext.length;
				AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData(seq, type, sslProtocol, aadLength);
				LOGGER.debug("Encrypt AAD: {}", aad);

				Cipher cipher = bulkCipher.cipher(Cipher.ENCRYPT_MODE, keys.getClientWriteKey(), bulkCipher.spec(fullIV));
				cipher.updateAAD(aad.toByteArray());

				byte[] encrypted = cipher.doFinal(plaintext);
				LOGGER.debug("Encrypted (ciphertext + tag):\n{}", Hex.dump(encrypted));
				return new Encrypted(explicitNonce, encrypted);
			}
			case BLOCK -> {
				Cipher cipher = bulkCipher.cipher(Cipher.ENCRYPT_MODE, keys.getClientWriteKey(), bulkCipher.spec(keys.getClientIV()));
				return new Encrypted(Bytes.EMPTY, cipher.doFinal(plaintext));
			}
			case STREAM -> {
				Cipher cipher = bulkCipher.cipher(Cipher.ENCRYPT_MODE, keys.getClientWriteKey(), bulkCipher.spec(Bytes.EMPTY));
				return new Encrypted(Bytes.EMPTY, cipher.doFinal(plaintext));
			}
			case NO_ENCRYPTION -> {
				return new Encrypted(Bytes.EMPTY, plaintext);
			}
			default -> throw new IllegalStateException("Unknown cipher type: " + cipherType);
		}
	}

	public byte[] decrypt(final Record tlsRecord, final ExchangeKeys keys) throws Exception {
		BulkCipher bulkCipher = serverCipherSuite.bulkCipher();
		CipherType cipherType = bulkCipher.type();

		long seq = this.serverSequenceNumber++;
		RecordContentType type = tlsRecord.getHeader().getType();
		Encrypted encrypted = tlsRecord.getFragment(TLSEncryptedObject.class).getEncrypted();

		switch (cipherType) {
			case AEAD -> {
				byte[] explicitNonce = encrypted.getNonce().toByteArray();
				byte[] fullIV = buildFullIV(keys.getServerIV(), explicitNonce, bulkCipher);

				short aadLength = (short) (encrypted.getEncryptedData().toByteArray().length - bulkCipher.tagLength());
				AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData(seq, type, sslProtocol, aadLength);
				LOGGER.debug("Decrypt AAD: {}", aad);

				Cipher cipher = bulkCipher.cipher(Cipher.DECRYPT_MODE, keys.getServerWriteKey(), bulkCipher.spec(fullIV));
				cipher.updateAAD(aad.toByteArray());

				byte[] decrypted = cipher.doFinal(encrypted.getEncryptedData().toByteArray());
				LOGGER.debug("Decrypted:\n{}", Hex.dump(decrypted));
				return decrypted;
			}
			case BLOCK -> {
				Cipher cipher = bulkCipher.cipher(Cipher.DECRYPT_MODE, keys.getServerWriteKey(), bulkCipher.spec(keys.getServerIV()));
				return cipher.doFinal(encrypted.getEncryptedData().toByteArray());
			}
			case STREAM -> {
				Cipher cipher = bulkCipher.cipher(Cipher.DECRYPT_MODE, keys.getServerWriteKey(), bulkCipher.spec(Bytes.EMPTY));
				return cipher.doFinal(encrypted.getEncryptedData().toByteArray());
			}
			case NO_ENCRYPTION -> {
				return encrypted.getEncryptedData().toByteArray();
			}
			default -> throw new IllegalStateException("Unknown cipher type: " + cipherType);
		}
	}

	public byte[] getClientPublicBytes(final ServerKeyExchange ske, final KeyExchangeHandler keys) {
		byte[] serverPubBytes = ske.getPublicKey().getValue().toByteArray();
		this.serverPublicKey = keys.publicKeyFrom(serverPubBytes, BytesOrder.LITTLE_ENDIAN);
		if (null == clientKeyPair) {
			this.clientKeyPair = keys.generateKeyPair();
		}
		return keys.toByteArray(clientKeyPair.getPublic(), BytesOrder.LITTLE_ENDIAN);
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
		ByteBuffer buffer = ByteBuffer.allocate(ByteSizeable.sizeOf(handshakeMessages));
		for (Handshake handshake : handshakeMessages) {
			buffer.put(handshake.toByteArray());
		}
		return buffer.array();
	}

	public String getConcatenatedHandshakeMessageTypes() {
		StringBuilder stringBuilder = new StringBuilder();
		for (Handshake handshake : handshakeMessages) {
			stringBuilder.append(handshake.getHeader().getType());
			stringBuilder.append(Strings.EOL);
		}
		return stringBuilder.toString();
	}
}
