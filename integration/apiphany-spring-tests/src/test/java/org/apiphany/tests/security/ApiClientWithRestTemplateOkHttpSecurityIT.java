package org.apiphany.tests.security;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.http.OkHttpLibrary;

/**
 * Security test class for {@link org.apiphany.client.http.RestTemplateExchangeClient} with OkHttp as the Spring request
 * factory backend.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestTemplateOkHttpSecurityIT extends ApiClientWithRestTemplateHC5SecurityIT {

	@Override
	public ClientProperties clientProperties() {
		return SpringRestClientProperties.withClientLibrary(super.clientProperties(), OkHttpLibrary.CLIENT_NAME);
	}
}
