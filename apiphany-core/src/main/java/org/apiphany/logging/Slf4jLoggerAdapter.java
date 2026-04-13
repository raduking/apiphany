package org.apiphany.logging;

import java.util.Objects;

import org.morphix.lang.function.LoggerAdapter;
import org.slf4j.Logger;

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
	 * @see LoggerAdapter#log(LoggingLevel, String, Object...)
	 */
	@Override
	public void log(final LoggingLevel level, final String message, final Object... args) {
		switch (level) {
			case TRACE -> logger.trace(message, args);
			case DEBUG -> logger.debug(message, args);
			case INFO -> logger.info(message, args);
			case WARN -> logger.warn(message, args);
			case ERROR -> logger.error(message, args);
		}
	}

	/**
	 * Returns the underlying SLF4J logger instance.
	 *
	 * @return the SLF4J logger instance to which log messages are delegated
	 */
	public Logger getLogger() {
		return logger;
	}
}
