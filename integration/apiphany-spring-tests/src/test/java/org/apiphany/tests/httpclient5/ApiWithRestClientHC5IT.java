package org.apiphany.tests.httpclient5;

import org.apiphany.Api;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.tests.ApiIT;

/**
 * Test class for {@link Api} using {@link SpringRestExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiWithRestClientHC5IT extends ApiIT {

	@Override
	public ExchangeClient client() {
		return new SpringRestExchangeClient();
	}
}
