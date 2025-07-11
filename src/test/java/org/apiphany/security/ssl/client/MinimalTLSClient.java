package org.apiphany.security.ssl.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
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

	private long clientSequenceNumber = 0;

	private KeyPair clientKeyPair;

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
		short bytesToRead = recordHeader.getLength().getValue();
		LOGGER.debug("Received record header (length: {} bytes): {}", bytesToRead, recordHeader);

		byte[] content = new byte[bytesToRead];
		is.read(content);
		LOGGER.debug("Received content ({} bytes):\n{}", bytesToRead, Bytes.hexDump(content));

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

	public byte[] performHandshake() throws Exception {
		connect();

		// 1. Send Client Hello
		TLSRecord clientHelloRecord = sendClientHello();

		ClientHello clientHello = clientHelloRecord.getHandshake().get(ClientHello.class);
		this.clientRandom = clientHello.getClientRandom().getRandom();

		// 2. Receive Server Hello
		TLSRecord serverHelloRecord = receiveServerHello();

		ServerHello serverHello = serverHelloRecord.getHandshake(ServerHello.class);
		this.serverRandom = serverHello.getServerRandom().toByteArray();

		Certificates certificates = serverHelloRecord.getHandshake(Certificates.class);
		X509Certificate x509Certificate = parseCertificate(certificates.getList().getFirst().getData().getBytes());
		LOGGER.debug("Received Server Certificate: {}", x509Certificate);

		ServerKeyExchange serverKeyExchange = serverHelloRecord.getHandshake(ServerKeyExchange.class);
		LOGGER.debug("Received Server Key Exchange:\n{}", Bytes.hexDump(serverKeyExchange.toByteArray()));
		LOGGER.debug("Received Server Hello Done: {}", serverHelloRecord.getHandshake(ServerHelloDone.class));

		// 3. Generate Client Key Exchange
		CipherSuiteName selectedCipher = serverHello.getCipherSuite().getCipher();
		byte[] clientPublic;
		if (serverKeyExchange.getCurveInfo().getName() == CurveName.X25519) {
			clientPublic = generateX25519ClientPublic(serverKeyExchange);
		} else {
			throw new SSLException("Unsupported cipher suite: " + selectedCipher);
		}

		// 4. Send Client Key Exchange
		TLSRecord clientKeyExchangeRecord = new TLSRecord(SSLProtocol.TLS_1_2, new ClientKeyExchange(clientPublic));
		byte[] clientKeyExchangeBytes = clientKeyExchangeRecord.toByteArray();
		accumulateHandshake(clientKeyExchangeBytes);

		LOGGER.debug("Sending Client Key Exchange: {}", clientKeyExchangeRecord);
		sendTLSRecord(clientKeyExchangeBytes);
		LOGGER.debug("Sent Client Key Exchange:\n{}", Bytes.hexDump(clientKeyExchangeBytes));

		// 5. Derive Master Secret and Keys
		this.masterSecret = deriveMasterSecret(preMasterSecret, clientRandom, serverRandom);
		byte[] keyBlock = deriveKeyBlock(masterSecret, serverRandom, clientRandom, 40);
		// Extract keys
		ExchangeKeys keys = ExchangeKeys.from(keyBlock, ExchangeKeys.Type.AHEAD);

		// 6. Client Change Cipher Spec
		TLSRecord changeCypherSpecRecord = new TLSRecord(SSLProtocol.TLS_1_2, new ChangeCipherSpec());
		LOGGER.debug("Sending Client Change Cipher Spec: {}", changeCypherSpecRecord);
		sendTLSRecord(changeCypherSpecRecord.toByteArray());
		LOGGER.debug("Sent Client Change Cipher Spec:\n{}", Bytes.hexDump(changeCypherSpecRecord.toByteArray()));

		// 7. Send Finished
		byte[] handshakeBytes = getHandshakeTranscript();
		byte[] handshakeHash = sha384(handshakeBytes);

		byte[] finished = createFinishedMessage(masterSecret, handshakeHash, keys);
		TLSRecord clientFinished = new TLSRecord(SSLProtocol.TLS_1_2, new ClientFinished(finished));

		LOGGER.debug("Handshake hash input ({} bytes): {}", handshakeBytes.length, Bytes.hexString(handshakeBytes));
		LOGGER.debug("Handshake SHA-256 hash: {}", Bytes.hexString(handshakeHash));
		LOGGER.debug("Master secret: {}", Bytes.hexString(masterSecret));
		LOGGER.debug("Client write key: {}", Bytes.hexString(keys.getClientWriteKey()));
		LOGGER.debug("Client IV: {}", Bytes.hexString(keys.getClientIV()));

		LOGGER.debug("Sending Client Finished Message: {}", clientFinished);
		byte[] clientFinishedBytes = clientFinished.toByteArray();
		sendTLSRecord(clientFinishedBytes);
		LOGGER.debug("Sent Client Finished Message:\n{}", Bytes.hexDump(clientFinishedBytes));

		// 7. Receive ChangeCipherSpec and Finished
		byte[] serverChangeCipherSpec = receiveTLSRecord(); // type 0x14
		LOGGER.debug("Received ChangeCipherSpec: {}", serverChangeCipherSpec);

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

	private TLSRecord sendClientHello() throws IOException {
		TLSRecord clientHelloRecord = new TLSRecord(SSLProtocol.TLS_1_0,
				new ClientHello(List.of(host), new CipherSuites(CipherSuiteName.values()), List.of(CurveName.values())));
		LOGGER.debug("Sending Client Hello: {}", clientHelloRecord);
		byte[] clientHelloBytes = clientHelloRecord.toByteArray();
		accumulateHandshake(clientHelloBytes);
		sendTLSRecord(clientHelloBytes);
		LOGGER.debug("Sent Client Hello:\n{}", Bytes.hexDump(clientHelloBytes));
		return clientHelloRecord;
	}

	private TLSRecord receiveServerHello() throws IOException {
		byte[] serverHelloBytes = receiveTLSRecord();
		accumulateHandshake(serverHelloBytes);
		ByteArrayInputStream bis = new ByteArrayInputStream(serverHelloBytes);
		TLSRecord serverHelloRecord = TLSRecord.from(bis);
		LOGGER.debug("Received Server Hello: {}", serverHelloRecord);
		return serverHelloRecord;
	}

	public X509Certificate parseCertificate(final byte[] certData) throws Exception {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certData));
	}

	public byte[] generateX25519ClientPublic(final ServerKeyExchange ske) throws Exception {
		byte[] serverPubBytes = ske.getPublicKey().getValue().getBytes();

		// 1. Parse server public key
		PublicKey serverPublicKey = X25519KeyGeneration.getPublicKey(serverPubBytes);

		// 2. Generate client key pair
		this.clientKeyPair = X25519KeyGeneration.generateKeyPair();

		// 3. Compute shared secret
		this.preMasterSecret = X25519KeyGeneration.getSharedSecret(clientKeyPair.getPrivate(), serverPublicKey);

		// 4. Encode client public key
		return X25519KeyGeneration.getBytes(clientKeyPair.getPublic());
	}

	public byte[] createFinishedMessage(final byte[] masterSecret, final byte[] handshakeHash, final ExchangeKeys keys) throws Exception {
		// 1. Compute verify_data
		byte[] verifyData = PseudoRandomFunction.apply(masterSecret, "client finished", handshakeHash, 12);
		LOGGER.debug("Computed verify_data: {}", Bytes.hexString(verifyData));

		// 2. Construct handshake body for Finished message
		TLSHandshake clientFinished = new TLSHandshake(new ClientFinished(verifyData));
		byte[] finishedPlaintext = clientFinished.toByteArray(); // Handshake message (unencrypted)

		// 3. Encrypt with keys
		return encryptWithClientKeyAEAD(finishedPlaintext, keys);
	}

	public byte[] encryptWithClientKeyAEAD(final byte[] plaintext, final ExchangeKeys keys) throws Exception {
		// 1. Generate 8-byte explicit nonce
		byte[] explicitNonce = new byte[8];
		new SecureRandom().nextBytes(explicitNonce);

		// 2. Full nonce = 4-byte fixed IV + 8-byte explicit nonce
		byte[] fullNonce = Bytes.concatenate(keys.getClientIV(), explicitNonce);

		// 3. Calculate AAD length = plaintext length + 16 (tag length)
		short tagLength = 16;
		int aadLength = plaintext.length + tagLength;

		// 4. Construct AAD (TLS header)
		ByteBuffer aadBuffer = ByteBuffer.allocate(13);
		// first 8 bytes is the AAD sequence number
		aadBuffer.putLong(clientSequenceNumber); // 8-byte sequence number
		aadBuffer.put(RecordType.HANDSHAKE.value()); // Handshake
		aadBuffer.putShort(SSLProtocol.TLS_1_2.handshakeVersion());
		aadBuffer.putShort((short) aadLength); // Encrypted Finished length
		byte[] aad = aadBuffer.array();

		this.clientSequenceNumber++;

		// 5. Initialize Cipher and provide AAD before encrypting
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmSpec = new GCMParameterSpec(128, fullNonce);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keys.getClientWriteKey(), "AES"), gcmSpec);
		cipher.updateAAD(aad);

		// 6. Encrypt plaintext
		byte[] ciphertext = cipher.doFinal(plaintext);

		// 7. Concatenate explicit nonce, ciphertext
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(explicitNonce);
		bos.write(ciphertext);

		LOGGER.debug("Encrypting Finished Message:");
		LOGGER.debug("Client IV: {}", Bytes.hexString(keys.getClientIV()));
		LOGGER.debug("Server IV: {}", Bytes.hexString(keys.getServerIV()));
		LOGGER.debug("Client Write Key: {}", Bytes.hexString(keys.getClientWriteKey()));
		LOGGER.debug("Server Write Key: {}", Bytes.hexString(keys.getServerWriteKey()));
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
		byte[] nonce = Bytes.concatenate(keys.getServerIV(), explicitNonce);
		LOGGER.debug("Server Finished full nonce: {}", Bytes.hexString(nonce));

		// 4. Decrypt
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keys.getServerWriteKey(), "AES"), gcmSpec);
		cipher.updateAAD(header); // must match encryption AAD
		return cipher.doFinal(ciphertext); // plaintext or throws AEADBadTagException
	}

	public void accumulateHandshake(final byte[] tlsRecordBytes) {
		RecordType recordType = RecordType.fromValue(tlsRecordBytes[0]);
		LOGGER.debug("Accumulate handshake: {}", recordType);
		if (RecordType.HANDSHAKE != recordType) {
			throw new IllegalArgumentException("Type " + recordType + " is invalid, only accumulate " + RecordType.HANDSHAKE + " type");
		}
		handshakeMessages.write(tlsRecordBytes, RecordHeader.BYTES, tlsRecordBytes.length - RecordHeader.BYTES);
	}

	public byte[] getHandshakeTranscript() {
		return handshakeMessages.toByteArray();
	}

	public static byte[] deriveMasterSecret(final byte[] preMasterSecret, final byte[] clientRandom, final byte[] serverRandom) throws Exception {
		return PseudoRandomFunction.apply(preMasterSecret, "master secret", Bytes.concatenate(clientRandom, serverRandom), 48);
	}

	public static byte[] deriveKeyBlock(final byte[] masterSecret, final byte[] serverRandom, final byte[] clientRandom, final int length)
			throws Exception {
		return PseudoRandomFunction.apply(masterSecret, "key expansion", Bytes.concatenate(serverRandom, clientRandom), length);
	}

	public static byte[] sha256(final byte[] input) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(input);
	}

	public static byte[] sha384(final byte[] input) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-384");
		return digest.digest(input);
	}
}
