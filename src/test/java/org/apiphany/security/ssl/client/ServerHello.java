package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class ServerHello implements Sizeable {

	private RecordHeader recordHeader;

	private HandshakeHeader handshakeHeader;

	private Version version;

	private ExchangeRandom serverRandom;

	private SessionId sessionId;

	private CipherSuite cipherSuite;

	private CompressionMethod compressionMethod;

	private Extensions extensions;

	private ServerCertificate serverCertificate;

	private ServerKeyExchange serverKeyExchange;

	private HandshakeHeader serverHelloDone;

	public ServerHello(final RecordHeader recordHeader,
			final HandshakeHeader handshakeHeader,
			final Version version,
			final ExchangeRandom serverRandom,
			final SessionId sessionId,
			final CipherSuite cipherSuite,
			final CompressionMethod compressionMethod,
			final Extensions extensions,
			final ServerCertificate serverCertificate,
			final ServerKeyExchange serverKeyExchange,
			final HandshakeHeader serverHelloDone) {
		this.recordHeader = recordHeader;
		this.handshakeHeader = handshakeHeader;
		this.version = version;
		this.serverRandom = serverRandom;
		this.sessionId = sessionId;
		this.cipherSuite = cipherSuite;
		this.compressionMethod = compressionMethod;
		this.extensions = extensions;
		this.serverCertificate = serverCertificate;
		this.serverKeyExchange = serverKeyExchange;
		this.serverHelloDone = serverHelloDone;
	}

	public static ServerHello from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		Version version = Version.from(is);
		ExchangeRandom serverRandom = ExchangeRandom.from(is);
		SessionId sessionId = SessionId.from(is);
		CipherSuite cipherSuite = CipherSuite.from(is);
		CompressionMethod compressionMethod = CompressionMethod.from(is);
		Extensions extensions = Extensions.from(is);
		ServerCertificate serverCertificate = ServerCertificate.from(is);
		ServerKeyExchange serverKeyExchange = ServerKeyExchange.from(is);
		HandshakeHeader serverHelloDone = HandshakeHeader.from(is);

		return new ServerHello(
				recordHeader,
				handshakeHeader,
				version,
				serverRandom,
				sessionId,
				cipherSuite,
				compressionMethod,
				extensions,
				serverCertificate,
				serverKeyExchange,
				serverHelloDone);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(recordHeader.toByteArray());
		dos.write(handshakeHeader.toByteArray());
		dos.write(version.toByteArray());
		dos.write(serverRandom.toByteArray());
		dos.write(sessionId.toByteArray());
		dos.write(cipherSuite.toByteArray());
		dos.write(compressionMethod.toByteArray());
		dos.write(extensions.toByteArray());
		dos.write(serverCertificate.toByteArray());
		dos.write(serverKeyExchange.toByteArray());
		dos.write(serverHelloDone.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public int size() {
		return recordHeader.size()
				+ handshakeHeader.size()
				+ version.size()
				+ serverRandom.size()
				+ sessionId.size()
				+ cipherSuite.size()
				+ compressionMethod.size()
				+ extensions.size()
				+ serverCertificate.size()
				+ serverKeyExchange.size()
				+ serverHelloDone.size();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public RecordHeader getRecordHeader() {
		return recordHeader;
	}

	public HandshakeHeader getHandshakeHeader() {
		return handshakeHeader;
	}

	public Version getVersion() {
		return version;
	}

	public ExchangeRandom getServerRandom() {
		return serverRandom;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}

	public CompressionMethod getCompressionMethod() {
		return compressionMethod;
	}

	public Extensions getExtensions() {
		return extensions;
	}

	public ServerCertificate getServerCertificate() {
		return serverCertificate;
	}

	public ServerKeyExchange getServerKeyExchange() {
		return serverKeyExchange;
	}

	public HandshakeHeader getServerHelloDone() {
		return serverHelloDone;
	}
}
