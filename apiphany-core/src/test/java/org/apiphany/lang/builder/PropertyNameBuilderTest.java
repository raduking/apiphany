package org.apiphany.lang.builder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link PropertyNameBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class PropertyNameBuilderTest {

	private static final String DELIMITER = ".";

	@Test
	void shouldBuildADelimitedStringWithMultiplePaths() {
		String result = PropertyNameBuilder.builder()
				.path("xxx", "yyy", "zzz")
				.build();

		assertThat(result, equalTo("xxx" + DELIMITER + "yyy" + DELIMITER + "zzz"));
	}

	@Test
	void shouldBuildADelimitedStringWithMultiplePathsWithOf() {
		String result = PropertyNameBuilder.of("xxx", "yyy", "zzz").build();

		assertThat(result, equalTo("xxx" + DELIMITER + "yyy" + DELIMITER + "zzz"));
	}

	@Test
	void shouldBuildADelimitedStringWithMultiplePathsAsSuffix() {
		String result = PropertyNameBuilder.builder()
				.asSuffix()
				.path("xxx", "yyy", "zzz")
				.build();

		assertThat(result, equalTo(DELIMITER + "xxx" + DELIMITER + "yyy" + DELIMITER + "zzz"));
	}

	@Test
	void shouldThrowExceptionIfPathIsNull() {
		PropertyNameBuilder dsb = PropertyNameBuilder.builder();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> dsb.path((String) null));

		assertThat(e.getMessage(), equalTo("Parameter path should not be null"));
	}

	@Test
	void shouldThrowExceptionIfPathsIsNull() {
		PropertyNameBuilder dsb = PropertyNameBuilder.builder();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> dsb.path((String[]) null));

		assertThat(e.getMessage(), equalTo("Parameter paths should not be null"));
	}

}
