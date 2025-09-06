package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link HttpMessages}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpMessagesTest {

	private static final long LONG_42 = 42L;
	private static final long LONG_666 = 666L;

	@Test
	void shouldBuildRangeString() {
		String result = HttpMessages.getRangeString(LONG_42, LONG_666);

		assertThat(result, equalTo("bytes=42-666"));
	}

	@Test
	void shouldAssumeZeroForNullsOnRangeString() {
		String result = HttpMessages.getRangeString(null, null);

		assertThat(result, equalTo("bytes=0-0"));
	}

	@Test
	void shouldThrowExceptionIfFirstParameterIsBiggerThanTheSecondOnRagneString() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> HttpMessages.getRangeString(LONG_666, LONG_42));

		assertThat(e.getMessage(), equalTo("rangeEnd must be greater or equal to rangeStart"));
	}
}
