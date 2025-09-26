package org.apiphany.lang;

import org.morphix.reflection.Constructors;

/**
 * Utility class for argument and state validations.
 * <p>
 * Provides static methods to check conditions and throw exceptions with formatted messages. This is a lightweight
 * alternative to Guava's {@code Preconditions}.
 * </p>
 * <p>
 * Usage example:
 *
 * <pre>
 * Asserts.checkArgument(value > 0, "Value must be positive but was %d", value);
 * </pre>
 * </p>
 * <p>
 * This class cannot be instantiated.
 * </p>
 *
 * @author Radu Sebastian LAZIN
 */
public final class Assert {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Assert() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Checks that the given condition is {@code true}. If not, throws an {@link IllegalArgumentException} with a formatted
	 * message.
	 *
	 * @param condition the condition to check
	 * @param message the error message template
	 * @param args optional arguments to format the message
	 * @throws IllegalArgumentException if {@code condition} is {@code false}
	 */
	public static void thatArgument(final boolean condition, final String message, final Object... args) {
		if (!condition) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}
}