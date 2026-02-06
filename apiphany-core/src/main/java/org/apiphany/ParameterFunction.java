package org.apiphany;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiphany.lang.Require;
import org.apiphany.lang.collections.Lists;
import org.apiphany.openapi.MultiValueStrategy;

/**
 * Functional interface for defining how parameters are inserted into a map. This interface is used to build parameter
 * maps dynamically.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ParameterFunction extends Consumer<Map<String, List<String>>> {

	/**
	 * Cache empty function instance.
	 */
	ParameterFunction EMPTY = map -> {
		// empty
	};

	/**
	 * Inserts parameters into the given map.
	 *
	 * @param map the map to insert parameters into
	 */
	void putInto(Map<String, List<String>> map);

	/**
	 * @see Consumer#accept(Object)
	 */
	@Override
	default void accept(final Map<String, List<String>> map) {
		putInto(map);
	}

	/**
	 * Inserts a single key-value pair into the given map.
	 *
	 * @param map the map to insert the key-value pair into
	 * @param name the parameter name
	 * @param value the parameter value
	 * @throws IllegalArgumentException if the name or value is null
	 */
	static void insertInto(final Map<String, List<String>> map, final String name, final String value) {
		Require.notNull(name, "Parameter name cannot be null");
		Require.notNull(value, "Parameter value cannot be null");
		map.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
	}

	/**
	 * Inserts multiple values for a single key into the given map.
	 *
	 * @param map the map to insert the key-value pairs into
	 * @param name the parameter name
	 * @param values the parameter values
	 * @throws IllegalArgumentException if the name is null or if the values list is null or empty
	 */
	static void insertInto(final Map<String, List<String>> map, final String name, final List<String> values) {
		Require.notNull(name, "Parameter name cannot be null");
		Require.that(Lists.isNotEmpty(values), "Parameter values cannot be null or empty");
		map.computeIfAbsent(name, key -> new ArrayList<>()).addAll(values);
	}

	/**
	 * Creates a {@link ParameterFunction} for a list of elements, using the specified multi-value encoding strategy.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the elements
	 *
	 * @param name the parameter name
	 * @param elements the list of elements to join
	 * @param multiValueStrategy the multi-value encoding strategy
	 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
	 */
	static <T, U> ParameterFunction parameter(final T name, final List<U> elements, final MultiValueStrategy multiValueStrategy) {
		return multiValueStrategy.apply(name, elements);
	}

	/**
	 * Creates a {@link ParameterFunction} for a list of elements, where both the key and elements are converted to strings.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the elements
	 *
	 * @param name the parameter name
	 * @param elements the list of elements to join
	 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
	 */
	static <T, U> ParameterFunction parameter(final T name, final List<U> elements) {
		return parameter(name, elements, MultiValueStrategy.MULTI);
	}

	/**
	 * Creates a {@link ParameterFunction} for a single key-value pair, where both the key and value are converted to
	 * strings.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the value
	 *
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
	 */
	static <T, U> ParameterFunction parameter(final T name, final U value) {
		if (null == value) {
			return ignored();
		}
		// optimize for single value
		if (RequestParameter.isSingleValued(value)) {
			return map -> insertInto(map, String.valueOf(name), String.valueOf(value));
		}
		return parameter(name, RequestParameter.toValues(value));
	}

	/**
	 * Creates a {@link ParameterFunction} for a single key-value pair, where the value is provided by a supplier.
	 *
	 * @param <T> the type of the name
	 * @param <U> the type of the value
	 *
	 * @param name the parameter name
	 * @param value the supplier of the parameter value
	 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
	 */
	static <T, U> ParameterFunction parameter(final T name, final Supplier<U> value) {
		return parameter(name, value.get());
	}

	/**
	 * Creates a {@link ParameterFunction} for a single key-value pair, where both the key and value are provided by
	 * suppliers.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the value
	 *
	 * @param name the supplier of the parameter name
	 * @param value the supplier of the parameter value
	 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
	 */
	static <T, U> ParameterFunction parameter(final Supplier<T> name, final Supplier<U> value) {
		return parameter(name.get(), value.get());
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
	 * @param params the map containing the parameters to insert
	 * @return a {@link ParameterFunction} that inserts all entries from the map
	 */
	static ParameterFunction parameters(final Map<String, List<String>> params) {
		return map -> {
			for (Map.Entry<String, List<String>> entry : params.entrySet()) {
				insertInto(map, entry.getKey(), entry.getValue());
			}
		};
	}

	/**
	 * Creates a {@link ParameterFunction} that inserts all entries from more parameter functions.
	 *
	 * @param paramFunctions the parameters containing the entries to insert
	 * @return a {@link ParameterFunction} that inserts all entries from more parameter functions
	 */
	static ParameterFunction parameters(final ParameterFunction... paramFunctions) {
		if (null == paramFunctions) {
			return ignored();
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
	static ParameterFunction ignored() {
		return EMPTY;
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
		return ignored();
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
	 *
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
	 *
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
	 *
	 * @param obj the object to evaluate
	 * @param predicate the predicate to test
	 * @param paramFunctions the {@link ParameterFunction}s to execute if the predicate is true
	 * @return a {@link ParameterFunction} that conditionally inserts parameters
	 */
	static <T> ParameterFunction when(final T obj, final Predicate<T> predicate, final ParameterFunction... paramFunctions) {
		return withPredicate(obj, predicate, paramFunctions);
	}
}
