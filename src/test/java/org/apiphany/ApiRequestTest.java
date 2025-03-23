package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.http.HttpMethod;
import org.apiphany.json.JsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

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

		ApiClientFluentAdapter adapter = ApiClientFluentAdapter.of(null)
				.get()
				.body(dto)
				.url(URL)
				.responseType(String.class);

		@SuppressWarnings("unchecked")
		ApiRequest<String> expected = JsonBuilder.fromJson(
				"""
				{
				  "body" : {
				    "id" : "666",
				    "count" : 13
				  },
				  "headers" : { },
				  "url" : "http://localhost:666/api",
				  "urlEncoded" : false,
				  "classResponseType" : "java.lang.String",
				  "charset" : "UTF-8",
				  "stream" : false
				}
				""",
		ApiRequest.class);
		Fields.IgnoreAccess.set(expected, "method", HttpMethod.GET);

		String json = adapter.toString();

		assertThat(json, equalTo(expected.toString()));
	}

}
