package org.apiphany.security.ssl.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

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

	public static byte[] readServerHello(final InputStream in) throws Exception {
		RecordHeader recordHeader = RecordHeader.from(in);
		LOGGER.debug("[ServerHello] record header: {}", recordHeader);

		HandshakeHeader handshakeHeader = HandshakeHeader.from(in);
		LOGGER.debug("[ServerHello] handshake header: {}", handshakeHeader);

		Version version = Version.from(in);
		LOGGER.debug("[ServerHello] version: {}", version);

		HandshakeRandom handshakeRandom = HandshakeRandom.from(in);
		LOGGER.debug("[ServerHello] random: {}", handshakeRandom);

		SessionId sessionId = SessionId.from(in);
		LOGGER.debug("[ServerHello] session ID: {}", sessionId);

		CypherSuite cypherSuite = CypherSuite.from(in);
		LOGGER.debug("[ServerHello] session ID: {}", cypherSuite);

		CompressionMethod compressionMethod = CompressionMethod.from(in);
		LOGGER.debug("[ServerHello] compression method: {}", compressionMethod);

		Int16 extensionsLength = Int16.from(in);
		LOGGER.debug("[ServerHello] extensions length: {}", extensionsLength);

		RenegotiationInfo renegotiationInfo = RenegotiationInfo.from(in);
		LOGGER.debug("[ServerHello] renegotiation information: {}", renegotiationInfo);

		return handshakeRandom.getRandom();
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

	public static void readServerKeyExchange(final InputStream in) throws Exception {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(in);
		LOGGER.debug("[ServerKeyExchange] handshake header: {}", handshakeHeader);

		CurveInfo curveInfo = CurveInfo.from(in);
		LOGGER.debug("[ServerKeyExchange] curve information: {}", curveInfo);

		PublicKey publicKey = PublicKey.from(in);
		LOGGER.debug("[ServerKeyExchange] public key: {}", publicKey);

		Signature signature = Signature.from(in);
		LOGGER.debug("[ServerKeyExchange] signature: {}", signature);
	}

	public static void readServerHelloDone(final InputStream in) throws Exception {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(in);
		LOGGER.debug("[ServerHelloDone] handshake header: {}", handshakeHeader);
	}

	public byte[] performHandshake() throws Exception {
		connect();

		// 1. Send Client Hello
		ClientHello clientHello = new ClientHello(List.of(host), new CypherSuites(CypherSuiteName.values()));
		byte[] clientHelloBytes = clientHello.toByteArray();
		accumulateHandshake(clientHelloBytes);
		this.clientRandom = clientHello.clientRandom.getRandom();

		LOGGER.debug("Sending Client Hello: {}", Bytes.hexString(clientHelloBytes));
		sendTLSRecord(clientHelloBytes);
		LOGGER.debug("Sent Client Hello");

		// 2. Receive Server Hello
		byte[] serverHello = receiveTLSRecord();
		LOGGER.debug("Received Server Hello: {}", Bytes.hexString(serverHello));

		ByteArrayInputStream bis = new ByteArrayInputStream(serverHello);
		// a. Read Server Hello
		this.serverRandom = readServerHello(bis);

		// b. Read Server Certificate
		byte[] certificate = readServerCertificate(bis);
		X509Certificate x509Certificate = parseCertificate(certificate);
		LOGGER.debug("Received Server Certificate: {}", x509Certificate);

		// c. Read Server Key Exchange
		readServerKeyExchange(bis);

		// d. Read Server Hello Done
		readServerHelloDone(bis);

		// 3. Generate Client Key Exchange
		byte[] encryptedPreMaster = generateKeyExchange(x509Certificate);

		// 4. Send Client Key Exchange
		byte[] clientKeyExchange = createKeyExchangeMessage(encryptedPreMaster);
		accumulateHandshake(clientKeyExchange);
		sendTLSRecord(clientKeyExchange);
		LOGGER.debug("Sent Client Key Exchange");

		// 5. Derive Master Secret and Keys
		this.masterSecret = Bytes.deriveMasterSecret(preMasterSecret, clientRandom, serverRandom);
		byte[] keyBlock = Bytes.deriveKeyBlock(masterSecret, serverRandom, clientRandom, 104);

		// Extract keys
		ByteBuffer buf = ByteBuffer.wrap(keyBlock);
		byte[] clientMAC = new byte[20];
		buf.get(clientMAC);
		byte[] serverMAC = new byte[20];
		buf.get(serverMAC);
		byte[] clientKey = new byte[16];
		buf.get(clientKey);
		byte[] serverKey = new byte[16];
		buf.get(serverKey);
		byte[] clientIV = new byte[16];
		buf.get(clientIV);
		byte[] serverIV = new byte[16];
		buf.get(serverIV);

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
}
