package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.apiphany.client.ClientProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

/**
 * Tests for {@link SpringRestExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class SpringRestExchangeClientTest {

	@Nested
	class ConstructorTests {

		@Test
		void shouldBuildWithDefaultConstructor() throws Exception {
			try (SpringRestExchangeClient client = new SpringRestExchangeClient()) {
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getClientProperties(), notNullValue());
			}
		}

		@Test
		void shouldBuildWithClientProperties() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties)) {
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getClientProperties(), notNullValue());
			}
		}

		@Test
		void shouldBuildWithRestClientBuilder() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			RestClient.Builder builder = RestClient.builder();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties, builder)) {
				assertThat(client.getMessageConverters(), notNullValue());
			}
		}

		@Test
		void shouldBuildWithCustomRestClient() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			RestClient customClient = RestClient.builder().build();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties, customClient)) {
				assertThat(client.getMessageConverters(), notNullValue());
			}
		}
	}

	@Nested
	class MessageConvertersTests {

		@Test
		void shouldReturnDefaultMessageConverters() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties)) {
				List<?> converters = client.getMessageConverters();

				assertThat(converters.size(), equalTo(3));
			}
		}
	}
}
