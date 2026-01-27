package org.apiphany.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link HttpMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpMethodTest {

	@ParameterizedTest
	@EnumSource(HttpMethod.class)
	void shouldConvertFromString(final HttpMethod method) {
		assertEquals(method, HttpMethod.fromString(method.toString()));
	}

	@ParameterizedTest
	@EnumSource(HttpMethod.class)
	void shouldReturnStringValue(final HttpMethod method) {
		assertEquals(method.name(), method.toString());
	}

	@ParameterizedTest
	@EnumSource(HttpMethod.class)
	void shouldMatchAnyCaseString(final HttpMethod method) {
		assertEquals(method, HttpMethod.fromString(method.name().toLowerCase()));
		assertEquals(method, HttpMethod.fromString(method.name().toUpperCase()));
		assertTrue(method.matches(method.name().toLowerCase()));
		assertTrue(method.matches(method.name().toUpperCase()));
	}
}
