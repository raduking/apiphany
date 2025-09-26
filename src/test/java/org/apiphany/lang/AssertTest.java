package org.apiphany.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Assert}.
 *
 * @author Radu Sebastian LAZIN
 */
class AssertTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Assert.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldThrowExceptionIfConditionIsFalseOnThatArgument() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Assert.thatArgument(false, "%d", 1));

		assertThat(e.getMessage(), equalTo("1"));
	}
}
