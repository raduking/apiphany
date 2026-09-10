package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.apiphany.client.ClientProperties;
import org.apiphany.http.SpringHttpSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.reflection.Fields;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
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
		@SuppressWarnings("resource")
		void shouldBuildWithDefaultConstructor() throws Exception {
			try (SpringRestExchangeClient client = new SpringRestExchangeClient()) {
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getClientProperties(), notNullValue());
				assertThat(client.getRequestFactory(), notNullValue());
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldBuildWithClientProperties() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties)) {
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getClientProperties(), notNullValue());
				assertThat(client.getRequestFactory(), notNullValue());
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldKeepConfiguredRequestFactoryOnRestClientBuilder() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			ClientHttpRequestFactory requestFactory = mock(ClientHttpRequestFactory.class);
			RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties, builder)) {
				assertThat(SpringHttpSupport.getRequestFactory(builder), equalTo(requestFactory));
				assertThat(Fields.IgnoreAccess.get(client.getRequestFactory(), "delegate"), equalTo(requestFactory));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldSetDetectedRequestFactoryWhenRestClientBuilderHasNone() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			RestClient.Builder builder = RestClient.builder();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties, builder)) {
				assertThat(SpringHttpSupport.getRequestFactory(builder), equalTo(client.getRequestFactory()));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldBuildWithCustomRestClient() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			RestClient customClient = RestClient.builder().build();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties, customClient)) {
				assertThat(client.getClientProperties(), notNullValue());
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getRequestFactory(), notNullValue());
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
				assertThat(converters.get(0), instanceOf(ByteArrayHttpMessageConverter.class));
				assertThat(converters.get(1), instanceOf(StringHttpMessageConverter.class));
				assertThat(converters.get(2), instanceOf(ResourceHttpMessageConverter.class));
			}
		}
	}
}
