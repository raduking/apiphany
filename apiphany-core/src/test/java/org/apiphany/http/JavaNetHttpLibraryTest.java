package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link JavaNetHttpLibrary}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpLibraryTest {

	@Test
	void shouldHaveClientName() {
		assertEquals("java-net-http", JavaNetHttpLibrary.CLIENT_NAME);
	}

	@Test
	void shouldBePresent() {
		assertTrue(JavaNetHttpLibrary.isPresent());
	}

	@Test
	void shouldThrowExceptionOnInstantiation() {
		UnsupportedOperationException exception = Assertions.assertDefaultConstructorThrows(JavaNetHttpLibrary.class);

		assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
