package org.apiphany.security;

/**
 * Functional interface for supplying the value of the Authorization header.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface AuthorizationHeaderProvider {

	/**
	 * Returns the value for the Authorization header for the given request.
	 *
	 * @return the value for the Authorization header
	 */
	String getAuthorizationHeader();

}
