package org.apiphany.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link ContentType}.
 *
 * @author Radu Sebastian LAZIN
 */
class ContentTypeTest {

	@Test
	void shouldThrowExceptionOnCallingValueConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(ContentType.Value.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@ParameterizedTest
	@EnumSource(ContentType.class)
	void shouldCheckInValues(final ContentType contentType) {
		boolean result = contentType.in(contentType.value());

		assertTrue(result);
	}

	@ParameterizedTest
	@EnumSource(ContentType.class)
	void shouldReturnThisOnContentType(final ContentType contentType) {
		boolean result = contentType == contentType.contentType();

		assertTrue(result);
	}
}
