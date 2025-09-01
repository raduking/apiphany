package org.apiphany.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.annotation.AsValue;

/**
 * Immutable wrapper for an 8-bit unsigned integer (byte) with binary serialization capabilities.
 * <p>
 * This class provides:
 * <ul>
 * <li>Type-safe representation of byte values</li>
 * <li>Binary serialization/deserialization</li>
 * <li>Constant-time size operations</li>
 * </ul>
 *
 * <p>
 * Primary use cases include:
 * <ul>
 * <li>Network protocol implementations</li>
 * <li>Binary file formats</li>
 * <li>Cryptographic operations</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class UInt8 implements ByteSizeable, BinaryRepresentable {

	/**
	 * The size in bytes of an {@code UInt8} value (constant value: 1).
	 */
	public static final int BYTES = 1;

	/**
	 * Predefined instance representing zero (0x00).
	 */
	public static final UInt8 ZERO = of((byte) 0x00);

	/**
	 * The actual encapsulated byte value.
	 */
	private final byte value;

	/**
	 * Constructs a new {@code UInt8} instance.
	 *
	 * @param value the byte value to wrap
	 */
	protected UInt8(final byte value) {
		this.value = value;
	}

	/**
	 * Creates a new {@code UInt8} instance for the specified byte value.
	 *
	 * @param value the byte value to wrap
	 * @return a new {@code UInt8} instance
	 */
	public static UInt8 of(final byte value) {
		return new UInt8(value);
	}

	/**
	 * Reads a single byte from the input stream and returns it as an {@code UInt8}.
	 *
	 * @param is the input stream to read from
	 * @return a new {@code UInt8} containing the read byte
	 * @throws IOException if an I/O error occurs
	 * @throws EOFException if the end of stream is reached before reading a byte
	 * @throws NullPointerException if {@code is} is {@code null}
	 */
	public static UInt8 from(final InputStream is) throws IOException {
		int byteRead = is.read();
		if (-1 == byteRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		return UInt8.of((byte) byteRead);
	}

	/**
	 * @see #toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	/**
	 * Converts a byte value to its binary representation.
	 *
	 * @param value the byte value to convert
	 * @return a new byte array containing the value
	 */
	public static byte[] toByteArray(final byte value) {
		return new byte[] {
				(byte) (value & 0xFF)
		};
	}

	/**
	 * Returns a JSON representation of this object.
	 *
	 * @return JSON string representation
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the wrapped byte value.
	 * <p>
	 * Annotated with {@code @AsValue} for direct serialization.
	 *
	 * @return the wrapped byte value
	 */
	@AsValue
	public byte getValue() {
		return value;
	}

	/**
	 * Returns the size in bytes of this object (always 1).
	 *
	 * @return constant value {@link #BYTES}
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Checks if this value is equal to zero.
	 *
	 * @return true if this value equals {@link #ZERO}
	 */
	public boolean isZero() {
		return 0 == value;
	}
}
