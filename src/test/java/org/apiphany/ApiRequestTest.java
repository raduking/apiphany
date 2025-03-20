package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
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

		ApiClientFluentAdapter adapter = ApiClientFluentAdapter.of(null)
				.get()
				.body(dto)
				.url(URL)
				.responseType(String.class);

		String expected = "\n" + removeTabs(
				"""
				{
				  "body" : {
				    "id" : "666",
				    "count" : 13
				  },
				  "headers" : { },
				  "httpMethod" : "GET",
				  "url" : "http://localhost:666/api",
				  "urlEncoded" : false,
				  "classResponseType" : "java.lang.String",
				  "charset" : "UTF-8",
				  "stream" : false,
				  "uri" : "http://localhost:666/api"
				}
				"""
		);

		String json = adapter.toString();

		assertThat(json, equalTo(expected));
	}

	private static String removeTabs(final String s) {
		return s.replace("\t", "").trim();
	}
}
