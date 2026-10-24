package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.hc.client5.http.CircularRedirectException;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
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
	void shouldIdentifyCircularRedirectException() {
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

	@Test
	void shouldCreateClientWithDefaultCustomizers() throws Exception {
		ClientProperties properties = new ClientProperties();

		try (CloseableHttpClient client = ApacheHC5Clients.createClient(properties)) {
			assertThat(client, notNullValue());
		}
	}

	@Test
	void shouldCreateClientWithConnectionManagerBuilderCustomizer() throws Exception {
		ClientProperties properties = new ClientProperties();
		AtomicBoolean customized = new AtomicBoolean();
		Consumer<PoolingHttpClientConnectionManagerBuilder> customizer = builder -> customized.set(true);

		try (CloseableHttpClient client = ApacheHC5Clients.createClient(properties, customizer)) {
			assertThat(client, notNullValue());
		}
		assertThat(customized.get(), equalTo(true));
	}

	@Test
	void shouldReturnNoOpCustomizer() {
		Consumer<String> customizer = ApacheHC5Clients.noCustomizer();

		assertThat(customizer, notNullValue());
		assertDoesNotThrow(() -> customizer.accept("ignored"));
	}
}
