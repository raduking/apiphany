package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apiphany.http.HttpContentType;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiMimeType}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiMimeTypeTest {

	@Test
	void shouldConvertStringToBytesAndBackWithAGivenCharset() {
		String original = "Hello, world!";
		Charset charset = StandardCharsets.UTF_8;

		byte[] bytes = original.getBytes(charset);
		String result = new String(bytes, charset);

		assertThat(result, equalTo(original));
	}

	@Test
	void shouldConvertBytesToStringAndBackWithAGivenCharset() {
		String original = "Hello, world!";
		byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
		String intermediate = new String(originalBytes, StandardCharsets.UTF_8);
		Charset charset = StandardCharsets.UTF_8;

		byte[] bytes = intermediate.getBytes(charset);
		String result = new String(bytes, charset);

		assertThat(result, equalTo(original));
	}

	@Test
	void shouldReturnNullWhenCharsetCannotBeParsed() {
		Charset charset = ApiMimeType.parseCharset("<unknown>");

		assertNull(charset);
	}

	@Test
	void shouldReturnTheCharsetFromTheMimeType() {
		HttpContentType ct = HttpContentType.of(ContentType.APPLICATION_JSON, StandardCharsets.UTF_16BE);

		Charset result = ApiMimeType.charset(ct);

		assertThat(result, equalTo(StandardCharsets.UTF_16BE));
	}

	@Test
	void shouldReturnDefaultCharsetWhenMimeTypeIsNull() {
		Charset result = ApiMimeType.charset(null);

		assertThat(result, equalTo(Strings.DEFAULT_CHARSET));
	}

	@Test
	void shouldReturnDefaultCharsetWhenMimeTypeHasNoCharset() {
		ApiMimeType ct = new ApiMimeType() {

			@Override
			public String value() {
				return ContentType.APPLICATION_JSON.value();
			}

			@Override
			public Charset charset() {
				return null;
			}

			@Override
			public ContentType contentType() {
				return ContentType.APPLICATION_JSON;
			}
		};

		Charset result = ApiMimeType.charset(ct);

		assertThat(result, equalTo(Strings.DEFAULT_CHARSET));
	}

	@Test
	void shouldParseCharsetSuccessfully() {
		Charset charset = ApiMimeType.parseCharset("UTF-8");

		assertThat(charset, equalTo(StandardCharsets.UTF_8));
	}
}
