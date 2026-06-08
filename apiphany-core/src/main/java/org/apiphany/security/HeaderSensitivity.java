package org.apiphany.security;

/**
 * Interface for defining header sensitivity rules.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface HeaderSensitivity {

	/**
	 * Checks if the given header name is considered sensitive.
	 *
	 * @param headerName the name of the header to check
	 * @return {@code true} if the header is sensitive, {@code false} otherwise
	 */
	boolean isSensitiveHeader(String headerName);
}
