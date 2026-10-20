package org.apiphany.tests;

import org.apiphany.Api;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.SpringRestExchangeClient;

/**
 * Test class for {@link Api} using {@link SpringRestExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiWithRestClientHC5IT extends ApiIT {

	@Override
	public ExchangeClient client() {
		return new SpringRestExchangeClient();
	}
}
