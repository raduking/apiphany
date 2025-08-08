package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;

/**
 * Represents the ServerHello message in TLS handshake protocol.
 * <p>
 * This is the server's response to ClientHello, containing the selected connection parameters for the TLS session.
 *
 * @author Radu Sebastian LAZIN
 */
public class ServerHello implements TLSHandshakeBody {

	/**
	 * The selected protocol version.
	 */
	private final Version version;

	/**
	 * The server-generated random value.
	 */
	private final ExchangeRandom serverRandom;

	/**
	 * The session ID for session resumption.
	 */
	private final SessionId sessionId;

	/**
	 * The selected cipher suite.
	 */
	private final CipherSuite cipherSuite;

	/**
	 * The selected compression method.
	 */
	private final CompressionMethod compressionMethod;

	/**
	 * The server extensions.
	 */
	private final Extensions extensions;

	/**
	 * Constructs a ServerHello message.
	 *
	 * @param version the selected protocol version
	 * @param serverRandom the server random value
	 * @param sessionId the session identifier
	 * @param cipherSuite the chosen cipher suite
	 * @param compressionMethod the chosen compression method
	 * @param extensions the server extensions
	 */
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

	/**
	 * Parses a ServerHello from an input stream.
	 *
	 * @param is the input stream containing the message data
	 * @return the parsed ServerHello object
	 * @throws IOException if an I/O error occurs
	 */
	public static ServerHello from(final InputStream is) throws IOException {
		Version version = Version.from(is);
		ExchangeRandom serverRandom = ExchangeRandom.from(is);
		SessionId sessionId = SessionId.from(is);
		CipherSuite cipherSuite = CipherSuite.fromValue(UInt16.from(is).getValue());
		CompressionMethod compressionMethod = CompressionMethod.fromValue(UInt8.from(is).getValue());
		Extensions extensions = Extensions.from(is);

		return new ServerHello(
				version,
				serverRandom,
				sessionId,
				cipherSuite,
				compressionMethod,
				extensions);
	}

	/**
	 * Returns the binary representation of this ServerHello.
	 *
	 * @return byte array containing all message fields
	 */
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

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of all fields combined
	 */
	@Override
	public int sizeOf() {
		return version.sizeOf()
				+ serverRandom.sizeOf()
				+ sessionId.sizeOf()
				+ cipherSuite.sizeOf()
				+ compressionMethod.sizeOf()
				+ extensions.sizeOf();
	}

	/**
	 * Returns the handshake message type.
	 *
	 * @return always returns SERVER_HELLO
	 */
	@Override
	public HandshakeType getType() {
		return HandshakeType.SERVER_HELLO;
	}

	/**
	 * Returns a JSON representation of this ServerHello.
	 *
	 * @return JSON string containing the message data
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the protocol version.
	 *
	 * @return the Version object
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Returns the server random value.
	 *
	 * @return the ExchangeRandom object
	 */
	public ExchangeRandom getServerRandom() {
		return serverRandom;
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
	 * Returns the selected cipher suite.
	 *
	 * @return the CipherSuite object
	 */
	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}

	/**
	 * Returns the compression method.
	 *
	 * @return the CompressionMethod object
	 */
	public CompressionMethod getCompressionMethod() {
		return compressionMethod;
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
