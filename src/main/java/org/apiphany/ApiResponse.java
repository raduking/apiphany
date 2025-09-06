package org.apiphany;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.apiphany.client.ExchangeClient;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.lang.Unchecked;
import org.morphix.reflection.Constructors;

/**
 * Generic API response.
 *
 * @param <T> body type
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiResponse<T> extends ApiMessage<T> {

	/**
	 * Response status.
	 */
	private final Status status;

	/**
	 * Response error message.
	 */
	private final String errorMessage;

	/**
	 * Response exception.
	 */
	private final Exception exception;

	/**
	 * The exchange client that generated this response.
	 */
	private final ExchangeClient exchangeClient;

	/**
	 * Constructs a response based on the give builder.
	 *
	 * @param builder response builder
	 */
	private ApiResponse(final Builder<T> builder) {
		super(builder.body, builder.headers);
		this.status = builder.status;
		this.errorMessage = null != builder.errorMessage
				? Nullables.nonNullOrDefault(builder.errorMessagePrefix, "") + builder.errorMessage
				: null;
		this.exception = builder.exception;
		this.exchangeClient = builder.exchangeClient;
	}

	/**
	 * Returns the response body or the given default if the request wasn't 2xx successful.
	 *
	 * @param defaultBody default value to be returned
	 * @return response body
	 */
	public T orDefault(final T defaultBody) {
		return orDefault(() -> defaultBody);
	}

	/**
	 * Returns the response body or an instance of the given class by calling its default constructor. This is useful when
	 * you want to return a non <code>null</code> value from a client and usually all DTOs (return types from clients) have
	 * a default constructor.
	 * <p>
	 * We don't worry about performance here because this should
	 *
	 * @param cls class to create an instance of
	 * @return an instance of the given class
	 */
	public T orDefault(final Class<T> cls) {
		return orDefault(() -> Constructors.IgnoreAccess.newInstance(cls));
	}

	/**
	 * Returns the response body or the default value supplied.
	 *
	 * @param defaultSupplier default return value supplier
	 * @return an instance of the given class
	 */
	public T orDefault(final Supplier<T> defaultSupplier) {
		return isSuccessful() ? getBody() : defaultSupplier.get();
	}

	/**
	 * Returns the response body or null if the request wasn't 2xx successful.
	 *
	 * @return response body or null
	 */
	public T orNull() {
		return orDefault(Nullables.supplyNull());
	}

	/**
	 * Returns a part / field from the response body by giving a function or the given default if the request wasn't 2xx
	 * successful.
	 *
	 * @param <R> return type
	 *
	 * @param bodyFunction function that sends the body as parameter
	 * @param defaultValue default value to be returned
	 * @return body part / field
	 */
	public <R> R fromOrDefault(final Function<T, R> bodyFunction, final R defaultValue) {
		return isSuccessful() ? bodyFunction.apply(getBody()) : defaultValue;
	}

	/**
	 * Returns a part / field from the response body by giving a function or the given default if the request wasn't 2xx
	 * successful.
	 *
	 * @param <A> the argument type of the body function
	 * @param <R> return type
	 *
	 * @param bodyFunction function that sends the body as parameter
	 * @param arg the argument of the body function
	 * @param defaultValue default value to be returned
	 * @return body part / field
	 */
	public <A, R> R fromOrDefault(final BiFunction<T, A, R> bodyFunction, final A arg, final R defaultValue) {
		return isSuccessful() ? bodyFunction.apply(getBody(), arg) : defaultValue;
	}

	/**
	 * Returns a list from the response body when the body is an array or the given default list if the request wasn't 2xx
	 * successful.
	 *
	 * @param <E> return list component type
	 *
	 * @param defaultList default list to be returned
	 * @return response body list
	 */
	public <E> List<E> asListOrDefault(final List<E> defaultList) {
		return asListFromOrDefault((Function<E[], List<E>>) List::of, defaultList);
	}

	/**
	 * Returns a list from the response body when the body is an array, or an empty list if the request wasn't 2xx
	 * successful.
	 *
	 * @param <E> return list component type
	 *
	 * @return response body list
	 */
	public <E> List<E> asList() {
		return asListOrDefault(Collections.emptyList());
	}

	/**
	 * Returns a list from the response body or the given default list if the request wasn't 2xx successful.
	 *
	 * @param <U> the actual body type
	 * @param <E> return list component type
	 *
	 * @param bodyListFunction function to be used on the body to retrieve the wanted list
	 * @param defaultList default list to be returned
	 * @return response body list
	 */
	public <U, E> List<E> asListFromOrDefault(final Function<U, List<E>> bodyListFunction, final List<E> defaultList) {
		return isSuccessful() ? bodyListFunction.apply(JavaObjects.cast(getBody())) : defaultList;
	}

	/**
	 * Returns a list from the response body or empty list if the request wasn't 2xx successful.
	 *
	 * @param <U> the actual body type
	 * @param <E> return list component type
	 *
	 * @param bodyListFunction function to be used on the body to retrieve the wanted list
	 * @return response body list or empty list if the body doesn't exist
	 */
	public <U, E> List<E> asListFromOrEmpty(final Function<U, List<E>> bodyListFunction) {
		return asListFromOrDefault(bodyListFunction, Collections.emptyList());
	}

	/**
	 * Returns the first element from a list in the response body defined by the list function or default value if the
	 * request wasn't 2xx successful or the list is empty.
	 *
	 * @param <U> the actual body type
	 * @param <E> return list component type
	 *
	 * @param bodyListFunction function to be used on the body to retrieve the wanted list
	 * @param defaultValue value returned when the request was unsuccessful or the list is empty
	 * @return Returns the first element from a list from the response body
	 */
	public <U, E> E firstFromOrDefault(final Function<U, List<E>> bodyListFunction, final E defaultValue) {
		return Lists.first(asListFromOrDefault(bodyListFunction, Collections.emptyList()), defaultValue);
	}

	/**
	 * Returns the response body or throws the given {@link Throwable} if the request wasn't 2xx successful.
	 *
	 * @param t throwable to throw
	 * @return response body or throws the given throwable
	 */
	public T orThrow(final Throwable t) {
		return isSuccessful() ? getBody() : Unchecked.reThrow(t);
	}

	/**
	 * Re-throws the exception in the response or {@code null} if the response has no exception.
	 *
	 * @return null if the response has no errors or re-throws the exception
	 */
	public T orRethrow() {
		return hasException() ? orThrow(getException()) : getBody();
	}

	/**
	 * Re-throws the exception in the response or {@code null} if the response has no exception.
	 *
	 * @param exceptionWrappingFunction function to wrap the existing exception into a new one
	 * @return null if the response has no errors or re-throws the exception
	 */
	public T orRethrow(final Function<? super Throwable, Throwable> exceptionWrappingFunction) {
		return orThrow(exceptionWrappingFunction.apply(getException()));
	}

	/**
	 * Handles the error in the response.
	 *
	 * @param onError error handling function which takes the exception as a parameter
	 * @return error handling function result
	 */
	public T orHandleError(final Function<? super Exception, T> onError) {
		return onError.apply(getException());
	}

	/**
	 * Returns the body as an input stream. The caller is responsible for closing the input stream.
	 *
	 * @return the body as an input stream
	 */
	public InputStream inputStream() {
		return inputStream(ApiResponse::defaultInputStreamConverter);
	}

	/**
	 * Returns the body as an input stream. The caller must provide a conversion from the response type to
	 * {@link InputStream} and for closing the input stream.
	 *
	 * @param inputStreamConverter input stream converter
	 * @return the body as an input stream
	 */
	public InputStream inputStream(final Function<T, InputStream> inputStreamConverter) {
		if (hasNoBody()) {
			throw new IllegalStateException("Cannot transform null body to input stream.");
		}
		return inputStreamConverter.apply(getBody());
	}

	/**
	 * The default body to {@link InputStream} converter.
	 *
	 * @param body body to convert
	 * @return the input stream
	 */
	private static <T> InputStream defaultInputStreamConverter(final T body) {
		if (body instanceof InputStream is) {
			return is;
		}
		if (body instanceof byte[] bytes) {
			return new ByteArrayInputStream(bytes);
		}
		if (body instanceof CharSequence cs) {
			return new ByteArrayInputStream(cs.toString().getBytes());
		}
		// Fallback to toString()
		return new ByteArrayInputStream(Strings.safe(body.toString()).getBytes());
	}

	/**
	 * Returns the error message or <code>"No error message."</code> if none is returned.
	 *
	 * @return the error message
	 */
	public String getErrorMessage() {
		return !Strings.isEmpty(errorMessage) ? errorMessage : "No error message.";
	}

	/**
	 * Returns the status code.
	 *
	 * @return the status code
	 */
	public int getStatusCode() {
		return null != status ? status.getCode() : Status.UNKNOWN;
	}

	/**
	 * Returns the status.
	 *
	 * @param <S> status type
	 *
	 * @return the status
	 */
	public <S extends Status> S getStatus() {
		return JavaObjects.cast(status);
	}

	/**
	 * Returns the exception in case of an error.
	 *
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Returns true if the response has an exception, false otherwise.
	 *
	 * @return true if the response has an exception, false otherwise
	 */
	public boolean hasException() {
		return null != getException();
	}

	/**
	 * Returns true if the {@link #getStatus()} is successful, false otherwise. It is a shortcut method which also validates
	 * against null value for status code.
	 *
	 * @return true if the {@link #getStatus()} is successful, false otherwise.
	 */
	public boolean isSuccessful() {
		return null != status && status.isSuccess();
	}

	/**
	 * @see #getHeadersAsString()
	 */
	@Override
	public String getHeadersAsString() {
		if (null != exchangeClient) {
			return exchangeClient.getHeadersAsString(this);
		}
		return super.getHeadersAsString();
	}

	/**
	 * Returns the API response builder.
	 *
	 * @param <T> response body type
	 * @return an API request builder
	 */
	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	/**
	 * Returns the API response builder.
	 *
	 * @param <T> response body type
	 *
	 * @param body response body
	 * @return an API request builder
	 */
	public static <T> Builder<T> create(final T body) {
		return ApiResponse.<T>builder().body(body);
	}

	/**
	 * Builder for API response objects.
	 *
	 * @param <T> response type
	 * @author Radu Sebastian LAZIN
	 */
	public static class Builder<T> {

		/**
		 * The response body content.
		 */
		private T body;

		/**
		 * The response headers, initialized as an empty map.
		 */
		private Map<String, List<String>> headers = Collections.emptyMap();

		/**
		 * The response status.
		 */
		private Status status;

		/**
		 * The error message for error responses.
		 */
		private String errorMessage;

		/**
		 * The prefix for error messages.
		 */
		private String errorMessagePrefix;

		/**
		 * The exception associated with the response.
		 */
		private Exception exception;

		/**
		 * The exchange client associated with the response.
		 */
		private ExchangeClient exchangeClient;

		/**
		 * Private constructor to enforce builder pattern usage.
		 */
		private Builder() {
			// empty
		}

		/**
		 * Sets the response body.
		 *
		 * @param body the response body
		 * @return this builder instance
		 */
		public Builder<T> body(final T body) {
			this.body = body;
			return this;
		}

		/**
		 * Sets the response headers.
		 *
		 * @param headers the response headers (cannot be null)
		 * @return this builder instance
		 * @throws NullPointerException if headers map is null
		 */
		public Builder<T> headers(final Map<String, List<String>> headers) {
			this.headers = Objects.requireNonNull(headers, "headers cannot be null");
			return this;
		}

		/**
		 * Sets the response status.
		 *
		 * @param status the response status
		 * @return this builder instance
		 */
		public Builder<T> status(final Status status) {
			this.status = status;
			return this;
		}

		/**
		 * Sets the response status using a status code converter.
		 *
		 * @param status the status code
		 * @param statusCodeConverter function to convert status code to Status
		 * @param <S> the Status type
		 * @return this builder instance
		 */
		public <S extends Status> Builder<T> status(final int status, final IntFunction<S> statusCodeConverter) {
			return status(statusCodeConverter.apply(status));
		}

		/**
		 * Sets the error message.
		 *
		 * @param errorMessage the error message
		 * @return this builder instance
		 */
		public Builder<T> errorMessage(final String errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		/**
		 * Sets the error message prefix.
		 *
		 * @param errorMessagePrefix the error message prefix
		 * @return this builder instance
		 */
		public Builder<T> errorMessagePrefix(final String errorMessagePrefix) {
			this.errorMessagePrefix = errorMessagePrefix;
			return this;
		}

		/**
		 * Sets the exception associated with this response.
		 *
		 * @param exception the exception
		 * @return this builder instance
		 */
		public Builder<T> exception(final Exception exception) {
			this.exception = exception;
			return errorMessage(exception.getMessage());
		}

		/**
		 * Sets the exchange client associated with this response.
		 *
		 * @param exchangeClient the exchange client
		 * @return this builder instance
		 */
		public Builder<T> exchangeClient(final ExchangeClient exchangeClient) {
			this.exchangeClient = exchangeClient;
			return this;
		}

		/**
		 * Builds the API response using the configured values.
		 *
		 * @return the constructed API response
		 */
		public ApiResponse<T> build() {
			return new ApiResponse<>(this);
		}
	}
}
