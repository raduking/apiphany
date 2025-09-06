package org.apiphany.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.lang.annotation.AsValue;

/**
 * Immutable wrapper for a 32-bit unsigned integer with binary serialization capabilities.
 * <p>
 * This class provides:
 * <ul>
 * <li>Type-safe representation of 32-bit integers</li>
 * <li>Big-endian binary serialization/deserialization</li>
 * <li>Network byte order (MSB-first) support</li>
 * <li>Constant-time size operations</li>
 * </ul>
 *
 * <p>
 * Primary use cases include:
 * <ul>
 * <li>Network protocol implementations (e.g., IP headers)</li>
 * <li>Binary file formats</li>
 * <li>System-level programming</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class UInt32 implements ByteSizeable, BinaryRepresentable {

	/**
	 * The size in bytes of an {@code UInt32} value (constant value: 4).
	 */
	public static final int BYTES = 4;

	/**
	 * Predefined instance representing zero (0x00000000).
	 */
	public static final UInt32 ZERO = of(0x00_00_00_00);

	/**
	 * The actual encapsulated value.
	 */
	private final int value;

	/**
	 * Constructs a new {@code UInt32} instance.
	 *
	 * @param value the 32-bit integer value to wrap
	 */
	protected UInt32(final int value) {
		this.value = value;
	}

	/**
	 * Creates a new {@code UInt32} instance for the specified value.
	 *
	 * @param value the 32-bit integer value to wrap
	 * @return a new {@code UInt32} instance
	 */
	public static UInt32 of(final int value) {
		return new UInt32(value);
	}

	/**
	 * Reads 4 bytes from the input stream and returns them as a big-endian {@code UInt32}.
	 *
	 * @param is the input stream to read from
	 * @return a new {@code UInt32} containing the read value
	 * @throws IOException if an I/O error occurs
	 * @throws EOFException if fewer than 4 bytes are available
	 * @throws NullPointerException if {@code is} is {@code null}
	 */
	public static UInt32 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		int bytesRead = is.read(buffer);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		int int32 = 0;
		for (int i = BYTES; i > 0; --i) {
			int32 |= (buffer[BYTES - i] & 0xFF) << ((i - 1) * 8);
		}
		return UInt32.of(int32);
	}

	/**
	 * @see #toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	/**
	 * Converts a 32-bit value to its big-endian binary representation.
	 *
	 * @param value the value to convert
	 * @return a new byte array containing the value in network byte order
	 */
	public static byte[] toByteArray(final int value) {
		ByteBuffer buffer = ByteBuffer.allocate(BYTES);
		buffer.putInt(value);
		return buffer.array();
	}

	/**
	 * Returns a string representation of this object.
	 *
	 * @return string representation
	 */
	@Override
	public String toString() {
		return String.valueOf(value);
	}

	/**
	 * Returns the wrapped 32-bit value.
	 * <p>
	 * Annotated with {@code @AsValue} for direct serialization.
	 *
	 * @return the wrapped int value
	 */
	@AsValue
	public long getValue() {
		return value;
	}

	/**
	 * @see #sizeOf()
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}
}
