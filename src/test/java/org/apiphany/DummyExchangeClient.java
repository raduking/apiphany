package org.apiphany;

import org.apiphany.auth.AuthenticationType;
import org.apiphany.client.ExchangeClient;

public class DummyExchangeClient implements ExchangeClient {

	@Override
	public <T> ApiResponse<T> exchange(final ApiRequest request) {
		return null;
	}

	@Override
	public AuthenticationType getType() {
		return AuthenticationType.NO_AUTHENTICATION;
	}

}
