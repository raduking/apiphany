package org.apiphany.tests;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.apiphany.ApiClient;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify behavior when no JSON library is present.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientNoJsonLibraryTest {

	@Test
	@SuppressWarnings("resource")
	void shouldNotHaveAnyJsonConverterRegisteredWhenNoJsonLibraryIsPresent() throws Exception {
		List<ContentConverter<?>> converters;
		try (ApiClient client = ApiClient.of(ApiClient.EMPTY_BASE_URL, ExchangeClientBuilder.create().withDefaultClient())) {
			converters = client.client().getExchangeClient(JavaNetHttpExchangeClient.class).getContentConverters();
		}

		assertThat(converters, hasSize(1));
		assertThat(converters.getFirst(), is(instanceOf(ContentConverter.class)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRegisterJackson2ConverterWhenJackson2IsPresentWithTestClient() throws Exception {
		List<ContentConverter<?>> converters;
		try (TestClient client = new TestClient()) {
			converters = client.client().getExchangeClient(JavaNetHttpExchangeClient.class).getContentConverters();
		}

		assertThat(converters, hasSize(1));
		assertThat(converters.getFirst(), is(instanceOf(ContentConverter.class)));
	}

	static class TestClient extends ApiClient {
		// empty
	}
}
