package org.apiphany.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.lang.annotation.AsValue;

/**
 * Immutable wrapper for a 24-bit unsigned integer with binary serialization capabilities.
 * <p>
 * This class provides:
 * <ul>
 * <li>Type-safe representation of 24-bit values (stored in a Java {@code int})</li>
 * <li>Big-endian binary serialization/deserialization</li>
 * <li>Network byte order support</li>
 * <li>Constant-time size operations</li>
 * </ul>
 *
 * <p>
 * Primary use cases include:
 * <ul>
 * <li>Network protocols with 24-bit fields (e.g., audio/video streaming formats)</li>
 * <li>Embedded systems communication</li>
 * <li>Legacy file formats</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class UInt24 implements ByteSizeable, BinaryRepresentable {

	/**
	 * The size in bytes of an {@code UInt24} value (constant value: 3).
	 */
	public static final int BYTES = 3;

	/**
	 * The maximum value that fits in 24 bits (16,777,215).
	 */
	public static final int MAX_VALUE = 0xFF_FF_FF;

	/**
	 * Predefined instance representing zero (0x000000).
	 */
	public static final UInt24 ZERO = of(0x00_00_00);

	/**
	 * The actual encapsulated value.
	 */
	private final int value;

	/**
	 * Constructs a new {@code UInt24} instance.
	 *
	 * @param value the 24-bit value to wrap (only lower 24 bits are used)
	 * @throws IllegalArgumentException if value exceeds 24-bit unsigned range
	 */
	protected UInt24(final int value) {
		this.value = value & MAX_VALUE;
	}

	/**
	 * Creates a new {@code UInt24} instance for the specified value.
	 *
	 * @param value the 24-bit value to wrap (only lower 24 bits are used)
	 * @return a new {@code UInt24} instance
	 * @throws IllegalArgumentException if value exceeds 24-bit unsigned range
	 */
	public static UInt24 of(final int value) {
		return new UInt24(value);
	}

	/**
	 * Reads 3 bytes from the input stream and returns them as a big-endian {@code UInt24}.
	 *
	 * @param is the input stream to read from
	 * @return a new {@code UInt24} containing the read value
	 * @throws IOException if an I/O error occurs
	 * @throws EOFException if fewer than 3 bytes are available
	 * @throws NullPointerException if {@code is} is {@code null}
	 */
	public static UInt24 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		int bytesRead = is.read(buffer);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		int int24 = ((buffer[0] & 0xFF) << 16) |
				((buffer[1] & 0xFF) << 8) |
				(buffer[2] & 0xFF);
		return UInt24.of(int24);
	}

	/**
	 * @see #toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	/**
	 * Converts a 24-bit value to its big-endian binary representation.
	 *
	 * @param value the value to convert (only lower 24 bits are used)
	 * @return a new byte array containing the value in network byte order
	 */
	public static byte[] toByteArray(final int value) {
		return new byte[] {
				(byte) ((value >> 16) & 0xFF),
				(byte) ((value >> 8) & 0xFF),
				(byte) (value & 0xFF)
		};
	}

	/**
	 * Returns a string representation of this object.
	 *
	 * @return string representation
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
		return getValue();
	}

	/**
	 * Returns the wrapped value as a Java {@code int}.
	 * <p>
	 * Note: The value is always in the range 0-16,777,215 (0xFFFFFF).
	 * <p>
	 * Annotated with {@code @AsValue} for direct serialization.
	 *
	 * @return the wrapped value (unsigned 24-bit as int)
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @see #sizeOf()
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * @see #equals(Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof UInt24 other) {
			return this.toUnsignedInt() == other.toUnsignedInt();
		}
		return false;
	}

	/**
	 * @see #hashCode()
	 */
	@Override
	public int hashCode() {
		return Integer.hashCode(toUnsignedInt());
	}
}
