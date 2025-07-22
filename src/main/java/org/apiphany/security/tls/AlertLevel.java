package org.apiphany.security.tls;

import java.util.Map;

import org.apiphany.io.UInt8;
import org.morphix.lang.Enums;

/**
 * Represents the severity level of a TLS alert message.
 * <p>
 * Defined in <a href="https://tools.ietf.org/html/rfc5246#section-7.2">RFC 5246 (TLS 1.2)</a> and
 * <a href="https://tools.ietf.org/html/rfc8446#section-6">RFC 8446 (TLS 1.3)</a>. Alerts with level {@link #FATAL}
 * terminate the connection immediately, while {@link #WARNING} alerts may allow continued operation.
 * </p>
 *
 * <p>
 * <b>Protocol Notes:</b>
 * </p>
 * <ul>
 * <li>In TLS 1.3, all alerts except {@code close_notify} are treated as fatal.</li>
 * <li>Warning alerts are rare in practice due to security risks.</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * This enum and its methods are thread-safe.
 *
 * @see AlertDescription
 *
 * @author Radu Sebastian LAZIN
 */
public enum AlertLevel implements TLSObject {

	/**
	 * Non-fatal alert condition (e.g., graceful closure notification). TLS encoding: {@code 0x01}. The connection may
	 * remain open after this alert.
	 *
	 * @see AlertDescription#CLOSE_NOTIFY
	 */
	WARNING((byte) 0x01),

	/**
	 * Fatal error requiring immediate connection termination. TLS encoding: {@code 0x02}. The connection must be closed
	 * after this alert.
	 *
	 * @see AlertDescription#BAD_RECORD_MAC
	 * @see AlertDescription#HANDSHAKE_FAILURE
	 */
	FATAL((byte) 0x02);

	/**
	 * The size (in bytes) of an {@link AlertLevel} in TLS messages.
	 */
	public static final int BYTES = 1;

	/**
	 * Value map for easy {@link #fromValue(byte)} implementation.
	 */
	private static final Map<Byte, AlertLevel> VALUE_MAP = Enums.buildNameMap(values(), AlertLevel::value);

	/**
	 * The encapsulated TLS value.
	 */
	private final byte value;

	/**
	 * Creates an {@link AlertLevel} with the given TLS-encoded value.
	 *
	 * @param value the byte representation of this alert level (either {@code 0x01} or {@code 0x02}).
	 */
	AlertLevel(final byte value) {
		this.value = value;
	}

	/**
	 * Returns the TLS-encoded value of this alert level.
	 *
	 * @return {@code 0x01} for {@link #WARNING}, {@code 0x02} for {@link #FATAL}.
	 */
	public byte value() {
		return value;
	}

	/**
	 * Parses a TLS-encoded byte into an {@link AlertLevel}.
	 *
	 * @param value the byte value (either {@code 0x01} or {@code 0x02}).
	 * @return the corresponding {@link AlertLevel}, or {@code null} if unmatched.
	 * @throws IllegalArgumentException If {@code value} is invalid (if enforced by {@link Enums#from}).
	 */
	public static AlertLevel fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Returns the size of this object when serialized in TLS (always {@value #BYTES} byte). Alert levels are always 1 byte
	 * in TLS records.
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Serializes this {@link AlertLevel} to its TLS-encoded byte representation.
	 *
	 * @return a single-byte array containing {@link #value()}.
	 */
	@Override
	public byte[] toByteArray() {
		return UInt8.toByteArray(value);
	}
}
