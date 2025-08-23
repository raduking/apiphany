package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ResolvedContentType}.
 *
 * @author Radu Sebastian LAZIN
 */
class ResolvedContentTypeTest {

	@Test
	void shouldResolveContentTypeAndCharset() {
		ResolvedContentType ct = ResolvedContentType.parseHeader("application/json; charset=ISO-8859-1");

		assertThat(ct.contentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.charset(), equalTo(StandardCharsets.ISO_8859_1));
	}

}
