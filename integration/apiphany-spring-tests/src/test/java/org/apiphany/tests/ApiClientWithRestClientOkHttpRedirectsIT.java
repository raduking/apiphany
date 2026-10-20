package org.apiphany.tests;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.http.OkHttpLibrary;

/**
 * Redirect-focused test class for {@link org.apiphany.client.http.SpringRestExchangeClient} with OkHttp as the Spring
 * request factory backend.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestClientOkHttpRedirectsIT extends ApiClientWithRestClientHC5RedirectsIT {

	@Override
	public ClientProperties clientProperties() {
		return SpringRestClientProperties.withClientLibrary(super.clientProperties(), OkHttpLibrary.CLIENT_NAME);
	}
}
