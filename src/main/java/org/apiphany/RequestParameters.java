package org.apiphany;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.apiphany.lang.collections.Maps;
import org.morphix.reflection.Constructors;

/**
 * Utility class for building and manipulating request parameters. This class provides methods for creating parameter
 * maps, encoding parameters, and converting them into URL-friendly formats. It also includes a functional interface
 * {@link ParameterFunction} for defining parameter insertion logic.
 *
 * @author Radu Sebastian LAZIN
 */
public class RequestParameters {

	/**
	 * Request parameters separator.
	 */
	public static final String SEPARATOR = "&";

	/**
	 * Functional interface for defining how parameters are inserted into a map. This interface is used to build parameter
	 * maps dynamically.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	public interface ParameterFunction {

		/**
		 * Inserts parameters into the given map.
		 *
		 * @param map the map to insert parameters into
		 */
		void putInto(Map<String, String> map);

		/**
		 * Creates a {@link ParameterFunction} for a single key-value pair.
		 *
		 * @param name the parameter name
		 * @param value the parameter value
		 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
		 */
		static ParameterFunction parameter(final String name, final String value) {
			return map -> map.put(name, value);
		}

		/**
		 * Creates a {@link ParameterFunction} for a single key-value pair, where the value is converted to a string.
		 *
		 * @param <T> the type of the value
		 * @param name the parameter name
		 * @param value the parameter value
		 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
		 */
		static <T> ParameterFunction parameter(final String name, final T value) {
			return parameter(name, String.valueOf(value));
		}

		/**
		 * Creates a {@link ParameterFunction} for a single key-value pair, where both the key and value are converted to
		 * strings.
		 *
		 * @param <T> the type of the key
		 * @param <U> the type of the value
		 * @param name the parameter name
		 * @param value the parameter value
		 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
		 */
		static <T, U> ParameterFunction parameter(final T name, final U value) {
			return parameter(String.valueOf(name), String.valueOf(value));
		}

		/**
		 * Creates a {@link ParameterFunction} for a single key-value pair, where the value is provided by a supplier.
		 *
		 * @param <T> the type of the value
		 * @param name the parameter name
		 * @param value the supplier of the parameter value
		 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
		 */
		static <T> ParameterFunction parameter(final String name, final Supplier<T> value) {
			return parameter(name, String.valueOf(value.get()));
		}

		/**
		 * Creates a {@link ParameterFunction} for a single key-value pair, where the value is provided by a supplier.
		 *
		 * @param <T> the type of the name
		 * @param <U> the type of the value
		 * @param name the parameter name
		 * @param value the supplier of the parameter value
		 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
		 */
		static <T, U> ParameterFunction parameter(final T name, final Supplier<U> value) {
			return parameter(String.valueOf(name), String.valueOf(value.get()));
		}

		/**
		 * Creates a {@link ParameterFunction} for a single key-value pair, where both the key and value are provided by
		 * suppliers.
		 *
		 * @param <T> the type of the key
		 * @param <U> the type of the value
		 * @param name the supplier of the parameter name
		 * @param value the supplier of the parameter value
		 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
		 */
		static <T, U> ParameterFunction parameter(final Supplier<T> name, final Supplier<U> value) {
			return parameter(String.valueOf(name.get()), String.valueOf(value.get()));
		}

		/**
		 * Creates a {@link ParameterFunction} for a filter.
		 *
		 * @param filter the filter to apply
		 * @return a {@link ParameterFunction} that inserts parameters based on the filter
		 */
		static ParameterFunction parameter(final Filter filter) {
			return filter::putInto;
		}

		/**
		 * Creates a {@link ParameterFunction} for a list of elements, which are joined into a single string.
		 *
		 * @param name the parameter name
		 * @param elements the list of elements to join
		 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
		 */
		static ParameterFunction parameter(final String name, final List<String> elements) {
			return map -> {
				if (Lists.isNotEmpty(elements)) {
					map.put(name, String.join(",", elements));
				}
			};
		}

		/**
		 * Creates a {@link ParameterFunction} for a list of elements, where both the key and elements are converted to strings.
		 *
		 * @param <T> the type of the key
		 * @param <U> the type of the elements
		 * @param name the parameter name
		 * @param elements the list of elements to join
		 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
		 */
		static <T, U> ParameterFunction parameter(final T name, final List<U> elements) {
			return parameter(String.valueOf(name), Lists.safe(elements).stream().map(String::valueOf).toList());
		}

		/**
		 * Creates a {@link ParameterFunction} that delegates to another {@link ParameterFunction}.
		 *
		 * @param paramFunction the delegate {@link ParameterFunction}
		 * @return a {@link ParameterFunction} that delegates to the provided function
		 */
		static ParameterFunction parameter(final ParameterFunction paramFunction) {
			return paramFunction::putInto;
		}

		/**
		 * Creates a {@link ParameterFunction} that inserts all entries from a map.
		 *
		 * @param map the map containing the entries to insert
		 * @return a {@link ParameterFunction} that inserts all entries from the map
		 */
		static ParameterFunction parameters(final Map<String, String> map) {
			return m -> m.putAll(map);
		}

		/**
		 * Creates a {@link ParameterFunction} that conditionally inserts parameters based on a condition.
		 *
		 * @param condition the condition to evaluate
		 * @param paramFunctions the {@link ParameterFunction}s to execute if the condition is true
		 * @return a {@link ParameterFunction} that conditionally inserts parameters
		 */
		static ParameterFunction withCondition(final boolean condition, final ParameterFunction... paramFunctions) {
			return map -> {
				if (condition && null != paramFunctions) {
					for (ParameterFunction paramFunction : paramFunctions) {
						paramFunction.putInto(map);
					}
				}
			};
		}

		/**
		 * Creates a {@link ParameterFunction} that conditionally inserts parameters based on a condition. This is an alias for
		 * {@link #withCondition(boolean, ParameterFunction...)}.
		 *
		 * @param condition the condition to evaluate
		 * @param paramFunctions the {@link ParameterFunction}s to execute if the condition is true
		 * @return a {@link ParameterFunction} that conditionally inserts parameters
		 */
		static ParameterFunction when(final boolean condition, final ParameterFunction... paramFunctions) {
			return withCondition(condition, paramFunctions);
		}

		/**
		 * Creates a {@link ParameterFunction} that conditionally inserts parameters if the provided object is non-null.
		 *
		 * @param <T> the type of the object
		 * @param obj the object to check for null
		 * @param paramFunctions the {@link ParameterFunction}s to execute if the object is non-null
		 * @return a {@link ParameterFunction} that conditionally inserts parameters
		 */
		static <T> ParameterFunction withNonNull(final T obj, final ParameterFunction... paramFunctions) {
			return withCondition(null != obj, paramFunctions);
		}

		/**
		 * Creates a {@link ParameterFunction} that conditionally inserts parameters based on a predicate.
		 *
		 * @param <T> the type of the object
		 * @param obj the object to evaluate
		 * @param predicate the predicate to test
		 * @param paramFunctions the {@link ParameterFunction}s to execute if the predicate is true
		 * @return a {@link ParameterFunction} that conditionally inserts parameters
		 */
		static <T> ParameterFunction withPredicate(final T obj, final Predicate<T> predicate, final ParameterFunction... paramFunctions) {
			return withCondition(predicate.test(obj), paramFunctions);
		}

		/**
		 * Creates a {@link ParameterFunction} that conditionally inserts parameters based on a predicate. This is an alias for
		 * {@link #withPredicate(Object, Predicate, ParameterFunction...)}.
		 *
		 * @param <T> the type of the object
		 * @param obj the object to evaluate
		 * @param predicate the predicate to test
		 * @param paramFunctions the {@link ParameterFunction}s to execute if the predicate is true
		 * @return a {@link ParameterFunction} that conditionally inserts parameters
		 */
		static <T> ParameterFunction when(final T obj, final Predicate<T> predicate, final ParameterFunction... paramFunctions) {
			return withPredicate(obj, predicate, paramFunctions);
		}
	}

	/**
	 * Creates a new map and populates it with the given external put functions.
	 *
	 * @param paramFunctions the {@link ParameterFunction}s to execute
	 * @return a new map containing the inserted parameters
	 */
	public static Map<String, String> of(final ParameterFunction... paramFunctions) {
		if (null == paramFunctions || 0 == paramFunctions.length) {
			return Collections.emptyMap();
		}
		var map = new HashMap<String, String>();
		for (ParameterFunction paramFunction : paramFunctions) {
			paramFunction.putInto(map);
		}
		return map;
	}

	/**
	 * Transforms the request parameters map into a string usable in URLs/URIs. The string will start with the {@code '?'}
	 * character, so no additional concatenation is needed. If the map is empty, an empty string is returned.
	 *
	 * @param params the request parameters map
	 * @return a URL-friendly string representation of the parameters
	 */
	public static String asUrlSuffix(final Map<String, String> params) {
		String result = asString(params);
		if (Strings.isNotEmpty(result)) {
			result = "?" + result;
		}
		return result;
	}

	/**
	 * Transforms the request parameters map into a string usable in request bodies. If the map is empty, an empty string is
	 * returned.
	 *
	 * @param params the request parameters map
	 * @return a string representation of the parameters
	 */
	public static String asString(final Map<String, String> params) {
		if (Maps.isEmpty(params)) {
			return "";
		}
		String[] paramsArray = params.entrySet().stream()
				.map(e -> String.join("=", e.getKey(), e.getValue()))
				.toArray(String[]::new);
		return String.join(SEPARATOR, paramsArray);
	}

	/**
	 * Transforms the request parameters string into a request parameters {@link Map}. If the string is empty, an empty
	 * immutable map is returned. It assumes the body character encoding is {@link StandardCharsets#UTF_8}.
	 *
	 * @param body the request parameters string
	 * @return a {@link Map} representation of the parameters
	 */
	public static Map<String, String> from(final String body) {
		return from(body, Strings.DEFAULT_CHARSET);
	}

	/**
	 * Transforms the request parameters string into a request parameters {@link Map}. If the string is empty, an empty
	 * immutable map is returned.
	 *
	 * @param body the request parameters string
	 * @param encoding the body character encoding
	 * @return a {@link Map} representation of the parameters
	 */
	public static Map<String, String> from(final String body, final Charset encoding) {
		if (Strings.isEmpty(body)) {
			return Collections.emptyMap();
		}
		String[] params = body.split(SEPARATOR);
		Map<String, String> paramsMap = new HashMap<>();

		for (String param : params) {
			String[] pair = param.split("=");
			if (pair.length == 2) {
				String value = URLDecoder.decode(pair[1], encoding);
				paramsMap.put(pair[0], value);
			}
		}
		return paramsMap;
	}

	/**
	 * Encodes the given request parameters using UTF-8 encoding. Returns a new map with all parameters encoded.
	 *
	 * @param requestParameters the request parameters to encode
	 * @return a new map containing the encoded parameters
	 */
	public static Map<String, String> encode(final Map<String, String> requestParameters) {
		return encode(requestParameters, Strings.DEFAULT_CHARSET);
	}

	/**
	 * Encodes the given request parameters using the specified character set. Returns a new map with all parameters
	 * encoded.
	 *
	 * @param requestParameters the request parameters to encode
	 * @param encoding the character set to use for encoding
	 * @return a new map containing the encoded parameters
	 */
	public static Map<String, String> encode(final Map<String, String> requestParameters, final Charset encoding) {
		Map<String, String> encodedParams = HashMap.newHashMap(requestParameters.size());
		requestParameters.forEach((k, v) -> {
			String name = URLEncoder.encode(k, encoding);
			String value = URLEncoder.encode(v, encoding);
			encodedParams.put(name, value);
		});
		return encodedParams;
	}

	/**
	 * Hide constructor.
	 */
	private RequestParameters() {
		throw Constructors.unsupportedOperationException();
	}
}
