package org.apiphany.lang;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.morphix.lang.Messages;
import org.morphix.reflection.Constructors;

/**
 * Utility class for enforcing argument and state preconditions.
 * <p>
 * Provides static methods to validate conditions and throw {@link IllegalArgumentException}s with formatted messages.
 * This is a lightweight alternative to Guava's {@code Preconditions}.
 * <p>
 * Note: the name {@code Require} emphasizes the intent of enforcing preconditions even though it is a verb rather than
 * a noun because it conveys the action of requiring certain conditions to be met and aligns with similar utility
 * classes in other libraries. The usage of {@code Require} helps to clearly communicate that the methods within this
 * class are used to enforce requirements on method arguments and state.
 * <p>
 * Usage example:
 *
 * <pre>
 * Require.that(value > 0, "Value must be positive but was {}", value);
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public final class Require {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Require() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Checks that the given condition is {@code true}. If not, throws an exception supplied by the exception instance
	 * function with the given message.
	 *
	 * @param <E> the type of the exception to be thrown
	 *
	 * @param condition the condition to check
	 * @param exceptionInstanceFunction function to create the exception instance
	 * @param message the error message template
	 * @throws E if {@code condition} is {@code false}
	 */
	public static <E extends RuntimeException> void that(final boolean condition, final Function<String, E> exceptionInstanceFunction,
			final String message) {
		if (!condition) {
			throw exceptionInstanceFunction.apply(message);
		}
	}

	/**
	 * Checks that the given condition is {@code true}. If not, throws an exception supplied by the exception instance
	 * function with a formatted message. The message formatting is done using {@link Messages#message(String, Object...)}.
	 *
	 * @param <E> the type of the exception to be thrown
	 *
	 * @param condition the condition to check
	 * @param exceptionInstanceFunction function to create the exception instance
	 * @param message the error message template
	 * @param args optional arguments to format the message
	 * @throws E if {@code condition} is {@code false}
	 * @see Messages#message(String, Object...)
	 */
	public static <E extends RuntimeException> void that(final boolean condition, final Function<String, E> exceptionInstanceFunction,
			final String message, final Object... args) {
		if (!condition) {
			throw exceptionInstanceFunction.apply(Messages.message(message, args));
		}
	}

	/**
	 * Checks that the given condition is {@code true}. If not, throws an {@link IllegalArgumentException} with a formatted
	 * message. The message formatting is done using {@link Messages#message(String, Object...)}.
	 *
	 * @param condition the condition to check
	 * @param message the error message template
	 * @param args optional arguments to format the message
	 * @throws IllegalArgumentException if {@code condition} is {@code false}
	 * @see Messages#message(String, Object...)
	 */
	public static void that(final boolean condition, final String message, final Object... args) {
		that(condition, IllegalArgumentException::new, message, args);
	}

	/**
	 * Checks that the given object satisfies the provided condition. If not, throws an {@link IllegalArgumentException}.
	 * The object is returned if the condition is met. The message formatting is done using
	 * {@link Messages#message(String, Object...)}.
	 *
	 * @param <T> the type of the object to check
	 *
	 * @param object the object to check
	 * @param condition the condition to test
	 * @param message the error message template
	 * @param args optional arguments to format the message
	 * @return the object if the condition is met
	 * @see Messages#message(String, Object...)
	 */
	public static <T> T thatObject(final T object, final Predicate<T> condition, final String message, final Object... args) {
		Require.that(condition.test(Objects.requireNonNull(object, "object cannot be null")), message, args);
		return object;
	}

	/**
	 * Checks that the given condition is {@code false}. If not, throws an {@link IllegalArgumentException} with a formatted
	 * message. The message formatting is done using {@link Messages#message(String, Object...)}.
	 *
	 * @param condition the condition to check
	 * @param message the error message template
	 * @param args optional arguments to format the message
	 * @throws IllegalArgumentException if {@code condition} is {@code true}
	 * @see Messages#message(String, Object...)
	 */
	public static void thatNot(final boolean condition, final String message, final Object... args) {
		that(!condition, message, args);
	}

	/**
	 * Ensures that the provided object reference is not {@code null}. If it is {@code null}, throws an
	 * {@link IllegalArgumentException} with a formatted message. This is different from
	 * {@link Objects#requireNonNull(Object, String)} which throws a {@link NullPointerException}. The message formatting is
	 * done using {@link Messages#message(String, Object...)}.
	 * <p>
	 * This method should be used to validate method arguments and state where {@code null} values are not allowed and an
	 * {@link IllegalArgumentException} is more appropriate than a {@link NullPointerException}.
	 *
	 * @param <T> the type of the object reference
	 *
	 * @param obj the object reference to check
	 * @param message the error message template
	 * @param args optional arguments to format the message
	 * @return the non-null object reference
	 * @throws IllegalArgumentException if {@code obj} is {@code null}
	 * @see Messages#message(String, Object...)
	 */
	public static <T> T notNull(final T obj, final String message, final Object... args) {
		Require.that(null != obj, message, args);
		return obj;
	}
}
