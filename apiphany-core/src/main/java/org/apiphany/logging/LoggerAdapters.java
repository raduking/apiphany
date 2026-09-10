package org.apiphany.logging;

import org.apiphany.logging.slf4j.Slf4jLibrary;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.logging.JulLoggerAdapter;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.TypedArguments;
import org.morphix.runtime.Libraries;

/**
 * Factory for {@link LoggerAdapter} instances with optional library-specific implementations.
 * <p>
 * This class decouples application code from a concrete logging library. When SLF4J is present on the classpath,
 * {@link #of(Class)} returns an SLF4J-backed adapter. Otherwise it falls back to Morphix's {@link JulLoggerAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
public class LoggerAdapters {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private LoggerAdapters() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a {@link LoggerAdapter} for the given class.
	 *
	 * @param clazz the class for which the logger will be created
	 * @return a logger adapter
	 * @throws NullPointerException if the provided class is null
	 */
	public static LoggerAdapter of(final Class<?> clazz) {
		return Libraries.instance(TypedArguments.of(Class.class, clazz), () -> JulLoggerAdapter.of(clazz), Slf4jLibrary.LOGGER_ADAPTER);
	}
}
