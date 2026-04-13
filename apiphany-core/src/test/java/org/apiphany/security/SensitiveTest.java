package org.apiphany.security;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Sensitive}.
 *
 * @author Radu Sebastian LAZIN
 */
class SensitiveTest {

	private static final String REDACTED_VALUE = "-REDACTED-";

	@Test
	void shouldHaveTheRedactedValue() {
		assertThat(Sensitive.Value.REDACTED, equalTo(REDACTED_VALUE));
	}

	@Test
	void shouldThrowExceptionOnCallingValueConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Sensitive.Value.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldThrowExceptionOnCallingFieldConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Sensitive.Field.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
