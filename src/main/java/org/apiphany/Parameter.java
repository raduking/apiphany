package org.apiphany;

import java.util.Map;
import java.util.Objects;

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
public record Parameter(String name, String value) implements ParameterFunction {

	/**
	 * Constructs a {@link Parameter} instance ensuring the name is not null or blank.
	 *
	 * @param name the name of the parameter
	 * @param value the value of the parameter
	 */
	public Parameter {
		Objects.requireNonNull(name, "parameter name cannot be null");
		if (Strings.isBlank(name)) {
			throw new IllegalArgumentException("Parameter name cannot be blank");
		}
	}

	/**
	 * Creates a {@link Parameter} instance from the given name and value.
	 *
	 * @param <N> the type of the parameter name
	 * @param <V> the type of the parameter value
	 *
	 * @param name the name of the parameter
	 * @param value the value of the parameter
	 * @return a new {@link Parameter} instance
	 */
	public static <N, V> Parameter of(final N name, final V value) {
		return new Parameter(String.valueOf(Objects.requireNonNull(name, "parameter name cannot be null")), value(value));
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
	 * Puts this parameter into the given map.
	 *
	 * @param map the map to put the parameter into
	 */
	@Override
	public void putInto(final Map<String, String> map) {
		map.put(name, value);
	}

	/**
	 * Converts the given value to its string representation suitable for request parameters.
	 *
	 * @param obj the object to convert to string parameter value
	 * @return the string representation of the value
	 */
	public static String value(final Object obj) {
		if (null == obj) {
			return null;
		}
		return switch (obj) {
			case String str -> str;
			case Iterable<?> iterable -> value(JavaArrays.toArray(iterable));
			case Object[] array -> value(array);
			case Object o when o.getClass().isArray() -> value(JavaArrays.toArray(o));
			default -> String.valueOf(obj);
		};
	}

	/**
	 * Converts the given array of values to a comma-separated string representation suitable for request parameters.
	 *
	 * @param objs the array of objects to convert to string parameter value
	 * @return the comma-separated string representation of the values
	 */
	public static String value(final Object[] objs) {
		if (null == objs) {
			return null;
		}
		return String.join(",", ArrayConversions.convertArray(objs, String::valueOf).toArray(String[]::new));
	}
}
