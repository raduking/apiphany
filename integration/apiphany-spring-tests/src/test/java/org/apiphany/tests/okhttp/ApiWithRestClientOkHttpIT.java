package org.apiphany.tests.okhttp;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.http.OkHttpLibrary;
import org.apiphany.tests.ApiIT;

/**
 * Test class for {@link org.apiphany.Api} using {@link SpringRestExchangeClient} with OkHttp as the Spring request
 * factory backend.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiWithRestClientOkHttpIT extends ApiIT {

	@Override
	public ExchangeClient client() {
		ClientProperties properties = SpringRestClientProperties.withClientLibrary(OkHttpLibrary.CLIENT_NAME);
		return new SpringRestExchangeClient(properties);
	}
}
