package org.apiphany.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.apiphany.ApiClient;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify that Jackson converter is registered when Jackson is present.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientJackson2Test {

	private static final String JACKSON2 = "Jackson2";

	@Test
	@SuppressWarnings("resource")
	void shouldRegisterJackson2ConverterWhenJackson2IsPresent() throws Exception {
		List<ContentConverter<?>> converters;
		try (ApiClient client = ApiClient.of(ApiClient.EMPTY_BASE_URL, ExchangeClientBuilder.create().withDefaultClient())) {
			converters = client.client().getExchangeClient(JavaNetHttpExchangeClient.class).getContentConverters();
		}

		ContentConverter<?> jacksonConverter = converters.stream()
				.filter(c -> c.getClass().getName().contains(JACKSON2))
				.findFirst()
				.orElse(null);

		assertThat(jacksonConverter, notNullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRegisterJackson2ConverterWhenJackson2IsPresentWithTestClient() throws Exception {
		List<ContentConverter<?>> converters;
		try (TestClient client = new TestClient()) {
			converters = client.client().getExchangeClient(JavaNetHttpExchangeClient.class).getContentConverters();
		}

		ContentConverter<?> jacksonConverter = converters.stream()
				.filter(c -> c.getClass().getName().contains(JACKSON2))
				.findFirst()
				.orElse(null);

		assertThat(jacksonConverter, notNullValue());
	}

	static class TestClient extends ApiClient {
		// empty
	}
}
