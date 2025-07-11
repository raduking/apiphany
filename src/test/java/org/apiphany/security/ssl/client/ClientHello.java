package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.function.ThrowingRunnable;

/**
 * Client Hello TLS record.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientHello implements TLSHandshakeBody {

	private Version clientVersion;

	private ExchangeRandom clientRandom;

	private SessionId sessionId;

	private CipherSuites cipherSuites;

	private CompressionMethods compressionMethods;

	private Extensions extensions;

	protected ClientHello(
			final Version clientVersion,
			final ExchangeRandom clientRandom,
			final SessionId sessionId,
			final CipherSuites cipherSuites,
			final CompressionMethods compressionMethods,
			final Extensions extensions) {
		this.clientVersion = clientVersion;
		this.clientRandom = clientRandom;
		this.sessionId = sessionId;
		this.cipherSuites = cipherSuites;
		this.compressionMethods = compressionMethods;
		this.extensions = extensions;
	}

	public ClientHello(final List<String> serverNames, final CipherSuites cypherSuites, final List<CurveName> curveNames) {
		this(
				new Version(SSLProtocol.TLS_1_2),
				new ExchangeRandom(ExchangeRandom.generateLinear()),
				new SessionId(),
				cypherSuites,
				new CompressionMethods(),
				new Extensions(serverNames, curveNames)
		);
	}

	public ClientHello(final List<String> serverNames, final List<CipherSuite> cypherSuites, final List<CurveName> curveNames) {
		this(serverNames, new CipherSuites(cypherSuites), curveNames);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(clientVersion.toByteArray());
			dos.write(clientRandom.toByteArray());
			dos.write(sessionId.toByteArray());
			dos.write(cipherSuites.toByteArray());
			dos.write(compressionMethods.toByteArray());
			dos.write(extensions.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	public static ClientHello from(final InputStream is) throws IOException {
		Version clientVersion = Version.from(is);
		ExchangeRandom clientRandom = ExchangeRandom.from(is);
		SessionId sessionId = SessionId.from(is);
		CipherSuites cipherSuites = CipherSuites.from(is);
		CompressionMethods compressionMethods = CompressionMethods.from(is);
		Extensions extensions = Extensions.from(is);

		return new ClientHello(clientVersion, clientRandom,
				sessionId, cipherSuites, compressionMethods, extensions);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return clientVersion.size()
				+ clientRandom.size()
				+ sessionId.size()
				+ cipherSuites.size()
				+ compressionMethods.size()
				+ extensions.size();
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.CLIENT_HELLO;
	}

	public Version getClientVersion() {
		return clientVersion;
	}

	public ExchangeRandom getClientRandom() {
		return clientRandom;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public CipherSuites getCipherSuites() {
		return cipherSuites;
	}

	public CompressionMethods getCompressionMethods() {
		return compressionMethods;
	}

	public Extensions getExtensions() {
		return extensions;
	}
}
