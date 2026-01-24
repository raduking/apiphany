package org.apiphany.lang;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Require}.
 *
 * @author Radu Sebastian LAZIN
 */
class RequireTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Require.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldThrowExceptionIfConditionIsFalseOnThatArgument() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Require.that(false, "{}", 1));

		assertThat(e.getMessage(), equalTo("1"));
	}

	@Test
	void shouldNotThrowExceptionIfConditionIsTrueOnThatArgument() {
		assertDoesNotThrow(() -> Require.that(true, "This should not throw"));
	}

	@Test
	void shouldNotThrowExceptionIfConditionIsFalseOnThatNotArgument() {
		assertDoesNotThrow(() -> Require.thatNot(false, "This should not throw"));
	}

	@Test
	void shouldThrowExceptionIfConditionIsTrueOnThatNotArgument() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Require.thatNot(true, "{}", 1));

		assertThat(e.getMessage(), equalTo("1"));
	}

	@Test
	void shouldReturnObjectIfConditionIsMet() {
		String result = Require.that("test", obj -> obj.length() == 4, "Length must be 4 but was {}", "test".length());

		assertThat(result, equalTo("test"));
	}

	@Test
	void shouldThrowExceptionIfConditionIsNotMet() {
		Predicate<String> condition = obj -> obj.length() != 4;
		int length = "test".length();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> Require.that("test", condition, "Length must not be 4 but was {}", length));

		assertThat(e.getMessage(), equalTo("Length must not be 4 but was 4"));
	}

	@Test
	void shouldThrowExceptionIfObjectIsNull() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Require.notNull(null, "Object must not be null"));

		assertThat(e.getMessage(), equalTo("Object must not be null"));
	}

	@Test
	void shouldReturnObjectIfNotNull() {
		String result = Require.notNull("test", "Object must not be null");

		assertThat(result, equalTo("test"));
	}
}
