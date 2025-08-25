package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.StandardCharsets;

import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link HttpContentType}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpContentTypeTest {

	@Test
	void shouldResolveContentTypeAndCharset() {
		HttpContentType ct = HttpContentType.parseHeaderValue("application/json; charset=ISO-8859-1");

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldResolveContentTypeAndCharsetInLowerCase() {
		HttpContentType ct = HttpContentType.parseHeaderValue("application/json; charset=iso-8859-1");

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}
}
