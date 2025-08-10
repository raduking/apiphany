package org.apiphany.security;

import java.time.Instant;

import org.apiphany.json.JsonBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents an authentication token used for accessing secured resources. This class encapsulates the access token,
 * refresh token, token type, and expiration details.
 * <p>
 * TODO: create a serializer/deserializer so that it is JSON library agnostic
 *
 * @author Radu Sebastian LAZIN
 */
@JsonPropertyOrder({
    "access_token",
    "refresh_token",
    "expires_in",
    "token_type",
    "refresh_expires_in",
    "not-before-policy",
    "scope"
})
public class AuthenticationToken {

	/**
	 * The access token used for authenticating requests.
	 */
	@JsonProperty("access_token")
	private String accessToken;

	/**
	 * The refresh token used to obtain a new access token when the current one expires.
	 */
	@JsonProperty("refresh_token")
	private String refreshToken;

	/**
	 * The time in seconds until the access token expires.
	 */
	@JsonProperty("expires_in")
	private long expiresIn;

	/**
	 * The type of the token (e.g., "Bearer").
	 */
	@JsonProperty("token_type")
	private String tokenType;

	/**
	 * The time in seconds until the access token expires.
	 */
	@JsonProperty("refresh_expires_in")
	private long refreshExpiresIn;

	/**
	 * The "not-before-policy" (nbf) claim indicates the time before which the token MUST NOT be accepted. This is typically
	 * used to implement token revocation or policy changes, ensuring tokens issued before a certain time are rejected even
	 * if they haven't expired.
	 * <p>
	 * In Keycloak (and some other OAuth2 providers), this field represents the timestamp (in seconds since epoch) when the
	 * token becomes valid relative to policy changes. A value of 0 typically means no restriction.
	 *
	 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.5">RFC 7519 (JWT) - nbf Claim</a>
	 * @see <a href="https://www.keycloak.org/docs/latest/securing_apps/index.html#token-timeouts">Keycloak Token
	 * Timeouts</a>
	 */
	@JsonProperty("not-before-policy")
	private int notBeforePolicy;

	/**
	 * The scope of the access token as a space-separated list of case-sensitive strings.
	 * <p>
	 * In OAuth 2.0, scopes define the specific permissions/access rights granted by the token. Common examples include
	 * "profile", "email", "openid" for OpenID Connect, or custom API scopes.
	 * <p>
	 * Example values:
	 * <ul>
	 * <li>"profile email"</li>
	 * <li>"read write"</li>
	 * <li>"openid profile email"</li>
	 * </ul>
	 *
	 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-3.3">RFC 6749 - OAuth 2.0 Scopes</a>
	 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#ScopeClaims">OpenID Connect Scopes</a>
	 */
	private String scope;

	/**
	 * The exact instant when the token expires.
	 */
	private Instant expiration;

	/**
	 * Default constructor.
	 */
	public AuthenticationToken() {
		// empty
	}

	/**
	 * Returns a JSON representation of this authentication token.
	 *
	 * @return a JSON string representing this object.
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the access token.
	 *
	 * @return the access token.
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * Sets the access token.
	 *
	 * @param accessToken the access token to set.
	 */
	public void setAccessToken(final String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * Returns the refresh token.
	 *
	 * @return the refresh token.
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * Sets the refresh token.
	 *
	 * @param refreshToken the refresh token to set.
	 */
	public void setRefreshToken(final String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * Returns the time in seconds until the access token expires.
	 *
	 * @return the time in seconds until the access token expires.
	 */
	public long getExpiresIn() {
		return expiresIn;
	}

	/**
	 * Sets the time in seconds until the access token expires.
	 *
	 * @param expiresIn the time in seconds until the access token expires.
	 */
	public void setExpiresIn(final long expiresIn) {
		this.expiresIn = expiresIn;
	}

	/**
	 * Sets the exact instant when the token expires.
	 *
	 * @param expiration the instant when the token expires.
	 */
	public void setExpiration(final Instant expiration) {
		this.expiration = expiration;
	}

	/**
	 * Returns the exact instant when the token expires.
	 *
	 * @return the instant when the token expires.
	 */
	public Instant getExpiration() {
		return expiration;
	}

	/**
	 * Checks if the token has expired.
	 *
	 * @return true if the token has expired, false otherwise.
	 */
	@JsonIgnore
	public boolean isExpired() {
		return null != expiration && expiration.isBefore(Instant.now());
	}

	/**
	 * Returns the type of the token.
	 *
	 * @return the token type.
	 */
	public String getTokenType() {
		return tokenType;
	}

	/**
	 * Sets the type of the token.
	 *
	 * @param tokenType the token type to set.
	 */
	public void setTokenType(final String tokenType) {
		this.tokenType = tokenType;
	}

	/**
	 * Returns the time in seconds until the refresh token expires.
	 *
	 * @return the time in seconds until the refresh token expires.
	 */
	public long getRefreshExpiresIn() {
		return refreshExpiresIn;
	}

	/**
	 * Sets the time in seconds until the refresh token expires.
	 *
	 * @param refreshExpiresIn the time in seconds until the refresh token expires.
	 */
	public void setRefreshExpiresIn(final long refreshExpiresIn) {
		this.refreshExpiresIn = refreshExpiresIn;
	}

	/**
	 * Returns the "not-before-policy" (nbf) claim.
	 *
	 * @return the "not-before-policy" (nbf) claim
	 */
	public int getNotBeforePolicy() {
		return notBeforePolicy;
	}

	/**
	 * Sets the {@code "not-before-policy"} (nbf) claim.
	 *
	 * @param notBeforePolicy the {@code "not-before-policy"} (nbf) claim to set
	 */
	public void setNotBeforePolicy(final int notBeforePolicy) {
		this.notBeforePolicy = notBeforePolicy;
	}

	/**
	 * Returns the scope of the access token as a space-separated list of case-sensitive strings.
	 *
	 * @return the scope of the access token as a space-separated list of case-sensitive strings
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * Sets the scope of the access token as a space-separated list of case-sensitive strings.
	 *
	 * @param scope scope of the access token to set
	 */
	public void setScope(final String scope) {
		this.scope = scope;
	}
}
