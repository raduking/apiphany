package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link HttpSensitive}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpSensitiveTest {

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateName() {
		UnsupportedOperationException e = Assertions.assertDefaultConstructorThrows(HttpSensitive.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
