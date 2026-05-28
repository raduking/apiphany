package org.apiphany.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ParsingMode}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParsingModeTest {

	@Test
	void shouldHaveStrictAsDefault() {
		assertEquals(ParsingMode.STRICT, ParsingMode.DEFAULT);
	}
}
