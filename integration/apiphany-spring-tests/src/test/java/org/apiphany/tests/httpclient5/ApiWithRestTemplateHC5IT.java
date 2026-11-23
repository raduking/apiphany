package org.apiphany.tests.httpclient5;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.RestTemplateExchangeClient;
import org.apiphany.tests.ApiIT;

/**
 * Test class for {@link ApiClient} using {@link RestTemplateExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiWithRestTemplateHC5IT extends ApiIT {

	@Override
	public ExchangeClient client() {
		return new RestTemplateExchangeClient();
	}
}
