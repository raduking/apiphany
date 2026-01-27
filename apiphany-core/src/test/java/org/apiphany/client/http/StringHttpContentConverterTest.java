package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.http.HttpContentType;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Test class for {@link StringHttpContentConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class StringHttpContentConverterTest {

	private static final String HELLO_WORLD = "Hello, World!";

	@Test
	void shouldConvertObjectToString() {
		Object obj = 12345;
		String result = StringHttpContentConverter.from(obj, null);

		assertThat(result, equalTo("12345"));
	}

	@Test
	void shouldConvertNullToNull() {
		Object obj = null;
		String result = StringHttpContentConverter.from(obj, null);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldConvertStringToString() {
		Object obj = HELLO_WORLD;
		String result = StringHttpContentConverter.from(obj, null);

		assertThat(result, equalTo(HELLO_WORLD));
	}

	@Test
	void shouldConvertBytesToString() {
		Object obj = "Hello, Bytes!".getBytes();
		String result = StringHttpContentConverter.from(obj, null);

		assertThat(result, equalTo("Hello, Bytes!"));
	}

	@Test
	void shouldConvertBytesWithCharsetToString() {
		Object obj = "Hello, Charset!".getBytes();
		String result = StringHttpContentConverter.from(obj, ContentType.TEXT_PLAIN);

		assertThat(result, equalTo("Hello, Charset!"));
	}

	@Test
	void shouldConvertBytesWithDifferentCharsetToString() {
		Object obj = "Hello, Different Charset!".getBytes();
		String result = StringHttpContentConverter.from(obj, HttpContentType.of(ContentType.TEXT_PLAIN, StandardCharsets.ISO_8859_1));

		assertThat(result, equalTo("Hello, Different Charset!"));
	}

	@Test
	void shouldConvertStringToStringWithInstance() {
		Object obj = HELLO_WORLD;
		String result = StringHttpContentConverter.instance().from(obj, null, String.class);

		assertThat(result, equalTo(HELLO_WORLD));
	}

	@Test
	void shouldConvertStringToStringWithGenericClassAndIgnoreGenericClassParameter() {
		Object obj = HELLO_WORLD;
		String result = StringHttpContentConverter.instance().from(obj, null, (GenericClass<String>) null);

		assertThat(result, equalTo(HELLO_WORLD));
	}

	@Test
	void shouldConvertInputStreamToString() {
		Object obj = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
		String result = StringHttpContentConverter.from(obj, null);

		assertThat(result, equalTo(HELLO_WORLD));
	}

	@Test
	void shouldReturnTrueOnIsConvertibleWhenMimeTypeIsTextPlain() {
		boolean result = StringHttpContentConverter.instance().isConvertible(null, ContentType.TEXT_PLAIN, null, null);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnFalseOnIsConvertibleWhenMimeTypeIsNotTextPlain() {
		boolean result = StringHttpContentConverter.instance().isConvertible(null, ContentType.APPLICATION_JSON, null, null);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnIsConvertibleWhenMimeTypeIsNull() {
		boolean result = StringHttpContentConverter.instance().isConvertible(null, null, null, null);

		assertThat(result, equalTo(false));
	}
}
