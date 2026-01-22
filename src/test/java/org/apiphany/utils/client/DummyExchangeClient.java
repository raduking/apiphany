package org.apiphany.utils.client;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;

/**
 * Dummy implementation of {@link ExchangeClient} for testing purposes.
 * <p>
 * Only the abstract methods are implemented with default behavior.
 *
 * @author Radu Sebastian LAZIN
 */
public class DummyExchangeClient implements ExchangeClient {

	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
		return null;
	}

	@Override
	public void close() {
		// empty
	}

}
