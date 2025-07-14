package org.apiphany.security.ssl.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
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

	private KeyPair clientKeyPair;
	private PublicKey serverPublicKey;

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
		LOGGER.debug("Client random:\n{}", Bytes.hexDumpRaw(clientRandom));

		// 2. Receive Server Hello
		TLSRecord serverHelloRecord = receiveServerHello();

		ServerHello serverHello = serverHelloRecord.getHandshake(ServerHello.class);
		this.serverRandom = serverHello.getServerRandom().toByteArray();
		LOGGER.debug("Server random:\n{}", Bytes.hexDumpRaw(serverRandom));
		LOGGER.debug("Received Server Hello:\n{}", Bytes.hexDumpRaw(serverHello.toByteArray()));

		if (!serverHelloRecord.hasHandshake(Certificates.class)) {
			serverHelloRecord = TLSRecord.from(in);
			accumulateHandshakes(serverHelloRecord.getFragments(TLSHandshake.class));
		}
		Certificates certificates = serverHelloRecord.getHandshake(Certificates.class);
		X509Certificate x509Certificate = parseCertificate(certificates.getList().getFirst().getData().getBytes());
		LOGGER.debug("Received Server Certificate:\n{}", Bytes.hexDumpRaw(certificates.toByteArray()));
		LOGGER.debug("Received Server X509Certificate: {}", x509Certificate);

		if (!serverHelloRecord.hasHandshake(ServerKeyExchange.class)) {
			serverHelloRecord = TLSRecord.from(in);
			accumulateHandshakes(serverHelloRecord.getFragments(TLSHandshake.class));
		}
		ServerKeyExchange serverKeyExchange = serverHelloRecord.getHandshake(ServerKeyExchange.class);
		LOGGER.debug("Received Server Key Exchange:\n{}", Bytes.hexDumpRaw(serverKeyExchange.toByteArray()));

		if (!serverHelloRecord.hasHandshake(ServerHelloDone.class)) {
			serverHelloRecord = TLSRecord.from(in);
			accumulateHandshakes(serverHelloRecord.getFragments(TLSHandshake.class));
		}
		ServerHelloDone serverHelloDone = serverHelloRecord.getHandshake(ServerHelloDone.class);
		LOGGER.debug("Received Server Hello Done:\n{}", Bytes.hexDumpRaw(serverHelloDone.toByteArray()));

		// 3. Generate Client Key Exchange
		CipherSuiteName selectedCipher = serverHello.getCipherSuite().getCipher();
		byte[] clientPublic;
		if (serverKeyExchange.getCurveInfo().getName() == CurveName.X25519) {
			byte[] leServerPublic = serverKeyExchange.getPublicKey().getValue().getBytes();
			LOGGER.debug("Server public key (raw bytes from key exchange):\n{}", Bytes.hexDumpRaw(leServerPublic));
			clientPublic = getX25519ClientPublicBytes(serverKeyExchange);
			LOGGER.debug("Server public key ({}):\n{}", serverPublicKey.getClass(), serverPublicKey);
			LOGGER.debug("Keys match: {}", X25519Keys.verifyKeyMatch(X25519Keys.toRawByteArrayV1(serverPublicKey), serverPublicKey));
		} else {
			throw new SSLException("Unsupported cipher suite: " + selectedCipher);
		}
		// Compute shared secret
		this.preMasterSecret = X25519Keys.getECDHESharedSecret(clientKeyPair.getPrivate(), serverPublicKey);
		LOGGER.debug("Pre Master Secret:\n{}", Bytes.hexDumpRaw(preMasterSecret));

		// 4. Send Client Key Exchange
		TLSRecord clientKeyExchangeRecord = new TLSRecord(SSLProtocol.TLS_1_2, new ClientKeyExchange(clientPublic));
		byte[] clientKeyExchangeBytes = clientKeyExchangeRecord.toByteArray();

		sendTLSRecord(clientKeyExchangeBytes);
		accumulateHandshakes(clientKeyExchangeRecord.getFragments(TLSHandshake.class));
		LOGGER.debug("Sent Client Key Exchange:\n{}", Bytes.hexDumpRaw(clientKeyExchangeBytes));

		// 5. Derive Master Secret and Keys
		this.masterSecret = PseudoRandomFunction.apply(preMasterSecret, "master secret", Bytes.concatenate(clientRandom, serverRandom), 48);
		LOGGER.debug("Master secret:\n{}", Bytes.hexDumpRaw(masterSecret));
		byte[] keyBlock = PseudoRandomFunction.apply(masterSecret, "key expansion",
				Bytes.concatenate(serverRandom, clientRandom), (ExchangeKeys.KEY_LENGTH + ExchangeKeys.IV_LENGTH) * 2);
		// Extract keys
		ExchangeKeys keys = ExchangeKeys.from(keyBlock, ExchangeKeys.Type.AHEAD);

		// 6. Send Client Change Cipher Spec
		TLSRecord changeCypherSpecRecord = new TLSRecord(SSLProtocol.TLS_1_2, new ChangeCipherSpec());
		sendTLSRecord(changeCypherSpecRecord.toByteArray());
		LOGGER.debug("Sent Client Change Cipher Spec:\n{}", Bytes.hexDumpRaw(changeCypherSpecRecord.toByteArray()));

		// 7. Send Finished
		byte[] handshakeBytes = getConcatenatedHandshakeMessages();
		LOGGER.debug("Concatenated handshake message content types:\n{}", getConcatenatedHandshakeMessageTypes());
		byte[] handshakeHash = sha("SHA-384", handshakeBytes);

		byte[] verifyData = PseudoRandomFunction.apply(masterSecret, "client finished", handshakeHash, 12);
		LOGGER.debug("Computed verify_data:\n{}", Bytes.hexDumpRaw(verifyData));
		TLSHandshake finished = new TLSHandshake(new Finished(verifyData));

		TLSRecord clientFinished = createClientFinished(finished, keys);

		byte[] clientFinishedBytes = clientFinished.toByteArray();
		sendTLSRecord(clientFinishedBytes);
		LOGGER.debug("Sent Client Finished Message:\n{}", Bytes.hexDumpRaw(clientFinishedBytes));

		// 8. Receive ChangeCipherSpec and Finished
		TLSRecord serverChangeCipherSpec = TLSRecord.from(in); // type 0x14
		LOGGER.debug("Received Change Cipher Spec: {}", serverChangeCipherSpec);

		// 9. Receive Server Finished Record
		TLSRecord serverFinishedRecord = TLSRecord.from(in, ThrowingBiFunction.unchecked((is, total) -> EncryptedFinished.from(is, total, 12)));
		LOGGER.debug("Server Finished TLS Record Header: {}", Bytes.hexDumpRaw(serverFinishedRecord.toByteArray()));

		LOGGER.info("TLS 1.2 handshake complete!");

		return serverFinishedRecord.toByteArray();
	}

	private TLSRecord sendClientHello() throws IOException {
		TLSRecord clientHelloRecord = new TLSRecord(SSLProtocol.TLS_1_0,
				new ClientHello(List.of(host), new CipherSuites(CipherSuiteName.values()), List.of(CurveName.values())));
		byte[] clientHelloBytes = clientHelloRecord.toByteArray();
		sendTLSRecord(clientHelloBytes);
		accumulateHandshakes(clientHelloRecord.getFragments(TLSHandshake.class));
		LOGGER.debug("Sent Client Hello:\n{}", Bytes.hexDumpRaw(clientHelloBytes));
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
		return X25519Keys.toRawByteArrayV1(clientKeyPair.getPublic());
	}

	public TLSRecord createClientFinished(final TLSHandshake handshake, final ExchangeKeys keys) throws Exception {
		byte[] plaintext = handshake.toByteArray();
		LOGGER.debug("Finished (hex):\n{}", Bytes.hexDumpRaw(plaintext));

		long seq = this.clientSequenceNumber++;
		byte[] seqBytes = Int64.toByteArray(seq); // 8 bytes

		byte[] fixedIV = keys.getClientIV(); // 4 bytes
		byte[] fullIV = new byte[12];
		System.arraycopy(fixedIV, 0, fullIV, 0, 4);
		System.arraycopy(seqBytes, 0, fullIV, 4, 8);

		short aadLength = (short) plaintext.length;
		AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData(seq, SSLProtocol.TLS_1_2, aadLength);
		LOGGER.debug("AAD:\n{}", Bytes.hexDumpRaw(aad.toByteArray()));

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec spec = new GCMParameterSpec(128, fullIV);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keys.getClientWriteKey(), "AES"), spec);
		cipher.updateAAD(aad.toByteArray());

		byte[] explicitNonce = Arrays.copyOfRange(fullIV, 4, 12);
		byte[] encrypted = cipher.doFinal(plaintext);
		LOGGER.debug("Encrypted (ciphertext + tag):\n{}", Bytes.hexDumpRaw(encrypted));

		EncryptedFinished encryptedFinished = new EncryptedFinished(explicitNonce, encrypted);
		return new TLSRecord(SSLProtocol.TLS_1_2, encryptedFinished);
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
