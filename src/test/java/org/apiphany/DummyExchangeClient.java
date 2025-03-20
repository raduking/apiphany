package org.apiphany;

import org.apiphany.auth.AuthenticationType;
import org.apiphany.client.ExchangeClient;

public class DummyExchangeClient implements ExchangeClient {

	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
		return null;
	}

	@Override
	public AuthenticationType getType() {
		return AuthenticationType.NO_AUTHENTICATION;
	}

}
