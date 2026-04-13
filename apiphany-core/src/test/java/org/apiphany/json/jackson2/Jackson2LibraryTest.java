package org.apiphany.json.jackson2;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apiphany.json.JsonBuilder;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Jackson2Library}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson2LibraryTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Jackson2Library.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldHaveDescriptor() {
		// because both Jackson 2 and Jackson 3 libraries are present in the classpath, the descriptor should be present
		assertThat(Jackson2Library.isPresent(), equalTo(true));
	}

	@Test
	void shouldReturnSingletonInstanceOfJsonBuilder() {
		JsonBuilder jsonBuilder1 = Jackson2Library.DESCRIPTOR.getSpecificInstance();
		JsonBuilder jsonBuilder2 = Jackson2JsonBuilder.instance();

		assertThat(jsonBuilder1, equalTo(jsonBuilder2));
	}
}
