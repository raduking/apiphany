package org.apiphany.tests;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.OkHttpExchangeClient;
import org.apiphany.security.AuthenticationType;

/**
 * Test class for {@link ApiClient} using {@link OkHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithOkHttpIT extends ApiClientWithDefaultClientIT {

	@Override
	public Class<? extends ExchangeClient> exchangeClientClass() {
		return OkHttpExchangeClient.class;
	}

	@Override
	public ExchangeClient getClient(final AuthenticationType authType) {
		return new OkHttpExchangeClient() {
			@Override
			public AuthenticationType getAuthenticationType() {
				return authType;
			}
		};
	}
}
