package org.apiphany.auth;

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
	NO_AUTHENTICATION,

	/**
	 * Session-based authentication, typically using cookies or session IDs.
	 */
	SESSION,

	/**
	 * SSL/TLS client certificate authentication.
	 */
	SSL_CERTIFICATE,

	/**
	 * Bearer token authentication, commonly used with OAuth 2.0.
	 */
	BEARER_TOKEN,

	/**
	 * OAuth 2.0 token-based authentication.
	 */
	OAUTH2_TOKEN,

	/**
	 * JSON Web Token (JWT) authentication.
	 */
	JWT_TOKEN
}
