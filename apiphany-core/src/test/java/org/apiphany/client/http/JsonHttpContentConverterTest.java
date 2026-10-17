package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Test class for {@link JsonHttpContentConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonHttpContentConverterTest {

	@Test
	void shouldWriteJsonWithJsonBuilder() {
		JsonHttpContentConverter<?> converter = new JsonHttpContentConverter<>();
		Map<String, String> value = Map.of("name", "Ada");

		assertThat(converter.toString(value, ContentType.APPLICATION_JSON), equalTo(JsonBuilder.toJson(value)));
	}

	@Test
	void shouldConvertApplicationJson() {
		JsonHttpContentConverter<?> converter = new JsonHttpContentConverter<>();

		assertThat(converter.isConvertible(ContentType.APPLICATION_JSON, null, null), equalTo(true));
	}

	@Test
	void shouldNotConvertOtherContentTypes() {
		JsonHttpContentConverter<?> converter = new JsonHttpContentConverter<>();

		assertThat(converter.isConvertible(ContentType.TEXT_PLAIN, null, null), equalTo(false));
		assertThat(converter.isConvertible(null, null, null), equalTo(false));
	}

	@Test
	void shouldReadJson() {
		JsonHttpContentConverter<NamedValue> converter = new JsonHttpContentConverter<>();

		NamedValue value = converter.from("{\"name\":\"Ada\"}", ContentType.APPLICATION_JSON, NamedValue.class);

		assertThat(value.name, equalTo("Ada"));
	}

	@Test
	void shouldReturnNullWhenInputIsNull() {
		JsonHttpContentConverter<NamedValue> converter = new JsonHttpContentConverter<>();

		NamedValue value = converter.from(null, ContentType.APPLICATION_JSON, NamedValue.class);

		assertThat(value, nullValue());
	}

	@Test
	void shouldReturnNullWhenGenericInputIsNull() {
		JsonHttpContentConverter<List<NamedValue>> converter = new JsonHttpContentConverter<>();

		List<NamedValue> value = converter.from(null, ContentType.APPLICATION_JSON, new GenericClass<List<NamedValue>>() {
			// empty
		});

		assertThat(value, nullValue());
	}

	/**
	 * Bean read from JSON.
	 */
	static class NamedValue {

		public String name;
	}
}
