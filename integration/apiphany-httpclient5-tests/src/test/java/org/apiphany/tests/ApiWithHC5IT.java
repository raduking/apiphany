package org.apiphany.tests;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.ApacheHC5HttpExchangeClient;

/**
 * Test class for {@link ApiClient} using {@link ApacheHC5HttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiWithHC5IT extends ApiIT {

	@Override
	public ExchangeClient client() {
		return new ApacheHC5HttpExchangeClient();
	}
}
