package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class ServerHello implements TLSHandshakeBody {

	private Version version;

	private ExchangeRandom serverRandom;

	private SessionId sessionId;

	private CipherSuite cipherSuite;

	private CompressionMethod compressionMethod;

	private Extensions extensions;

	public ServerHello(
			final Version version,
			final ExchangeRandom serverRandom,
			final SessionId sessionId,
			final CipherSuite cipherSuite,
			final CompressionMethod compressionMethod,
			final Extensions extensions) {
		this.version = version;
		this.serverRandom = serverRandom;
		this.sessionId = sessionId;
		this.cipherSuite = cipherSuite;
		this.compressionMethod = compressionMethod;
		this.extensions = extensions;
	}

	public static ServerHello from(final InputStream is) throws IOException {
		Version version = Version.from(is);
		ExchangeRandom serverRandom = ExchangeRandom.from(is);
		SessionId sessionId = SessionId.from(is);
		CipherSuite cipherSuite = CipherSuite.fromValue(Int16.from(is).getValue());
		CompressionMethod compressionMethod = CompressionMethod.fromValue(Int8.from(is).getValue());
		Extensions extensions = Extensions.from(is);

		return new ServerHello(
				version,
				serverRandom,
				sessionId,
				cipherSuite,
				compressionMethod,
				extensions);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(version.toByteArray());
		buffer.put(serverRandom.toByteArray());
		buffer.put(sessionId.toByteArray());
		buffer.put(cipherSuite.toByteArray());
		buffer.put(compressionMethod.toByteArray());
		buffer.put(extensions.toByteArray());
		return buffer.array();
	}

	@Override
	public int sizeOf() {
		return version.sizeOf()
				+ serverRandom.sizeOf()
				+ sessionId.sizeOf()
				+ cipherSuite.sizeOf()
				+ compressionMethod.sizeOf()
				+ extensions.sizeOf();
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.SERVER_HELLO;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
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
}
