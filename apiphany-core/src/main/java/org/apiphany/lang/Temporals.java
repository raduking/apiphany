package org.apiphany.lang;

import java.time.Duration;
import java.util.Locale;

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
		long seconds = duration.getSeconds();
		int millis = duration.getNano() / 1_000_000;
		return seconds + millis / 1000.0;
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
	 * Formats a double duration in seconds with 3 decimals. This method always uses {@code .} as decimal separator.
	 * <p>
	 * Returns "N/A" if the value is equal to {@link Double#NaN}.
	 *
	 * @param duration duration in seconds as double
	 * @return the duration formatted to seconds.
	 */
	static String formatToSeconds(final double duration) {
		return formatToSeconds(duration, Locale.ROOT);
	}

	/**
	 * Formats a double duration in seconds with 3 decimals.
	 * <p>
	 * Returns "N/A" if the value is equal to {@link Double#NaN}.
	 *
	 * @param duration duration in seconds as double
	 * @param locale the locale used for formatting
	 * @return the duration formatted to seconds.
	 */
	static String formatToSeconds(final double duration, final Locale locale) {
		if (Double.isNaN(duration)) {
			return "N/A";
		}
		return String.format(locale, "%.3fs", duration);
	}

	/**
	 * Parses a simple duration format like "10s", "5m", "2h" or "7d".
	 *
	 * @param input the input string
	 * @return the parsed duration
	 * @throws IllegalArgumentException if the format is unsupported
	 * @throws NumberFormatException if the numeric part of the input is not a valid number
	 */
	public static Duration parseSimpleDuration(final String input) {
		if (Strings.isBlank(input)) {
			throw new IllegalArgumentException("Input cannot be empty or null");
		}
		char unit = input.charAt(input.length() - 1);
		String numberPart = input.substring(0, input.length() - 1);
		long value = Long.parseLong(numberPart);
		return switch (unit) {
			case 's' -> Duration.ofSeconds(value);
			case 'm' -> Duration.ofMinutes(value);
			case 'h' -> Duration.ofHours(value);
			case 'd' -> Duration.ofDays(value);
			default -> throw new IllegalArgumentException("Unsupported unit: " + unit + " in: " + input);
		};
	}
}
