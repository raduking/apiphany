package org.apiphany.auth.oauth2.client;

import java.util.Map;

import org.morphix.lang.Enums;

/**
 * Client authentication method.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ClientAuthenticationMethod {

	BASIC("basic"),
	CLIENT_SECRET_BASIC("client_secret_basic"),
	CLIENT_SECRET_POST("client_secret_post"),
	CLIENT_SECRET_JWT("client_secret_jwt"),
	PRIVATE_KEY_JWT("private_key_jwt"),
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
	private ClientAuthenticationMethod(final String value) {
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
