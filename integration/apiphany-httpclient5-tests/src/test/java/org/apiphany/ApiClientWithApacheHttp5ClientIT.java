package org.apiphany;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.ApacheHttp5ExchangeClient;

/**
 * Test class for {@link ApiClient} using {@link ApacheHttp5ExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithApacheHttp5ClientIT extends ApiClientWithJavaNetHttpIT {

	@Override
	protected Class<? extends ExchangeClient> exchangeClientClass() {
		return ApacheHttp5ExchangeClient.class;
	}

}
