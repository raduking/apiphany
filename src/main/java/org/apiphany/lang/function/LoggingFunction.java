package org.apiphany.lang.function;

/**
 * Functional interface for logging messages with a specific format and arguments.
 * <p>
 * This interface assumes a logging mechanism where messages can be formatted similarly to SLF4J.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface LoggingFunction {

	/**
	 * Logs a message with the specified format and arguments.
	 *
	 * @param format the log message format.
	 * @param arguments the arguments to include in the log message.
	 */
	void log(String format, Object... arguments);
}
