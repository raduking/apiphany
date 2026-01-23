package org.apiphany.lang.builder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link DelimitedStringBuilder}
 *
 * @author Radu Sebastian LAZIN
 */
class DelimitedStringBuilderTest {

	private static final String DELIMITER = "666";

	@Test
	void shouldBuildADelimitedStringWithMultiplePaths() {
		String result = DelimitedStringBuilder.builder(DELIMITER)
				.path("xxx", "yyy", "zzz")
				.build();

		assertThat(result, equalTo("xxx" + DELIMITER + "yyy" + DELIMITER + "zzz"));
	}

	@Test
	void shouldBuildADelimitedStringWithMultiplePathsWithFactoryMethod() {
		String result = DelimitedStringBuilder.of(DELIMITER, "xxx", "yyy", "zzz").build();

		assertThat(result, equalTo("xxx" + DELIMITER + "yyy" + DELIMITER + "zzz"));
	}

	@Test
	void shouldThrowExceptionIfFactoryMethodIsCalledWithNoPaths() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, DelimitedStringBuilder::of);

		assertThat(e.getMessage(), equalTo("Parameter paths should not be null or empty"));
	}

	@Test
	void shouldThrowExceptionIfFactoryMethodIsCalledWithNullPaths() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> DelimitedStringBuilder.of((String[]) null));

		assertThat(e.getMessage(), equalTo("Parameter paths should not be null or empty"));
	}

	@Test
	void shouldBuildADelimitedStringWithMultiplePathsAsSuffix() {
		String result = DelimitedStringBuilder.builder(DELIMITER)
				.asSuffix()
				.path("xxx", "yyy", "zzz")
				.build();

		assertThat(result, equalTo(DELIMITER + "xxx" + DELIMITER + "yyy" + DELIMITER + "zzz"));
	}

	@Test
	void shouldThrowExceptionIfPathIsNull() {
		DelimitedStringBuilder dsb = DelimitedStringBuilder.builder(DELIMITER);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> dsb.path((String) null));

		assertThat(e.getMessage(), equalTo("Parameter path should not be null"));
	}

	@Test
	void shouldThrowExceptionIfPathsIsNull() {
		DelimitedStringBuilder dsb = DelimitedStringBuilder.builder(DELIMITER);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> dsb.path((String[]) null));

		assertThat(e.getMessage(), equalTo("Parameter paths should not be null"));
	}

}
