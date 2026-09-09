package org.apiphany.logging.slf4j;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Slf4jLibrary}.
 *
 * @author Radu Sebastian LAZIN
 */
class Slf4jLibraryTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Slf4jLibrary.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldBePresent() {
		assertTrue(Slf4jLibrary.isPresent());
	}

	@Test
	void shouldReturnSlf4jDiagnosticContextFromDescriptor() {
		assertThat(Slf4jLibrary.DIAGNOSTIC_CONTEXT.getSpecificInstance().getClass(), equalTo(Slf4jDiagnosticContext.class));
	}

	@Test
	void shouldReturnSlf4jLoggerAdapterFromDescriptor() {
		assertThat(Slf4jLibrary.LOGGER_ADAPTER.getFacadeClass(), equalTo(Slf4jLoggerAdapter.class));
	}
}
