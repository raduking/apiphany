package org.apiphany.tests.okhttp;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.http.OkHttpLibrary;
import org.apiphany.tests.ApiClientWithDefaultClientRedirectsIT;

/**
 * Redirect-focused test class for {@link org.apiphany.client.http.RestTemplateExchangeClient} with OkHttp as the Spring
 * request factory backend.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithRestTemplateOkHttpRedirectsIT extends ApiClientWithDefaultClientRedirectsIT {

	@Override
	public ClientProperties clientProperties() {
		return SpringRestClientProperties.withClientLibrary(super.clientProperties(), OkHttpLibrary.CLIENT_NAME);
	}
}
