package org.apiphany.tests;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.RestTemplateExchangeClient;
import org.apiphany.security.AuthenticationType;

/**
 * Test class for {@link ApiClient} using {@link RestTemplateExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestTemplateHC5IT extends ApiClientWithDefaultClientIT {

	@Override
	protected Class<? extends ExchangeClient> exchangeClientClass() {
		return RestTemplateExchangeClient.class;
	}

	@Override
	protected ExchangeClient getClient(final AuthenticationType authType) {
		return new RestTemplateExchangeClient() {
			@Override
			public AuthenticationType getAuthenticationType() {
				return authType;
			}
		};
	}
}
