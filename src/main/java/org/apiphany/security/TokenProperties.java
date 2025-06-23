package org.apiphany.security;

/**
 * Properties specifying the bearer token.
 *
 * @author Radu Sebastian LAZIN
 */
public class TokenProperties {

	/**
	 * The root property prefix for bearer token configuration.
	 */
	public static final String ROOT = "token";

	/**
	 * The token value.
	 */
	private String value;

	/**
	 * The authentication scheme (eg: {@code Bearer}, {@code Basic}, etc.).
	 */
	private String authenticationScheme;

	/**
	 * Default constructor.
	 */
	public TokenProperties() {
		// empty
	}

	/**
	 * Returns the token value.
	 *
	 * @return the token value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the token value.
	 *
	 * @param token the token value to set
	 */
	public void setValue(final String token) {
		this.value = token;
	}

	/**
	 * Returns the authentication scheme.
	 *
	 * @return the authentication scheme
	 */
	public String getAuthenticationScheme() {
		return authenticationScheme;
	}

	/**
	 * Sets the authentication scheme.
	 *
	 * @param authenticationScheme the authentication scheme to set
	 */
	public void setAuthenticationScheme(String authenticationScheme) {
		this.authenticationScheme = authenticationScheme;
	}

}
