package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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

}
