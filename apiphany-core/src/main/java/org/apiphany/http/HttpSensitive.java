package org.apiphany.http;

import java.util.Set;

import org.apiphany.lang.Strings;
import org.morphix.reflection.Constructors;

/**
 * Common HTTP-sensitive names used for redaction and secure logging.
 * <p>
 * Contains default header and parameter names that are commonly used to transport credentials, authentication tokens,
 * session identifiers, or other sensitive values.
 *
 * @author Radu Sebastian LAZIN
 */
public final class HttpSensitive {

	/**
	 * A predefined set of common HTTP-sensitive header names that should be treated with special care.
	 */
	public static final Set<String> HEADERS = Set.of(
			HttpHeader.Name.AUTHORIZATION,
			HttpHeader.Name.PROXY_AUTHORIZATION,
			HttpHeader.Name.COOKIE,
			HttpHeader.Name.SET_COOKIE,
			HttpHeader.Name.SET_COOKIE2,
			HttpHeader.Name.WWW_AUTHENTICATE,
			DeFactoHeader.Name.API_KEY,
			DeFactoHeader.Name.X_API_KEY,
			DeFactoHeader.Name.X_AUTH_TOKEN,
			DeFactoHeader.Name.X_AUTHORIZATION);

	/**
	 * A predefined set of common HTTP-sensitive query parameter names that should be treated with special care.
	 */
	public static final Set<String> PARAMS = Set.of(
			"token",
			"access_token",
			"refresh_token",
			"api_key",
			"apikey",
			"code",
			"client_secret",
			"password");

	/**
	 * Checks if the given name is a common sensitive header name.
	 *
	 * @param name the header name to check
	 * @return true if the name is a common sensitive header, false otherwise
	 */
	public static boolean isHeader(final String name) {
		return Strings.containsIgnoreCase(name, HEADERS);
	}

	/**
	 * Checks if the given name is a common sensitive parameter name.
	 *
	 * @param name the parameter name to check
	 * @return true if the name is a common sensitive parameter, false otherwise
	 */
	public static boolean isParam(final String name) {
		return Strings.containsIgnoreCase(name, PARAMS);
	}

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private HttpSensitive() {
		throw Constructors.unsupportedOperationException();
	}
}
