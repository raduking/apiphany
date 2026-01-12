package org.apiphany.client;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.header.Headers;

/**
 * An ExchangeClient that delegates all calls to another ExchangeClient.
 *
 * @author Radu Sebastian LAZIN
 */
public interface DelegatingExchangeClient extends ExchangeClient {

	/**
	 * Returns the delegate {@link ExchangeClient}.
	 *
	 * @return the delegate exchange client
	 */
	ExchangeClient getExchangeClient();

	/**
	 * Delegates the exchange to the underlying exchange client.
	 * <p>
	 * Implementations may override this method to perform the exchange, possibly decorating the request/response as needed
	 * but they should call {@code super.exchange(...)} to delegate to the underlying exchange client.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest the API request
	 * @return the API response
	 *
	 * @see ExchangeClient#exchange(ApiRequest)
	 */
	@SuppressWarnings("resource")
	@Override
	default <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		Headers.addTo(apiRequest.getHeaders(), getCommonHeaders());
		return getExchangeClient().exchange(apiRequest);
	}

	/**
	 * Delegates the {@link ExchangeClient#getClientProperties} to the underlying exchange client.
	 *
	 * @param <T> client properties type
	 *
	 * @see ExchangeClient#getClientProperties()
	 */
	@SuppressWarnings("resource")
	@Override
	default <T extends ClientProperties> T getClientProperties() {
		return getExchangeClient().getClientProperties();
	}
}
