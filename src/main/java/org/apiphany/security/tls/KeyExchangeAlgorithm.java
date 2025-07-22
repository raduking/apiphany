package org.apiphany.security.tls;

import java.util.Map;

import org.morphix.lang.Enums;

/**
 * Represents key exchange algorithms supported in TLS/SSL handshakes.
 * <p>
 * These algorithms determine how cryptographic keys are negotiated between client and server during secure socket
 * setup. Some algorithms (e.g., {@code ECDHE}) provide forward secrecy, while others (e.g., {@code RSA}) are legacy but
 * widely supported.
 * </p>
 *
 * <p>
 * <b>Compatibility Notes:</b>
 * </p>
 * <ul>
 * <li>{@code NONE} is reserved for TLS 1.3+ where key exchange occurs post-handshake.</li>
 * <li>{@code DHE} and {@code ECDHE} provide forward secrecy but may impact performance.</li>
 * <li>{@code RSA} is vulnerable to replay attacks if session resumption is enabled.</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * This enum and its methods are thread-safe.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8446#section-2">TLS 1.3 Key Exchange</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum KeyExchangeAlgorithm {

	/**
	 * Elliptic Curve Diffie-Hellman Ephemeral (ECDHE). Provides forward secrecy and strong security with modern elliptic
	 * curves. Preferred for TLS 1.2+ when performance permits.
	 */
	ECDHE,

	/**
	 * Finite Field Diffie-Hellman Ephemeral (DHE). Provides forward secrecy but is computationally expensive. Used when
	 * ECDHE is unavailable.
	 */
	DHE,

	/**
	 * RSA key exchange (non-ephemeral). Legacy algorithm without forward secrecy. Avoid in new deployments unless
	 * compatibility with older systems is required.
	 */
	RSA,

	/**
	 * No key exchange during initial handshake (TLS 1.3+). Key material is negotiated post-handshake or reused from prior
	 * sessions. For TLS 1.3 and future post-handshake schemes.
	 */
	NONE;

	private static final Map<String, KeyExchangeAlgorithm> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * Parses a string value (case-insensitive) into a {@link KeyExchangeAlgorithm}.
	 *
	 * @param value The string representation of the algorithm (e.g., "DHE", "RSA").
	 * @return the corresponding enum constant, or {@code null} if no match is found.
	 * @throws IllegalArgumentException If {@code value} is {@code null} (if enforced by {@link Enums#fromString}).
	 */
	public static KeyExchangeAlgorithm fromValue(final String value) {
		return Enums.fromString(value, NAME_MAP, values());
	}
}
