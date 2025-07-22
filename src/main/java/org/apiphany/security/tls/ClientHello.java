package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

/**
 * Represents the ClientHello message in TLS handshake protocol.
 * <p>
 * This is the first message sent by the client when initiating a TLS connection, containing the client's capabilities
 * and preferences for the secure session.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientHello implements TLSHandshakeBody {

	/**
	 * The TLS protocol version supported by the client.
	 */
	private final Version clientVersion;

	/**
	 * The client-generated random value for cryptographic purposes.
	 */
	private final ExchangeRandom clientRandom;

	/**
	 * The session ID for session resumption (empty for new sessions).
	 */
	private final SessionId sessionId;

	/**
	 * The list of cipher suites supported by the client.
	 */
	private final CipherSuites cipherSuites;

	/**
	 * The compression methods supported by the client.
	 */
	private final CompressionMethods compressionMethods;

	/**
	 * The extensions requested by the client.
	 */
	private final Extensions extensions;

	/**
	 * Constructs a ClientHello message with all components.
	 *
	 * @param clientVersion the protocol version
	 * @param clientRandom the client random value
	 * @param sessionId the session identifier
	 * @param cipherSuites the supported cipher suites
	 * @param compressionMethods the compression methods
	 * @param extensions the TLS extensions
	 */
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

	/**
	 * Constructs a ClientHello with common parameters.
	 *
	 * @param secureRandom the secure random generator
	 * @param cypherSuites the cipher suites container
	 * @param serverNames the SNI hostnames
	 * @param namedCurves the supported elliptic curves
	 * @param signatureAlgorithms the supported signature algorithms
	 */
	public ClientHello(final SecureRandom secureRandom, final CipherSuites cypherSuites, final List<String> serverNames,
			final List<NamedCurve> namedCurves, final List<SignatureAlgorithm> signatureAlgorithms) {
		this(
				new Version(SSLProtocol.TLS_1_2),
				new ExchangeRandom(secureRandom),
				new SessionId(),
				cypherSuites,
				new CompressionMethods(),
				new Extensions(serverNames, namedCurves, signatureAlgorithms));
	}

	/**
	 * Constructs a ClientHello with list of cipher suites.
	 *
	 * @param secureRandom the secure random generator
	 * @param cypherSuites the list of cipher suites
	 * @param serverNames the SNI hostnames
	 * @param namedCurves the supported elliptic curves
	 * @param signatureAlgorithms the supported signature algorithms
	 */
	public ClientHello(final SecureRandom secureRandom, final List<CipherSuite> cypherSuites, final List<String> serverNames,
			final List<NamedCurve> namedCurves, final List<SignatureAlgorithm> signatureAlgorithms) {
		this(secureRandom, new CipherSuites(cypherSuites), serverNames, namedCurves, signatureAlgorithms);
	}

	/**
	 * Returns the binary representation of this ClientHello.
	 *
	 * @return byte array containing all message fields
	 */
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

	/**
	 * Parses a ClientHello from an input stream.
	 *
	 * @param is the input stream containing the message data
	 * @return the parsed ClientHello object
	 * @throws IOException if an I/O error occurs
	 */
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

	/**
	 * Returns a JSON representation of this ClientHello.
	 *
	 * @return JSON string containing the message data
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of all fields combined
	 */
	@Override
	public int sizeOf() {
		return clientVersion.sizeOf()
				+ clientRandom.sizeOf()
				+ sessionId.sizeOf()
				+ cipherSuites.sizeOf()
				+ compressionMethods.sizeOf()
				+ extensions.sizeOf();
	}

	/**
	 * Returns the handshake message type.
	 *
	 * @return always returns CLIENT_HELLO
	 */
	@Override
	public HandshakeType getType() {
		return HandshakeType.CLIENT_HELLO;
	}

	/**
	 * Returns the client's protocol version.
	 *
	 * @return the Version object
	 */
	public Version getClientVersion() {
		return clientVersion;
	}

	/**
	 * Returns the client random value.
	 *
	 * @return the ExchangeRandom object
	 */
	public ExchangeRandom getClientRandom() {
		return clientRandom;
	}

	/**
	 * Returns the session ID.
	 *
	 * @return the SessionId object
	 */
	public SessionId getSessionId() {
		return sessionId;
	}

	/**
	 * Returns the cipher suites.
	 *
	 * @return the CipherSuites object
	 */
	public CipherSuites getCipherSuites() {
		return cipherSuites;
	}

	/**
	 * Returns the compression methods.
	 *
	 * @return the CompressionMethods object
	 */
	public CompressionMethods getCompressionMethods() {
		return compressionMethods;
	}

	/**
	 * Returns the extensions.
	 *
	 * @return the Extensions object
	 */
	public Extensions getExtensions() {
		return extensions;
	}
}
