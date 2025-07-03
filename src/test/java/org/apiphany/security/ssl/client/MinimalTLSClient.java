package org.apiphany.security.ssl.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;

import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ThrowingConsumer;
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

	private final ByteArrayOutputStream handshakeMessages = new ByteArrayOutputStream();

	public MinimalTLSClient(final String host, final int port) {
		this.host = host;
		this.port = port;
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

	public static byte[] receiveTLSRecord(final InputStream is) throws IOException {
		LOGGER.debug("Waiting for server response...");
		RecordHeader recordHeader = RecordHeader.from(is);

		int length = recordHeader.getMessageLength().getValue();
		byte[] content = new byte[length];
		is.read(content);

		LOGGER.debug("Received content ({} bytes): {}", length, Bytes.hexString(content));

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(recordHeader.toByteArray());
		bos.write(content);
		return bos.toByteArray();
	}

	public byte[] receiveTLSRecord() throws IOException {
		return receiveTLSRecord(in);
	}

	public byte[] receiveTLSRecord(final int timeout) throws IOException {
		int savedTimeout = tcpSocket.getSoTimeout();
		tcpSocket.setSoTimeout(timeout);
		try {
			return receiveTLSRecord();
		} finally {
			tcpSocket.setSoTimeout(savedTimeout);
		}
	}

	public X509Certificate parseCertificate(final byte[] certData) throws Exception {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certData));
	}

	public byte[] generateKeyExchange(final X509Certificate serverCert) throws Exception {
		SecureRandom random = new SecureRandom();

		// Generate random pre-master secret (48 bytes total)
		this.preMasterSecret = new byte[48];

		// First 2 bytes indicate TLS version
		Bytes.set(SSLProtocol.TLS_1_2.handshakeVersion(), preMasterSecret, 0);

		// Remaining 46 bytes are random
		byte[] randomBytes = new byte[46];
		random.nextBytes(randomBytes);
		System.arraycopy(randomBytes, 0, preMasterSecret, 2, 46);

		// Encrypt with server's public key
		Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		rsa.init(Cipher.ENCRYPT_MODE, serverCert.getPublicKey());
		return rsa.doFinal(preMasterSecret);
	}

	public byte[] createKeyExchangeMessage(final byte[] encryptedPreMasterSecret) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		RecordHeader recordHeader = new RecordHeader(RecordHeaderType.HANDSHAKE_RECORD, SSLProtocol.TLS_1_2);
		dos.write(recordHeader.toByteArray());

		HandshakeHeader handshakeHeader = new HandshakeHeader(HandshakeMessageType.CLIENT_KEY_EXCHANGE);
		dos.write(handshakeHeader.toByteArray());

		PublicKey publicKey = new PublicKey(encryptedPreMasterSecret);
		dos.write(publicKey.getBytes());

		// Update lengths
		byte[] message = bos.toByteArray();
		int payloadLength = message.length - 5; // minus record header
		message[3] = (byte) (payloadLength >> 8);
		message[4] = (byte) payloadLength;

		int handshakeLength = payloadLength - 4; // minus handshake header
		message[6] = (byte) (handshakeLength >> 8);
		message[7] = (byte) handshakeLength;

		return message;
	}

	public static ServerHello readServerHello(final InputStream in) throws Exception {
		return ServerHello.from(in);
	}

	public static byte[] readServerCertificate(final InputStream in) throws Exception {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(in);
		LOGGER.debug("[ServerCertificate] handshake header: {}", handshakeHeader);

		Int24 certificatesLength = Int24.from(in);
		LOGGER.debug("[ServerCertificate] certificates length: {}", certificatesLength);

		Certificate certificate = Certificate.from(in);
		LOGGER.debug("[ServerCertificate] certificate: {}", certificate);
		return certificate.getBytes();
	}

	public static ServerKeyExchange readServerKeyExchange(final InputStream in) throws Exception {
		return ServerKeyExchange.from(in);
	}

	public static HandshakeHeader readServerHelloDone(final InputStream in) throws Exception {
		return HandshakeHeader.from(in);
	}

	public byte[] createFinishedMessage(byte[] masterSecret, byte[] handshakeHash,
			byte[] clientWriteKey, byte[] clientMacKey, byte[] clientIV) throws Exception {
		// 1. Compute verify_data
		byte[] verifyData = prf(masterSecret, "client finished", handshakeHash, 12);

		// 2. Construct handshake body for Finished message
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.write(0x14); // Handshake type: Finished
		dos.writeByte(0x00); // Length high byte
		dos.writeShort(verifyData.length); // Length low bytes
		dos.write(verifyData); // Payload
		byte[] finishedPlaintext = bos.toByteArray(); // Handshake message (unencrypted)

		// 3. Encrypt with keys
		return encryptWithClientKey(finishedPlaintext, clientWriteKey, clientMacKey, clientIV);
	}

	public static byte[] buildTLSRecord(byte type, byte[] data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(type);
		bos.write(0x03);
		bos.write(0x03); // TLS 1.2
		bos.write((data.length >> 8) & 0xFF);
		bos.write(data.length & 0xFF);
		bos.write(data);
		return bos.toByteArray();
	}

	public byte[] encryptWithClientKey(byte[] plaintext, byte[] clientKey, byte[] clientMacKey, byte[] clientIV) throws Exception {
		// 1. Compute MAC
		byte[] mac = hmacSha1(clientMacKey, plaintext);

		// 2. Append MAC to plaintext
		byte[] macAppended = concatenate(plaintext, mac);

		// 3. Apply PKCS#7 padding (AES block = 16 bytes)
		byte[] padded = padPKCS7(macAppended, 16);

		// 4. Encrypt with AES/CBC
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		SecretKeySpec keySpec = new SecretKeySpec(clientKey, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(clientIV);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

		byte[] encrypted = cipher.doFinal(padded);

		// 5. Wrap into TLS Record
		return buildTLSRecord((byte) 0x16, encrypted); // Handshake record
	}

	public byte[] decryptWithServerKey(byte[] tlsRecord, byte[] serverKey, byte[] serverMacKey, byte[] serverIV) throws Exception {
		// Extract ciphertext
		byte[] ciphertext = Arrays.copyOfRange(tlsRecord, 5, tlsRecord.length); // skip record header

		// Decrypt AES/CBC
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		SecretKeySpec keySpec = new SecretKeySpec(serverKey, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(serverIV);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] decryptedPadded = cipher.doFinal(ciphertext);

		// Remove PKCS#7 padding
		byte[] decryptedWithMac = unpadPKCS7(decryptedPadded);

		// Separate MAC
		int macLen = 20;
		byte[] data = Arrays.copyOfRange(decryptedWithMac, 0, decryptedWithMac.length - macLen);
		byte[] mac = Arrays.copyOfRange(decryptedWithMac, decryptedWithMac.length - macLen, decryptedWithMac.length);

		// Validate MAC
		byte[] expectedMac = hmacSha1(serverMacKey, data);
		if (!Arrays.equals(mac, expectedMac)) {
			throw new SSLException("Invalid MAC in server Finished record");
		}

		return data; // includes Finished handshake
	}

	public byte[] performHandshake() throws Exception {
		connect();

		// 1. Send Client Hello
		ClientHello clientHello = new ClientHello(List.of(host), new CipherSuites(CipherSuiteName.values()));
		byte[] clientHelloBytes = clientHello.toByteArray();
		accumulateHandshake(clientHelloBytes);
		this.clientRandom = clientHello.getClientRandom().getRandom();

		LOGGER.debug("Sending Client Hello: {}", clientHello);
		sendTLSRecord(clientHelloBytes);
		LOGGER.debug("Sent Client Hello");

		// 2. Receive Server Hello
		byte[] serverHelloBytes = receiveTLSRecord();
		accumulateHandshake(serverHelloBytes);
		LOGGER.debug("Received Server Hello: {}", Bytes.hexString(serverHelloBytes));

		// it looks like this contains all bytes including Server Hello Done so we create a new input
		// stream to read all the needed information from these bytes
		ByteArrayInputStream bis = new ByteArrayInputStream(serverHelloBytes);
		// a. Read Server Hello
		ServerHello serverHello = readServerHello(bis);
		LOGGER.debug("Received Server Hello: {}", serverHello);
		this.serverRandom = serverHello.getServerRandom().getRandom();

		// b. Read Server Certificate
		byte[] certificate = readServerCertificate(bis);
		X509Certificate x509Certificate = parseCertificate(certificate);
		LOGGER.debug("Received Server Certificate: {}", x509Certificate);

		// c. Read Server Key Exchange
		ServerKeyExchange serverKeyExchange = readServerKeyExchange(bis);
		LOGGER.debug("[ServerKeyExchange]: {}", serverKeyExchange);

		// d. Read Server Hello Done
		HandshakeHeader serverHelloDone = readServerHelloDone(bis);
		LOGGER.debug("[ServerHelloDone] handshake header: {}", serverHelloDone);
		// accumulateHandshake(serverHelloDone);

		// 3. Generate Client Key Exchange
		byte[] encryptedPreMaster = generateKeyExchange(x509Certificate);

		// 4. Send Client Key Exchange
		byte[] clientKeyExchange = createKeyExchangeMessage(encryptedPreMaster);
		accumulateHandshake(clientKeyExchange);
		sendTLSRecord(clientKeyExchange);
		LOGGER.debug("Sent Client Key Exchange");

		// 5. Derive Master Secret and Keys
		this.masterSecret = deriveMasterSecret(preMasterSecret, clientRandom, serverRandom);
		byte[] keyBlock = deriveKeyBlock(masterSecret, serverRandom, clientRandom, 104);

		// Extract keys
		ByteBuffer buf = ByteBuffer.wrap(keyBlock);
		byte[] clientMACKey = new byte[20];
		buf.get(clientMACKey);
		byte[] serverMACKey = new byte[20];
		buf.get(serverMACKey);
		byte[] clientWriteKey = new byte[16];
		buf.get(clientWriteKey);
		byte[] serverWriteKey = new byte[16];
		buf.get(serverWriteKey);
		byte[] clientIV = new byte[16];
		buf.get(clientIV);
		byte[] serverIV = new byte[16];
		buf.get(serverIV);

		// 6. Client Change Cipher Spec
		Record changeCypherSpecRecord = new Record(RecordHeaderType.CHANGE_CIPHER_SPEC, SSLProtocol.TLS_1_2);
		sendTLSRecord(changeCypherSpecRecord.toByteArray());

		// 7. Send Finished
		byte[] handshakeHash = sha256(getHandshakeTranscript()); // SHA-256 hash of accumulated handshake
		byte[] finished = createFinishedMessage(masterSecret, handshakeHash, clientWriteKey, clientMACKey, clientIV);
		sendTLSRecord(finished);

		// 7. Receive ChangeCipherSpec and Finished
		byte[] serverChangeCipherSpec = receiveTLSRecord(); // type 0x14
		LOGGER.debug("Received ChangeCipherSpec: ", serverChangeCipherSpec);

		// 8. Receive Server Finished Record
		byte[] serverFinishedRecord = receiveTLSRecord(); // type 0x16

		byte[] serverFinishedDecrypted = decryptWithServerKey(serverFinishedRecord, serverWriteKey, serverIV, serverMACKey);
		byte[] expectedServerVerifyData = prf(masterSecret, "server finished", handshakeHash, 12);

		byte[] serverVerifyData = Arrays.copyOfRange(serverFinishedDecrypted, 4, 4 + 12); // skip handshake header
		if (!Arrays.equals(serverVerifyData, expectedServerVerifyData)) {
			throw new SSLException("Server Finished verification failed!");
		}

		LOGGER.info("TLS 1.2 handshake complete!");

		return serverIV;
	}

	public void accumulateHandshake(final byte[] tlsRecord) {
		// Skip the 5-byte TLS record header: [ContentType, Version (2), Length (2)]
		if (tlsRecord.length < 6) {
			return;
		}
		handshakeMessages.write(tlsRecord, 5, tlsRecord.length - 5);
	}

	public byte[] getHandshakeTranscript() {
		return handshakeMessages.toByteArray();
	}

	public static byte[] prf(final byte[] secret, final String label, final byte[] seed, final int length) throws Exception {
		byte[] labelSeed = concatenate(label.getBytes(StandardCharsets.US_ASCII), seed);
		return pHash(secret, labelSeed, length);
	}

	private static byte[] pHash(final byte[] secret, final byte[] seed, final int length) throws Exception {
		Mac hmac = Mac.getInstance("HmacSHA256");
		SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA256");
		hmac.init(keySpec);

		byte[] seedBytes = seed;
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		while (result.size() < length) {
			seedBytes = hmac.doFinal(seedBytes);
			byte[] output = hmac.doFinal(concatenate(seedBytes, seed));
			result.write(output);
		}
		return Arrays.copyOf(result.toByteArray(), length);
	}

	public static byte[] concatenate(final byte[]... arrays) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (byte[] arr : arrays) {
			bos.write(arr);
		}
		return bos.toByteArray();
	}

	public static byte[] deriveMasterSecret(final byte[] preMasterSecret, final byte[] clientRandom, final byte[] serverRandom) throws Exception {
		return prf(preMasterSecret, "master secret", concatenate(clientRandom, serverRandom), 48);
	}

	public static byte[] deriveKeyBlock(final byte[] masterSecret, final byte[] serverRandom, final byte[] clientRandom, final int length)
			throws Exception {
		return prf(masterSecret, "key expansion", concatenate(serverRandom, clientRandom), length);
	}

	public static byte[] hmacSha1(byte[] key, byte[] data) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(new SecretKeySpec(key, "HmacSHA1"));
		return mac.doFinal(data);
	}

	public static byte[] padPKCS7(byte[] data, int blockSize) throws IOException {
		int padLen = blockSize - (data.length % blockSize);
		byte[] padding = new byte[padLen];
		Arrays.fill(padding, (byte) padLen);
		return concatenate(data, padding);
	}

	public static byte[] unpadPKCS7(byte[] padded) {
		int padLen = padded[padded.length - 1];
		return Arrays.copyOfRange(padded, 0, padded.length - padLen);
	}

	public static byte[] sha256(byte[] input) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(input);
	}

}
