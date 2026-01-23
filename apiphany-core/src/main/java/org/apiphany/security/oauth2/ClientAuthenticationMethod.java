package org.apiphany.security.oauth2;

import java.util.Map;

import org.morphix.lang.Enums;

/**
 * Enumeration of OAuth2 client authentication methods. Defines the various ways a client can authenticate with the
 * authorization server when requesting an access token.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ClientAuthenticationMethod {

	/**
	 * Client secret basic authentication (OAuth2 specification). The client authenticates with its client_id and
	 * client_secret using HTTP Basic authentication scheme.
	 */
	CLIENT_SECRET_BASIC("client_secret_basic"),

	/**
	 * Client secret post authentication (OAuth2 specification). The client credentials are included in the request body.
	 */
	CLIENT_SECRET_POST("client_secret_post"),

	/**
	 * Client secret JWT authentication (RFC 7523). The client authenticates by signing a JWT with the client secret.
	 */
	CLIENT_SECRET_JWT("client_secret_jwt"),

	/**
	 * Private key JWT authentication (RFC 7523). The client authenticates by signing a JWT with its private key.
	 */
	PRIVATE_KEY_JWT("private_key_jwt"),

	/**
	 * No authentication method. Used for public clients that don't require authentication.
	 */
	NONE("none");

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, ClientAuthenticationMethod> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The {@link String} value.
	 */
	private final String value;

	/**
	 * Constructor with value.
	 *
	 * @param value the value to set
	 */
	ClientAuthenticationMethod(final String value) {
		this.value = value;
	}

	/**
	 * Returns the string value.
	 *
	 * @return the string value
	 */
	public String value() {
		return value;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Returns a {@link ClientAuthenticationMethod} enum from a {@link String}.
	 *
	 * @param method client authentication method as string
	 * @return client authentication method
	 */
	public static ClientAuthenticationMethod fromString(final String method) {
		return Enums.fromString(method, NAME_MAP, values());
	}
}
