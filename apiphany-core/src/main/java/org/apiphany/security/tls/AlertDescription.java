package org.apiphany.security.tls;

import java.util.Map;

import org.apiphany.io.UInt8;
import org.morphix.lang.Enums;

/**
 * Represents TLS alert messages used to signal errors or connection state changes.
 * <p>
 * Alerts are defined in <a href="https://tools.ietf.org/html/rfc5246#section-7.2">RFC 5246 (TLS 1.2)</a> and
 * <a href="https://tools.ietf.org/html/rfc8446#section-6">RFC 8446 (TLS 1.3)</a>. Each alert has a 1-byte code and a
 * human-readable description of the error condition.
 * </p>
 *
 * <p>
 * <b>Severity Levels:</b>
 * </p>
 * <ul>
 * <li><b>Warning:</b> {@link #CLOSE_NOTIFY} (connection may continue)</li>
 * <li><b>Fatal:</b> All others (connection must terminate)</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * This enum and its methods are thread-safe.
 *
 * @see <a href="https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#tls-parameters-6">IANA Alert
 * Registry</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum AlertDescription implements TLSObject {

	/**
	 * Notifies the peer that the connection will be closed. Code: {@code 0x00}. This is a <b>warning</b>-level alert.
	 */
	CLOSE_NOTIFY((byte) 0x00, "Graceful connection closure"),

	/**
	 * Indicates an invalid message was received (e.g., malformed handshake). Code: {@code 0x0A}. <b>Fatal</b> in all
	 * versions.
	 */
	UNEXPECTED_MESSAGE((byte) 0x0A, "Invalid message received"),

	/**
	 * Signals that a record's MAC failed verification. Code: {@code 0x14}. <b>Fatal</b> (possible tampering detected).
	 */
	BAD_RECORD_MAC((byte) 0x14, "MAC verification failed"),

	/**
	 * Indicates decryption failed (TLS 1.0) or cipher mismatch (TLS 1.2+). Code: {@code 0x15}. <b>Fatal</b>.
	 */
	DECRYPTION_FAILED((byte) 0x15, "Decryption error (TLS 1.0) / Generic cipher mismatch (TLS 1.2+)"),

	/**
	 * A TLS record exceeded the maximum allowed size. Code: {@code 0x16}. <b>Fatal</b> (possible buffer overflow attack).
	 */
	RECORD_OVERFLOW((byte) 0x16, "TLS record exceeded max size"),

	/**
	 * No common ciphersuite or parameters could be negotiated. Code: {@code 0x28}. <b>Fatal</b>.
	 */
	HANDSHAKE_FAILURE((byte) 0x28, "No common ciphersuite or unsupported parameters"),

	/**
	 * The certificate was valid but access was denied by the application. Code: {@code 0x2F}. <b>Fatal</b>.
	 */
	ACCESS_DENIED((byte) 0x2F, "Certificate valid but access denied"),

	/**
	 * A message was malformed (e.g., corrupt certificate). Code: {@code 0x30}. <b>Fatal</b>.
	 */
	DECODE_ERROR((byte) 0x30, "Message corrupted (e.g., malformed certificate)"),

	/**
	 * The peer doesn't support a required extension. Code: {@code 0x3C}. <b>Fatal</b> in TLS 1.3, warning in 1.2.
	 */
	UNSUPPORTED_EXTENSION((byte) 0x3C, "Peer doesn’t support the extension"),

	/**
	 * The requested TLS version is unsupported. Code: {@code 0x46}. <b>Fatal</b>.
	 */
	PROTOCOL_VERSION((byte) 0x46, "Unsupported TLS version"),

	/**
	 * An unspecified internal error occurred (e.g., bug). Code: {@code 0x50}. <b>Fatal</b>.
	 */
	INTERNAL_ERROR((byte) 0x50, "Unspecified implementation error");

	/**
	 * The size (in bytes) of a {@link AlertDescription} code in TLS messages.
	 */
	public static final int BYTES = 1;

	/**
	 * Value map for easy {@link #fromCode(byte)} implementation.
	 */
	private static final Map<Byte, AlertDescription> VALUE_MAP = Enums.buildNameMap(values(), AlertDescription::code);

	/**
	 * The TLS code encapsulation.
	 */
	private final byte code;

	/**
	 * The alert description as {@link String}.
	 */
	private final String description;

	/**
	 * Creates an {@link AlertDescription} with the given TLS code and description.
	 *
	 * @param code the 1-byte alert code defined in RFC 5246/8446.
	 * @param description human-readable explanation of the alert.
	 */
	AlertDescription(final byte code, final String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * Returns the TLS-encoded alert code.
	 *
	 * @return the 1-byte value (e.g., {@code 0x14} for {@link #BAD_RECORD_MAC}).
	 */
	public byte code() {
		return code;
	}

	/**
	 * Returns a human-readable description of the alert condition.
	 *
	 * @return the description (e.g., "MAC verification failed").
	 */
	public String description() {
		return description;
	}

	/**
	 * Parses a TLS-encoded alert code into an {@link AlertDescription}.
	 *
	 * @param value the 1-byte alert code (e.g., {@code 0x28}).
	 * @return the corresponding alert, or {@code null} if unmatched.
	 * @throws IllegalArgumentException If {@code value} is invalid (if enforced by {@link Enums#from}).
	 */
	public static AlertDescription fromCode(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Formats the alert as {@code "name: description"} (e.g., "close_notify: Graceful connection closure").
	 */
	@Override
	public String toString() {
		return name().toLowerCase() + ": " + description();
	}

	/**
	 * Returns the size of this object when serialized in TLS (always {@value #BYTES} byte). Alert codes are always 1 byte
	 * in TLS records.
	 *
	 * @return the size of this object when serialized in TLS
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Serializes this alert to its TLS-encoded 1-byte representation.
	 *
	 * @return a single-byte array containing {@link #code()}.
	 */
	@Override
	public byte[] toByteArray() {
		return UInt8.toByteArray(code);
	}
}
