package org.apiphany.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Logging}.
 *
 * @author Radu Sebastian LAZIN
 */
class LoggingTest {

	@Test
	void shouldThrowExceptionOnInstantiatingIncludeDefault() {
		UnsupportedOperationException exception = Assertions.assertDefaultConstructorThrows(Logging.Include.Default.class);

		assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
