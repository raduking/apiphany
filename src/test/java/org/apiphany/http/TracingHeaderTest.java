package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link TracingHeader}.
 *
 * @author Radu Sebastian LAZIN
 */
class TracingHeaderTest {

	@ParameterizedTest
	@EnumSource(TracingHeader.class)
	void shouldBuildWithFromStringWithValidValueEvenIfUppercase(final TracingHeader tracingHeader) {
		String stringValue = tracingHeader.value().toUpperCase();
		TracingHeader result = TracingHeader.fromString(stringValue);
		assertThat(result, equalTo(tracingHeader));
	}

	@ParameterizedTest
	@EnumSource(TracingHeader.class)
	void shouldMatchValidValueEvenIfUppercase(final TracingHeader tracingHeader) {
		String stringValue = tracingHeader.value().toUpperCase();
		boolean result = tracingHeader.matches(stringValue);
		assertTrue(result);
	}

}
