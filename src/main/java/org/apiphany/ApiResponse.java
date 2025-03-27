package org.apiphany;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.collections.Lists;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
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
	 * Hidden constructor, use {@code ApiResponse.of} factory methods to construct response objects.
	 *
	 * @param body response body
	 * @param status response status
	 * @param headers response headers
	 * @param errorMessage response error message
	 * @param exception response exception
	 */
	private ApiResponse(final T body, final Status status, final Map<String, List<String>> headers,
			final String errorMessage, final Exception exception) {
		this.body = body;
		this.status = status;
		this.headers = headers;
		this.errorMessage = errorMessage;
		this.exception = exception;
	}

	/**
	 * Hidden constructor, use {@code ApiResponse.of} factory methods to construct response objects.
	 *
	 * @param exception response exception
	 * @param errorMessagePrefix response error message prefix
	 */
	private ApiResponse(final Exception e, final String errorMessagePrefix) {
		this(null, null, Collections.emptyMap(),
				Nullables.nonNullOrDefault(errorMessagePrefix, "") + e.getMessage(), e);
	}

	/**
	 * Returns the body as an input stream. The caller is responsible to close the input stream.
	 *
	 * @return the body as an input stream
	 */
	public InputStream inputStream() {
		if (hasNoBody()) {
			throw new IllegalStateException("Cannot transform null body to input stream.");
		}
		if (body instanceof InputStream inputStream) {
			return inputStream;
		}
		return new ByteArrayInputStream(body.toString().getBytes());
	}

	/**
	 * Returns the response body or the given default if the request wasn't 2xx successful.
	 *
	 * @param defaultBody default value to be returned
	 * @return response body
	 */
	public T orDefault(final T defaultBody) {
		return getBodyOrDefault(this, () -> defaultBody);
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
		return getBodyOrDefault(this, () -> Constructors.IgnoreAccess.newInstance(cls));
	}

	/**
	 * Returns the response body or the default value supplied.
	 *
	 * @param defaultSupplier default return value supplier
	 * @return an instance of the given class
	 */
	public T orDefault(final Supplier<T> defaultSupplier) {
		return getBodyOrDefault(this, defaultSupplier);
	}

	/**
	 * Returns the response body or the given default if the request wasn't 2xx successful or null otherwise.
	 *
	 * @return response body or null
	 */
	public T orNull() {
		return getBodyOrDefault(this, Nullables.supplyNull());
	}

	/**
	 * Returns a part / field from the response body by giving a function or the given default if the request wasn't 2xx
	 * successful.
	 *
	 * @param <U> return type
	 *
	 * @param bodyFunction function that sends the body as parameter
	 * @param defaultValue default value to be returned
	 * @return body part / field
	 */
	public <U> U fromOrDefault(final Function<T, U> bodyFunction, final U defaultValue) {
		return getFromBodyOrDefault(this, bodyFunction, defaultValue);
	}

	/**
	 * Returns a part / field from the response body by giving a function or the given default if the request wasn't 2xx
	 * successful.
	 *
	 * @param <U> return type
	 *
	 * @param bodyFunction function that sends the body as parameter
	 * @param defaultValue default value to be returned
	 * @return body part / field
	 */
	public <U> U fromOrDefault(final BiFunction<T, U, U> bodyFunction, final U defaultValue) {
		return getFromBodyOrDefault(this, bodyFunction, defaultValue);
	}

	/**
	 * Returns a list from the response body when the body is an array or the given default list if the request wasn't 2xx
	 * successful.
	 *
	 * @param <U> return list component type
	 *
	 * @param defaultList default list to be returned
	 * @return response body list
	 */
	public <U> List<U> asListOrDefault(final List<U> defaultList) {
		return getListBodyOrDefault(JavaObjects.cast(this), defaultList);
	}

	/**
	 * Returns a list from the response body when the body is an array, or an empty list if the request wasn't 2xx
	 * successful.
	 *
	 * @param <U> return list component type
	 *
	 * @return response body list
	 */
	public <U> List<U> asList() {
		return getListBodyOrDefault(JavaObjects.cast(this), Collections.emptyList());
	}

	/**
	 * Returns a list from the response body or the given default list if the request wasn't 2xx successful.
	 *
	 * @param <U> return list component type
	 *
	 * @param bodyListFunction function to be used on body to retrieve the wanted list
	 * @param defaultList default list to be returned
	 * @return response body list
	 */
	public <U> List<U> asListFromOrDefault(final Function<T, List<U>> bodyListFunction, final List<U> defaultList) {
		return getListFromBodyOrDefault(this, bodyListFunction, defaultList);
	}

	/**
	 * Returns a list from the response body or empty list if the request wasn't 2xx successful.
	 *
	 * @param <U> return list component type
	 *
	 * @param bodyListFunction function to be used on body to retrieve the wanted list
	 * @return response body list or empty list if body doesn't exist
	 */
	public <U> List<U> asListFromOrEmpty(final Function<T, List<U>> bodyListFunction) {
		return getListFromBodyOrDefault(this, bodyListFunction, Collections.emptyList());
	}

	/**
	 * Returns the first element from a list in the response body defined by the list function or default value if the
	 * request wasn't 2xx successful or the list is empty.
	 *
	 * @param <U> return list component type
	 *
	 * @param bodyListFunction function to be used on body to retrieve the wanted list
	 * @param defaultValue value returned when the request was unsuccessful or the list is empty
	 * @return Returns the first element from a list from the response body
	 */
	public <U> U firstFromOrDefault(final Function<T, List<U>> bodyListFunction, final U defaultValue) {
		return Lists.first(getListFromBodyOrDefault(this, bodyListFunction, Collections.emptyList()), defaultValue);
	}

	/**
	 * Returns the response body or the given default if the request wasn't 2xx successful.
	 *
	 * @param <U> return type
	 *
	 * @param response response object
	 * @param defaultBody default value to be returned
	 * @return response body
	 */
	public static <U> U getBodyOrDefault(final ApiResponse<U> response, final Supplier<U> defaultBody) {
		return response.isSuccessful() ? response.getBody() : defaultBody.get();
	}

	/**
	 * Returns a part / field from the response body by giving a function or the given default if the request wasn't 2xx
	 * successful.
	 *
	 * @param <T> response body type
	 * @param <U> return type
	 *
	 * @param response API response object
	 * @param bodyConverter function to apply on body
	 * @param defaultValue default value to be returned
	 * @return body part / field
	 */
	public static <T, U> U getFromBodyOrDefault(final ApiResponse<T> response, final Function<T, U> bodyConverter, final U defaultValue) {
		return response.isSuccessful() ? bodyConverter.apply(response.getBody()) : defaultValue;
	}

	/**
	 * Returns a part / field from the response body by giving a function or the given default if the request wasn't 2xx
	 * successful.
	 *
	 * @param <T> response body type
	 * @param <U> return type
	 *
	 * @param response API response object
	 * @param bodyConverter function to apply to body
	 * @param defaultValue default value to be returned
	 * @return body part / field
	 */
	public static <T, U> U getFromBodyOrDefault(final ApiResponse<T> response, final BiFunction<T, U, U> bodyConverter, final U defaultValue) {
		return response.isSuccessful() ? bodyConverter.apply(response.getBody(), defaultValue) : defaultValue;
	}

	/**
	 * Returns a list from the response body when the body is an array or the given default list if the request wasn't 2xx
	 * successful.
	 *
	 * @param <U> return list component type
	 *
	 * @param response response object
	 * @param defaultList default list to be returned
	 * @return response body list
	 */
	public static <U> List<U> getListBodyOrDefault(final ApiResponse<U[]> response, final List<U> defaultList) {
		return getFromBodyOrDefault(response, (Function<U[], List<U>>) Arrays::asList, defaultList);
	}

	/**
	 * Returns a list from the response body or the given default list if the request wasn't 2xx successful.
	 *
	 * @param <T> response body type
	 * @param <U> return type
	 *
	 * @param response response object
	 * @param listFunction function to be used on body to retrieve the wanted list
	 * @param defaultList default list to be returned
	 * @return response body list
	 */
	public static <T, U> List<U> getListFromBodyOrDefault(final ApiResponse<T> response,
			final Function<T, List<U>> listFunction, final List<U> defaultList) {
		return getFromBodyOrDefault(response, listFunction, defaultList);
	}

	/**
	 * Creates a new {@link ApiResponse} object.
	 *
	 * @param <T> body type
	 *
	 * @param body response body
	 * @param status response status
	 * @param headers headers
	 * @return API response object
	 */
	public static <T> ApiResponse<T> of(final T body, final Status status, final Map<String, List<String>> headers) {
		return new ApiResponse<>(body, status, headers, null, null);
	}

	/**
	 * Creates a new {@link ApiResponse} object.
	 *
	 * @param <T> body type
	 * @param <S> status type
	 *
	 * @param body response body
	 * @param statusCode status code
	 * @param statusCodeConverter function to convert status code to a {@link Status} object
	 * @return API response object
	 */
	public static <T, S extends Status> ApiResponse<T> of(final T body, final int statusCode, final IntFunction<S> statusCodeConverter) {
		return of(body, statusCodeConverter.apply(statusCode));
	}

	/**
	 * Creates a new {@link ApiResponse} object.
	 *
	 * @param <T> body type
	 *
	 * @param body response body
	 * @param status response status
	 * @return API response object
	 */
	public static <T> ApiResponse<T> of(final T body, final Status status) {
		return new ApiResponse<>(body, status, Collections.emptyMap(), null, null);
	}

	/**
	 * Creates a new {@link ApiResponse} object. This object will have the {@link #getStatus()} return BAD_REQUEST.
	 *
	 * @param <T> body type
	 *
	 * @param e exception
	 * @param errorMessagePrefix error message prefix to be appended to the exception message.
	 * @return API response object
	 */
	public static <T> ApiResponse<T> of(final Exception e, final String errorMessagePrefix) {
		return new ApiResponse<>(e, errorMessagePrefix);
	}

	/**
	 * Creates a new {@link ApiResponse} object. This object will have the {@link #getStatus()} return BAD_REQUEST.
	 *
	 * @param <T> body type
	 *
	 * @param e exception
	 * @return API response object
	 */
	public static <T> ApiResponse<T> of(final Exception e) {
		return of(e, (String) null);
	}

	/**
	 * Returns the error message or <code>"No error message."</code> if none is returned.
	 *
	 * @return the error message
	 */
	public String getErrorMessage() {
		return !StringUtils.isEmpty(errorMessage) ? errorMessage : "No error message.";
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
	 * @return true if the {@link #getStatus()} is 2xx successful, false otherwise.
	 */
	public boolean isSuccessful() {
		return null != status && status.isSuccess();
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

}
