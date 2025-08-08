package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt8;

/**
 * Represents an ECDHE (Elliptic Curve Diffie-Hellman Ephemeral) public key in TLS key exchange.
 * <p>
 * This class encapsulates the ephemeral public key used in ECDHE key exchange during TLS handshake.
 *
 * @author Radu Sebastian LAZIN
 */
public class ECDHEPublicKey implements TLSKeyExchange {

	/**
	 * The length of the public key in bytes.
	 */
	private final UInt8 length;

	/**
	 * The actual public key bytes.
	 */
	private final BytesWrapper value;

	/**
	 * Constructs an ECDHEPublicKey with length wrapper and value wrapper.
	 *
	 * @param length the length of public key
	 * @param value the wrapped public key bytes
	 */
	public ECDHEPublicKey(final UInt8 length, final BytesWrapper value) {
		this.length = length;
		this.value = value;
	}

	/**
	 * Constructs an ECDHEPublicKey with length wrapper and raw bytes.
	 *
	 * @param length the length of public key
	 * @param bytes the raw public key bytes
	 */
	public ECDHEPublicKey(final UInt8 length, final byte[] bytes) {
		this(length, new BytesWrapper(bytes));
	}

	/**
	 * Constructs an ECDHEPublicKey with primitive length and raw bytes.
	 *
	 * @param length the length of public key
	 * @param bytes the raw public key bytes
	 */
	public ECDHEPublicKey(final byte length, final byte[] bytes) {
		this(UInt8.of(length), bytes);
	}

	/**
	 * Constructs an ECDHEPublicKey with automatic length calculation.
	 *
	 * @param bytes the raw public key bytes
	 */
	public ECDHEPublicKey(final byte[] bytes) {
		this((byte) bytes.length, bytes);
	}

	/**
	 * Parses an ECDHEPublicKey from an input stream.
	 *
	 * @param is the input stream containing key data
	 * @return the parsed ECDHEPublicKey object
	 * @throws IOException if an I/O error occurs
	 */
	public static ECDHEPublicKey from(final InputStream is) throws IOException {
		UInt8 length = UInt8.from(is);
		BytesWrapper value = BytesWrapper.from(is, length.getValue());
		return new ECDHEPublicKey(length, value);
	}

	/**
	 * Returns the binary representation of this public key.
	 *
	 * @return byte array containing length and key data
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		buffer.put(value.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this public key.
	 *
	 * @return JSON string containing key information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the length of public key data.
	 *
	 * @return the UInt8 wrapper containing length
	 */
	public UInt8 getLength() {
		return length;
	}

	/**
	 * Returns the public key value.
	 *
	 * @return the BytesWrapper containing key bytes
	 */
	public BytesWrapper getValue() {
		return value;
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of length field plus key data
	 */
	@Override
	public int sizeOf() {
		return length.sizeOf() + value.sizeOf();
	}
}
