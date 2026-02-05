package org.apiphany;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apiphany.http.URIEncoder;
import org.apiphany.lang.Require;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.JavaArrays;
import org.morphix.convert.ArrayConversions;
import org.morphix.lang.Nullables;

/**
 * Represents a parameter in the API context. When constructing request parameters prefer using the
 * {@link ParameterFunction} together with {@link RequestParameters} and static imports for better readability. This
 * class exists mainly for cases where debugging or direct usage of parameters is needed.
 *
 * @param name the name of the parameter
 * @param values the values of the parameter
 * @param encoding the character set encoding used for URL encoding, if null {@link Strings#DEFAULT_CHARSET} is used
 *
 * @author Radu Sebastian LAZIN
 */
public record RequestParameter(String name, List<String> values, Charset encoding) implements ParameterFunction {

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
		encoding = Nullables.nonNullOrDefault(encoding, Strings.DEFAULT_CHARSET);
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
		return new RequestParameter(
				String.valueOf(Require.notNull(name, "Parameter name cannot be null")),
				toValues(value),
				Strings.DEFAULT_CHARSET);
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
			String encodedName = URIEncoder.encodeParamName(name, encoding);
			String encodedValue = URIEncoder.encodeParamValue(value, encoding);
			stringBuilder.append(encodedName);
			stringBuilder.append(NAME_VALUE_SEPARATOR);
			stringBuilder.append(encodedValue);
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
	 * Converts the given object to its values list representation suitable for request parameters.
	 *
	 * @param object the object to convert to string parameter value
	 * @return the string representation of the value
	 */
	public static List<String> toValues(final Object object) {
		if (null == object) {
			return null;
		}
		return switch (object) {
			case String string -> toValues(string);
			case Collection<?> collection -> toValues(collection);
			case Iterable<?> iterable -> toValues(iterable);
			case Object[] array -> toValues(array);
			case Object o when o.getClass().isArray() -> toValues(JavaArrays.toArray(o));
			default -> toValues(String.valueOf(object));
		};
	}

	/**
	 * Converts the given string to a list of values suitable for request parameters.
	 *
	 * @param string the string to convert to string parameter value
	 * @return the list containing the string value
	 */
	public static List<String> toValues(final String string) {
		if (null == string) {
			return null;
		}
		List<String> list = new ArrayList<>();
		list.add(string);
		return list;
	}

	/**
	 * Converts the given iterable of values to a list of values suitable for request parameters.
	 *
	 * @param iterable the iterable of objects to convert to string parameter value
	 * @return the list of string representations of the values
	 */
	public static List<String> toValues(final Iterable<?> iterable) {
		if (null == iterable) {
			return null;
		}
		if (iterable instanceof Collection<?> collection) {
			return toValues(collection);
		}
		Iterator<?> iterator = iterable.iterator();
		if (!iterator.hasNext()) {
			return null;
		}
		List<String> list = new ArrayList<>();
		iterator.forEachRemaining(item -> list.add(String.valueOf(item)));
		return list;
	}

	/**
	 * Converts the given collection of values to a list of values suitable for request parameters.
	 *
	 * @param collection the collection of objects to convert to string parameter value
	 * @return the list of string representations of the values
	 */
	public static List<String> toValues(final Collection<?> collection) {
		if (null == collection || collection.isEmpty()) {
			return null;
		}
		List<String> list = new ArrayList<>(collection.size());
		collection.forEach(item -> list.add(String.valueOf(item)));
		return list;
	}

	/**
	 * Converts the given array of values to a list of values suitable for request parameters.
	 *
	 * @param objects the array of objects to convert to string parameter value
	 * @return the comma-separated string representation of the values
	 */
	public static List<String> toValues(final Object[] objects) {
		if (JavaArrays.isEmpty(objects)) {
			return null;
		}
		List<String> list = new ArrayList<>(objects.length);
		return ArrayConversions.convertArray(objects, String::valueOf).to(list);
	}
}
