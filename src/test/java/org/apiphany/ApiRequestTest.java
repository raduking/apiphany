package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.List;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.collections.Maps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiRequest}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiRequestTest {

	private static final String ID = "666";
	private static final int COUNT = 13;
	private static final String URL = "http://localhost:666/api";

	@BeforeEach
	void setUp() {
		JsonBuilder.indentJsonOutput(true);
	}

	@AfterEach
	void tearDown() {
		JsonBuilder.indentJsonOutput(false);
	}

	@Test
	void shouldReturnCorrectJsonStringOnToString() {
		TestDto dto = TestDto.of(ID, COUNT);

		ExchangeClient exchangeClient = new HttpExchangeClient() {
			@Override
			public <T> String getHeadersAsString(ApiMessage<T> apiMessage) {
				return Maps.safe(apiMessage.getHeaders()).toString();
			}
		};
		ApiClient apiClient = ApiClient.of(ApiClient.NO_BASE_URL, exchangeClient);
		ApiClientFluentAdapter adapter = apiClient.client()
				.get()
				.body(dto)
				.url(URL)
				.responseType(String.class);

		@SuppressWarnings("unchecked")
		ApiRequest<String> apiRequest = JsonBuilder.fromJson(
				"""
				{
				  "body" : {
				    "id" : "666",
				    "count" : 13
				  },
				  "method" : "GET",
				  "headers" : { },
				  "url" : "http://localhost:666/api",
				  "urlEncoded" : false,
				  "classResponseType" : "java.lang.String",
				  "charset" : "UTF-8",
				  "stream" : false,
				  "authenticationType" : "NONE"
				}
				""",
		ApiRequest.class);

		String expected = apiRequest.toString();

		String json = adapter.toString();

	    List<Character> chars1 = expected.chars().mapToObj(c -> (char) c).toList();
	    List<Character> chars2 = json.chars().mapToObj(c -> (char) c).toList();
	    assertThat(chars1, containsInAnyOrder(chars2.toArray(Character[]::new)));
	}

}
