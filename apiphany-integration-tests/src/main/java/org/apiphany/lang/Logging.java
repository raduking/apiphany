package org.apiphany.lang;

import java.util.function.Supplier;

/**
 * Utility class for logging purposes.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Logging {

	/**
	 * Returns an object whose {@code toString()} method returns the value provided by the given supplier. This can be used
	 * for lazy evaluation of log messages.
	 *
	 * @param supplier string supplier
	 * @return object with lazy {@code toString()}
	 */
	static Object lazyToString(final Supplier<String> supplier) {
		return new Object() {
			@Override
			public String toString() {
				return supplier.get();
			}
		};
	}
}
