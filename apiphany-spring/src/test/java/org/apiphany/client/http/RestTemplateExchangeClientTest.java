package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import java.util.List;

import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpEntityRequestCallback;
import org.apiphany.http.ResponseEntityExtractor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;

/**
 * Tests for {@link RestTemplateExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class RestTemplateExchangeClientTest {

	@Nested
	class ConstructorTests {

		@Test
		void shouldBuildWithDefaultConstructor() throws Exception {
			try (RestTemplateExchangeClient client = new RestTemplateExchangeClient()) {
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getClientProperties(), notNullValue());
			}
		}

		@Test
		void shouldBuildWithClientProperties() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (RestTemplateExchangeClient client = new RestTemplateExchangeClient(properties)) {
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getClientProperties(), notNullValue());
			}
		}
	}

	@Nested
	class HttpEntityCallbackTests {

		@Test
		void shouldReturnHttpEntityRequestCallback() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			try (RestTemplateExchangeClient client = new RestTemplateExchangeClient(properties)) {
				HttpEntity<String> entity = new HttpEntity<>("body");
				var callback = client.httpEntityCallback(entity);

				assertThat(callback, instanceOf(HttpEntityRequestCallback.class));
			}
		}
	}

	@Nested
	class ResponseEntityExtractorTests {

		@Test
		void shouldReturnResponseEntityExtractor() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			try (RestTemplateExchangeClient client = new RestTemplateExchangeClient(properties)) {
				var extractor = client.responseEntityExtractor(byte[].class);

				assertThat(extractor, instanceOf(ResponseEntityExtractor.class));
			}
		}
	}

	@Nested
	class MessageConvertersTests {

		@Test
		void shouldReturnDefaultMessageConverters() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			try (RestTemplateExchangeClient client = new RestTemplateExchangeClient(properties)) {
				List<?> converters = client.getMessageConverters();

				assertThat(converters.size(), equalTo(3));
			}
		}
	}
}
