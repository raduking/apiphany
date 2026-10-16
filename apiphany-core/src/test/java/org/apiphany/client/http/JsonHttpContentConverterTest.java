package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JsonHttpContentConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonHttpContentConverterTest {

	@Test
	void shouldWriteJsonWithJsonBuilder() {
		JsonHttpContentConverter converter = new JsonHttpContentConverter();
		Map<String, String> value = Map.of("name", "Ada");

		assertThat(converter.to(value, ContentType.APPLICATION_JSON), equalTo(JsonBuilder.toJson(value)));
	}

	@Test
	void shouldWriteApplicationJson() {
		JsonHttpContentConverter converter = new JsonHttpContentConverter();

		assertThat(converter.isWritable(null, ContentType.APPLICATION_JSON), equalTo(true));
	}

	@Test
	void shouldNotWriteOtherContentTypes() {
		JsonHttpContentConverter converter = new JsonHttpContentConverter();

		assertThat(converter.isWritable(null, ContentType.TEXT_PLAIN), equalTo(false));
		assertThat(converter.isWritable(null, null), equalTo(false));
	}

	@Test
	void shouldNotConvertResponses() {
		JsonHttpContentConverter converter = new JsonHttpContentConverter();

		assertThat(converter.isConvertible(null, ContentType.APPLICATION_JSON, null, null), equalTo(false));
		assertThrows(UnsupportedOperationException.class, () -> converter.from(null, ContentType.APPLICATION_JSON, String.class));
	}
}
