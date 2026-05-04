package org.apiphany.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link RandomStrings}.
 *
 * @author Radu Sebastian LAZIN
 */
class RandomStringsTest {

	@Test
	void shouldGenerateStringWithTheGivenLenght() {
		int length = 16;
		String randomString = RandomStrings.secureAlphanumeric(length);

		assertNotNull(randomString);
		assertEquals(length, randomString.length());
	}

	@Test
	void shouldGenerateAlphanumericString() {
		String randomString = RandomStrings.secureAlphanumeric(12);

		assertTrue(randomString.matches("^[A-Za-z0-9]+$"));
	}

	@Test
	void shouldThrowExceptionForNegativeLength() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RandomStrings.secureAlphanumeric(-1));

		assertEquals("length must be positive", e.getMessage());
	}

	@Test
	void shouldGenerateDifferentStrings() {
		String randomString1 = RandomStrings.secureAlphanumeric(16);
		String randomString2 = RandomStrings.secureAlphanumeric(16);

		assertNotNull(randomString1);
		assertNotNull(randomString2);
		assertEquals(16, randomString1.length());
		assertEquals(16, randomString2.length());
		assertTrue(randomString1.matches("^[A-Za-z0-9]+$"));
		assertTrue(randomString2.matches("^[A-Za-z0-9]+$"));

		assertNotEquals(randomString1, randomString2);
	}

	@Test
	void shouldNotThrowExceptionForZeroLength() {
		String randomString = RandomStrings.secureAlphanumeric(0);

		assertEquals("", randomString);
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiate() {
		UnsupportedOperationException e = Assertions.assertDefaultConstructorThrows(RandomStrings.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
