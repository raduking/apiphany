package org.apiphany.lang;

import java.time.Duration;

/**
 * Utility methods for handling date/time/duration.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Temporals {

	/**
	 * Converts a {@link Duration} object to double, the whole part representing the seconds.
	 *
	 * @param duration java duration object
	 * @return duration as double
	 */
	static Double toDouble(final Duration duration) {
		String millis = String.format("%03d", duration.getNano() / 1_000_000);
		return Double.parseDouble(duration.getSeconds() + "." + millis);
	}

	/**
	 * Transforms a duration in milliseconds to a duration in seconds as a double type, the whole part being the seconds.
	 *
	 * @param millis duration in milliseconds
	 * @return duration in seconds
	 */
	static double toSeconds(final long millis) {
		return millis / 1000.0;
	}

	/**
	 * Transforms a duration to a duration in seconds as a double type, the whole part being the seconds and the decimal
	 * part being the milliseconds.
	 *
	 * @param duration duration in milliseconds
	 * @return duration in seconds
	 */
	static double toSeconds(final Duration duration) {
		return toSeconds(duration.toMillis());
	}

	/**
	 * Formats a double duration in seconds with 3 decimals.
	 * <p>
	 * Returns "N/A" if the value is equal to {@link Double#NaN}.
	 *
	 * @return the duration formatted to seconds.
	 */
	static String formatToSeconds(final double duration) {
		if (Double.isNaN(duration)) {
			return "N/A";
		}
		return String.format("%.3fs", duration);
	}
}
