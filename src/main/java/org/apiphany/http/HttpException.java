package org.apiphany.http;

import java.io.Serial;

import org.morphix.lang.function.ThrowingSupplier;

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
		this(HttpStatus.fromCode(statusCode), message, null);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified status code, message, and cause.
	 *
	 * @param statusCode the HTTP status code associated with this exception.
	 * @param message the detail message explaining the exception.
	 * @param cause the cause of the exception (can be null).
	 */
	public HttpException(final int statusCode, final String message, final Throwable cause) {
		this(HttpStatus.fromCode(statusCode), message, cause);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified HTTP status, message, and cause.
	 *
	 * @param status the HTTP status associated with this exception.
	 * @param message the detail message explaining the exception.
	 * @param cause the cause of the exception (can be null).
	 */
	public HttpException(final HttpStatus status, final String message, final Throwable cause) {
		super(message(status, message), cause);
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

	/**
	 * Returns the value supplied by the supplier if no exception is thrown, otherwise it wraps the throwable thrown by the
	 * supplier into a {@link HttpException}.
	 *
	 * @param <T> return type
	 *
	 * @param throwingSupplier supplier
	 * @param httpStatus HTTP status for the HTTP exception
	 * @return the value supplied
	 */
	public static <T> T ifThrows(final ThrowingSupplier<T> throwingSupplier, final HttpStatus httpStatus) {
		try {
			return throwingSupplier.get();
		} catch (Throwable e) {
			throw new HttpException(httpStatus, e.getMessage(), e);
		}
	}

	/**
	 * Returns the value supplied by the supplier if no exception is thrown, otherwise it wraps the throwable thrown by the
	 * supplier into a {@link HttpException}. The exception will have the status set to
	 * {@link HttpStatus#INTERNAL_SERVER_ERROR}.
	 *
	 * @param <T> return type
	 *
	 * @param throwingSupplier supplier
	 * @return the value supplied
	 */
	public static <T> T ifThrows(final ThrowingSupplier<T> throwingSupplier) {
		return ifThrows(throwingSupplier, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Return the status message string.
	 *
	 * @param status HTTP status
	 * @return the status message string
	 */
	public static String message(final HttpStatus status) {
		return "[" + status.value() + " " + status.message() + "]";
	}

	/**
	 * Return the exception message string based on the HTTP status and the message.
	 *
	 * @param status HTTP status
	 * @param message the human readable message
	 * @return the status message string
	 */
	public static String message(final HttpStatus status, final String message) {
		return null != message
				? String.join(" ", message(status), message)
				: message(status);
	}
}
