package org.apiphany.tests.client.http;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.apiphany.ApiClient;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.client.http.JsonHttpContentConverter;
import org.apiphany.client.http.StringHttpContentConverter;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify behavior when no JSON library is present.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpExchangeClientConvertersTest {

	@Test
	@SuppressWarnings("resource")
	void shouldRegisterTheJsonRequestConverterWhenNoJsonLibraryIsPresent() throws Exception {
		List<ContentConverter<?>> converters;
		try (ApiClient client = ApiClient.of(ApiClient.EMPTY_BASE_URL, ExchangeClientBuilder.create().withDefaultClient())) {
			converters = client.client().getExchangeClient(JavaNetHttpExchangeClient.class).getContentConverters();
		}

		assertThat(converters, hasSize(2));
		assertThat(converters.getFirst(), is(instanceOf(StringHttpContentConverter.class)));
		assertThat(converters.get(1), is(instanceOf(JsonHttpContentConverter.class)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRegisterTheJsonRequestConverterWhenNoJsonLibraryIsPresentWithTestClient() throws Exception {
		List<ContentConverter<?>> converters;
		try (TestClient client = new TestClient()) {
			converters = client.client().getExchangeClient(JavaNetHttpExchangeClient.class).getContentConverters();
		}

		assertThat(converters, hasSize(2));
		assertThat(converters.getFirst(), is(instanceOf(StringHttpContentConverter.class)));
		assertThat(converters.get(1), is(instanceOf(JsonHttpContentConverter.class)));
	}

	static class TestClient extends ApiClient {
		// empty
	}
}
