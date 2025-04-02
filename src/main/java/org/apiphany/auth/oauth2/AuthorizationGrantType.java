package org.apiphany.auth.oauth2;

import java.util.Map;

import org.morphix.lang.Enums;

/**
 * Enumeration of OAuth2 authorization grant types. Represents the different ways a client can obtain an access token.
 *
 * @author Radu Sebastian LAZIN
 */
public enum AuthorizationGrantType {

	/**
	 * Authorization code grant type (RFC 6749 Section 4.1).
	 */
	AUTHORIZATION_CODE("authorization_code"),

	/**
	 * Refresh token grant type (RFC 6749 Section 6).
	 */
	REFRESH_TOKEN("refresh_token"),

	/**
	 * Client credentials grant type (RFC 6749 Section 4.4).
	 */
	CLIENT_CREDENTIALS("client_credentials"),

	/**
	 * JWT bearer token grant type (RFC 7523).
	 */
	JWT_BEARER("urn:ietf:params:oauth:grant-type:jwt-bearer"),

	/**
	 * Device code grant type (RFC 8628).
	 */
	DEVICE_CODE("urn:ietf:params:oauth:grant-type:device_code");

	/**
	 * Mapping of string values to enum constants for lookup.
	 */
	private static final Map<String, AuthorizationGrantType> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The string representation of the grant type.
	 */
	private final String value;

	/**
	 * Constructs a new grant type with the specified string value.
	 *
	 * @param value the string representation of the grant type
	 */
	private AuthorizationGrantType(final String value) {
		this.value = value;
	}

	/**
	 * Returns the string representation of this grant type.
	 *
	 * @return the string value of this grant type
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns the string representation of this grant type.
	 *
	 * @return the string value of this grant type
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Converts a string value to the corresponding AuthorizationGrantType enum constant.
	 *
	 * @param method the grant type string to convert
	 * @return the matching AuthorizationGrantType enum constant
	 * @throws IllegalArgumentException if no matching grant type is found
	 */
	public static AuthorizationGrantType fromString(final String method) {
		return Enums.fromString(method, NAME_MAP, values());
	}
}
