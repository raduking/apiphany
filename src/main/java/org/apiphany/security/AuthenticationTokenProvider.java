package org.apiphany.security;

/**
 * Provides authentication tokens for security purposes. Implementations of this interface are responsible for
 * generating or retrieving authentication tokens that can be used to authenticate requests or establish secure
 * sessions.
 *
 * @author Radu Sebastian LAZIN
 */
public interface AuthenticationTokenProvider {

	/**
	 * Retrieves an authentication token.
	 *
	 * The implementation may generate a new token, retrieve a cached token, or obtain a token from an external
	 * authentication service depending on the specific implementation strategy.
	 *
	 * @return a non-null authentication token string that can be used for authentication purposes
	 * @throws AuthenticationException if the token cannot be generated or retrieved due to security constraints or
	 *     authentication failures
	 */
	AuthenticationToken getAuthenticationToken();

}
