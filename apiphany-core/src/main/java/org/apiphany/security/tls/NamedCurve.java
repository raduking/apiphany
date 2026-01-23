package org.apiphany.security.tls;

import java.util.Map;

import org.apiphany.io.UInt16;
import org.morphix.lang.Enums;

/**
 * Represents named elliptic curves supported in TLS key exchange.
 * <p>
 * These curves are defined by IANA and used in protocols like ECDHE for forward secrecy. Each curve has a unique 2-byte
 * identifier assigned in the
 * <a href="https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#tls-parameters-8">TLS NamedCurve
 * registry</a>.
 * </p>
 *
 * <p>
 * <b>Security Notes:</b>
 * </p>
 * <ul>
 * <li>{@link #X25519} and {@link #X448} are modern Curve25519/448-based algorithms (preferred for performance).</li>
 * <li>{@link #SECP256R1} (NIST P-256) is widely supported but has implementation variability.</li>
 * <li>Avoid {@link #SECP256K1} in TLS unless required (commonly used in blockchain systems).</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * This enum and its methods are thread-safe.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8422#section-5.1.1">RFC 8422 (TLS 1.3 Curve Definitions)</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum NamedCurve implements TLSObject {

	/**
	 * Curve25519 (32-byte key, fast and secure). TLS identifier: {@code 0x001D}. Preferred for most modern deployments.
	 */
	X25519((short) 0x001D),

	/**
	 * NIST P-256 (secp256r1, 32-byte key). TLS identifier: {@code 0x0017}. Widely supported but check for side-channel
	 * resistance.
	 */
	SECP256R1((short) 0x0017),

	/**
	 * NIST P-384 (secp384r1, 48-byte key). TLS identifier: {@code 0x0018}. Used when higher security margins are required.
	 */
	SECP384R1((short) 0x0018),

	/**
	 * NIST P-521 (secp521r1, 66-byte key). TLS identifier: {@code 0x0019}. Rarely needed due to performance overhead.
	 */
	SECP521R1((short) 0x0019),

	/**
	 * Curve448 (56-byte key, high-security alternative to X25519). TLS identifier: {@code 0x001E}. Used when post-quantum
	 * resistance is a priority.
	 */
	X448((short) 0x001E),

	/**
	 * secp256k1 (32-byte key, used in blockchain systems like Bitcoin). TLS identifier: {@code 0x0023}. Not recommended for
	 * general TLS use.
	 */
	SECP256K1((short) 0x0023);

	/**
	 * The size (in bytes) of a {@link NamedCurve} identifier in TLS messages.
	 */
	public static final int BYTES = 2;

	/**
	 * Value map for easy {@link #fromValue(short)} implementation.
	 */
	private static final Map<Short, NamedCurve> VALUE_MAP = Enums.buildNameMap(values(), NamedCurve::value);

	/**
	 * The encapsulated value.
	 */
	private final short value;

	/**
	 * Creates a {@link NamedCurve} with the given TLS-encoded value.
	 *
	 * @param value the 2-byte identifier assigned to this curve in the IANA registry.
	 */
	NamedCurve(final short value) {
		this.value = value;
	}

	/**
	 * Parses a TLS-encoded curve identifier into a {@link NamedCurve}.
	 *
	 * @param value The 2-byte identifier (e.g., {@code 0x001D} for {@link #X25519}).
	 * @return the corresponding {@link NamedCurve}, or {@code null} if unmatched.
	 * @throws IllegalArgumentException If {@code value} is invalid (if enforced by {@link Enums#from}).
	 */
	public static NamedCurve fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Returns the TLS-encoded 2-byte identifier for this curve.
	 *
	 * @return the IANA-assigned value (e.g., {@code 0x0017} for {@link #SECP256R1}).
	 */
	public short value() {
		return value;
	}

	/**
	 * Returns the size of this object when serialized in TLS (always {@value #BYTES} bytes). This is a fixed-size object;
	 * no dynamic calculation is needed.
	 *
	 * @return the size of this object when serialized
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Serializes this {@link NamedCurve} to its TLS-encoded 2-byte representation. The returned array is formatted
	 * according to {@link UInt16#toByteArray(short)}.
	 *
	 * @return the object serialized as byte array
	 */
	@Override
	public byte[] toByteArray() {
		return UInt16.toByteArray(value);
	}
}
