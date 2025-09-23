package org.apiphany.http;

import java.util.Map;
import java.util.Objects;

import org.morphix.lang.Enums;

/**
 * Represents all standard and widely used HTTP {@code Authorization} header schemes. Includes RFC-defined schemes
 * (Basic, Bearer, Digest) and common proprietary ones (AWS, NTLM).
 *
 * @see <a href="https://www.iana.org/assignments/http-authschemes/http-authschemes.xhtml"> IANA HTTP Authentication
 * Scheme Registry</a>
 */
public enum HttpAuthScheme {

	/**
	 * Basic authentication (RFC 7617). Credentials are base64-encoded. Example:
	 * {@code Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=}
	 */
	BASIC("Basic"),

	/**
	 * Bearer token authentication (RFC 6750). Used for OAuth 2.0 and JWTs. Example:
	 * {@code Authorization: Bearer eyJhbGciOiJ...}
	 */
	BEARER("Bearer"),

	/**
	 * Digest access authentication (RFC 7616). Uses MD5/SHA hashing. Example:
	 * {@code Authorization: Digest username="user", realm="...", nonce="..."}
	 */
	DIGEST("Digest"),

	/**
	 * HTTP Origin-Bound Authentication (RFC 7486). Uses digital signatures.
	 */
	HOBA("HOBA"),

	/**
	 * Mutual TLS authentication (RFC 8120).
	 */
	MUTUAL("Mutual"),

	/**
	 * SPNEGO (Kerberos/Negotiate) authentication (RFC 4559). Example: {@code Authorization: Negotiate TlRMTV...}
	 */
	NEGOTIATE("Negotiate"),

	/**
	 * Salted Challenge Response Authentication (RFC 7804).
	 */
	SCRAM("SCRAM"),

	/**
	 * Voluntary Application Server Identification (RFC 8292). Used in WebPush.
	 */
	VAPID("vapid"),

	/**
	 * OAuth 2.0 Demonstrating Proof-of-Possession (RFC 9449).
	 */
	DPOP("DPoP"),

	/**
	 * AWS Signature Version 4 authentication. Example: {@code Authorization: AWS4-HMAC-SHA256 Credential=...}
	 */
	AWS4_HMAC_SHA256("AWS4-HMAC-SHA256"),

	/**
	 * Microsoft NTLM authentication. Example: {@code Authorization: NTLM TlRMTV...}
	 */
	NTLM("NTLM"),

	/**
	 * OAuth 1.0 (RFC 5849, deprecated).
	 */
	@Deprecated
	OAUTH("OAuth"),

	/**
	 * Generic token-based authentication (non-standard). Example: {@code Authorization: Token abc123...}
	 */
	TOKEN("Token"),

	/**
	 * Custom API key authentication (non-standard). Example: {@code Authorization: Apikey abc123...}
	 */
	APIKEY("Apikey"),

	/**
	 * HMAC-signed request authentication (non-standard).
	 */
	SIGNATURE("Signature");

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, HttpAuthScheme> NAME_MAP = Enums.buildNameMap(values(), scheme -> scheme.toString().toLowerCase());

	/**
	 * The scheme string representation.
	 */
	private final String value;

	/**
	 * Constructs the enum.
	 *
	 * @param value scheme string value
	 */
	HttpAuthScheme(final String value) {
		this.value = value;
	}

	/**
	 * Returns the official scheme name (e.g., "Bearer").
	 *
	 * @return The scheme name as used in HTTP headers.
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns the scheme name for use in HTTP headers.
	 *
	 * @return The scheme name (e.g., "Basic").
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Returns a {@link HttpAuthScheme} enum from a {@link String}.
	 *
	 * @param header HTTP authentication scheme as string
	 * @return an HTTP authentication scheme enum
	 */
	public static HttpAuthScheme fromString(final String header) {
		return Enums.fromString(Objects.requireNonNull(header).toLowerCase(), NAME_MAP, values());
	}

}
