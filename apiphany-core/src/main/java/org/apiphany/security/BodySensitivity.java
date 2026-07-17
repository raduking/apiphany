package org.apiphany.security;

/**
 * Interface for defining body sensitivity rules.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface BodySensitivity {

	/**
	 * Checks if the given body is considered sensitive.
	 *
	 * @param <T> the body type
	 * @param body the body object to check
	 * @return {@code true} if the body is sensitive, {@code false} otherwise
	 */
	<T> boolean isSensitiveBody(T body);
}
