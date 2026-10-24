package org.apiphany.tests;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.OkHttpExchangeClient;

/**
 * Test class for {@link ApiClient} using {@link OkHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiWithOkHttpIT extends ApiIT {

	@Override
	public ExchangeClient client() {
		return new OkHttpExchangeClient();
	}
}
