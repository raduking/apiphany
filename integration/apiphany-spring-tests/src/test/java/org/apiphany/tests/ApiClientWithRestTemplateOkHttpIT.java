package org.apiphany.tests;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.http.OkHttpLibrary;

/**
 * Test class for {@link org.apiphany.client.http.RestTemplateExchangeClient} with OkHttp as the Spring request factory
 * backend.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestTemplateOkHttpIT extends ApiClientWithRestTemplateHC5IT {

	@Override
	public ClientProperties clientProperties() {
		return SpringRestClientProperties.withClientLibrary(super.clientProperties(), OkHttpLibrary.CLIENT_NAME);
	}
}
