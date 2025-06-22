package org.apiphany.security;

/**
 * Properties specifying the bearer token.
 *
 * @author Radu Sebastian LAZIN
 */
public class BearerTokenProperties {

	/**
	 * The root property prefix for bearer token configuration.
	 */
	public static final String ROOT = "bearer";

	/**
	 * The token.
	 */
	private String token;

	/**
	 * Default constructor.
	 */
	public BearerTokenProperties() {
		// empty
	}

	/**
	 * Returns the token.
	 *
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the token.
	 *
	 * @param token the token to set
	 */
	public void setToken(final String token) {
		this.token = token;
	}

}
