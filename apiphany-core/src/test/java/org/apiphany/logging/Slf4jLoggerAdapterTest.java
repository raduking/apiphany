package org.apiphany.logging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.morphix.lang.function.LoggerAdapter.LoggingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link Slf4jLoggerAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
class Slf4jLoggerAdapterTest {

	private static final String TEST_MESSAGE = "Test message";

	@Test
	void shouldNotAcceptNullLogger() {
		NullPointerException e = assertThrows(NullPointerException.class, () -> Slf4jLoggerAdapter.of(null));

		assertThat(e.getMessage(), is("logger must not be null"));
	}

	@Test
	void shouldCreateAdapterWithValidLogger() {
		Logger slf4jLogger = LoggerFactory.getLogger(Slf4jLoggerAdapterTest.class);

		Slf4jLoggerAdapter adapter = assertDoesNotThrow(() -> Slf4jLoggerAdapter.of(slf4jLogger));

		assertThat(adapter.getLogger(), is(slf4jLogger));
	}

	@Test
	void shouldDelegateTrace() {
		Logger slf4jLogger = mock(Logger.class);
		Slf4jLoggerAdapter adapter = Slf4jLoggerAdapter.of(slf4jLogger);

		adapter.log(LoggingLevel.TRACE, TEST_MESSAGE);

		verify(slf4jLogger).trace(eq(TEST_MESSAGE), any(Object[].class));
	}

	@Test
	void shouldDelegateDebug() {
		Logger slf4jLogger = mock(Logger.class);
		Slf4jLoggerAdapter adapter = Slf4jLoggerAdapter.of(slf4jLogger);

		adapter.log(LoggingLevel.DEBUG, TEST_MESSAGE);

		verify(slf4jLogger).debug(eq(TEST_MESSAGE), any(Object[].class));
	}

	@Test
	void shouldDelegateInfo() {
		Logger slf4jLogger = mock(Logger.class);
		Slf4jLoggerAdapter adapter = Slf4jLoggerAdapter.of(slf4jLogger);

		adapter.log(LoggingLevel.INFO, TEST_MESSAGE);

		verify(slf4jLogger).info(eq(TEST_MESSAGE), any(Object[].class));
	}

	@Test
	void shouldDelegateWarn() {
		Logger slf4jLogger = mock(Logger.class);
		Slf4jLoggerAdapter adapter = Slf4jLoggerAdapter.of(slf4jLogger);

		adapter.log(LoggingLevel.WARN, TEST_MESSAGE);

		verify(slf4jLogger).warn(eq(TEST_MESSAGE), any(Object[].class));
	}

	@Test
	void shouldDelegateError() {
		Logger slf4jLogger = mock(Logger.class);
		Slf4jLoggerAdapter adapter = Slf4jLoggerAdapter.of(slf4jLogger);

		adapter.log(LoggingLevel.ERROR, TEST_MESSAGE);

		verify(slf4jLogger).error(eq(TEST_MESSAGE), any(Object[].class));
	}
}
