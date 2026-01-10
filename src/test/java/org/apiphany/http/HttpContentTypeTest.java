package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

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
		HttpContentType ct = HttpContentType.parse(APPLICATION_JSON_CHARSET_ISO_8859_1);

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldResolveContentTypeAndCharsetWithList() {
		HttpContentType ct = HttpContentType.parse(List.of(APPLICATION_JSON_CHARSET_ISO_8859_1));

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldResolveContentTypeAndCharsetInLowerCase() {
		String lowerCaseValue = APPLICATION_JSON_CHARSET_ISO_8859_1.toLowerCase();
		HttpContentType ct = HttpContentType.parse(lowerCaseValue);

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldTransformToStringAndParseBack() {
		HttpContentType existing = HttpContentType.of(ContentType.APPLICATION_JSON, StandardCharsets.ISO_8859_1);

		HttpContentType ct = HttpContentType.parse(existing.toString());

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldBuildFromStringTransformToStringAndParseBack() {
		HttpContentType existing = HttpContentType.from("application/json", "iso-8859-1");

		HttpContentType ct = HttpContentType.parse(existing.toString());

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldReturnNullWhenHeaderValuesDoesNotHaveAContentType() {
		HttpContentType ct = HttpContentType.parse(List.of("someheadervalue1", "someheadervalue2"));

		assertNull(ct);
	}

	@Test
	void shouldReturnNullWhenHeaderValuesIsNull() {
		HttpContentType ct = HttpContentType.parse((List<String>) null);

		assertNull(ct);
	}

	@Test
	void shouldReturnNullWhenHeaderValueIsNull() {
		HttpContentType ct = HttpContentType.parse((String) null);

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

	@Test
	void shouldBeEqualToSelf() {
		HttpContentType ct = HttpContentType.parse(APPLICATION_JSON_CHARSET_ISO_8859_1);

		boolean equals = ct.equals(ct);

		assertTrue(equals);
	}

	@Test
	void shouldNotBeEqualIfContentTypeAndCharsetAreEqual() {
		HttpContentType ct1 = HttpContentType.parse(APPLICATION_JSON_CHARSET_ISO_8859_1);
		HttpContentType ct2 = HttpContentType.parse(APPLICATION_JSON_CHARSET_ISO_8859_1);

		boolean equals = ct1.equals(ct2);

		assertTrue(equals);
	}

	@Test
	void shouldNotBeEqualIfCharsetsDiffer() {
		HttpContentType ct1 = HttpContentType.parse(APPLICATION_JSON_CHARSET_ISO_8859_1);
		HttpContentType ct2 = HttpContentType.parse("application/json; charset=utf-8");

		boolean equals = ct1.equals(ct2);

		assertFalse(equals);
	}

	@Test
	void shouldNotBeEqualIfContentTypeDiffer() {
		HttpContentType ct1 = HttpContentType.parse(APPLICATION_JSON_CHARSET_ISO_8859_1);
		HttpContentType ct2 = HttpContentType.parse("text/plain; charset=ISO-8859-1");

		boolean equals = ct1.equals(ct2);

		assertFalse(equals);
	}

	@Test
	void shouldNotBeEqualToAnotherObject() {
		HttpContentType ct = HttpContentType.parse(APPLICATION_JSON_CHARSET_ISO_8859_1);

		@SuppressWarnings("unlikely-arg-type")
		boolean equals = ct.equals("bubu");

		assertFalse(equals);
	}

	@Test
	void shouldBuildHashCodeFromAllParams() {
		HttpContentType ct = HttpContentType.parse(APPLICATION_JSON_CHARSET_ISO_8859_1);

		int expected = Objects.hash(ct.getContentType(), ct.getCharset());
		int hash = ct.hashCode();

		assertThat(hash, equalTo(expected));
	}

	@Test
	void shouldBuildHashCodeWithNullCharset() {
		HttpContentType ct = HttpContentType.of(ContentType.APPLICATION_JSON, null);

		int expected = Objects.hash(ct.getContentType(), ct.getCharset());
		int hash = ct.hashCode();

		assertThat(hash, equalTo(expected));
	}

	@Test
	void shouldParseContentTypeWithExtraSpaces() {
		HttpContentType ct = HttpContentType.parse("  application/json  ;   charset=ISO-8859-1   ");

		assertThat(ct.getContentType(), equalTo(ContentType.APPLICATION_JSON));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.ISO_8859_1));
		assertThat(ct.charset(), equalTo(StandardCharsets.ISO_8859_1));
	}

	@Test
	void shouldParseContentTypeWithoutCharset() {
		HttpContentType ct = HttpContentType.parse("image/bmp");

		assertThat(ct.getContentType(), equalTo(ContentType.IMAGE_BMP));
		assertThat(ct.contentType(), equalTo(ContentType.IMAGE_BMP));
		assertNull(ct.getCharset());
		assertNull(ct.charset());
	}

	@Test
	void shouldParseContentTypeWithMultipleParameters() {
		HttpContentType ct = HttpContentType.parse("text/html; charset=UTF-8; boundary=something");

		assertThat(ct.getContentType(), equalTo(ContentType.TEXT_HTML));
		assertThat(ct.contentType(), equalTo(ContentType.TEXT_HTML));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.UTF_8));
		assertThat(ct.charset(), equalTo(StandardCharsets.UTF_8));
	}

	@Test
	void shouldParseContentTypeWithMultipleParametersAndSpaces() {
		HttpContentType ct = HttpContentType.parse(" text/html ;  charset=UTF-8 ; boundary=something ");

		assertThat(ct.getContentType(), equalTo(ContentType.TEXT_HTML));
		assertThat(ct.contentType(), equalTo(ContentType.TEXT_HTML));
		assertThat(ct.getCharset(), equalTo(StandardCharsets.UTF_8));
		assertThat(ct.charset(), equalTo(StandardCharsets.UTF_8));
	}

	@Test
	void shouldParseContentTypeWithNoCharsetParameter() {
		HttpContentType ct = HttpContentType.parse("image/png; comment=something");

		assertThat(ct.getContentType(), equalTo(ContentType.IMAGE_PNG));
		assertThat(ct.contentType(), equalTo(ContentType.IMAGE_PNG));
		assertNull(ct.getCharset());
		assertNull(ct.charset());
	}

	@Test
	void shouldParseContentTypeWithNoCharsetParameterWithoutEqualSign() {
		HttpContentType ct = HttpContentType.parse("image/png; comment");

		assertThat(ct.getContentType(), equalTo(ContentType.IMAGE_PNG));
		assertThat(ct.contentType(), equalTo(ContentType.IMAGE_PNG));
		assertNull(ct.getCharset());
		assertNull(ct.charset());
	}

	@Test
	void shouldReturnValueWithoutCharset() {
		HttpContentType ct = HttpContentType.of(ContentType.IMAGE_PNG);

		String value = ct.toString();

		assertThat(value, equalTo(ContentType.Value.IMAGE_PNG));
	}

	@Test
	void shouldReturnValueWithCharset() {
		HttpContentType ct = HttpContentType.of(ContentType.IMAGE_PNG, StandardCharsets.UTF_8);

		String value = ct.toString();

		assertThat(value, equalTo("image/png; charset=UTF-8"));
	}
}
