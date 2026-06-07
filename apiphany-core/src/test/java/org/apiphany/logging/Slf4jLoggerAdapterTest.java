package org.apiphany.logging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
		NullPointerException e = assertThrows(NullPointerException.class, () -> Slf4jLoggerAdapter.of((Logger) null));

		assertThat(e.getMessage(), is("logger must not be null"));
	}

	@Test
	void shouldNotAcceptNullClass() {
		NullPointerException e = assertThrows(NullPointerException.class, () -> Slf4jLoggerAdapter.of((Class<?>) null));

		assertThat(e.getMessage(), is("class must not be null"));
	}

	@Test
	void shouldCreateAdapterWithValidLogger() {
		Logger slf4jLogger = LoggerFactory.getLogger(Slf4jLoggerAdapterTest.class);

		Slf4jLoggerAdapter adapter = assertDoesNotThrow(() -> Slf4jLoggerAdapter.of(slf4jLogger));

		assertThat(adapter.getLogger(), is(slf4jLogger));
	}

	@Test
	void shouldCreateAdapterWithValidClass() {
		Slf4jLoggerAdapter adapter = assertDoesNotThrow(() -> Slf4jLoggerAdapter.of(Slf4jLoggerAdapterTest.class));

		assertThat(adapter.getLogger().getName(), is(Slf4jLoggerAdapterTest.class.getName()));
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

	@ParameterizedTest
	@EnumSource(LoggingLevel.class)
	void shouldReturnTrueWhenLoggingLevelIsEnabled(final LoggingLevel level) {
		Logger slf4jLogger = mock(Logger.class);
		enable(slf4jLogger, level, true);
		Slf4jLoggerAdapter adapter = Slf4jLoggerAdapter.of(slf4jLogger);

		boolean enabled = adapter.isEnabled(level);

		assertTrue(enabled);
	}

	@ParameterizedTest
	@EnumSource(LoggingLevel.class)
	void shouldReturnFalseWhenLoggingLevelIsDisabled(final LoggingLevel level) {
		Logger slf4jLogger = mock(Logger.class);
		enable(slf4jLogger, level, false);
		Slf4jLoggerAdapter adapter = Slf4jLoggerAdapter.of(slf4jLogger);

		boolean enabled = adapter.isEnabled(level);

		assertFalse(enabled);
	}

	private static void enable(final Logger logger, final LoggingLevel level, final boolean enabled) {
		switch (level) {
			case TRACE -> doReturn(enabled).when(logger).isTraceEnabled();
			case DEBUG -> doReturn(enabled).when(logger).isDebugEnabled();
			case INFO -> doReturn(enabled).when(logger).isInfoEnabled();
			case WARN -> doReturn(enabled).when(logger).isWarnEnabled();
			case ERROR -> doReturn(enabled).when(logger).isErrorEnabled();
			default -> throw new IllegalStateException("Unexpected value: " + level);
		}
	}
}
