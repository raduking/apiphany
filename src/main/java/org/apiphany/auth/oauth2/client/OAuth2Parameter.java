package org.apiphany.auth.oauth2.client;

import java.util.Map;

import org.apiphany.lang.Strings;
import org.morphix.lang.Enums;

/**
 * Enumeration of standard OAuth2 form parameter names. Contains all parameter names used in OAuth2 token requests,
 * authorization requests, and token responses as defined in RFC 6749 and related specifications.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749">RFC 6749 - OAuth 2.0</a>
 * @author Radu Sebastian LAZIN
 */
public enum OAuth2Parameter {

	/**
	 * The client secret parameter (RFC 6749 Section 2.3.1). Used in client authentication and token requests.
	 */
	CLIENT_SECRET("client_secret"),

	/**
	 * The client identifier parameter (RFC 6749 Section 2.2). Used to identify the client making the request.
	 */
	CLIENT_ID("client_id"),

	/**
	 * The expiration time parameter (RFC 6749 Section 4.2.2). Specifies the lifetime of the access token in seconds.
	 */
	EXPIRES_IN("expires_in"),

	/**
	 * The grant type parameter (RFC 6749 Section 4.1.3). Determines the OAuth2 authorization flow being used.
	 */
	GRANT_TYPE("grant_type"),

	/**
	 * The redirect URI parameter (RFC 6749 Section 4.1.3). Used in authorization requests to specify the callback location.
	 */
	REDIRECT_URI("redirect_uri"),

	/**
	 * The scope parameter (RFC 6749 Section 3.3). Specifies the scope of the access request.
	 */
	SCOPE("scope"),

	/**
	 * The state parameter (RFC 6749 Section 4.1.1). Used to maintain state between the request and callback.
	 */
	STATE("state"),

	/**
	 * The code parameter (RFC 6749 Section 4.1.3). Contains the authorization code received from the authorization
	 * endpoint.
	 */
	CODE("code"),

	/**
	 * The refresh token parameter (RFC 6749 Section 6). Used to request new access tokens using a refresh token.
	 */
	REFRESH_TOKEN("refresh_token"),

	/**
	 * The username parameter (RFC 6749 Section 4.3.2). Used in Resource Owner Password Credentials grant.
	 */
	USERNAME("username"),

	/**
	 * The password parameter (RFC 6749 Section 4.3.2). Used in Resource Owner Password Credentials grant.
	 */
	PASSWORD("password"),

	/**
	 * The assertion parameter (RFC 7521 Section 4.1). Used for JWT Bearer Token and SAML2 Bearer Assertion flows.
	 */
	ASSERTION("assertion"),

	/**
	 * The assertion type parameter (RFC 7521 Section 4.1). Specifies the type of assertion being used.
	 */
	ASSERTION_TYPE("assertion_type"),

	/**
	 * The response type parameter (RFC 6749 Section 3.1.1). Determines the authorization processing flow.
	 */
	RESPONSE_TYPE("response_type"),

	/**
	 * The token type parameter (RFC 6749 Section 5.1). Specifies the type of token returned.
	 */
	TOKEN_TYPE("token_type"),

	/**
	 * The access token parameter (RFC 6749 Section 4.2.2). Contains the issued access token.
	 */
	ACCESS_TOKEN("access_token"),

	/**
	 * The error parameter (RFC 6749 Section 5.2). Contains error codes for failed responses.
	 */
	ERROR("error"),

	/**
	 * The error description parameter (RFC 6749 Section 5.2). Provides human-readable error information.
	 */
	ERROR_DESCRIPTION("error_description"),

	/**
	 * The error URI parameter (RFC 6749 Section 5.2). Provides a URI identifying the error.
	 */
	ERROR_URI("error_uri"),

	/**
	 * The device code parameter (RFC 8628 Section 3.2). Used in Device Authorization Flow.
	 */
	DEVICE_CODE("device_code"),

	/**
	 * The user code parameter (RFC 8628 Section 3.2). Used in Device Authorization Flow for user verification.
	 */
	USER_CODE("user_code"),

	/**
	 * The verification URI parameter (RFC 8628 Section 3.2). Used in Device Authorization Flow for user verification.
	 */
	VERIFICATION_URI("verification_uri"),

	/**
	 * The verification URI complete parameter (RFC 8628 Section 3.2). Extended verification URI that includes user code.
	 */
	VERIFICATION_URI_COMPLETE("verification_uri_complete"),

	/**
	 * The interval parameter (RFC 8628 Section 3.2). Specifies polling interval for Device Authorization Flow.
	 */
	INTERVAL("interval");

	/**
	 * Mapping of lowercase string values to enum constants for case-insensitive lookup. Used by {@link #fromString(String)}
	 * for efficient conversion.
	 */
	private static final Map<String, OAuth2Parameter> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The string representation of the form parameter name.
	 */
	private final String value;

	/**
	 * Constructs a new OAuth2 form parameter enum constant.
	 *
	 * @param value the standard parameter name as defined in OAuth2 specifications
	 */
	OAuth2Parameter(final String value) {
		this.value = value;
	}

	/**
	 * Returns the standard parameter name.
	 *
	 * @return the string representation of this form parameter
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns the standard parameter name.
	 *
	 * @return the string representation of this form parameter
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Converts a string value to the corresponding {@link OAuth2Parameter} enum constant. The lookup is case-insensitive
	 * and handles null input safely.
	 *
	 * @param method the parameter name to convert (case-insensitive)
	 * @return the matching {@link OAuth2Parameter} enum constant
	 * @throws IllegalArgumentException if no matching parameter is found
	 */
	public static OAuth2Parameter fromString(final String method) {
		return Enums.fromString(Strings.safe(method).toLowerCase(), NAME_MAP, values());
	}
}
