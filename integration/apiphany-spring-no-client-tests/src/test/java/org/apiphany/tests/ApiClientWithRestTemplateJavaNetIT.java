package org.apiphany.tests;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.RestTemplateExchangeClient;
import org.apiphany.security.AuthenticationType;

/**
 * Test class for {@link ApiClient} using {@link RestTemplateExchangeClient} with JavaNet auto-detected (no HC5 on
 * classpath).
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestTemplateJavaNetIT extends ApiClientWithDefaultClientIT {

	@Override
	public Class<? extends ExchangeClient> exchangeClientClass() {
		return RestTemplateExchangeClient.class;
	}

	@Override
	public ExchangeClient getClient(final AuthenticationType authType) {
		return new RestTemplateExchangeClient() {
			@Override
			public AuthenticationType getAuthenticationType() {
				return authType;
			}
		};
	}
}
