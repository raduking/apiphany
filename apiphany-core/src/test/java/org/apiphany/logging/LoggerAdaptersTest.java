package org.apiphany.logging;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Supplier;

import org.apiphany.logging.slf4j.Slf4jLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.logging.JulLoggerAdapter;
import org.morphix.reflection.Constructors;
import org.morphix.runtime.OptionalLibrary;

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
	void shouldReturnJulLoggerAdapterOnInitialize() {
		LoggerAdapter adapter = LoggerAdapters.initializeInstance(LoggerAdaptersTest.class, julFallback());

		assertThat(adapter.getClass(), equalTo(JulLoggerAdapter.class));
	}

	@Test
	void shouldReturnJulLoggerAdapterOnInitializeWithNullDescriptors() {
		LoggerAdapter adapter = LoggerAdapters.initializeInstance(LoggerAdaptersTest.class, julFallback(),
				(OptionalLibrary<? extends LoggerAdapter>[]) null);

		assertThat(adapter.getClass(), equalTo(JulLoggerAdapter.class));
	}

	@Test
	void shouldReturnJulLoggerAdapterOnInitializeWhenLibraryIsNotAvailable() {
		LoggerAdapter adapter = LoggerAdapters.initializeInstance(LoggerAdaptersTest.class, julFallback(),
				OptionalLibrary.notPresent(DummyLoggerAdapter.class));

		assertThat(adapter.getClass(), equalTo(JulLoggerAdapter.class));
	}

	@Test
	void shouldInstantiateFacadeWithClassConstructorWhenLibraryIsPresent() {
		LoggerAdapter adapter = LoggerAdapters.initializeInstance(LoggerAdaptersTest.class, julFallback(),
				OptionalLibrary.present(DummyLoggerAdapter.class));

		assertThat(adapter.getClass(), equalTo(DummyLoggerAdapter.class));
	}

	@Test
	void shouldThrowExceptionOnNullClass() {
		NullPointerException e = assertThrows(NullPointerException.class, () -> LoggerAdapters.of(null));

		assertThat(e.getMessage(), equalTo("class must not be null"));
	}

	@Test
	void shouldThrowExceptionOnNullFallbackSupplier() {
		NullPointerException e = assertThrows(NullPointerException.class,
				() -> LoggerAdapters.initializeInstance(LoggerAdaptersTest.class, null));

		assertThat(e.getMessage(), equalTo("fallbackSupplier must not be null"));
	}

	private static Supplier<LoggerAdapter> julFallback() {
		return () -> JulLoggerAdapter.of(LoggerAdaptersTest.class);
	}

	static class DummyLoggerAdapter implements LoggerAdapter {

		DummyLoggerAdapter(@SuppressWarnings("unused") final Class<?> clazz) {
			// empty
		}

		@Override
		public void log(final LoggingLevel level, final String message, final Object... args) {
			// empty
		}

		@Override
		public boolean isEnabled(final LoggingLevel level) {
			return false;
		}
	}
}
