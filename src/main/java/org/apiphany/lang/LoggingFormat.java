package org.apiphany.lang;

import java.util.Map;

import org.morphix.lang.Enums;
import org.morphix.lang.Nullables;

/**
 * Represents the available formats for logging output.
 * <p>
 * This enum defines the supported logging formats that can be used to output data in different representations.
 *
 * @author Radu Sebastian LAZIN
 */
public enum LoggingFormat {

	/**
	 * Hexadecimal format logging. Outputs data as hexadecimal strings, useful for binary data inspection.
	 */
	HEX("hex"),

	/**
	 * JSON format logging. Outputs data as structured JSON, useful for machine-readable logs.
	 */
	JSON("json");

	/**
	 * The default logging format (JSON).
	 */
	public static final LoggingFormat DEFAULT = JSON;

	/**
	 * Map of format names to enum constants for case-insensitive lookup.
	 */
	private static final Map<String, LoggingFormat> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The display label for the format.
	 */
	private final String label;

	/**
	 * Constructs a logging format enum constant.
	 *
	 * @param label the display label for the format
	 */
	LoggingFormat(final String label) {
		this.label = label;
	}

	/**
	 * Returns the format's display label.
	 *
	 * @return the format label string
	 */
	@Override
	public String toString() {
		return getLabel();
	}

	/**
	 * Returns the format's display label.
	 *
	 * @return the format label string
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Converts a string to a LoggingFormat enum value.
	 * <p>
	 * The lookup is case-insensitive. Returns the DEFAULT format if the input doesn't match any format.
	 *
	 * @param input the string to convert
	 * @return the matching LoggingFormat or DEFAULT if no match found
	 */
	public static LoggingFormat fromString(final String input) {
		return Nullables.whenNotNull(input)
				.thenYield(i -> NAME_MAP.getOrDefault(i.toLowerCase(), DEFAULT))
				.orElse(DEFAULT);
	}
}
