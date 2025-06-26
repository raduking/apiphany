package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link HttpHeader}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpHeaderTest {

	@ParameterizedTest
	@EnumSource(HttpHeader.class)
	void shouldBuildWithFromStringWithValidValueEvenIfUppercase(final HttpHeader httpHeader) {
		String stringValue = httpHeader.value().toUpperCase();
		HttpHeader result = HttpHeader.fromString(stringValue);
		assertThat(result, equalTo(httpHeader));
	}

	@ParameterizedTest
	@EnumSource(HttpHeader.class)
	void shouldMatchValidValueEvenIfUppercase(final HttpHeader httpHeader) {
		String stringValue = httpHeader.value().toUpperCase();
		boolean result = httpHeader.matches(stringValue);
		assertTrue(result);
	}
}
