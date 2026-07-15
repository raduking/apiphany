package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apiphany.client.ClientProperties;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApacheHC5Clients}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApacheHC5ClientsTest {

	@Test
	void shouldApplyGenericFollowRedirectsToRequestConfig() {
		ClientProperties properties = new ClientProperties();
		properties.getConnection().setFollowRedirects(true);

		RequestConfig requestConfig = ApacheHC5Clients.createRequestConfig(properties);

		assertThat(requestConfig.isRedirectsEnabled(), equalTo(true));
	}

	@Test
	void shouldNotFollowRedirectsByDefaultInRequestConfig() {
		ClientProperties properties = new ClientProperties();

		RequestConfig requestConfig = ApacheHC5Clients.createRequestConfig(properties);

		assertThat(requestConfig.isRedirectsEnabled(), equalTo(false));
	}
}
