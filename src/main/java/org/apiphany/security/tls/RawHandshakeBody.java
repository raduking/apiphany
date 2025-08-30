package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;

/**
 * Represents a raw TLS handshake message body with unparsed content.
 * <p>
 * This class encapsulates the binary data of a TLS handshake message before it has been parsed into specific message
 * types. It maintains both the handshake type and the raw bytes of the message body.
 *
 * @author Radu Sebastian LAZIN
 */
public class RawHandshakeBody implements TLSHandshakeBody {

	/**
	 * The type of TLS handshake message
	 */
	private HandshakeType type;

	/**
	 * The raw bytes of the handshake message body.
	 */
	private BytesWrapper bytes;

	/**
	 * Constructs a new raw handshake body with the specified type and data.
	 *
	 * @param type the handshake message type
	 * @param bytes the raw bytes of the handshake message body
	 * @throws NullPointerException if either type or bytes is null
	 */
	public RawHandshakeBody(final HandshakeType type, final BytesWrapper bytes) {
		this.type = Objects.requireNonNull(type, "Handshake type cannot be null");
		this.bytes = Objects.requireNonNull(bytes, "Bytes wrapper cannot be null");
	}

	/**
	 * Creates a new RawHandshakeBody by reading from an input stream.
	 *
	 * @param is the input stream to read from
	 * @param type the handshake message type
	 * @param size the number of bytes to read
	 * @return a new RawHandshakeBody containing the read data
	 * @throws IOException if an I/O error occurs while reading from the stream
	 * @throws IllegalArgumentException if size is negative
	 */
	public static RawHandshakeBody from(final InputStream is, final HandshakeType type, final int size) throws IOException {
		if (size < 0) {
			throw new IllegalArgumentException("Size cannot be negative");
		}
		BytesWrapper data = BytesWrapper.from(is, size);
		return new RawHandshakeBody(type, data);
	}

	/**
	 * Returns a serialized string representation of this handshake message.
	 *
	 * @return the serialized string representation
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the size of the handshake message body in bytes.
	 *
	 * @return the size in bytes
	 */
	@Override
	public int sizeOf() {
		return bytes.sizeOf();
	}

	/**
	 * Converts the handshake message body to a byte array.
	 *
	 * @return the byte array representation of this handshake message
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(bytes.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns the type of this handshake message.
	 *
	 * @return the handshake message type
	 */
	@Override
	public HandshakeType getType() {
		return type;
	}

	/**
	 * Returns the raw bytes of this handshake message body.
	 *
	 * @return the bytes wrapper containing the raw message data
	 */
	public BytesWrapper getBytes() {
		return bytes;
	}
}
