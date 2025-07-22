package org.apiphany.security.tls;

import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Bytes;

/**
 * Represents the ServerHelloDone message in TLS handshake protocol.
 * <p>
 * This empty message signals the end of the server's hello-related messages and indicates the server is waiting for the
 * client's response.
 *
 * @author Radu Sebastian LAZIN
 */
public class ServerHelloDone implements TLSHandshakeBody {

	/**
	 * The size of this message when serialized (always 0 bytes).
	 */
	public static final int BYTES = 0;

	/**
	 * Constructs an empty ServerHelloDone message.
	 * <p>
	 * This message contains no data and serves as a signal.
	 */
	public ServerHelloDone() {
		// empty
	}

	/**
	 * Parses a ServerHelloDone from an input stream.
	 * <p>
	 * Note: This message contains no actual data to parse.
	 *
	 * @param is the input stream (unused)
	 * @return a new ServerHelloDone instance
	 */
	public static ServerHelloDone from(final InputStream is) {
		return new ServerHelloDone();
	}

	/**
	 * Returns the binary representation of this message.
	 *
	 * @return empty byte array (this message contains no data)
	 */
	@Override
	public byte[] toByteArray() {
		return Bytes.EMPTY;
	}

	/**
	 * Returns the size when serialized.
	 *
	 * @return always returns 0 (this message contains no data)
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Returns the handshake message type.
	 *
	 * @return always returns SERVER_HELLO_DONE
	 */
	@Override
	public HandshakeType getType() {
		return HandshakeType.SERVER_HELLO_DONE;
	}

	/**
	 * Returns a JSON representation of this message.
	 *
	 * @return JSON string containing message type information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}
}
