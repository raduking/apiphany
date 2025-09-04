package org.apiphany.security;

import java.io.Serial;

import org.apiphany.http.HttpStatus;

/**
 * Authentication exception.
 *
 * @author Radu Sebastian LAZIN
 */
public class AuthenticationException extends SecurityException {

	/**
	 * Serial version UID.
	 */
	@Serial
	private static final long serialVersionUID = 591403103621076702L;

	/**
	 * Constructor with an exception message. The HTTP status will default to {@link HttpStatus#UNAUTHORIZED}.
	 *
	 * @param message the exception message
	 */
	public AuthenticationException(final String message) {
		super(message);
	}

	/**
	 * Constructor with an exception message. The HTTP status will default to {@link HttpStatus#UNAUTHORIZED}.
	 *
	 * @param cause the cause of the exception
	 */
	public AuthenticationException(final Throwable cause) {
		super(null, cause);
	}

	/**
	 * Constructor with exception message and cause. The HTTP status will default to {@link HttpStatus#UNAUTHORIZED}.
	 *
	 * @param message the exception message
	 * @param cause cause of the exception
	 */
	public AuthenticationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
