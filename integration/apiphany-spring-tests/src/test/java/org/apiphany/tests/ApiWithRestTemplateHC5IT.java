package org.apiphany.tests;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.RestTemplateExchangeClient;

/**
 * Test class for {@link ApiClient} using {@link RestTemplateExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiWithRestTemplateHC5IT extends ApiIT {

	@Override
	ExchangeClient client() {
		return new RestTemplateExchangeClient();
	}
}
