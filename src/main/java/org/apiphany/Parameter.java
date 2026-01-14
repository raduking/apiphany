package org.apiphany;

import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.JavaArrays;
import org.morphix.convert.ArrayConversions;

/**
 * Represents a parameter in the API context.
 *
 * @param name the name of the parameter
 * @param value the value of the parameter
 *
 * @author Radu Sebastian LAZIN
 */
public record Parameter(String name, String value) {

	/**
	 * Constructs a {@link Parameter} instance ensuring the name is not null or blank.
	 *
	 * @param name the name of the parameter
	 * @param value the value of the parameter
	 */
	public Parameter {
		if (Strings.isBlank(name)) {
			throw new IllegalArgumentException("Parameter name cannot be null or blank");
		}
	}

	/**
	 * Creates a {@link Parameter} instance from the given name and value.
	 *
	 * @param name the name of the parameter
	 * @param value the value of the parameter
	 * @return a new {@link Parameter} instance
	 */
	public static Parameter of(final String name, final Object value) {
		return new Parameter(name, value(value));
	}

	/**
	 * Returns the string representation of the parameter in the format {@code "name=value"}.
	 *
	 * @return the string representation of the parameter
	 */
	@Override
	public final String toString() {
		return String.join("=", name, value);
	}

	/**
	 * Converts the given value to its string representation suitable for request parameters.
	 *
	 * @param value the value to convert
	 * @return the string representation of the value
	 */
	public static String value(final Object value) {
		if (null == value) {
			return null;
		}
		if (value.getClass().isArray()) {
			return value(JavaArrays.toArray(value));
		}
		return switch (value) {
			case String str -> str;
			case Iterable<?> iterable -> value(JavaArrays.toArray(iterable));
			default -> String.valueOf(value);
		};
	}

	/**
	 * Converts the given array of values to a comma-separated string representation suitable for request parameters.
	 *
	 * @param values the array of values to convert
	 * @return the comma-separated string representation of the values
	 */
	public static String value(final Object[] values) {
		if (null == values) {
			return null;
		}
		return String.join(",", ArrayConversions.convertArray(values, String::valueOf).toArray(String[]::new));
	}
}
