package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt8;

/**
 * Represents a ChangeCipherSpec message in the TLS protocol.
 * <p>
 * This message signals the transition from unencrypted to encrypted communication during the TLS handshake. It contains
 * a single byte payload (always 1 in TLS 1.2 and earlier).
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.1">RFC 5246 - Change Cipher Spec Protocol</a>
 * @author Radu Sebastian LAZIN
 */
public class ChangeCipherSpec implements TLSObject {

	/**
	 * The message payload (always 0x01 in standard TLS).
	 */
	private final UInt8 payload;

	/**
	 * Constructs a ChangeCipherSpec with specified payload.
	 *
	 * @param payload the message payload (should be 0x01)
	 */
	public ChangeCipherSpec(final UInt8 payload) {
		this.payload = payload;
	}

	/**
	 * Constructs a ChangeCipherSpec with primitive payload.
	 *
	 * @param payload the message payload as byte
	 */
	public ChangeCipherSpec(final byte payload) {
		this(UInt8.of(payload));
	}

	/**
	 * Constructs a default ChangeCipherSpec (payload = 0x01).
	 */
	public ChangeCipherSpec() {
		this((byte) 0x01);
	}

	/**
	 * Parses a ChangeCipherSpec from an input stream.
	 *
	 * @param is the input stream containing the message
	 * @return the parsed ChangeCipherSpec object
	 * @throws IOException if an I/O error occurs
	 */
	public static ChangeCipherSpec from(final InputStream is) throws IOException {
		UInt8 payload = UInt8.from(is);
		return new ChangeCipherSpec(payload);
	}

	/**
	 * Returns the binary representation of this message.
	 *
	 * @return single-element byte array containing the payload
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(payload.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns the size when serialized.
	 *
	 * @return always returns 1 (single byte payload)
	 */
	@Override
	public int sizeOf() {
		return payload.sizeOf();
	}

	/**
	 * Returns a JSON representation of this message.
	 *
	 * @return JSON string containing the payload
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the message payload.
	 *
	 * @return the UInt8 wrapper containing the payload
	 */
	public UInt8 getPayload() {
		return payload;
	}
}
