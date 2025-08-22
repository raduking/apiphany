package org.apiphany;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiphany.lang.collections.Lists;

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
		return paramFunction;
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
	 * Creates a {@link ParameterFunction} that inserts all entries from more parameter functions.
	 *
	 * @param parameters the parameters containing the entries to insert
	 * @return a {@link ParameterFunction} that inserts all entries from more parameter functions
	 */
	static ParameterFunction parameters(final ParameterFunction... paramFunctions) {
		if (null == paramFunctions) {
			return none();
		}
		return map -> {
			for (ParameterFunction paramFunction : paramFunctions) {
				paramFunction.putInto(map);
			}
		};
	}

	/**
	 * Returns an empty parameter function.
	 *
	 * @return an empty parameter function
	 */
	static ParameterFunction none() {
		return map -> {
			// empty
		};
	}

	/**
	 * Creates a {@link ParameterFunction} that conditionally inserts parameters based on a condition.
	 *
	 * @param condition the condition to evaluate
	 * @param paramFunctions the {@link ParameterFunction}s to execute if the condition is true
	 * @return a {@link ParameterFunction} that conditionally inserts parameters
	 */
	static ParameterFunction withCondition(final boolean condition, final ParameterFunction... paramFunctions) {
		if (condition) {
			return parameters(paramFunctions);
		}
		return none();
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
