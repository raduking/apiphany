package org.apiphany.json.jackson3;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Jackson3Library}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson2LibraryTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Jackson3Library.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
