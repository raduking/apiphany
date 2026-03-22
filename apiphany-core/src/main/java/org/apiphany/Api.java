package org.apiphany;

import org.apiphany.client.ClientLifecycle;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.HttpClientFluentAdapter;

/**
 * API interface for the Apiphany library. This interface provides static factory methods for creating API clients with
 * different configurations. It serves as the entry point for ephemeral API client creation, allowing users to easily
 * obtain clients for making HTTP requests with fluent syntax. The API clients created through this interface are
 * designed to be short-lived and are suitable for scenarios where clients are used for single operations or sessions
 * and then discarded.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Api {

	/**
	 * Returns an {@link HttpClientFluentAdapter} for fluent syntax. This method can be used safely without any
	 * ExchangeClientBuilder, as it will use the default exchange client.
	 *
	 * @param exchangeClientBuilders an array of exchange client builders to build the exchange clients for the API client.
	 * @return API client adapter
	 */
	@SuppressWarnings("resource")
	public static HttpClientFluentAdapter http(final ExchangeClientBuilder... exchangeClientBuilders) {
		ApiClient apiClient = ApiClient.of(exchangeClientBuilders);
		apiClient.setLifecycle(ClientLifecycle.EPHEMERAL);
		return apiClient.client().http();
	}
}
