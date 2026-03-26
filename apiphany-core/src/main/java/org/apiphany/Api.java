package org.apiphany;

import org.apiphany.client.ClientLifecycle;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.HttpClientFluentAdapter;

/**
 * API interface for the Apiphany library. This interface provides static factory methods for creating API clients with
 * different configurations. It serves as the entry point for ephemeral API client creation, allowing users to easily
 * obtain clients for making HTTP requests with fluent syntax.
 * <p>
 * The API clients created through this interface are designed to be short-lived and are suitable for scenarios where
 * clients are used for single operations or sessions and then discarded.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Api {

	/**
	 * Returns an {@link HttpClientFluentAdapter} for fluent syntax using the default exchange client.
	 * <p>
	 * This method assumes that one of the {@link ApiClientFluentAdapter}.{@code retrieve(...)} methods is called as a
	 * terminal operation on the returned adapter, which will trigger the closure of the underlying API client after the
	 * operation is completed.
	 *
	 * @return API client adapter
	 */
	public static HttpClientFluentAdapter http() {
		return http(ExchangeClientBuilder.create().withDefaultClient());
	}

	/**
	 * Returns an {@link HttpClientFluentAdapter} for fluent syntax.
	 *
	 * @param exchangeClientBuilder an exchange client builder to build the exchange client for the API client
	 * @return API client adapter
	 */
	@SuppressWarnings("resource")
	public static HttpClientFluentAdapter http(final ExchangeClientBuilder exchangeClientBuilder) {
		ApiClient apiClient = ApiClient.of(exchangeClientBuilder);
		apiClient.setLifecycle(ClientLifecycle.EPHEMERAL);
		return apiClient.http();
	}
}
