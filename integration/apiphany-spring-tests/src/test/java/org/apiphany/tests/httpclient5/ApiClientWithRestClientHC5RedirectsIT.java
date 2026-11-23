package org.apiphany.tests.httpclient5;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.security.AuthenticationType;
import org.apiphany.tests.ApiClientWithDefaultClientRedirectsIT;

/**
 * Redirect-focused test class for {@link SpringRestExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithRestClientHC5RedirectsIT extends ApiClientWithDefaultClientRedirectsIT {

	@Override
	public Class<? extends ExchangeClient> exchangeClientClass() {
		return SpringRestExchangeClient.class;
	}

	@Override
	public ExchangeClient getClient(final AuthenticationType authType) {
		return new SpringRestExchangeClient() {
			@Override
			public AuthenticationType getAuthenticationType() {
				return authType;
			}
		};
	}
}
