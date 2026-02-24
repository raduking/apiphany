package org.apiphany;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.ApacheHC5ExchangeClient;

/**
 * Test class for {@link ApiClient} using {@link ApacheHC5ExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithApacheHC5IT extends ApiClientWithJavaNetHttpIT {

	@Override
	protected Class<? extends ExchangeClient> exchangeClientClass() {
		return ApacheHC5ExchangeClient.class;
	}

}
