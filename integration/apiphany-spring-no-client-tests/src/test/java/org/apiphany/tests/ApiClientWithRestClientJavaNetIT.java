package org.apiphany.tests;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.security.AuthenticationType;

/**
 * Test class for {@link SpringRestExchangeClient} with JavaNet auto-detected (no HC5 on classpath).
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestClientJavaNetIT extends ApiClientWithDefaultClientIT {

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
