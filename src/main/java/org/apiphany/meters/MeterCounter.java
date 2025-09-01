package org.apiphany.meters;

/**
 * A specialized {@link Meter} that represents a monotonically increasing counter.
 * <p>
 * Counters are used to measure the <em>rate of change</em> or total occurrences of an event over time. They are
 * cumulative: the value only increases and is never reset except when the application restarts or the registry is
 * cleared.
 * <p>
 * Typical use cases include tracking the number of:
 * <ul>
 * <li>Requests processed</li>
 * <li>Errors encountered</li>
 * <li>Retries attempted</li>
 * </ul>
 *
 * <h2>Usage example:</h2>
 *
 * <pre>{@code
 * MeterCounter retries = registry.counter("retries.total");
 *
 * // Increment by one
 * retries.increment();
 *
 * // Increment by a custom amount
 * retries.increment(3.0);
 *
 * // Retrieve current count
 * double totalRetries = retries.count();
 * }</pre>
 *
 * @author Radu Sebastian LAZIN
 */
public interface MeterCounter extends Meter {

	/**
	 * Increments the counter by the given {@code amount}.
	 * <p>
	 * The amount must be non-negative. Passing a negative amount may result in an exception or undefined behavior depending
	 * on the underlying implementation.
	 *
	 * @param amount the non-negative amount to increment by
	 */
	void increment(double amount);

	/**
	 * Returns the current cumulative count for this counter.
	 * <p>
	 * The returned value is monotonically increasing and will never decrease during the lifetime of the application.
	 *
	 * @return the total count observed so far
	 */
	double count();

	/**
	 * Increments the counter by {@code 1}.
	 * <p>
	 * Equivalent to calling {@link #increment(double)} with {@code amount = 1.0}.
	 */
	default void increment() {
		increment(1.0);
	}

}
