package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt16;

/**
 * Represents a digital signature in TLS protocol messages.
 * <p>
 * This class encapsulates signature data used in various TLS handshake messages, including the signature algorithm and
 * the actual signature bytes.
 *
 * @author Radu Sebastian LAZIN
 */
public class Signature implements TLSObject {

	/**
	 * Reserved field for future use (currently always zero).
	 */
	private final UInt16 reserved;

	/**
	 * The length of the signature data in bytes.
	 */
	private final UInt16 length;

	/**
	 * The actual signature bytes.
	 */
	private final BytesWrapper value;

	/**
	 * Constructs a Signature with all fields specified.
	 *
	 * @param reserved the reserved field (should be zero)
	 * @param length the length of signature data
	 * @param value the wrapped signature bytes
	 */
	public Signature(final UInt16 reserved, final UInt16 length, final BytesWrapper value) {
		this.reserved = reserved;
		this.length = length;
		this.value = value;
	}

	/**
	 * Constructs a Signature with raw bytes.
	 *
	 * @param reserved the reserved field (should be zero)
	 * @param length the length of signature data
	 * @param bytes the raw signature bytes
	 */
	public Signature(final UInt16 reserved, final UInt16 length, final byte[] bytes) {
		this(reserved, length, new BytesWrapper(bytes));
	}

	/**
	 * Constructs a Signature with primitive length and raw bytes.
	 *
	 * @param length the length of signature data
	 * @param bytes the raw signature bytes
	 */
	public Signature(final short length, final byte[] bytes) {
		this(UInt16.ZERO, UInt16.of(length), bytes);
	}

	/**
	 * Constructs a Signature with raw bytes.
	 *
	 * @param bytes the raw signature bytes
	 */
	public Signature(final byte[] bytes) {
		this((short) bytes.length, bytes);
	}

	/**
	 * Parses a Signature from an input stream.
	 *
	 * @param is the input stream containing signature data
	 * @return the parsed Signature object
	 * @throws IOException if an I/O error occurs
	 */
	public static Signature from(final InputStream is) throws IOException {
		UInt16 reserved = UInt16.from(is);
		UInt16 length = UInt16.from(is);
		BytesWrapper value = BytesWrapper.from(is, length.getValue());
		return new Signature(reserved, length, value);
	}

	/**
	 * Returns the binary representation of this Signature.
	 *
	 * @return byte array containing reserved, length and signature data
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(reserved.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(value.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this Signature.
	 *
	 * @return JSON string containing signature information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of all fields combined
	 */
	@Override
	public int sizeOf() {
		return reserved.sizeOf() + length.sizeOf() + value.sizeOf();
	}

	/**
	 * Compares this {@link Signature} to another object for equality.
	 *
	 * @param obj the object to compare with
	 * @return true if equal, false otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Signature that) {
			return Objects.equals(this.reserved, that.reserved)
					&& Objects.equals(this.length, that.length)
					&& Objects.equals(this.value, that.value);
		}
		return false;
	}

	/**
	 * Returns the hash code for this {@link Signature}.
	 *
	 * @return hash code based on all fields
	 */
	@Override
	public int hashCode() {
		return Objects.hash(reserved, length, value);
	}

	/**
	 * Returns the reserved field.
	 *
	 * @return the UInt16 wrapper containing reserved value
	 */
	public UInt16 getReserved() {
		return reserved;
	}

	/**
	 * Returns the length of signature data.
	 *
	 * @return the UInt16 wrapper containing length
	 */
	public UInt16 getLength() {
		return length;
	}

	/**
	 * Returns the signature value.
	 *
	 * @return the BytesWrapper containing signature bytes
	 */
	public BytesWrapper getValue() {
		return value;
	}
}
