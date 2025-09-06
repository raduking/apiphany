package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apiphany.io.ContentType;
import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link HttpContentType}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpContentTypeTest {

	private static final String CHARSET = "charset";
	private static final String APPLICATION_JSON_CHARSET_ISO_8859_1 = "application/json; charset=ISO-8859-1";

	@Test
	void shouldResolveContentTypeAndCharset() {
		HttpContentType ct1 = HttpContentType.parseHeaderValue(APPLICATION_JSON_CHARSET_ISO_8859_1);
		HttpContentType ct2 = HttpContentType.parseHeader(APPLICATION_JSON_CHARSET_ISO_8859_1);

		assertThat(ct1.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct1.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
		assertThat(ct1, equalTo(ct2));
	}

	@Test
	void shouldResolveContentTypeAndCharsetInLowerCase() {
		String lowerCaseValue = APPLICATION_JSON_CHARSET_ISO_8859_1.toLowerCase();
		HttpContentType ct1 = HttpContentType.parseHeaderValue(lowerCaseValue);
		HttpContentType ct2 = HttpContentType.parseHeader(lowerCaseValue);

		assertThat(ct1.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct1.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
		assertThat(ct1, equalTo(ct2));
	}

	@Test
	void shouldTransformToStringAndParseBack() {
		HttpContentType existing = HttpContentType.of(ContentType.APPLICATION_JSON, StandardCharsets.ISO_8859_1);

		HttpContentType ct = HttpContentType.parseHeaderValue(existing.toString());

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldBuildFromStringTransformToStringAndParseBack() {
		HttpContentType existing = HttpContentType.from("application/json", "iso-8859-1");

		HttpContentType ct = HttpContentType.parseHeaderValue(existing.toString());

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldReturnNullWhenHeaderValuesDoesNotHaveAContentType() {
		HttpContentType ct = HttpContentType.parseHeader(List.of("someheadervalue1", "someheadervalue2"));

		assertNull(ct);
	}

	@Test
	void shouldThrowExceptionOnCallingParamConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(HttpContentType.Param.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldHaveTheCorectCharsetParam() {
		assertThat(HttpContentType.Param.CHARSET, equalTo(CHARSET));
	}
}
