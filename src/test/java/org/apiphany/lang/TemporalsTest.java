package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Temporals}.
 *
 * @author Radu Sebastian LAZIN
 */
class TemporalsTest {

	@Test
	void shouldFormatADoubleToStringAsSecondsWith3DecimalsRoundingDown() {
		double value = 1.2341;

		String result = Temporals.formatToSeconds(value);

		assertThat(result, equalTo("1.234s"));
	}

	@Test
	void shouldFormatADoubleToStringAsSecondsWith3DecimalsRoundingUp() {
		double value = 1.2346;

		String result = Temporals.formatToSeconds(value);

		assertThat(result, equalTo("1.235s"));
	}

	@Test
	void shouldReturnNotAvailableIfInputIsNaNOnFormtatToSecondsWithDouble() {
		String result = Temporals.formatToSeconds(Double.NaN);

		assertThat(result, equalTo("N/A"));
	}

}
