package org.apiphany.tests.okhttp.security;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.http.OkHttpLibrary;
import org.apiphany.tests.security.ApiClientWithDefaultClientSecurityIT;

/**
 * Security test class for {@link org.apiphany.client.http.SpringRestExchangeClient} with OkHttp as the Spring request
 * factory backend.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithRestClientOkHttpSecurityIT extends ApiClientWithDefaultClientSecurityIT {

	@Override
	public ClientProperties clientProperties() {
		return SpringRestClientProperties.withClientLibrary(super.clientProperties(), OkHttpLibrary.CLIENT_NAME);
	}
}
