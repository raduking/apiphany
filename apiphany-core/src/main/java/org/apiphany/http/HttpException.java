package org.apiphany.http;

import java.io.Serial;

import org.apiphany.Status;
import org.morphix.lang.function.ThrowingSupplier;

/**
 * Represents a basic HTTP exception, typically used to indicate an error during an HTTP request or response. This
 * exception includes an {@link HttpStatus} to provide detailed information about the HTTP error.
 * <p>
 * When building HTTP exceptions prefer using the {@link Builder} for better readability and maintainability.
 * <p>
 * The exception message is constructed based on the provided message, response body, and cause, following a specific
 * order of precedence to ensure meaningful error information is conveyed, see
 * {@link #exceptionMessage(String, String, Throwable)} for details.
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
	 * The response body associated with this exception, if available.
	 */
	private final String responseBody;

	/**
	 * Constructs a new {@link HttpException} with the specified HTTP status and message.
	 *
	 * @param status the HTTP status associated with this exception.
	 * @param message the detail message explaining the exception.
	 */
	public HttpException(final HttpStatus status, final String message) {
		this(status, message, null, null);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified HTTP status and message.
	 *
	 * @param status the HTTP status associated with this exception.
	 * @param message the detail message explaining the exception.
	 * @param responseBody the response body associated with this exception, if available
	 */
	public HttpException(final HttpStatus status, final String message, final String responseBody) {
		this(status, message, responseBody, null);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified status code and message.
	 *
	 * @param statusCode the HTTP status code associated with this exception.
	 * @param message the detail message explaining the exception.
	 */
	public HttpException(final int statusCode, final String message) {
		this(HttpStatus.fromCode(statusCode), message, null, null);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified status code and message.
	 *
	 * @param status the HTTP status associated with this exception.
	 * @param message the detail message explaining the exception.
	 * @param cause the cause of the exception (can be null).
	 */
	public HttpException(final HttpStatus status, final String message, final Throwable cause) {
		this(status, message, null, cause);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified status code, message, and cause.
	 *
	 * @param statusCode the HTTP status code associated with this exception.
	 * @param message the detail message explaining the exception.
	 * @param cause the cause of the exception (can be null).
	 */
	public HttpException(final int statusCode, final String message, final Throwable cause) {
		this(HttpStatus.fromCode(statusCode), message, null, cause);
	}

	/**
	 * Constructs a new {@link HttpException} with the specified HTTP status, message, and cause.
	 *
	 * @param status the HTTP status associated with this exception.
	 * @param message the detail message explaining the exception.
	 * @param responseBody the response body associated with this exception, if available
	 * @param cause the cause of the exception (can be null).
	 */
	public HttpException(final HttpStatus status, final String message, final String responseBody, final Throwable cause) {
		super(message(status, exceptionMessage(message, responseBody, cause)), cause);
		this.status = status;
		this.responseBody = responseBody;
	}

	/**
	 * Returns a new Builder for constructing an instance of {@link HttpException}.
	 *
	 * @return a new Builder for {@link HttpException}.
	 */
	public static Builder builder() {
		return new Builder();
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
	 * Returns the response body associated with this exception, if available.
	 *
	 * @return the response body, or null if not available.
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * Returns the value supplied by the supplier if no exception is thrown, otherwise it wraps the throwable thrown by the
	 * supplier into a {@link HttpException}. If the exception is already an instance of {@link HttpException}, it is
	 * re-thrown without wrapping. The exception will have the status set to the provided {@code httpStatus}.
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
		} catch (HttpException e) {
			throw e;
		} catch (Throwable e) {
			throw HttpException.builder().status(httpStatus).cause(e).build();
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
		return Status.message(status);
	}

	/**
	 * Return the exception message string based on the HTTP status and the message.
	 *
	 * @param status HTTP status
	 * @param message the human-readable message
	 * @return the status message string
	 */
	public static String message(final HttpStatus status, final String message) {
		return Status.message(status, message);
	}

	/**
	 * Return the exception message string based on the given message, response body, and cause. The message is determined
	 * in the following order of precedence:
	 * <ul>
	 * <li>If the provided message is not null, it is returned.</li>
	 * <li>if the cause is not null, the cause's message is returned.</li>
	 * <li>if both the message and cause are null, the response body is returned.</li>
	 * </ul>
	 *
	 * @param message the human-readable message
	 * @param responseBody the response body associated with this exception, if available
	 * @param cause the cause of the exception (can be null)
	 * @return the exception message string
	 */
	public static String exceptionMessage(final String message, final String responseBody, final Throwable cause) {
		if (null != message) {
			return message;
		}
		if (null != cause) {
			return cause.getMessage();
		}
		return responseBody;
	}

	/**
	 * Builder for {@link HttpException}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Builder {

		/**
		 * The HTTP status associated with the exception being built.
		 */
		private HttpStatus status;

		/**
		 * The human-readable message associated with the exception being built.
		 */
		private String message;

		/**
		 * The response body associated with the exception being built, if available.
		 */
		private String responseBody;

		/**
		 * The cause of the exception being built, if available.
		 */
		private Throwable cause;

		/**
		 * Constructs a new Builder for {@link HttpException}.
		 */
		private Builder() {
			// empty
		}

		/**
		 * Sets the HTTP status for the exception being built.
		 *
		 * @param status the HTTP status to set
		 * @return this Builder instance for method chaining
		 */
		public Builder status(final HttpStatus status) {
			this.status = status;
			return this;
		}

		/**
		 * Sets the HTTP status code for the exception being built.
		 *
		 * @param statusCode the HTTP status code to set
		 * @return this Builder instance for method chaining
		 */
		public Builder status(final int statusCode) {
			return status(HttpStatus.fromCode(statusCode));
		}

		/**
		 * Sets the human-readable message for the exception being built.
		 *
		 * @param message the message to set
		 * @return this Builder instance for method chaining
		 */
		public Builder message(final String message) {
			this.message = message;
			return this;
		}

		/**
		 * Sets the response body for the exception being built, if available.
		 *
		 * @param responseBody the response body to set
		 * @return this Builder instance for method chaining
		 */
		public Builder responseBody(final String responseBody) {
			this.responseBody = responseBody;
			return this;
		}

		/**
		 * Sets the cause for the exception being built, if available.
		 *
		 * @param cause the cause to set
		 * @return this Builder instance for method chaining
		 */
		public Builder cause(final Throwable cause) {
			this.cause = cause;
			return this;
		}

		/**
		 * Builds and returns a new {@link HttpException} instance based on the properties set in this Builder.
		 *
		 * @return a new HttpException instance
		 */
		public HttpException build() {
			return new HttpException(status, message, responseBody, cause);
		}
	}
}
