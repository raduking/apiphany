package org.apiphany.tests;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.ApacheHC5HttpExchangeClient;
import org.apiphany.security.AuthenticationType;

/**
 * Redirect-focused test class for {@link ApacheHC5HttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithApacheHC5RedirectsIT extends ApiClientWithDefaultClientRedirectsIT {

	@Override
	public Class<? extends ExchangeClient> exchangeClientClass() {
		return ApacheHC5HttpExchangeClient.class;
	}

	@Override
	public ExchangeClient getClient(final AuthenticationType authType) {
		return new ApacheHC5HttpExchangeClient() {
			@Override
			public AuthenticationType getAuthenticationType() {
				return authType;
			}
		};
	}
}
