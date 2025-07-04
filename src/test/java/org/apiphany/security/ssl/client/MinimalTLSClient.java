package org.apiphany.security.ssl.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
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

	public byte[] generateRSAKeyExchange(final X509Certificate serverCert) throws Exception {
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

	public byte[] generateECDHEKeyExchange(ServerKeyExchange ske, X509Certificate serverCert) throws Exception {
	    // 1. Parse server's EC parameters
		AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
		params.init(new ECGenParameterSpec("secp256r1"));
		ECParameterSpec ecParams = params.getParameterSpec(ECParameterSpec.class);
	    KeyFactory kf = KeyFactory.getInstance("EC");

	    ECPoint serverPoint = decodeECPublicPoint(ske.getPublicKey().getBytes(), ecParams.getCurve());
	    ECPublicKeySpec serverPubSpec = new ECPublicKeySpec(serverPoint, ecParams);
	    PublicKey serverECPublicKey = kf.generatePublic(serverPubSpec);

	    // 2. Generate client keypair
	    KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
	    kpg.initialize(ecParams);
	    KeyPair clientKeyPair = kpg.generateKeyPair();

	    // 3. Compute shared secret
	    KeyAgreement ka = KeyAgreement.getInstance("ECDH");
	    ka.init(clientKeyPair.getPrivate());
	    ka.doPhase(serverECPublicKey, true);
	    byte[] sharedSecret = ka.generateSecret();

	    // 4. Save sharedSecret as preMasterSecret
	    this.preMasterSecret = sharedSecret;

	    // 5. Build ClientKeyExchange message (with client's EC public key)
	    byte[] clientPublicEncoded = encodeECPublicPoint(
	        ((ECPublicKey) clientKeyPair.getPublic()).getW(), ecParams.getCurve());

	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    bos.write(0x10); // Handshake type: ClientKeyExchange
	    bos.write(0x00);
	    bos.write(0x00);
	    bos.write(clientPublicEncoded.length + 1); // length = pubkey len + 1 byte prefix
	    bos.write(clientPublicEncoded.length);     // EC point length
	    bos.write(clientPublicEncoded);            // EC point data

	    return buildTLSRecord((byte) 0x16, bos.toByteArray());
	}

	public static byte[] encodeECPublicPoint(ECPoint point, EllipticCurve curve) throws IOException {
	    int fieldSize = (curve.getField().getFieldSize() + 7) / 8;
	    byte[] x = point.getAffineX().toByteArray();
	    byte[] y = point.getAffineY().toByteArray();
	    x = Arrays.copyOfRange(x, x.length - fieldSize, x.length);
	    y = Arrays.copyOfRange(y, y.length - fieldSize, y.length);
	    return concatenate(new byte[] { 0x04 }, x, y);
	}

	public static ECPoint decodeECPublicPoint(byte[] encoded, EllipticCurve curve) {
	    if (encoded[0] != 0x04) {
	        throw new IllegalArgumentException("Only uncompressed EC points are supported");
	    }
	    int fieldSize = (curve.getField().getFieldSize() + 7) / 8;
	    byte[] x = Arrays.copyOfRange(encoded, 1, 1 + fieldSize);
	    byte[] y = Arrays.copyOfRange(encoded, 1 + fieldSize, 1 + 2 * fieldSize);
	    return new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
	}

	public static ServerHello readServerHello(final InputStream in) throws Exception {
		return ServerHello.from(in);
	}

	public static ServerCertificate readServerCertificate(final InputStream in) throws Exception {
		return ServerCertificate.from(in);
	}

	public static ServerKeyExchange readServerKeyExchange(final InputStream in) throws Exception {
		return ServerKeyExchange.from(in);
	}

	public static HandshakeHeader readServerHelloDone(final InputStream in) throws Exception {
		return HandshakeHeader.from(in);
	}

	public byte[] createFinishedMessage(byte[] masterSecret, byte[] handshakeHash,
			ExchangeKeys keys) throws Exception {
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
		return encryptWithClientKey72(finishedPlaintext, keys);
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

	public byte[] encryptWithClientKey104(byte[] plaintext, ExchangeKeys keys) throws Exception {
		// 1. Compute MAC
		byte[] mac = hmacSha1(keys.getClientMACKey(), plaintext);

		// 2. Append MAC to plaintext
		byte[] macAppended = concatenate(plaintext, mac);

		// 3. Apply PKCS#7 padding (AES block = 16 bytes)
		byte[] padded = padPKCS7(macAppended, 16);

		// 4. Encrypt with AES/CBC
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		SecretKeySpec keySpec = new SecretKeySpec(keys.getClientWriteKey(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keys.getClientIV());
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

		byte[] encrypted = cipher.doFinal(padded);

		// 5. Wrap into TLS Record
		return buildTLSRecord((byte) 0x16, encrypted); // Handshake record
	}

	public byte[] encryptWithClientKey72(byte[] plaintext, ExchangeKeys keys) throws Exception {
	    // 1. Compute MAC
	    byte[] mac = hmacSha1(keys.getClientMACKey(), plaintext);

	    // 2. Append MAC
	    byte[] macAppended = concatenate(plaintext, mac);

	    // 3. Pad using PKCS#7
	    byte[] padded = padPKCS7(macAppended, 16);

	    // 4. Generate fresh IV (TLS 1.2 explicit IV)
	    byte[] iv = new byte[16];
	    SecureRandom random = new SecureRandom();
	    random.nextBytes(iv);

	    // 5. Encrypt
	    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
	    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keys.getClientWriteKey(), "AES"), new IvParameterSpec(iv));
	    byte[] encrypted = cipher.doFinal(padded);

	    // 6. Prepend IV to ciphertext
	    byte[] fragment = concatenate(iv, encrypted);

	    // 7. Wrap in TLS record
	    return buildTLSRecord((byte) 0x16, fragment);
	}

	public byte[] decryptWithServerKey104(byte[] tlsRecord, ExchangeKeys keys) throws Exception {
		// Extract ciphertext
		byte[] ciphertext = Arrays.copyOfRange(tlsRecord, 5, tlsRecord.length); // skip record header

		// Decrypt AES/CBC
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		SecretKeySpec keySpec = new SecretKeySpec(keys.getServerWriteKey(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keys.getServerIV());
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] decryptedPadded = cipher.doFinal(ciphertext);

		// Remove PKCS#7 padding
		byte[] decryptedWithMac = unpadPKCS7(decryptedPadded);

		// Separate MAC
		int macLen = 20;
		byte[] data = Arrays.copyOfRange(decryptedWithMac, 0, decryptedWithMac.length - macLen);
		byte[] mac = Arrays.copyOfRange(decryptedWithMac, decryptedWithMac.length - macLen, decryptedWithMac.length);

		// Validate MAC
		byte[] expectedMac = hmacSha1(keys.getServerMACKey(), data);
		if (!Arrays.equals(mac, expectedMac)) {
			throw new SSLException("Invalid MAC in server Finished record");
		}

		return data; // includes Finished handshake
	}

	public byte[] decryptWithServerKey72(byte[] tlsRecord, ExchangeKeys keys) throws Exception {
	    // 1. Extract TLS record payload
	    byte[] fragment = Arrays.copyOfRange(tlsRecord, 5, tlsRecord.length); // skip 5-byte record header

	    // 2. Extract IV (first 16 bytes of the fragment)
	    byte[] iv = Arrays.copyOfRange(fragment, 0, 16);
	    byte[] ciphertext = Arrays.copyOfRange(fragment, 16, fragment.length);

	    // 3. Decrypt using AES/CBC with extracted IV
	    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
	    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keys.getServerWriteKey(), "AES"), new IvParameterSpec(iv));
	    byte[] decryptedPadded = cipher.doFinal(ciphertext);

	    // 4. Remove PKCS#7 padding
	    byte[] decryptedWithMac = unpadPKCS7(decryptedPadded);

	    // 5. Separate MAC and data
	    int macLen = 20; // HMAC-SHA1
	    int dataLen = decryptedWithMac.length - macLen;
	    byte[] data = Arrays.copyOfRange(decryptedWithMac, 0, dataLen);
	    byte[] mac = Arrays.copyOfRange(decryptedWithMac, dataLen, decryptedWithMac.length);

	    // 6. Verify MAC
	    byte[] expectedMac = hmacSha1(keys.getServerMACKey(), data);
	    if (!Arrays.equals(mac, expectedMac)) {
	        throw new SSLException("Invalid MAC in server Finished record");
	    }

	    return data; // Includes decrypted Finished handshake message
	}

	public byte[] performHandshake() throws Exception {
		connect();

		// 1. Send Client Hello
		ClientHello clientHello = new ClientHello(List.of(host), new CipherSuites(CipherSuiteName.values()));
		byte[] clientHelloBytes = clientHello.toByteArray();
		accumulateHandshake(clientHelloBytes, true);
		this.clientRandom = clientHello.getClientRandom().getRandom();

		LOGGER.debug("Sending Client Hello: {}", clientHello);
		sendTLSRecord(clientHelloBytes);
		LOGGER.debug("Sent Client Hello");

		// 2. Receive Server Hello
		byte[] serverHelloBytes = receiveTLSRecord();
		LOGGER.debug("Received Server Hello: {}", Bytes.hexString(serverHelloBytes));

		// it looks like this contains all bytes including Server Hello Done so we create a new input
		// stream to read all the needed information from these bytes
		ByteArrayInputStream bis = new ByteArrayInputStream(serverHelloBytes);
		// a. Read Server Hello
		ServerHello serverHello = readServerHello(bis);
		accumulateHandshake(serverHello.toByteArray(), true);
		LOGGER.debug("Received Server Hello: {}", serverHello);
		this.serverRandom = serverHello.getServerRandom().getRandom();

		// b. Read Server Certificate
		ServerCertificate serverCertificate = readServerCertificate(bis);
		accumulateHandshake(serverCertificate.toByteArray(), false);
		LOGGER.debug("Received Server Certificate: {}", serverCertificate);

		X509Certificate x509Certificate = parseCertificate(serverCertificate.getCertificate().getBytes());
		LOGGER.debug("Received Server Certificate: {}", x509Certificate);

		// c. Read Server Key Exchange
		ServerKeyExchange serverKeyExchange = readServerKeyExchange(bis);
		LOGGER.debug("Received Server Key Exchange: {}", serverKeyExchange);

		// d. Read Server Hello Done
		HandshakeHeader serverHelloDone = readServerHelloDone(bis);
		accumulateHandshake(serverHelloDone.toByteArray(), false);
		LOGGER.debug("[ServerHelloDone] handshake header: {}", serverHelloDone);

		// 3. Generate Client Key Exchange
		CipherSuiteName selectedCipher = serverHello.getCypherSuite().getCipher();
		byte[] encryptedPreMaster;
		if (selectedCipher.name().contains("ECDHE")) {
		    encryptedPreMaster = generateECDHEKeyExchange(serverKeyExchange, x509Certificate);
		} else if (selectedCipher.name().contains("RSA")) {
		    encryptedPreMaster = generateRSAKeyExchange(x509Certificate);
		} else {
		    throw new SSLException("Unsupported cipher suite: " + selectedCipher);
		}

		// 4. Send Client Key Exchange
		ClientKeyExchange clientKeyExchange = new ClientKeyExchange(encryptedPreMaster);
		byte[] clientKeyExchangeBytes = clientKeyExchange.toByteArray();

		LOGGER.debug("Sending Client Key Exchange: {}", clientKeyExchange);
		sendTLSRecord(clientKeyExchangeBytes);
		accumulateHandshake(clientKeyExchangeBytes, true);
		LOGGER.debug("Sent Client Key Exchange");

		// 5. Derive Master Secret and Keys
		this.masterSecret = deriveMasterSecret(preMasterSecret, clientRandom, serverRandom);
		byte[] keyBlock = deriveKeyBlock(masterSecret, serverRandom, clientRandom, 72); // 104 with IV
		// Extract keys
		ExchangeKeys keys = ExchangeKeys.from(keyBlock);

		// 6. Client Change Cipher Spec
		Record changeCypherSpecRecord = new Record(RecordHeaderType.CHANGE_CIPHER_SPEC, SSLProtocol.TLS_1_2);
		sendTLSRecord(changeCypherSpecRecord.toByteArray());
		LOGGER.debug("Sent Client Change Cipher Spec: {}", changeCypherSpecRecord);

		// 7. Send Finished
		byte[] handshakeHash = sha256(getHandshakeTranscript()); // SHA-256 hash of accumulated handshake
		byte[] finished = createFinishedMessage(masterSecret, handshakeHash, keys);
		sendTLSRecord(finished);

		// 7. Receive ChangeCipherSpec and Finished
		byte[] serverChangeCipherSpec = receiveTLSRecord(); // type 0x14
		LOGGER.debug("Received ChangeCipherSpec: ", serverChangeCipherSpec);

		// 8. Receive Server Finished Record
		byte[] serverFinishedRecord = receiveTLSRecord(); // type 0x16

		byte[] serverFinishedDecrypted = decryptWithServerKey72(serverFinishedRecord, keys);
		byte[] expectedServerVerifyData = prf(masterSecret, "server finished", handshakeHash, 12);

		byte[] serverVerifyData = Arrays.copyOfRange(serverFinishedDecrypted, 4, 4 + 12); // skip handshake header
		if (!Arrays.equals(serverVerifyData, expectedServerVerifyData)) {
			throw new SSLException("Server Finished verification failed!");
		}

		LOGGER.info("TLS 1.2 handshake complete!");

		return keys.getServerIV();
	}

	public void accumulateHandshake(final byte[] tlsRecord, boolean skipRecord) {
		// Skip the 5-byte TLS record header: [ContentType, Version (2), Length (2)]
		if (tlsRecord.length < 6) {
			return;
		}
		if (skipRecord) {
			handshakeMessages.write(tlsRecord, RecordHeader.SIZE, tlsRecord.length - RecordHeader.SIZE);
		} else {
			handshakeMessages.write(tlsRecord, 0, tlsRecord.length);
		}
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
