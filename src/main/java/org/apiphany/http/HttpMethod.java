package org.apiphany.http;

import java.util.Map;

import org.apiphany.RequestMethod;
import org.morphix.lang.Enums;

/**
 * Represents the standard HTTP methods as defined by the HTTP/1.1 specification and other relevant RFCs. Each method
 * has a corresponding string value and can be retrieved from a string representation.
 *
 * @author Radu Sebastian LAZIN
 */
public enum HttpMethod implements RequestMethod {

	/**
	 * The HTTP method {@code GET}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-get">RFC 9110, section 9.3.1</a>
	 */
	GET("GET"),

	/**
	 * The HTTP method {@code HEAD}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-head">RFC 9110, section 9.3.2</a>
	 */
	HEAD("HEAD"),

	/**
	 * The HTTP method {@code POST}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-post">RFC 9110, section 9.3.3</a>
	 */
	POST("POST"),

	/**
	 * The HTTP method {@code PUT}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-put">RFC 9110, section 9.3.4</a>
	 */
	PUT("PUT"),

	/**
	 * The HTTP method {@code PATCH}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc5789.html#section-2">RFC 5789, section 2</a>
	 */
	PATCH("PATCH"),

	/**
	 * The HTTP method {@code DELETE}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-delete">RFC 9110, section 9.3.5</a>
	 */
	DELETE("DELETE"),

	/**
	 * The HTTP method {@code CONNECT}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-connect">RFC 9110, section 9.3.6</a>
	 */
	CONNECT("CONNECT"),

	/**
	 * The HTTP method {@code OPTIONS}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-options">RFC 9110, section 9.3.7</a>
	 */
	OPTIONS("OPTIONS"),

	/**
	 * The HTTP method {@code TRACE}.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-trace">RFC 9110, section 9.3.8</a>
	 */
	TRACE("TRACE");

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, HttpMethod> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The {@link String} value.
	 */
	private final String value;

	/**
	 * Constructs an {@link HttpMethod} with the specified string value.
	 *
	 * @param value string value
	 */
	HttpMethod(final String value) {
		this.value = value;
	}

	/**
	 * Returns the string value.
	 *
	 * @return the string value
	 */
	@Override
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
	 * Returns a {@link HttpMethod} enum from a {@link String}.
	 *
	 * @param method HTTP method as string
	 * @return an HTTP method enum
	 */
	public static HttpMethod fromString(final String method) {
		return Enums.fromString(method, NAME_MAP, values());
	}

	/**
	 * Returns true if the given string matches the enum value ignoring the case, false otherwise. The HTTP methods are
	 * case-insensitive.
	 *
	 * @param method method as string to match
	 * @return true if the given string matches the enum value ignoring the case, false otherwise.
	 */
	public boolean matches(final String method) {
		return value().equalsIgnoreCase(method);
	}

}
