package org.apiphany;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apiphany.lang.Require;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.JavaArrays;
import org.morphix.convert.ArrayConversions;

/**
 * Represents a parameter in the API context. When constructing request parameters prefer using the
 * {@link ParameterFunction} together with {@link RequestParameters} and static imports for better readability. This
 * class exists mainly for cases where debugging or direct usage of parameters is needed.
 *
 * @param name the name of the parameter
 * @param values the values of the parameter
 *
 * @author Radu Sebastian LAZIN
 */
public record RequestParameter(String name, List<String> values) implements ParameterFunction {

	/**
	 * Separator between parameter name and value.
	 */
	public static final String NAME_VALUE_SEPARATOR = "=";

	/**
	 * Constructs a {@link RequestParameter} instance ensuring the name is not null or blank.
	 *
	 * @param name the name of the parameter
	 * @param values the value of the parameter
	 */
	public RequestParameter {
		Require.notNull(name, "Parameter name cannot be null");
		Require.thatObject(name, Strings::isNotBlank, "Parameter name cannot be blank");
	}

	/**
	 * Creates a {@link RequestParameter} instance from the given name and value.
	 *
	 * @param <N> the type of the parameter name
	 * @param <V> the type of the parameter value
	 *
	 * @param name the name of the parameter
	 * @param value the value of the parameter
	 * @return a new {@link RequestParameter} instance
	 */
	public static <N, V> RequestParameter of(final N name, final V value) {
		return new RequestParameter(String.valueOf(Require.notNull(name, "Parameter name cannot be null")), values(value));
	}

	/**
	 * Returns the string representation of the parameter in the format {@code "name=value"}.
	 *
	 * @return the string representation of the parameter
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (String value : values) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(RequestParameters.SEPARATOR);
			}
			stringBuilder.append(name);
			stringBuilder.append(NAME_VALUE_SEPARATOR);
			stringBuilder.append(value);
		}
		return stringBuilder.toString();
	}

	/**
	 * Puts this parameter into the given map.
	 *
	 * @param map the map to put the parameter into
	 */
	@Override
	public void putInto(final Map<String, List<String>> map) {
		map.computeIfAbsent(name, key -> new ArrayList<>()).addAll(values);
	}

	/**
	 * Converts the given value to its string representation suitable for request parameters.
	 *
	 * @param object the object to convert to string parameter value
	 * @return the string representation of the value
	 */
	public static List<String> values(final Object object) {
		if (null == object) {
			return null;
		}
		return switch (object) {
			case String string -> List.of(string);
			case Iterable<?> iterable -> value(JavaArrays.toArray(iterable));
			case Object[] array -> value(array);
			case Object o when o.getClass().isArray() -> value(JavaArrays.toArray(o));
			default -> List.of(String.valueOf(object));
		};
	}

	/**
	 * Converts the given array of values to a comma-separated string representation suitable for request parameters.
	 *
	 * @param objects the array of objects to convert to string parameter value
	 * @return the comma-separated string representation of the values
	 */
	public static List<String> value(final Object[] objects) {
		if (null == objects) {
			return Collections.emptyList();
		}
		return ArrayConversions.convertArray(objects, String::valueOf).toList();
	}
}
