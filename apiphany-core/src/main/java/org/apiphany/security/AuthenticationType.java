package org.apiphany.security;

/**
 * Represents the type of authentication used in the application. This enum defines various authentication mechanisms
 * supported by the system.
 *
 * @author Radu Sebastian LAZIN
 */
public enum AuthenticationType {

	/**
	 * No authentication is required.
	 */
	NONE,

	/**
	 * Session-based authentication, typically using cookies or session IDs.
	 */
	SESSION,

	/**
	 * SSL/TLS client certificate authentication.
	 */
	SSL,

	/**
	 * Token authentication.
	 */
	TOKEN,

	/**
	 * OAuth 2.0 token-based authentication.
	 */
	OAUTH2,

	/**
	 * JSON Web Token (JWT) authentication.
	 */
	JWT,

	/**
	 * API Key-based authentication.
	 */
	API_KEY
}
