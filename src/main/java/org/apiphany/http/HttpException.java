package org.apiphany.http;

import java.io.Serial;

/**
 * Represents a basic HTTP exception, typically used to indicate an error during an HTTP request or response. This
 * exception includes an {@link HttpStatus} to provide detailed information about the HTTP error.
 *
 * @author Radu Sebastian LAZIN
 */
public class HttpException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	@Serial
	private static final long serialVersionUID = 3351854568871437596L;

	/**
	 * The HTTP status associated with this exception.
	 */
	private final HttpStatus status;

	/**
	 * Constructs a new {@link HttpException} with the specified HTTP status and message.
	 *
	 * @param status the HTTP status associated with this exception.
	 * @param message the detail message explaining the exception.
	 */
	public HttpException(final HttpStatus status, final String message) {
		this(status, message, null);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified status code and message.
	 *
	 * @param statusCode the HTTP status code associated with this exception.
	 * @param message the detail message explaining the exception.
	 */
	public HttpException(final int statusCode, final String message) {
		this(HttpStatus.from(statusCode), message, null);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified status code, message, and cause.
	 *
	 * @param statusCode the HTTP status code associated with this exception.
	 * @param message the detail message explaining the exception.
	 * @param cause the cause of the exception (can be null).
	 */
	public HttpException(final int statusCode, final String message, final Throwable cause) {
		this(HttpStatus.from(statusCode), message, cause);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified HTTP status, message, and cause.
	 *
	 * @param status the HTTP status associated with this exception.
	 * @param message the detail message explaining the exception.
	 * @param cause the cause of the exception (can be null).
	 */
	public HttpException(final HttpStatus status, final String message, final Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	/**
	 * Returns the HTTP status associated with this exception.
	 *
	 * @return the HTTP status.
	 */
	public HttpStatus getStatus() {
		return status;
	}

	/**
	 * Returns the HTTP status code associated with this exception.
	 *
	 * @return the HTTP status code.
	 */
	public int getStatusCode() {
		return getStatus().value();
	}
}
