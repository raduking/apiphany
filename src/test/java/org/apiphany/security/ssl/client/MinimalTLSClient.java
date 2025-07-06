package org.apiphany.security.ssl.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
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
		LOGGER.debug("Received record header (size: {}): {}", recordHeader.size(), recordHeader);

		int length = recordHeader.getLength().getValue();
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

	public byte[] generateX25519KeyExchange(final ServerKeyExchange ske) throws Exception {
		byte[] serverPubBytes = ske.getPublicKey().getValue().getBytes();

		// 1. Parse server public key
		KeyFactory kf = KeyFactory.getInstance("XDH");
		NamedParameterSpec paramSpec = new NamedParameterSpec("X25519");
		XECPublicKeySpec serverKeySpec = new XECPublicKeySpec(paramSpec, new BigInteger(1, serverPubBytes));
		PublicKey serverPublicKey = kf.generatePublic(serverKeySpec);

		// 2. Generate client keypair
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("XDH");
		kpg.initialize(new NamedParameterSpec("X25519"));
		KeyPair kp = kpg.generateKeyPair();

		// 3. Compute shared secret
		KeyAgreement ka = KeyAgreement.getInstance("XDH");
		ka.init(kp.getPrivate());
		ka.doPhase(serverPublicKey, true);
		byte[] sharedSecret = ka.generateSecret();

		this.preMasterSecret = sharedSecret;

		// 4. Encode client public key
		byte[] clientPublic = ((XECPublicKey) kp.getPublic()).getU().toByteArray();
		clientPublic = Arrays.copyOfRange(clientPublic, clientPublic.length - 32, clientPublic.length); // ensure 32 bytes

		// 5. Format TLS ClientKeyExchange message
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(0x10); // handshake type
		bos.write(0x00);
		bos.write(0x00);
		bos.write(1 + clientPublic.length); // length
		bos.write(clientPublic.length);
		bos.write(clientPublic);

		return buildTLSRecord((byte) 0x16, bos.toByteArray());
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

	public byte[] createFinishedMessage(final byte[] masterSecret, final byte[] handshakeHash, final ExchangeKeys keys) throws Exception {
		// 1. Compute verify_data
		byte[] verifyData = PseudoRandomFunction.apply(masterSecret, "client finished", handshakeHash, 12);

		// 2. Construct handshake body for Finished message
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.write(0x14); // Handshake type: Finished
		dos.writeByte(0x00); // Length high byte
		dos.writeShort(verifyData.length); // Length low bytes
		dos.write(verifyData); // Payload
		byte[] finishedPlaintext = bos.toByteArray(); // Handshake message (unencrypted)

		// 3. Encrypt with keys
		return encryptWithClientKeyAEAD(finishedPlaintext, keys);
	}

	public static byte[] buildTLSRecord(final byte type, final byte[] data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(type);
		bos.write(0x03);
		bos.write(0x03); // TLS 1.2
		bos.write((data.length >> 8) & 0xFF);
		bos.write(data.length & 0xFF);
		bos.write(data);
		return bos.toByteArray();
	}

	public byte[] encryptWithClientKeyAEAD(final byte[] plaintext, final ExchangeKeys keys) throws Exception {
		// 1. Generate 8-byte explicit nonce
		byte[] explicitNonce = new byte[8];
		new SecureRandom().nextBytes(explicitNonce);

		// 2. Full nonce = 4-byte fixed IV + 8-byte explicit nonce
		byte[] fullNonce = concatenate(keys.getClientIV(), explicitNonce);

		// 3. Calculate AAD length = plaintext length + 16 (tag length)
		int aadLen = plaintext.length + 16;

		// 4. Construct AAD (TLS header)
		// first 8 bytes is the AAD sequence number (zero in this case for the finished message)
		byte[] aad = new byte[] {
				0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00,
				0x16, 0x03, 0x03,
				(byte) ((aadLen >> 8) & 0xFF),
				(byte) (aadLen & 0xFF)
		};

		// 5. Initialize Cipher and provide AAD before encrypting
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmSpec = new GCMParameterSpec(128, fullNonce);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keys.getClientWriteKey(), "AES"), gcmSpec);
		cipher.updateAAD(aad);

		// 6. Encrypt plaintext
		byte[] ciphertext = cipher.doFinal(plaintext);

		// 7. Prepare TLS record header (nonce + ciphertext length)
		int recordLen = explicitNonce.length + ciphertext.length;
		byte[] recordHeader = new byte[] {
				0x16, 0x03, 0x03,
				(byte) ((recordLen >> 8) & 0xFF),
				(byte) (recordLen & 0xFF)
		};

		// 8. Concatenate record header, explicit nonce, ciphertext
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(recordHeader);
		bos.write(explicitNonce);
		bos.write(ciphertext);

		LOGGER.debug("Encrypting Finished Message:");
		LOGGER.debug("Fixed IV: {}", Bytes.hexString(keys.getClientIV()));
		LOGGER.debug("Explicit nonce: {}", Bytes.hexString(explicitNonce));
		LOGGER.debug("Full nonce: {}", Bytes.hexString(fullNonce));
		LOGGER.debug("AAD (TLS Header): {}", Bytes.hexString(aad));
		LOGGER.debug("Plaintext: {}", Bytes.hexString(plaintext));
		LOGGER.debug("Ciphertext + Tag: {}", Bytes.hexString(ciphertext));

		return bos.toByteArray();
	}

	public byte[] decryptWithServerKeyAEAD(final byte[] record, final ExchangeKeys keys) throws Exception {
		// 1. Extract TLS header and fragment
		byte[] header = Arrays.copyOfRange(record, 0, 5);
		byte[] fragment = Arrays.copyOfRange(record, 5, record.length);
		LOGGER.debug("Server Finished explicit nonce: {}", Bytes.hexString(Arrays.copyOfRange(fragment, 0, 8)));

		// 2. Extract explicit nonce (8 bytes)
		byte[] explicitNonce = Arrays.copyOfRange(fragment, 0, 8);
		byte[] ciphertext = Arrays.copyOfRange(fragment, 8, fragment.length); // includes tag

		// 3. Rebuild nonce
		byte[] nonce = concatenate(keys.getServerIV(), explicitNonce);
		LOGGER.debug("Server Finished full nonce: {}", Bytes.hexString(nonce));

		// 4. Decrypt
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keys.getServerWriteKey(), "AES"), gcmSpec);
		cipher.updateAAD(header); // must match encryption AAD
		return cipher.doFinal(ciphertext); // plaintext or throws AEADBadTagException
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
		LOGGER.debug("Sent Client Hello: \n{}", Bytes.hexDump(clientHelloBytes));

		// 2. Receive Server Hello
		byte[] serverHelloBytes = receiveTLSRecord();
		accumulateHandshake(serverHelloBytes);
		LOGGER.debug("Received Server Hello: {}", Bytes.hexDump(serverHelloBytes));

		// it looks like this contains all bytes including Server Hello Done so we create a new input
		// stream to read all the needed information from these bytes
		ByteArrayInputStream bis = new ByteArrayInputStream(serverHelloBytes);
		// a. Read Server Hello
		ServerHello serverHello = readServerHello(bis);
		LOGGER.debug("Received Server Hello: {}", serverHello);
		this.serverRandom = serverHello.getServerRandom().getRandom();

		// b. Read Server Certificate
		ServerCertificate serverCertificate = readServerCertificate(bis);
		LOGGER.debug("Received Server Certificate: {}", serverCertificate);

		X509Certificate x509Certificate = parseCertificate(serverCertificate.getCertificate().getData().getBytes());
		LOGGER.debug("Received Server Certificate: {}", x509Certificate);

		// c. Read Server Key Exchange
		ServerKeyExchange serverKeyExchange = readServerKeyExchange(bis);
		LOGGER.debug("Received Server Key Exchange: {}", serverKeyExchange);

		// d. Read Server Hello Done
		HandshakeHeader serverHelloDone = readServerHelloDone(bis);
		LOGGER.debug("[ServerHelloDone] handshake header: {}", serverHelloDone);

		// 3. Generate Client Key Exchange
		CipherSuiteName selectedCipher = serverHello.getCipherSuite().getCipher();
		byte[] encryptedPreMaster;
		if (serverKeyExchange.getCurveInfo().getName() == CurveName.X25519) {
			encryptedPreMaster = generateX25519KeyExchange(serverKeyExchange);
		} else {
			throw new SSLException("Unsupported cipher suite: " + selectedCipher);
		}

		// 4. Send Client Key Exchange
		ClientKeyExchange clientKeyExchange = new ClientKeyExchange(encryptedPreMaster);
		byte[] clientKeyExchangeBytes = clientKeyExchange.toByteArray();
		accumulateHandshake(clientKeyExchangeBytes);

		LOGGER.debug("Sending Client Key Exchange: {}", clientKeyExchange);
		sendTLSRecord(clientKeyExchangeBytes);
		LOGGER.debug("Sent Client Key Exchange");

		// 5. Derive Master Secret and Keys
		this.masterSecret = deriveMasterSecret(preMasterSecret, clientRandom, serverRandom);
		byte[] keyBlock = deriveKeyBlock(masterSecret, serverRandom, clientRandom, 40);
		// Extract keys
		ExchangeKeys keys = ExchangeKeys.from(keyBlock, ExchangeKeys.Type.AHEAD);

		// 6. Client Change Cipher Spec
		Record changeCypherSpecRecord = new Record(RecordHeaderType.CHANGE_CIPHER_SPEC, SSLProtocol.TLS_1_2);
		sendTLSRecord(changeCypherSpecRecord.toByteArray());
		LOGGER.debug("Sent Client Change Cipher Spec: {}", changeCypherSpecRecord);

		// 7. Send Finished
		byte[] handshakeHash = sha256(getHandshakeTranscript()); // SHA-256 hash of accumulated handshake
		byte[] verifyData = PseudoRandomFunction.apply(masterSecret, "client finished", handshakeHash, 12);
		byte[] finished = createFinishedMessage(masterSecret, handshakeHash, keys);

		LOGGER.debug("Handshake hash input ({} bytes): {}", getHandshakeTranscript().length, Bytes.hexString(getHandshakeTranscript()));
		LOGGER.debug("Handshake SHA-256 hash: {}", Bytes.hexString(sha256(getHandshakeTranscript())));
		LOGGER.debug("Computed verify_data: {}", Bytes.hexString(verifyData));
		LOGGER.debug("Master secret: {}", Bytes.hexString(masterSecret));
		LOGGER.debug("Client write key: {}", Bytes.hexString(keys.getClientWriteKey()));
		LOGGER.debug("Client IV: {}", Bytes.hexString(keys.getClientIV()));
		sendTLSRecord(finished);

		// 7. Receive ChangeCipherSpec and Finished
		byte[] serverChangeCipherSpec = receiveTLSRecord(); // type 0x14
		LOGGER.debug("Received ChangeCipherSpec: ", serverChangeCipherSpec);

		// 8. Receive Server Finished Record
		byte[] serverFinishedRecord = receiveTLSRecord(); // type 0x16
		LOGGER.debug("Server Finished TLS Record Header: {}", Bytes.hexString(Arrays.copyOfRange(serverFinishedRecord, 0, 5)));

		byte[] serverFinishedDecrypted = decryptWithServerKeyAEAD(serverFinishedRecord, keys);
		byte[] expectedServerVerifyData = PseudoRandomFunction.apply(masterSecret, "server finished", handshakeHash, 12);

		byte[] serverVerifyData = Arrays.copyOfRange(serverFinishedDecrypted, 4, 4 + 12); // skip handshake header
		if (!Arrays.equals(serverVerifyData, expectedServerVerifyData)) {
			throw new SSLException("Server Finished verification failed!");
		}

		LOGGER.info("TLS 1.2 handshake complete!");

		return keys.getServerIV();
	}

	public void accumulateHandshake(final byte[] tlsRecord) {
		handshakeMessages.write(tlsRecord, RecordHeader.BYTES, tlsRecord.length - RecordHeader.BYTES);
	}

	public byte[] getHandshakeTranscript() {
		return handshakeMessages.toByteArray();
	}

	public static byte[] concatenate(final byte[]... arrays) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (byte[] arr : arrays) {
			bos.write(arr);
		}
		return bos.toByteArray();
	}

	public static byte[] deriveMasterSecret(final byte[] preMasterSecret, final byte[] clientRandom, final byte[] serverRandom) throws Exception {
		return PseudoRandomFunction.apply(preMasterSecret, "master secret", concatenate(clientRandom, serverRandom), 48);
	}

	public static byte[] deriveKeyBlock(final byte[] masterSecret, final byte[] serverRandom, final byte[] clientRandom, final int length)
			throws Exception {
		return PseudoRandomFunction.apply(masterSecret, "key expansion", concatenate(serverRandom, clientRandom), length);
	}

	public static byte[] sha256(final byte[] input) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(input);
	}

}
