package org.apiphany.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.BinaryRepresentable;
import org.apiphany.lang.ByteSizeable;
import org.apiphany.lang.Bytes;
import org.apiphany.lang.Hex;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Immutable container for raw byte data with serialization capabilities.
 * <p>
 * This class provides:
 * <ul>
 * <li>Thread-safe immutable byte storage</li>
 * <li>Hexadecimal string representation</li>
 * <li>JSON serialization support</li>
 * <li>Stream reading capabilities</li>
 * <li>Zero-copy {@link ByteBuffer} conversion</li>
 * </ul>
 *
 * <p>
 * Primary use cases include:
 * <ul>
 * <li>Binary protocol implementations (e.g., TLS)</li>
 * <li>Cryptographic operations</li>
 * <li>Low-level I/O operations</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class BytesWrapper implements ByteSizeable, BinaryRepresentable {

	/**
	 * Cache an empty bytes wrapper.
	 */
	private static final BytesWrapper EMPTY = new BytesWrapper();

	/**
	 * Underlying byte array.
	 */
	private final byte[] bytes;

	/**
	 * Creates a new instance containing a copy of the specified bytes.
	 *
	 * @param bytes the byte array to wrap (may be {@code null}, which is treated as empty)
	 */
	public BytesWrapper(final byte[] bytes) {
		this.bytes = null != bytes ? Arrays.copyOf(bytes, bytes.length) : Bytes.EMPTY;
	}

	/**
	 * Creates a new instance with a zero-initialized byte array of the specified size.
	 *
	 * @param size the length of the new byte array (must be non-negative)
	 * @throws IllegalArgumentException if {@code size} is negative
	 */
	public BytesWrapper(final int size) {
		this(new byte[size]);
	}

	/**
	 * Creates a new empty instance.
	 */
	public BytesWrapper() {
		this(Bytes.EMPTY);
	}

	/**
	 * Reads exactly {@code size} bytes from the input stream and wraps them in a new instance.
	 *
	 * @param is the input stream to read from (must not be {@code null})
	 * @param size the number of bytes to read (must be non-negative)
	 * @return a new {@code BytesWrapper} containing the read bytes
	 * @throws IOException if an I/O error occurs
	 * @throws EOFException if the stream ends before reading {@code size} bytes
	 * @throws IllegalArgumentException if {@code size} is negative
	 */
	public static BytesWrapper from(final InputStream is, final int size) throws IOException {
		if (0 > size) {
			throw new IllegalArgumentException("Size cannot be negative: " + size);
		}
		if (0 == size) {
			return EMPTY;
		}
		byte[] bytes = new byte[size];
		int bytesRead = is.read(bytes);
		if (size != bytesRead) {
			throw new EOFException("Error reading " + size + " bytes");
		}

		return new BytesWrapper(bytes);
	}

	/**
	 * Returns a copy of the wrapped byte array.
	 *
	 * @return a new byte array containing the wrapped data (or empty array if empty)
	 */
	@Override
	public byte[] toByteArray() {
		return isEmpty() ? Bytes.EMPTY : Arrays.copyOf(bytes, bytes.length);
	}

	/**
	 * Returns a read-only {@link ByteBuffer} view of the wrapped bytes.
	 *
	 * @return a read-only buffer sharing the underlying array
	 */
	public ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
	}

	/**
	 * Checks if this instance contains no data.
	 *
	 * @return {@code true} if empty, {@code false} otherwise
	 */
	public boolean isEmpty() {
		return 0 == sizeOf();
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
	 * Returns a hexadecimal string representation of the wrapped bytes.
	 *
	 * @return hex string without separators
	 */
	@JsonValue
	public String toHexString() {
		return Hex.string(bytes, "");
	}

	/**
	 * @see #sizeOf()
	 */
	@Override
	public int sizeOf() {
		return bytes.length;
	}

	/**
	 * @see BytesWrapper#equals(Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that instanceof BytesWrapper bytesWrapper) {
			return Arrays.equals(bytes, bytesWrapper.bytes);
		}
		return false;
	}

	/**
	 * @see #hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(bytes);
	}
}
