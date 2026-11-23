package org.apiphany.tests;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.http.JavaNetHttpLibrary;

/**
 * Redirect-focused test class for {@link org.apiphany.client.http.SpringRestExchangeClient} with explicit JavaNet
 * client library configuration. Extends the auto-detected JavaNet base class and adds the {@code clientLibrary}
 * property override.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithRestClientJavaNetRedirectsWithClientLibraryIT extends ApiClientWithRestClientJavaNetRedirectsIT {

	@Override
	public ClientProperties clientProperties() {
		return SpringRestClientProperties.withClientLibrary(super.clientProperties(), JavaNetHttpLibrary.CLIENT_NAME);
	}
}
