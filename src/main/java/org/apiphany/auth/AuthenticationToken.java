package org.apiphany.auth;

import java.time.Instant;

import org.apiphany.json.JsonBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an authentication token used for accessing secured resources. This class encapsulates the access token,
 * refresh token, token type, and expiration details.
 *
 * @author Radu Sebastian LAZIN
 */
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
	 * The exact instant when the token expires.
	 */
	private Instant expiration;

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
}
