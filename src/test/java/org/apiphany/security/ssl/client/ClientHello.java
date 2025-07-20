package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

/**
 * Client Hello TLS record.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientHello implements TLSHandshakeBody {

	private final Version clientVersion;

	private final ExchangeRandom clientRandom;

	private final SessionId sessionId;

	private final CipherSuites cipherSuites;

	private final CompressionMethods compressionMethods;

	private final Extensions extensions;

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

	public ClientHello(final List<String> serverNames, final CipherSuites cypherSuites, final List<NamedCurve> namedCurves, final List<SignatureAlgorithm> signatureAlgorithms) {
		this(
				new Version(SSLProtocol.TLS_1_2),
				ExchangeRandom.linear(),
				new SessionId(),
				cypherSuites,
				new CompressionMethods(),
				new Extensions(serverNames, namedCurves, signatureAlgorithms)
		);
	}

	public ClientHello(final List<String> serverNames, final List<CipherSuite> cypherSuites, final List<NamedCurve> namedCurves, final List<SignatureAlgorithm> signatureAlgorithms) {
		this(serverNames, new CipherSuites(cypherSuites), namedCurves, signatureAlgorithms);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(clientVersion.toByteArray());
		buffer.put(clientRandom.toByteArray());
		buffer.put(sessionId.toByteArray());
		buffer.put(cipherSuites.toByteArray());
		buffer.put(compressionMethods.toByteArray());
		buffer.put(extensions.toByteArray());
		return buffer.array();
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
	public int sizeOf() {
		return clientVersion.sizeOf()
				+ clientRandom.sizeOf()
				+ sessionId.sizeOf()
				+ cipherSuites.sizeOf()
				+ compressionMethods.sizeOf()
				+ extensions.sizeOf();
	}

	@Override
	public HandshakeType getType() {
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
