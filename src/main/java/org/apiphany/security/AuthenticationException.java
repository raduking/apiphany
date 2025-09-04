package org.apiphany.security;

import java.io.Serial;

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
	 * Constructor with an exception message.
	 *
	 * @param message the exception message
	 */
	public AuthenticationException(final String message) {
		super(message);
	}

	/**
	 * Constructor with an exception message.
	 *
	 * @param cause the cause of the exception
	 */
	public AuthenticationException(final Throwable cause) {
		super(null, cause);
	}

	/**
	 * Constructor with exception message and cause.
	 *
	 * @param message the exception message
	 * @param cause cause of the exception
	 */
	public AuthenticationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
