package org.apiphany.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.BinaryRepresentable;
import org.apiphany.lang.ByteSizeable;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Immutable wrapper for a 64-bit unsigned integer with binary serialization capabilities.
 * <p>
 * This class provides:
 * <ul>
 * <li>Type-safe representation of 64-bit integers</li>
 * <li>Big-endian binary serialization/deserialization</li>
 * <li>Network byte order (MSB-first) support</li>
 * <li>JSON integration via {@link JsonValue}</li>
 * <li>Constant-time size operations</li>
 * </ul>
 *
 * <p>
 * Primary use cases include:
 * <ul>
 * <li>High-precision timestamps and counters</li>
 * <li>Cryptographic operations</li>
 * <li>Database record identifiers</li>
 * <li>Binary protocols requiring 64-bit values</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class UInt64 implements ByteSizeable, BinaryRepresentable {

	/**
	 * The size in bytes of an {@code Int64} value (constant value: 8).
	 */
	public static final int BYTES = 8;

	/**
	 * Predefined instance representing zero (0x0000000000000000).
	 */
	public static final UInt64 ZERO = of(0x00_00_00_00_00_00_00_00);

	/**
	 * The actual encapsulated value.
	 */
	private final long value;

	/**
	 * Constructs a new {@code Int64} instance.
	 *
	 * @param value the 64-bit integer value to wrap
	 */
	protected UInt64(final long value) {
		this.value = value;
	}

	/**
	 * Creates a new {@code Int64} instance for the specified value.
	 *
	 * @param value the 64-bit integer value to wrap
	 * @return a new {@code Int64} instance
	 */
	public static UInt64 of(final long value) {
		return new UInt64(value);
	}

	/**
	 * Reads 8 bytes from the input stream and returns them as a big-endian {@code Int64}.
	 *
	 * @param is the input stream to read from
	 * @return a new {@code Int64} containing the read value
	 * @throws IOException if an I/O error occurs
	 * @throws EOFException if fewer than 8 bytes are available
	 * @throws NullPointerException if {@code is} is {@code null}
	 */
	public static UInt64 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		int bytesRead = is.read(buffer);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		long int64 = 0;
		for (int i = BYTES; i > 0; --i) {
			int64 |= ((long) buffer[BYTES - i] & 0xFF) << ((i - 1) * 8);
		}
		return UInt64.of(int64);
	}

	/**
	 * @see #toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	/**
	 * Converts a 64-bit value to its big-endian binary representation.
	 *
	 * @param value the value to convert
	 * @return a new byte array containing the value in network byte order
	 */
	public static byte[] toByteArray(final long value) {
		ByteBuffer buffer = ByteBuffer.allocate(BYTES);
		buffer.putLong(value);
		return buffer.array();
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
	 * Returns the wrapped 64-bit value.
	 * <p>
	 * Annotated with {@code @JsonValue} for direct JSON serialization.
	 *
	 * @return the wrapped long value
	 */
	@JsonValue
	public long getValue() {
		return value;
	}

	/**
	 * @see #toByteArray()
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}
}
