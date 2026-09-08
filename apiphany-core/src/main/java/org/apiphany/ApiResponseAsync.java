package org.apiphany;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
	 * Returns a future for the response body, completing with {@code null} if the request wasn't 2xx successful.
	 *
	 * @return a future for the response body or null
	 * @see ApiResponse#orNull()
	 */
	public CompletableFuture<T> orNull() {
		return thenApply(ApiResponse::orNull);
	}

	/**
	 * Returns a future for the response body, completing with the given default if the request wasn't 2xx successful.
	 *
	 * @param defaultBody default value to be returned
	 * @return a future for the response body
	 * @see ApiResponse#orDefault(Object)
	 */
	public CompletableFuture<T> orDefault(final T defaultBody) {
		return thenApply(apiResponse -> apiResponse.orDefault(defaultBody));
	}

	/**
	 * Returns a future for the response body, completing with a new instance of the given class if the request wasn't 2xx
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
	 * Returns a future for the response body, completing with the supplied default if the request wasn't 2xx successful.
	 *
	 * @param defaultSupplier default return value supplier
	 * @return a future for the response body
	 * @see ApiResponse#orDefault(Supplier)
	 */
	public CompletableFuture<T> orDefault(final Supplier<T> defaultSupplier) {
		return thenApply(apiResponse -> apiResponse.orDefault(defaultSupplier));
	}
}
