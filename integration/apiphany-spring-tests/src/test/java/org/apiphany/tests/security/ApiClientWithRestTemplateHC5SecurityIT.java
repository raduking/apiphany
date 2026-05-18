package org.apiphany.tests.security;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.RestTemplateExchangeClient;
import org.apiphany.security.AuthenticationType;

/**
 * Security test class for {@link ApiClient} using {@link RestTemplateExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestTemplateHC5SecurityIT extends ApiClientWithDefaultClientSecurityIT {

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
