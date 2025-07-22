package org.apiphany.security.tls;

import java.util.Map;

import org.apiphany.io.UInt8;
import org.morphix.lang.Enums;

/**
 * Represents compression methods supported in TLS handshakes.
 * <p>
 * Modern TLS deployments typically disable compression due to security vulnerabilities like CRIME and BREACH. This enum
 * currently only supports {@link #NO_COMPRESSION}.
 * </p>
 *
 * <p>
 * <b>Security Note:</b>
 * </p>
 * Compression in TLS is considered unsafe and should not be enabled unless required for legacy compatibility. Even
 * then, it must be combined with other mitigations.
 *
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * This enum and its methods are thread-safe.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3749">RFC 3749 (Deflate Compression)</a>
 * @see <a href="https://en.wikipedia.org/wiki/CRIME">CRIME Attack</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum CompressionMethod implements TLSObject {

	/**
	 * Indicates no compression is used in the TLS session. This is the only safe and widely supported option. TLS
	 * identifier: {@code 0x00}.
	 */
	NO_COMPRESSION((byte) 0x00);

	/**
	 * The size (in bytes) of a {@link CompressionMethod} identifier in TLS messages.
	 */
	public static final int BYTES = 1;

	/**
	 * Value map for easy {@link #fromValue(byte)} implementation.
	 */
	private static final Map<Byte, CompressionMethod> VALUE_MAP = Enums.buildNameMap(values(), CompressionMethod::value);

	/**
	 * The TLS encapsulated value.
	 */
	private final byte value;

	/**
	 * Creates a {@link CompressionMethod} with the given TLS-encoded value.
	 *
	 * @param value the byte value representing this compression method in TLS.
	 */
	CompressionMethod(final byte value) {
		this.value = value;
	}

	/**
	 * Returns the TLS-encoded value of this compression method.
	 *
	 * @return The byte value (always {@code 0x00} for {@link #NO_COMPRESSION}).
	 */
	public byte value() {
		return value;
	}

	/**
	 * Parses a TLS-encoded byte into a {@link CompressionMethod}.
	 *
	 * @param value the byte value (only {@code 0x00} is valid in this implementation).
	 * @return the corresponding {@link CompressionMethod}, or {@code null} if unmatched.
	 * @throws IllegalArgumentException If {@code value} is invalid (if enforced by {@link Enums#from}).
	 */
	public static CompressionMethod fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Returns the size of this object when serialized in TLS (always {@value #BYTES} byte). Compression method identifiers
	 * are always 1 byte in TLS handshakes.
	 *
	 * @return the size of this object when serialized
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Serializes this {@link CompressionMethod} to its TLS-encoded byte representation. The returned array contains a
	 * single byte (e.g., {@code [0x00]} for no compression).
	 *
	 * @return the byte array of the object
	 */
	@Override
	public byte[] toByteArray() {
		return UInt8.toByteArray(value);
	}
}
