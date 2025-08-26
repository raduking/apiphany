package org.apiphany.security;

import java.io.Serial;

import org.apiphany.http.HttpException;
import org.apiphany.http.HttpStatus;

/**
 * Authentication exception.
 *
 * @author Radu Sebastian LAZIN
 */
public class AuthenticationException extends HttpException {

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
		super(HttpStatus.UNAUTHORIZED, message);
	}

	/**
	 * Constructor with an exception message. The HTTP status will default to {@link HttpStatus#UNAUTHORIZED}.
	 *
	 * @param cause the cause of the exception
	 */
	public AuthenticationException(final Throwable cause) {
		super(HttpStatus.UNAUTHORIZED, null, cause);
	}

	/**
	 * Constructor with exception message and cause. The HTTP status will default to {@link HttpStatus#UNAUTHORIZED}.
	 *
	 * @param message the exception message
	 * @param cause cause of the exception
	 */
	public AuthenticationException(final String message, final Throwable cause) {
		super(HttpStatus.UNAUTHORIZED, message, cause);
	}

	/**
	 * Constructor with HTTP status and exception message.
	 *
	 * @param httpStatus the HTTP status
	 * @param message the exception message
	 */
	public AuthenticationException(final HttpStatus httpStatus, final String message) {
		super(httpStatus, message);
	}

	/**
	 * Constructor with HTTP status, exception message and cause.
	 *
	 * @param httpStatus the HTTP status
	 * @param message the exception message
	 * @param cause cause of the exception
	 */
	public AuthenticationException(final HttpStatus httpStatus, final String message, final Throwable cause) {
		super(httpStatus, message, cause);
	}

	/**
	 * Constructor with HTTP status and exception message.
	 *
	 * @param httpStatus the HTTP status as an integer value
	 * @param message the exception message
	 */
	public AuthenticationException(final int httpStatus, final String message) {
		super(httpStatus, message);
	}

	/**
	 * Constructor with HTTP status, exception message and cause.
	 *
	 * @param httpStatus the HTTP status as an integer value
	 * @param message the exception message
	 * @param cause cause of the exception
	 */
	public AuthenticationException(final int httpStatus, final String message, final Throwable cause) {
		super(httpStatus, message, cause);
	}
}
