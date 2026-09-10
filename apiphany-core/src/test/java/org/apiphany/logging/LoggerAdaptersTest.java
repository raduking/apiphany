package org.apiphany.logging;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.logging.slf4j.Slf4jLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;
import org.morphix.reflection.ReflectionException;

/**
 * Test class for {@link LoggerAdapters}.
 *
 * @author Radu Sebastian LAZIN
 */
class LoggerAdaptersTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException exception = assertDefaultConstructorThrows(LoggerAdapters.class);

		assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldReturnSlf4jLoggerAdapterFromOfWhenSlf4jIsPresent() {
		assertThat(LoggerAdapters.of(LoggerAdaptersTest.class).getClass(), equalTo(Slf4jLoggerAdapter.class));
	}

	@Test
	void shouldThrowReflectionExceptionWithNullPointerExceptionCauseOnNullClass() {
		ReflectionException exception = assertThrows(ReflectionException.class, () -> LoggerAdapters.of(null));

		Throwable cause = Reflection.unwrapException(exception);
		assertThat(cause.getClass(), equalTo(NullPointerException.class));
		assertThat(cause.getMessage(), equalTo("class must not be null"));
	}
}
