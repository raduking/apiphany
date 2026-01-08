package org.apiphany.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.lang.annotation.AsValue;

/**
 * Immutable wrapper for a 16-bit unsigned integer (short) with binary serialization capabilities.
 * <p>
 * This class provides:
 * <ul>
 * <li>Type-safe representation of 16-bit values</li>
 * <li>Big-endian binary serialization/deserialization</li>
 * <li>Network byte order support</li>
 * <li>Constant-time size operations</li>
 * </ul>
 *
 * <p>
 * Primary use cases include:
 * <ul>
 * <li>Network protocol implementations (e.g., TCP headers)</li>
 * <li>Binary file formats</li>
 * <li>Hardware register mapping</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class UInt16 implements ByteSizeable, BinaryRepresentable {

	/**
	 * The size in bytes of an {@code UInt16} value (constant value: 2).
	 */
	public static final int BYTES = 2;

	/**
	 * Predefined instance representing zero (0x0000).
	 */
	public static final UInt16 ZERO = of((short) 0x00_00);

	/**
	 * The actual encapsulated value.
	 */
	private final short value;

	/**
	 * Constructs a new {@code UInt16} instance.
	 *
	 * @param value the 16-bit value to wrap
	 */
	protected UInt16(final short value) {
		this.value = value;
	}

	/**
	 * Creates a new {@code UInt16} instance for the specified value.
	 *
	 * @param value the 16-bit value to wrap
	 * @return a new {@code UInt16} instance
	 */
	public static UInt16 of(final short value) {
		return new UInt16(value);
	}

	/**
	 * Reads 2 bytes from the input stream and returns them as a big-endian {@code UInt16}.
	 *
	 * @param is the input stream to read from
	 * @return a new {@code UInt16} containing the read value
	 * @throws IOException if an I/O error occurs
	 * @throws EOFException if fewer than 2 bytes are available
	 * @throws NullPointerException if {@code is} is {@code null}
	 */
	public static UInt16 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		IOStreams.readFully(is, buffer, 0, BYTES);
		int unsigned = ((buffer[0] & 0xFF) << 8) |
				(buffer[1] & 0xFF);
		return UInt16.of((short) unsigned);
	}

	/**
	 * @see BinaryRepresentable#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	/**
	 * Converts a 16-bit value to its big-endian binary representation.
	 *
	 * @param value the value to convert
	 * @return a new byte array containing the value in network byte order
	 */
	public static byte[] toByteArray(final short value) {
		return new byte[] {
				(byte) ((value >> 8) & 0xFF),
				(byte) (value & 0xFF)
		};
	}

	/**
	 * Returns a string representation of this object.
	 *
	 * @return string representation
	 * @see #toUnsignedInt()
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(toUnsignedInt());
	}

	/**
	 * Returns an unsigned integer.
	 *
	 * @return an unsigned integer
	 */
	@AsValue
	public int toUnsignedInt() {
		return value & 0xFF_FF;
	}

	/**
	 * Returns the wrapped 16-bit value.
	 * <p>
	 * Annotated with {@code @AsValue} for direct serialization.
	 *
	 * @return the wrapped short value
	 */
	public short getValue() {
		return value;
	}

	/**
	 * @see ByteSizeable#sizeOf()
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof UInt16 other) {
			return this.toUnsignedInt() == other.toUnsignedInt();
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Integer.hashCode(toUnsignedInt());
	}
}
