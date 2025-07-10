package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

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
		CipherSuite cipherSuite = CipherSuite.from(is);
		CompressionMethod compressionMethod = CompressionMethod.from(is);
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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(version.toByteArray());
			dos.write(serverRandom.toByteArray());
			dos.write(sessionId.toByteArray());
			dos.write(cipherSuite.toByteArray());
			dos.write(compressionMethod.toByteArray());
			dos.write(extensions.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public int size() {
		return version.size()
				+ serverRandom.size()
				+ sessionId.size()
				+ cipherSuite.size()
				+ compressionMethod.size()
				+ extensions.size();
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
