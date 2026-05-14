package org.apiphany.tests;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.JavaNetHttpProperties;
import org.apiphany.client.http.RestTemplateExchangeClient;
import org.apiphany.client.http.RestTemplateProperties;
import org.apiphany.security.AuthenticationType;

/**
 * Test class for {@link ApiClient} using {@link RestTemplateExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestTemplateJavaNetIT extends ApiClientWithDefaultClientIT {

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

	@Override
	protected ClientProperties clientProperties() {
		ClientProperties properties = super.clientProperties();
		properties.setCustomProperties(RestTemplateProperties.ROOT, new RestTemplateProperties() {
			{
				setClientLibrary(JavaNetHttpProperties.ROOT);
			}
		});
		return properties;
	}
}
