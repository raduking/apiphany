package org.apiphany.tests.client.http;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.apiphany.ApiClient;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.client.http.JsonHttpContentConverter;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify that the JSON content converter is registered when Jackson is present.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpExchangeClientJackson2Test {

	@Test
	@SuppressWarnings("resource")
	void shouldRegisterTheJsonConverterWhenJackson2IsPresent() throws Exception {
		List<ContentConverter<?>> converters;
		try (ApiClient client = ApiClient.of(ApiClient.EMPTY_BASE_URL, ExchangeClientBuilder.create().withDefaultClient())) {
			converters = client.client().getExchangeClient(JavaNetHttpExchangeClient.class).getContentConverters();
		}

		ContentConverter<?> jsonConverter = converters.stream()
				.filter(JsonHttpContentConverter.class::isInstance)
				.findFirst()
				.orElse(null);

		assertThat(jsonConverter, notNullValue());
		assertThat(jsonConverter, instanceOf(JsonHttpContentConverter.class));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRegisterTheJsonConverterWhenJackson2IsPresentWithTestClient() throws Exception {
		List<ContentConverter<?>> converters;
		try (TestClient client = new TestClient()) {
			converters = client.client().getExchangeClient(JavaNetHttpExchangeClient.class).getContentConverters();
		}

		ContentConverter<?> jsonConverter = converters.stream()
				.filter(JsonHttpContentConverter.class::isInstance)
				.findFirst()
				.orElse(null);

		assertThat(jsonConverter, notNullValue());
		assertThat(jsonConverter, instanceOf(JsonHttpContentConverter.class));
	}

	static class TestClient extends ApiClient {
		// empty
	}
}
