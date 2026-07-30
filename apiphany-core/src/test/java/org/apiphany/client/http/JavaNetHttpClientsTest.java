package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.http.HttpClient;
import java.time.Duration;

import org.apiphany.client.ClientProperties;
import org.apiphany.http.JavaNetHttpClients;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaNetHttpClients}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpClientsTest {

	@Nested
	class CustomizeTests {

		@Test
		void shouldNotFollowRedirectsByDefault() {
			ClientProperties clientProperties = new ClientProperties();
			HttpClient.Builder builder = HttpClient.newBuilder();

			HttpClient.Builder result = JavaNetHttpClients.customize(builder, clientProperties, null);

			try (HttpClient httpClient = result.build()) {
				assertThat(httpClient.followRedirects(), equalTo(HttpClient.Redirect.NEVER));
			}
		}

		@Test
		void shouldFollowRedirectsWhenConfigured() {
			ClientProperties clientProperties = new ClientProperties();
			clientProperties.getConnection().setFollowRedirects(true);
			HttpClient.Builder builder = HttpClient.newBuilder();

			HttpClient.Builder result = JavaNetHttpClients.customize(builder, clientProperties, null);

			try (HttpClient httpClient = result.build()) {
				assertThat(httpClient.followRedirects(), equalTo(HttpClient.Redirect.NORMAL));
			}
		}
	}

	@Nested
	class GetUsableTimeoutTests {

		@Test
		void shouldReturnANullUsableTimeputFromNull() {
			Duration timeout = JavaNetHttpClients.getTimeout(null, t -> null);

			assertThat(timeout, equalTo(null));
		}

		@Test
		void shouldReturnGivenTimeoutWhenUsableAndNotInfinite() {
			Duration timeout = JavaNetHttpClients.getTimeout(null, t -> Duration.ofSeconds(10));

			assertThat(timeout, equalTo(Duration.ofSeconds(10)));
		}
	}
}
