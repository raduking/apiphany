package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt8;

/**
 * Represents a TLS Session Identifier used for session resumption.
 * <p>
 * The Session ID is a variable-length opaque identifier established by the server to identify an active or resumable
 * session state. This class handles both the binary protocol format and higher-level representations of session IDs.
 *
 * <p>
 * Implements {@link TLSObject} for proper serialization in TLS messages.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.4.1.2">RFC 5246 - Session ID</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class SessionId implements TLSObject {

	/**
	 * The value length.
	 */
	private final UInt8 length;

	/**
	 * The session ID value as bytes.
	 */
	private final BytesWrapper value;

	/**
	 * Constructs a SessionId with explicit length and value wrapper.
	 *
	 * @param length the length of the session ID (0-32 bytes)
	 * @param value the wrapped session ID bytes
	 */
	public SessionId(final UInt8 length, final BytesWrapper value) {
		this.length = length;
		this.value = value;
	}

	/**
	 * Constructs a SessionId with explicit length and byte array value.
	 *
	 * @param length the length of the session ID
	 * @param value the raw session ID bytes (will be copied)
	 */
	public SessionId(final UInt8 length, final byte[] value) {
		this(length, new BytesWrapper(value));
	}

	/**
	 * Constructs a SessionId with primitive length and byte array value.
	 *
	 * @param length the length of the session ID as a byte
	 * @param value the raw session ID bytes (will be copied)
	 */
	public SessionId(final byte length, final byte[] value) {
		this(UInt8.of(length), value);
	}

	/**
	 * Constructs a SessionId from a string (encoded as ASCII).
	 *
	 * @param value The string to use as session ID (max 32 chars)
	 */
	public SessionId(final String value) {
		this((byte) value.length(), value.getBytes(StandardCharsets.US_ASCII));
	}

	/**
	 * Constructs an empty SessionId (length = 0).
	 */
	public SessionId() {
		this("");
	}

	/**
	 * Parses a SessionId from an input stream.
	 *
	 * @param is the input stream containing the session ID
	 * @return the parsed SessionId object
	 * @throws IOException If an I/O error occurs
	 * @throws IllegalArgumentException If the length is invalid (>32)
	 */
	public static SessionId from(final InputStream is) throws IOException {
		UInt8 length = UInt8.from(is);
		BytesWrapper value = BytesWrapper.from(is, length.getValue());

		return new SessionId(length, value);
	}

	/**
	 * Returns the binary representation of this SessionId.
	 *
	 * @return byte array containing length byte followed by session ID bytes
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		buffer.put(value.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this SessionId.
	 *
	 * @return JSON string containing the session ID information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size of this SessionId when serialized.
	 *
	 * @return size in bytes (1 byte length + session ID bytes)
	 */
	@Override
	public int sizeOf() {
		return length.sizeOf() + value.sizeOf();
	}

	/**
	 * Returns the length of the session ID.
	 *
	 * @return UInt8 wrapper containing the length
	 */
	public UInt8 getLength() {
		return length;
	}

	/**
	 * Returns the session ID value.
	 *
	 * @return BytesWrapper containing the session ID bytes
	 */
	public BytesWrapper getValue() {
		return value;
	}
}
