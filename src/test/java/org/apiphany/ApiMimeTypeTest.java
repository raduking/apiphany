package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apiphany.http.HttpContentType;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiMimeType}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiMimeTypeTest {

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

}
