package org.apiphany.logging;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link LoggingFormat}.
 *
 * @author Radu Sebastian LAZIN
 */
class LoggingFormatTest {

	@ParameterizedTest
	@EnumSource(LoggingFormat.class)
	void shouldReadTheLoggingFormat(final LoggingFormat input) {
		LoggingFormat result = LoggingFormat.fromString(input.getLabel());

		assertThat(result, equalTo(input));
	}

	@Test
	void shouldReadTheLoggingFormatCaseInsensitive() {
		assertThat(LoggingFormat.fromString("HEX"), equalTo(LoggingFormat.HEX));
		assertThat(LoggingFormat.fromString("json"), equalTo(LoggingFormat.JSON));
		assertThat(LoggingFormat.fromString("CuStOm"), equalTo(LoggingFormat.CUSTOM));
	}

	@Test
	void shouldReturnDefaultOnInvalidOrNullInput() {
		assertThat(LoggingFormat.fromString("unknown"), equalTo(LoggingFormat.DEFAULT));
		assertThat(LoggingFormat.fromString(null), equalTo(LoggingFormat.DEFAULT));
	}

	@Test
	void shouldUseCustomAsDefault() {
		assertThat(LoggingFormat.DEFAULT, equalTo(LoggingFormat.CUSTOM));
	}

	@Test
	void shouldReturnLabelAsToString() {
		assertThat(LoggingFormat.HEX.toString(), equalTo(LoggingFormat.HEX.getLabel()));
		assertThat(LoggingFormat.JSON.toString(), equalTo(LoggingFormat.JSON.getLabel()));
		assertThat(LoggingFormat.CUSTOM.toString(), equalTo(LoggingFormat.CUSTOM.getLabel()));
	}
}
