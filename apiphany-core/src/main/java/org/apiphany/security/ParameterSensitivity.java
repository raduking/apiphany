package org.apiphany.security;

/**
 * Interface for defining parameter sensitivity rules.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ParameterSensitivity {

	/**
	 * Checks if the given query parameter name is considered sensitive.
	 *
	 * @param paramName the name of the query parameter to check
	 * @return {@code true} if the query parameter is sensitive, {@code false} otherwise
	 */
	boolean isSensitiveParameter(String paramName);
}
