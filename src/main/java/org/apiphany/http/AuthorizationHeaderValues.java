package org.apiphany.http;

/**
 * Utility methods for building {@code Authorization} header value.
 *
 * @author Radu Sebastian LAZIN
 */
public interface AuthorizationHeaderValues {

	/**
	 * Prefix for building the header with Bearer token.
	 */
	String BEARER = "Bearer";

	/**
	 * Builds the value for the authorization header for the request.
	 *
	 * @param accessToken access token
	 * @return the authorization header value
	 */
	static String bearerHeaderValue(final String accessToken) {
		return String.join(" ", BEARER, accessToken);
	}

}
