package org.apiphany.security.token;

import org.apiphany.client.ClientProperties;

/**
 * Properties specifying the bearer token.
 * <p>
 * To configure these properties in the {@link ClientProperties} under the {@code custom} root, use the prefix
 * {@code ssl} as defined in {@link #ROOT}. For example:
 *
 * <pre>
 * my-client-properties.custom.token.value=123456789abcdef
 * </pre>
 *
 * or in YAML:
 *
 * <pre>
 * my-client-properties:
 *   custom:
 *     token:
 *       value: 123456789abcdef
 * </pre>
 *
 * This would set the token value to {@code 123456789abcdef}, similarly for authentication scheme.
 *
 * @author Radu Sebastian LAZIN
 */
public class TokenProperties {

	/**
	 * The root property prefix for token configuration.
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
	public void setAuthenticationScheme(final String authenticationScheme) {
		this.authenticationScheme = authenticationScheme;
	}
}
