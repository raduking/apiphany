package org.apiphany.tests;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.http.JavaNetHttpLibrary;
import org.apiphany.security.AuthenticationType;

/**
 * Redirect-focused test class for {@link SpringRestExchangeClient} using JavaNet backend.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestClientJavaNetRedirectsIT extends ApiClientWithDefaultClientRedirectsIT {

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

	@Override
	public ClientProperties clientProperties() {
		ClientProperties properties = super.clientProperties();
		properties.setCustomProperties(SpringRestClientProperties.ROOT, new SpringRestClientProperties() {
			{
				setClientLibrary(JavaNetHttpLibrary.CLIENT_NAME);
			}
		});
		return properties;
	}
}
