package org.apiphany.security.http;

/**
 * Interface for defining HTTP sensitivity rules.
 *
 * @author Radu Sebastian LAZIN
 */
public interface HttpSensitivity {

	/**
	 * Checks if the given header name is considered sensitive.
	 *
	 * @param headerName the name of the HTTP header to check
	 * @return {@code true} if the header is sensitive, {@code false} otherwise
	 */
	boolean isSensitiveHeader(String headerName);

	/**
	 * Checks if the given query parameter name is considered sensitive.
	 *
	 * @param paramName the name of the query parameter to check
	 * @return {@code true} if the query parameter is sensitive, {@code false} otherwise
	 */
	boolean isSensitiveParam(String paramName);
}
