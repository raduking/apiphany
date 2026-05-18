package org.apiphany.tests.security;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.RestTemplateExchangeClient;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.security.AuthenticationType;

/**
 * Security test class for {@link ApiClient} using {@link RestTemplateExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestClientHC5SecurityIT extends ApiClientWithDefaultClientSecurityIT {

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
