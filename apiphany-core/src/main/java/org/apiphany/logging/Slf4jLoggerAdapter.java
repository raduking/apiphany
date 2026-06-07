package org.apiphany.logging;

import java.util.Objects;

import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.function.LoggingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLF4J-based implementation of {@link LoggerAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Slf4jLoggerAdapter implements LoggerAdapter {

	/**
	 * The SLF4J logger instance to which log messages will be delegated.
	 */
	private final Logger logger;

	/**
	 * Constructs a new {@code Slf4jLoggerAdapter} with the specified SLF4J logger.
	 *
	 * @param logger the SLF4J logger to which log messages will be delegated
	 * @throws NullPointerException if the provided logger is null
	 */
	private Slf4jLoggerAdapter(final Logger logger) {
		this.logger = Objects.requireNonNull(logger, "logger must not be null");
	}

	/**
	 * Creates a new {@code Slf4jLoggerAdapter} instance that delegates to the specified SLF4J logger.
	 *
	 * @param logger the SLF4J logger to which log messages will be delegated
	 * @return a new {@code Slf4jLoggerAdapter} instance
	 * @throws NullPointerException if the provided logger is null
	 */
	public static Slf4jLoggerAdapter of(final Logger logger) {
		return new Slf4jLoggerAdapter(logger);
	}

	/**
	 * Creates a new {@code Slf4jLoggerAdapter} instance that delegates to an SLF4J logger associated with the specified
	 * class.
	 *
	 * @param clazz the class for which the SLF4J logger will be created
	 * @return a new {@code Slf4jLoggerAdapter} instance
	 * @throws NullPointerException if the provided class is null
	 */
	public static Slf4jLoggerAdapter of(final Class<?> clazz) {
		return of(LoggerFactory.getLogger(Objects.requireNonNull(clazz, "class must not be null")));
	}

	/**
	 * @see LoggerAdapter#log(LoggingLevel, String, Object...)
	 */
	@Override
	public void log(final LoggingLevel level, final String message, final Object... args) {
		LoggingFunction loggingFunction = switch (level) {
			case TRACE -> logger::trace;
			case DEBUG -> logger::debug;
			case INFO -> logger::info;
			case WARN -> logger::warn;
			case ERROR -> logger::error;
		};
		loggingFunction.log(message, args);
	}

	/**
	 * Returns the underlying SLF4J logger instance.
	 *
	 * @return the SLF4J logger instance to which log messages are delegated
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @see LoggerAdapter#isEnabled(LoggingLevel)
	 */
	@Override
	public boolean isEnabled(final LoggingLevel level) {
		return switch (level) {
			case TRACE -> logger.isTraceEnabled();
			case DEBUG -> logger.isDebugEnabled();
			case INFO -> logger.isInfoEnabled();
			case WARN -> logger.isWarnEnabled();
			case ERROR -> logger.isErrorEnabled();
		};
	}
}
