package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link DeFactoHeader}.
 *
 * @author Radu Sebastian LAZIN
 */
class DeFactoHeaderTest {

	@ParameterizedTest
	@EnumSource(DeFactoHeader.class)
	void shouldBuildWithFromStringWithValidValueEvenIfUppercase(final DeFactoHeader deFactoHeader) {
		String stringValue = deFactoHeader.value().toUpperCase();
		DeFactoHeader result = DeFactoHeader.fromString(stringValue);

		assertThat(result, equalTo(deFactoHeader));
	}

	@ParameterizedTest
	@EnumSource(DeFactoHeader.class)
	void shouldMatchValidValueEvenIfUppercase(final DeFactoHeader deFactoHeader) {
		String stringValue = deFactoHeader.value().toUpperCase();
		boolean result = deFactoHeader.matches(stringValue);

		assertTrue(result);
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateName() {
		UnsupportedOperationException e = Assertions.assertDefaultConstructorThrows(DeFactoHeader.Name.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
