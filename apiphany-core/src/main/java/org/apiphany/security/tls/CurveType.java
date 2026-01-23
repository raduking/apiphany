package org.apiphany.security.tls;

import java.util.Map;

import org.apiphany.io.UInt8;
import org.morphix.lang.Enums;

/**
 * Represents the type of elliptic curve used in TLS key exchange.
 * <p>
 * This enum identifies the curve format supported in TLS handshakes. Currently, only {@link #NAMED_CURVE} (standardized
 * curves defined by IANA) is supported.
 * </p>
 *
 * <p>
 * <b>TLS Protocol Notes:</b>
 * </p>
 * <ul>
 * <li>Defined in <a href="https://tools.ietf.org/html/rfc8422#section-5.1.1">RFC 8422 (Section 5.1.1)</a>.</li>
 * <li>Value {@code 0x03} (NAMED_CURVE) indicates IANA-registered curves like secp256r1.</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * This enum and its methods are thread-safe.
 *
 * @see TLSObject
 * @see <a href="https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#tls-parameters-8">IANA Named
 * Curves</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum CurveType implements TLSObject {

	/**
	 * Standardized elliptic curves registered with IANA (e.g., secp256r1, x25519). Represented by the value {@code 0x03} in
	 * TLS handshake messages.
	 */
	NAMED_CURVE((byte) 0x03);

	/**
	 * The size (in bytes) of a {@link CurveType} when serialized in TLS.
	 */
	public static final int BYTES = 1;

	/**
	 * Value map for easy {@link #fromValue(byte) implementation.
	 */
	private static final Map<Byte, CurveType> VALUE_MAP = Enums.buildNameMap(values(), CurveType::value);

	/**
	 * The encapsulated value.
	 */
	private final byte value;

	/**
	 * Creates a {@link CurveType} with the given TLS-encoded value.
	 *
	 * @param value the byte value representing this curve type in TLS messages.
	 */
	CurveType(final byte value) {
		this.value = value;
	}

	/**
	 * Returns the TLS-encoded value of this curve type.
	 *
	 * @return the byte value (e.g., {@code 0x03} for {@link #NAMED_CURVE}).
	 */
	public byte value() {
		return value;
	}

	/**
	 * Parses a TLS-encoded byte into a {@link CurveType}.
	 *
	 * @param value the byte value (e.g., {@code 0x03}).
	 * @return the corresponding {@link CurveType}, or {@code null} if unmatched.
	 * @throws IllegalArgumentException If {@code value} is invalid (if enforced by {@link Enums#from}).
	 */
	public static CurveType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Returns the size of this object when serialized in TLS (always {@value #BYTES} byte).
	 *
	 * @return {@link #BYTES} (1).
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Serializes this {@link CurveType} to its TLS-encoded byte representation. Delegates to
	 * {@link UInt8#toByteArray(byte)} for consistent unsigned handling.
	 *
	 * @return a 1-byte array containing {@link #value()}.
	 */
	@Override
	public byte[] toByteArray() {
		return UInt8.toByteArray(value);
	}
}
