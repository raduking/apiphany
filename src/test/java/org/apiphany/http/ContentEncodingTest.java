package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link ContentEncoding}.
 *
 * @author Radu Sebastian LAZIN
 */
class ContentEncodingTest {

	@ParameterizedTest
	@EnumSource(ContentEncoding.class)
	void shouldBuildWithFromStringWithValidValueEvenIfUppercase(final ContentEncoding contentEncoding) {
		String stringValue = contentEncoding.value().toUpperCase();
		ContentEncoding result = ContentEncoding.fromString(stringValue);
		assertThat(result, equalTo(contentEncoding));
	}

	@ParameterizedTest
	@EnumSource(ContentEncoding.class)
	void shouldMatchValidValueEvenIfUppercase(final ContentEncoding contentEncoding) {
		String stringValue = contentEncoding.value().toUpperCase();
		boolean result = contentEncoding.matches(stringValue);
		assertTrue(result);
	}
}
