package org.apiphany.security.http;

import java.util.Map;
import java.util.Objects;

import org.morphix.lang.Enums;
import org.morphix.lang.function.ToStringFunction;

/**
 * Represents all standard and widely used HTTP authentication schemes ({@code Authorization} header schemes). Includes
 * RFC-defined schemes (Basic, Bearer, Digest) and common proprietary/widely used ones (AWS, TOKEN, etc.).
 *
 * @see <a href="https://www.iana.org/assignments/http-authschemes/http-authschemes.xhtml"> IANA HTTP Authentication
 * Scheme Registry</a>
 */
public enum HttpAuthenticationScheme {

	/**
	 * Basic authentication (RFC 7617). Credentials are base64-encoded. Example:
	 * {@code Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7617">RFC 7617 - The 'Basic' HTTP Authentication Scheme</a>
	 */
	BASIC("Basic"),

	/**
	 * Bearer token authentication (RFC 6750). Used for OAuth 2.0 and JWTs. Example:
	 * {@code Authorization: Bearer eyJhbGciOiJ...}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6750">RFC 6750 - The OAuth 2.0 Authorization Framework: Bearer Token
	 * Usage</a>
	 */
	BEARER("Bearer"),

	/**
	 * HTTP Authentication Scheme for Access Tokens in HTTP Requests (RFC 9729). Similar to Bearer but with token binding.
	 * Example: {@code Authorization: Concealed eyJhbGciOiJ...}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc9729">RFC 9729 - The Concealed HTTP Authentication Scheme</a>
	 */
	CONCEALED("Concealed"),

	/**
	 * Digest access authentication (RFC 7616). Uses MD5/SHA hashing. Example:
	 * {@code Authorization: Digest username="user", realm="...", nonce="..."}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7616">RFC 7616 - HTTP Digest Access Authentication</a>
	 */
	DIGEST("Digest"),

	/**
	 * Grant Negotiation and Authorization Protocol (GNAP) authentication (RFC 9635). A modern alternative to OAuth 2.0.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc9635">RFC 9635 - Grant Negotiation and Authorization Protocol (GNAP)</a>
	 */
	GNAP("GNAP"),

	/**
	 * OAuth 2.0 Demonstrating Proof-of-Possession (RFC 9449).
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc9449">RFC 9449 - OAuth 2.0 Demonstrating Proof-of-Possession (DPoP)</a>
	 */
	DPOP("DPoP"),

	/**
	 * HTTP Origin-Bound Authentication (RFC 7486). Uses digital signatures.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7486">RFC 7486 - HTTP Origin-Bound Authentication (HOBA)</a>
	 */
	HOBA("HOBA"),

	/**
	 * Mutual Authentication Protocol for HTTP (RFC 8120).
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc8120">RFC 8120 - Mutual Authentication Protocol for HTTP</a>
	 */
	MUTUAL("Mutual"),

	/**
	 * SPNEGO (Kerberos/Negotiate) authentication (RFC 4559). Example: {@code Authorization: Negotiate TlRMTV...}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc4559">RFC 4559 - SPNEGO-based Kerberos and NTLM HTTP Authentication in
	 * Microsoft Windows</a>
	 */
	NEGOTIATE("Negotiate"),

	/**
	 * OAuth 1.0 (RFC 5849, deprecated).
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc5849">RFC 5849 - The OAuth 1.0 Protocol</a>
	 */
	@Deprecated
	OAUTH("OAuth"),

	/**
	 * Private token authentication (non-standard). Example: {@code Authorization: PrivateToken abc123...}
	 *
	 * @see <a href="https://tools.ietf.org/doc/html/rfc9577">RFC 9577 - The Privacy Pass HTTP Authentication Scheme</a>
	 */
	PRIVATE_TOKEN("PrivateToken"),

	/**
	 * Salted Challenge Response Authentication (RFC 7804).
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7804">RFC 7804 - Salted Challenge Response HTTP Authentication
	 * Mechanism</a>
	 */
	SCRAM("SCRAM"),

	/**
	 * SCRAM-SHA-1 authentication (RFC 7804). Uses SHA-1 hashing. Example: {@code Authorization: SCRAM-SHA-1 ...}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7804">RFC 7804 - Salted Challenge Response HTTP Authentication
	 * Mechanism</a>
	 */
	SCRAM_SHA_1("SCRAM-SHA-1"),

	/**
	 * SCRAM-SHA-256 authentication (RFC 7804). Uses SHA-256 hashing. Example: {@code Authorization: SCRAM-SHA-256 ...}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7804">RFC 7804 - Salted Challenge Response HTTP Authentication
	 * Mechanism</a>
	 */
	SCRAM_SHA_256("SCRAM-SHA-256"),

	/**
	 * Voluntary Application Server Identification (RFC 8292). Used in WebPush.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc8292">RFC 8292 - Voluntary Application Server Identification (VAPID) for
	 * Web Push</a>
	 */
	VAPID("vapid"),

	/**
	 * AWS Signature Version 4 authentication. Example: {@code Authorization: AWS4-HMAC-SHA256 Credential=...}
	 */
	AWS4_HMAC_SHA256("AWS4-HMAC-SHA256"),

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
	private static final Map<String, HttpAuthenticationScheme> NAME_MAP = Enums.buildNameMap(values(), ToStringFunction.toLowerCase());

	/**
	 * The scheme string representation.
	 */
	private final String value;

	/**
	 * Constructs the enum.
	 *
	 * @param value scheme string value
	 */
	HttpAuthenticationScheme(final String value) {
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
	 * Returns a {@link HttpAuthenticationScheme} enum from a {@link String}.
	 *
	 * @param header HTTP authentication scheme as string
	 * @return an HTTP authentication scheme enum
	 */
	public static HttpAuthenticationScheme fromString(final String header) {
		return Enums.fromString(Objects.requireNonNull(header).toLowerCase(), NAME_MAP, values());
	}
}
