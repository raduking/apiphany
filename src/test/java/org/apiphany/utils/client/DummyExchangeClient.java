package org.apiphany.utils.client;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;

public class DummyExchangeClient implements ExchangeClient {

	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
		return null;
	}

	@Override
	public void close() throws Exception {
		// empty
	}

}
