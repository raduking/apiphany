package org.apiphany.lang;

import java.time.Duration;

/**
 * Utility methods for handling date/time.
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
	public static Double toDouble(final Duration duration) {
		String millis = String.format("%03d", duration.getNano() / 1_000_000);
		return Double.parseDouble(duration.getSeconds() + "." + millis);
	}

	/**
	 * Transforms a duration in milliseconds to a duration in seconds
	 * as a double type, the decimal part being the seconds.
	 *
	 * @param millis duration in milliseconds
	 * @return duration in seconds
	 */
	public static double toSeconds(final long millis) {
		return millis / 1000.0;
	}

}
