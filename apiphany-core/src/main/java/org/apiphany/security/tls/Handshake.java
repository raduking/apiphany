package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.apiphany.io.UInt24;
import org.morphix.lang.JavaObjects;

/**
 * Represents a TLS Handshake protocol message.
 * <p>
 * This class encapsulates both the handshake header and body, providing type-safe access to various handshake message
 * types (ClientHello, ServerHello, etc.).
 *
 * @author Radu Sebastian LAZIN
 */
public class Handshake implements TLSObject {

	/**
	 * The handshake message header containing type and length.
	 */
	private final HandshakeHeader header;

	/**
	 * The handshake message body content.
	 */
	private final TLSHandshakeBody body;

	/**
	 * Constructs a Handshake message with optional header updating.
	 *
	 * @param header the handshake header
	 * @param body the handshake body
	 * @param updateHeader if true, the header length will be recalculated
	 */
	public Handshake(final HandshakeHeader header, final TLSHandshakeBody body, final boolean updateHeader) {
		this.header = updateHeader ? new HandshakeHeader(header.getType(), UInt24.of(body.sizeOf())) : header;
		this.body = body;
	}

	/**
	 * Constructs a Handshake message from body only.
	 *
	 * @param body the handshake body
	 */
	public Handshake(final TLSHandshakeBody body) {
		this(new HandshakeHeader(body.getType()), body, true);
	}

	/**
	 * Constructs a Handshake message with automatic header length calculation.
	 *
	 * @param header the handshake header
	 * @param body the handshake body
	 */
	public Handshake(final HandshakeHeader header, final TLSHandshakeBody body) {
		this(header, body, true);
	}

	/**
	 * Parses a Handshake message from an input stream.
	 *
	 * @param is the input stream containing handshake data
	 * @return the parsed Handshake object
	 * @throws IOException if an I/O error occurs
	 */
	public static Handshake from(final InputStream is) throws IOException {
		HandshakeHeader header = HandshakeHeader.from(is);
		HandshakeType type = header.getType();
		TLSHandshakeBody body = type.handshake().from(is, header.getLength().getValue());
		return new Handshake(header, body, false);
	}

	/**
	 * Returns the binary representation of this Handshake.
	 *
	 * @return byte array containing header and body
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(header.toByteArray());
		buffer.put(body.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this Handshake.
	 *
	 * @return JSON string containing handshake data
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of header plus body
	 */
	@Override
	public int sizeOf() {
		return header.sizeOf() + body.sizeOf();
	}

	/**
	 * Compares this {@link Handshake} to another object for equality.
	 *
	 * @param obj the object to compare with
	 * @return true if both are Handshake objects with equal header and body
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Handshake that) {
			return Objects.equals(this.header, that.header) &&
					Objects.equals(this.body, that.body);
		}
		return false;
	}

	/**
	 * Returns the hash code for this {@link Handshake}.
	 *
	 * @return hash code based on header and body
	 */
	@Override
	public int hashCode() {
		return Objects.hash(header, body);
	}

	/**
	 * Returns the handshake header.
	 *
	 * @return the HandshakeHeader object
	 */
	public HandshakeHeader getHeader() {
		return header;
	}

	/**
	 * Returns the handshake body.
	 *
	 * @return the TLSHandshakeBody object
	 */
	public TLSHandshakeBody getBody() {
		return body;
	}

	/**
	 * Checks if the handshake body is of specified type.
	 *
	 * @param <T> the expected body type
	 * @param tlsObjectClass the class object of expected type
	 * @return true if body is of specified type
	 */
	public <T extends TLSHandshakeBody> boolean is(final Class<T> tlsObjectClass) {
		return body.getClass().isAssignableFrom(tlsObjectClass);
	}

	/**
	 * Returns the handshake body cast to specified type.
	 *
	 * @param <T> the target body type
	 * @param tlsObjectClass the class object of target type
	 * @return the cast body
	 * @throws IllegalArgumentException if cast is not possible
	 */
	public <T extends TLSHandshakeBody> T get(final Class<T> tlsObjectClass) {
		if (is(tlsObjectClass)) {
			return JavaObjects.cast(body);
		}
		throw new IllegalArgumentException("Cannot cast TLS handshake body from " +
				body.getClass() + " to " + tlsObjectClass);
	}
}
