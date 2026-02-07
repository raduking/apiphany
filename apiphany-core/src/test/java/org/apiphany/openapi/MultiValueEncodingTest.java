package org.apiphany.openapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link MultiValueStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class MultiValueEncodingTest {

	@ParameterizedTest
	@EnumSource(MultiValueStrategy.class)
	void shouldHaveNonNullStyleAndSeparator(final MultiValueStrategy encoding) {
		assertNotNull(encoding.style(), "Style should not be null");
		assertNotNull(encoding.separator(), "Separator should not be null");
	}

}
