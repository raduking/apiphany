package org.apiphany.auth.oauth2;

import java.util.Map;

import org.morphix.lang.Enums;

/**
 * Enumeration of client authentication methods. Represents the different ways a client can authenticate with an OAuth2
 * server.
 *
 * @author Radu Sebastian LAZIN
 */
public enum AuthenticationMethod {

	/**
	 * Authentication via HTTP headers (e.g., Basic Auth or Bearer Token).
	 */
	HEADER("header"),

	/**
	 * Authentication via form parameters (client_secret in POST body).
	 */
	FORM("form"),

	/**
	 * Authentication via query parameters (client_secret in URL).
	 */
	QUERY("query");

	/**
	 * Mapping of string values to enum constants for efficient lookup.
	 */
	private static final Map<String, AuthenticationMethod> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The string representation of the authentication method.
	 */
	private final String value;

	/**
	 * Constructs a new authentication method with the specified string value.
	 *
	 * @param value the string representation of the authentication method
	 */
	private AuthenticationMethod(final String value) {
		this.value = value;
	}

	/**
	 * Returns the string representation of this authentication method.
	 *
	 * @return the string value of this authentication method
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns the string representation of this authentication method.
	 *
	 * @return the string value of this authentication method
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Converts a string value to the corresponding AuthenticationMethod enum constant.
	 *
	 * @param method the authentication method string to convert
	 * @return the matching AuthenticationMethod enum constant
	 * @throws IllegalArgumentException if no matching authentication method is found
	 */
	public static AuthenticationMethod fromString(final String method) {
		return Enums.fromString(method, NAME_MAP, values());
	}
}
