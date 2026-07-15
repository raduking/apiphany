package org.apiphany.tests;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.RestTemplateExchangeClient;
import org.apiphany.security.AuthenticationType;

/**
 * Redirect-focused test class for {@link RestTemplateExchangeClient} using Apache HC5 backend.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestTemplateHC5RedirectsIT extends ApiClientWithDefaultClientRedirectsIT {

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
