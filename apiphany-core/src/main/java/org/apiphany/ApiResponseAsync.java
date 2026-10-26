package org.apiphany;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Asynchronous counterpart of {@link ApiResponse} which exposes the most common terminal operations without blocking
 * the calling thread.
 * <p>
 * This is a {@link CompletableFuture} of an {@link ApiResponse}, so it can be composed with any {@link CompletionStage}
 * operation, but it also allows extracting the response body directly:
 *
 * <pre>
 * CompletableFuture&lt;Info&gt; info = client()
 * 		.get()
 * 		.path("api", "v1", "info")
 * 		.retrieveAsync(Info.class)
 * 		.orDefault(Info::new);
 * </pre>
 *
 * @param <T> body type
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiResponseAsync<T> extends CompletableFuture<ApiResponse<T>> {

	/**
	 * Constructs a new incomplete asynchronous API response.
	 */
	public ApiResponseAsync() {
		// empty
	}

	/**
	 * Returns a future for the response body, completing with the given default if the request wasn't successful.
	 *
	 * @param defaultBody default value to be returned
	 * @return a future for the response body
	 * @see ApiResponse#orDefault(Object)
	 */
	public CompletableFuture<T> orDefault(final T defaultBody) {
		return thenApply(apiResponse -> apiResponse.orDefault(defaultBody));
	}

	/**
	 * Returns a future for the response body, completing with a new instance of the given class if the request wasn't
	 * successful.
	 *
	 * @param cls class to create an instance of
	 * @return a future for the response body
	 * @see ApiResponse#orDefault(Class)
	 */
	public CompletableFuture<T> orDefault(final Class<T> cls) {
		return thenApply(apiResponse -> apiResponse.orDefault(cls));
	}

	/**
	 * Returns a future for the response body, completing with the supplied default if the request wasn't successful.
	 *
	 * @param defaultSupplier default return value supplier
	 * @return a future for the response body
	 * @see ApiResponse#orGet(Supplier)
	 */
	public CompletableFuture<T> orGet(final Supplier<T> defaultSupplier) {
		return thenApply(apiResponse -> apiResponse.orGet(defaultSupplier));
	}

	/**
	 * Returns a future for the response body, completing with {@code null} if the request wasn't successful.
	 *
	 * @return a future for the response body or null
	 * @see ApiResponse#orNull()
	 */
	public CompletableFuture<T> orNull() {
		return thenApply(ApiResponse::orNull);
	}

	/**
	 * Returns a future for a part / field from the response body, or the given default if the request wasn't successful.
	 *
	 * @param <R> return type
	 *
	 * @param bodyFunction function that sends the body as parameter
	 * @param defaultValue default value to be returned
	 * @return a future for the body part / field
	 * @see ApiResponse#fromOrDefault(Function, Object)
	 */
	public <R> CompletableFuture<R> fromOrDefault(final Function<T, R> bodyFunction, final R defaultValue) {
		return thenApply(apiResponse -> apiResponse.fromOrDefault(bodyFunction, defaultValue));
	}

	/**
	 * Returns a future for a part / field from the response body, or the given default if the request wasn't successful.
	 *
	 * @param <A> the argument type of the body function
	 * @param <R> return type
	 *
	 * @param bodyFunction function that sends the body as parameter
	 * @param arg the argument of the body function
	 * @param defaultValue default value to be returned
	 * @return a future for the body part / field
	 * @see ApiResponse#fromOrDefault(BiFunction, Object, Object)
	 */
	public <A, R> CompletableFuture<R> fromOrDefault(final BiFunction<T, A, R> bodyFunction, final A arg,
			final R defaultValue) {
		return thenApply(apiResponse -> apiResponse.fromOrDefault(bodyFunction, arg, defaultValue));
	}

	/**
	 * Returns a future for a list from the response body when the body is an array, or the given default list if the
	 * request wasn't successful.
	 *
	 * @param <E> return list component type
	 *
	 * @param defaultList default list to be returned
	 * @return a future for the response body list
	 * @see ApiResponse#asListOrDefault(List)
	 */
	public <E> CompletableFuture<List<E>> asListOrDefault(final List<E> defaultList) {
		return thenApply(apiResponse -> apiResponse.asListOrDefault(defaultList));
	}

	/**
	 * Returns a future for a list from the response body when the body is an array, or an empty list if the request wasn't
	 * successful.
	 *
	 * @param <E> return list component type
	 *
	 * @return a future for the response body list
	 * @see ApiResponse#asList()
	 */
	public <E> CompletableFuture<List<E>> asList() {
		return thenApply(ApiResponse::asList);
	}

	/**
	 * Returns a future for a list from the response body, or the given default list if the request wasn't successful.
	 *
	 * @param <U> the actual body type
	 * @param <E> return list component type
	 *
	 * @param bodyListFunction function to be used on the body to retrieve the wanted list
	 * @param defaultList default list to be returned
	 * @return a future for the response body list
	 * @see ApiResponse#asListFromOrDefault(Function, List)
	 */
	public <U, E> CompletableFuture<List<E>> asListFromOrDefault(final Function<U, List<E>> bodyListFunction,
			final List<E> defaultList) {
		return thenApply(apiResponse -> apiResponse.asListFromOrDefault(bodyListFunction, defaultList));
	}

	/**
	 * Returns a future for a list from the response body, or an empty list if the request wasn't successful.
	 *
	 * @param <U> the actual body type
	 * @param <E> return list component type
	 *
	 * @param bodyListFunction function to be used on the body to retrieve the wanted list
	 * @return a future for the response body list
	 * @see ApiResponse#asListFromOrEmpty(Function)
	 */
	public <U, E> CompletableFuture<List<E>> asListFromOrEmpty(final Function<U, List<E>> bodyListFunction) {
		return thenApply(apiResponse -> apiResponse.asListFromOrEmpty(bodyListFunction));
	}

	/**
	 * Returns a future for the first element from a list in the response body, or the default value if the request wasn't
	 * successful or the list is empty.
	 *
	 * @param <U> the actual body type
	 * @param <E> return list component type
	 *
	 * @param bodyListFunction function to be used on the body to retrieve the wanted list
	 * @param defaultValue value returned when the request was unsuccessful or the list is empty
	 * @return a future for the first list element
	 * @see ApiResponse#firstFromOrDefault(Function, Object)
	 */
	public <U, E> CompletableFuture<E> firstFromOrDefault(final Function<U, List<E>> bodyListFunction,
			final E defaultValue) {
		return thenApply(apiResponse -> apiResponse.firstFromOrDefault(bodyListFunction, defaultValue));
	}

	/**
	 * Returns a future for the response body, completing exceptionally with the given throwable if the request wasn't
	 * successful.
	 *
	 * @param t throwable to throw
	 * @return a future for the response body
	 * @see ApiResponse#orThrow(Throwable)
	 */
	public CompletableFuture<T> orThrow(final Throwable t) {
		return thenApply(apiResponse -> apiResponse.orThrow(t));
	}

	/**
	 * Returns a future for the response body, re-throwing the exception in the response if the request wasn't successful.
	 *
	 * @return a future for the response body
	 * @see ApiResponse#orRethrow()
	 */
	public CompletableFuture<T> orRethrow() {
		return thenApply(ApiResponse::orRethrow);
	}

	/**
	 * Returns a future for the response body, re-throwing a wrapped exception if the request wasn't successful.
	 *
	 * @param exceptionWrappingFunction function to wrap the existing exception into a new one
	 * @return a future for the response body
	 * @see ApiResponse#orRethrow(Function)
	 */
	public CompletableFuture<T> orRethrow(final Function<? super Throwable, Throwable> exceptionWrappingFunction) {
		return thenApply(apiResponse -> apiResponse.orRethrow(exceptionWrappingFunction));
	}

	/**
	 * Returns a future for the response body, applying the error handler when the response has an exception.
	 *
	 * @param onError error handling function which takes the exception as a parameter
	 * @return a future for the response body or the error handling result
	 * @see ApiResponse#orHandleError(Function)
	 */
	public CompletableFuture<T> orHandleError(final Function<? super Exception, T> onError) {
		return thenApply(apiResponse -> apiResponse.orHandleError(onError));
	}

	/**
	 * Returns a future for the mapped result if the request was successful, or the given default value.
	 *
	 * @param <U> return type
	 *
	 * @param responseMapper function to map the response
	 * @param defaultValue default value to be returned
	 * @return a future for the mapped result or default value
	 * @see ApiResponse#mapResultOrDefault(Function, Object)
	 */
	public <U> CompletableFuture<U> mapResultOrDefault(final Function<ApiResponse<T>, U> responseMapper,
			final U defaultValue) {
		return thenApply(apiResponse -> apiResponse.mapResultOrDefault(responseMapper, defaultValue));
	}

	/**
	 * Returns a future for the mapped result if the request was successful, or the supplied default value.
	 *
	 * @param <U> return type
	 *
	 * @param responseMapper function to map the response
	 * @param defaultValueSupplier supplier for the default value to be returned
	 * @return a future for the mapped result or default value
	 * @see ApiResponse#mapResultOrGet(Function, Supplier)
	 */
	public <U> CompletableFuture<U> mapResultOrGet(final Function<ApiResponse<T>, U> responseMapper,
			final Supplier<U> defaultValueSupplier) {
		return thenApply(apiResponse -> apiResponse.mapResultOrGet(responseMapper, defaultValueSupplier));
	}

	/**
	 * Returns a future for the mapped result if the request was successful, or {@code null}.
	 *
	 * @param <U> return type
	 *
	 * @param responseMapper function to map the response
	 * @return a future for the mapped result or null
	 * @see ApiResponse#mapResultOrNull(Function)
	 */
	public <U> CompletableFuture<U> mapResultOrNull(final Function<ApiResponse<T>, U> responseMapper) {
		return thenApply(apiResponse -> apiResponse.mapResultOrNull(responseMapper));
	}

	/**
	 * Runs the given action when the response has a success status, then completes with the same response.
	 * <p>
	 * The callback runs when the underlying {@link ApiResponse} is available; it is not registered as a listener for future
	 * events.
	 *
	 * @param action action to run with this response
	 * @return a future for the response, for chaining
	 * @see ApiResponse#onSuccess(Consumer)
	 */
	public CompletableFuture<ApiResponse<T>> onSuccess(final Consumer<ApiResponse<T>> action) {
		return thenApply(apiResponse -> apiResponse.onSuccess(action));
	}

	/**
	 * Runs the given action when the response has an error status, then completes with the same response.
	 * <p>
	 * The callback runs when the underlying {@link ApiResponse} is available; it is not registered as a listener for future
	 * events.
	 *
	 * @param action action to run with this response
	 * @return a future for the response, for chaining
	 * @see ApiResponse#onFailure(Consumer)
	 */
	public CompletableFuture<ApiResponse<T>> onFailure(final Consumer<ApiResponse<T>> action) {
		return thenApply(apiResponse -> apiResponse.onFailure(action));
	}

	/**
	 * Runs the given action when the response has an exception, then completes with the same response.
	 * <p>
	 * The callback runs when the underlying {@link ApiResponse} is available; it is not registered as a listener for future
	 * events.
	 *
	 * @param action action to run with the response exception
	 * @return a future for the response, for chaining
	 * @see ApiResponse#onException(Consumer)
	 */
	public CompletableFuture<ApiResponse<T>> onException(final Consumer<Exception> action) {
		return thenApply(apiResponse -> apiResponse.onException(action));
	}

	/**
	 * Returns a future for the body as an input stream. The caller is responsible for closing the input stream.
	 *
	 * @return a future for the body as an input stream
	 * @see ApiResponse#inputStream()
	 */
	public CompletableFuture<InputStream> inputStream() {
		return thenApply(ApiResponse::inputStream);
	}

	/**
	 * Returns a future for the body as an input stream using the given converter. The caller is responsible for closing the
	 * input stream.
	 *
	 * @param inputStreamConverter input stream converter
	 * @return a future for the body as an input stream
	 * @see ApiResponse#inputStream(Function)
	 */
	public CompletableFuture<InputStream> inputStream(final Function<T, InputStream> inputStreamConverter) {
		return thenApply(apiResponse -> apiResponse.inputStream(inputStreamConverter));
	}
}
