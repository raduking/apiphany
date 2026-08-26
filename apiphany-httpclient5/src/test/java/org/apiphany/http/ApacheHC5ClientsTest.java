package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import org.apache.hc.client5.http.CircularRedirectException;
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

	@Test
	void shouldIdentifyCircularRedirectException() throws CircularRedirectException {
		CircularRedirectException exception = new CircularRedirectException("circular redirect");

		assertThat(ApacheHC5Clients.isCircularRedirectException(exception), equalTo(true));
	}

	@Test
	void shouldIdentifyCircularRedirectExceptionWrappedInCauseChain() {
		CircularRedirectException circular = new CircularRedirectException("circular redirect");
		IOException wrapped = new IOException("circular redirect", circular);

		assertThat(ApacheHC5Clients.isCircularRedirectException(wrapped), equalTo(true));
	}

	@Test
	void shouldNotIdentifyNonCircularRedirectException() {
		IOException exception = new IOException("some error");

		assertThat(ApacheHC5Clients.isCircularRedirectException(exception), equalTo(false));
	}
}
