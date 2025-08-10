package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt24;
import org.apiphany.io.UInt8;

/**
 * Represents the header of a TLS Handshake protocol message.
 * <p>
 * This class encapsulates the common header fields for all handshake messages, including the message type and payload
 * length.
 *
 * @author Radu Sebastian LAZIN
 */
public class HandshakeHeader implements TLSObject {

	/**
	 * The fixed size of a handshake header in bytes.
	 */
	public static final int BYTES = HandshakeType.BYTES + UInt24.BYTES;

	/**
	 * The type of handshake message.
	 */
	private final HandshakeType type;

	/**
	 * The length of the handshake message payload.
	 */
	private final UInt24 length;

	/**
	 * Constructs a HandshakeHeader with specified type and length.
	 *
	 * @param type the handshake message type
	 * @param length the length of the message payload
	 */
	public HandshakeHeader(final HandshakeType type, final UInt24 length) {
		this.type = type;
		this.length = length;
	}

	/**
	 * Constructs a HandshakeHeader with primitive length.
	 *
	 * @param type the handshake message type
	 * @param length the length of the message payload
	 */
	public HandshakeHeader(final HandshakeType type, final int length) {
		this(type, UInt24.of(length));
	}

	/**
	 * Constructs a HandshakeHeader with zero length.
	 *
	 * @param type the handshake message type
	 */
	public HandshakeHeader(final HandshakeType type) {
		this(type, (short) 0x0000);
	}

	/**
	 * Parses a HandshakeHeader from an input stream.
	 *
	 * @param is the input stream containing header data
	 * @return the parsed HandshakeHeader object
	 * @throws IOException if an I/O error occurs
	 */
	public static HandshakeHeader from(final InputStream is) throws IOException {
		UInt8 int8 = UInt8.from(is);
		HandshakeType type = HandshakeType.fromValue(int8.getValue());
		UInt24 messageLength = UInt24.from(is);
		return new HandshakeHeader(type, messageLength);
	}

	/**
	 * Returns the binary representation of this HandshakeHeader.
	 *
	 * @return byte array containing type and length fields
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this HandshakeHeader.
	 *
	 * @return JSON string containing header information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return always returns {@value #BYTES} (4) bytes
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Returns the handshake message type.
	 *
	 * @return the HandshakeType enum value
	 */
	public HandshakeType getType() {
		return type;
	}

	/**
	 * Returns the payload length.
	 *
	 * @return the UInt24 wrapper containing length
	 */
	public UInt24 getLength() {
		return length;
	}
}
