package org.apiphany;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Functional interface for defining how parameters are inserted into a map. This interface is used to build parameter
 * maps dynamically.
 * <p>
 * This interface extends {@link ParameterFunction}, inheriting its method for inserting parameters into a map.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface Parameter extends ParameterFunction {

	/**
	 * Cache empty function instance.
	 */
	Parameter EMPTY = map -> {
		// empty
	};

	/**
	 * Creates a {@link Parameter} for a single key-value pair.
	 *
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return a {@link Parameter} that inserts the key-value pair into the map
	 */
	static Parameter of(final String name, final String value) {
		return ParameterFunction.parameter(name, value)::putInto;
	}

	/**
	 * Creates a {@link Parameter} for a single key-value pair, where the value is converted to a string.
	 *
	 * @param <T> the type of the value
	 *
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return a {@link Parameter} that inserts the key-value pair into the map
	 */
	static <T> Parameter of(final String name, final T value) {
		return ParameterFunction.parameter(name, value)::putInto;
	}

	/**
	 * Creates a {@link Parameter} for a single key-value pair, where both the key and value are converted to strings.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the value
	 *
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return a {@link Parameter} that inserts the key-value pair into the map
	 */
	static <T, U> Parameter of(final T name, final U value) {
		return ParameterFunction.parameter(name, value)::putInto;
	}

	/**
	 * Creates a {@link Parameter} for a single key-value pair, where the value is provided by a supplier.
	 *
	 * @param <T> the type of the value
	 *
	 * @param name the parameter name
	 * @param value the supplier of the parameter value
	 * @return a {@link Parameter} that inserts the key-value pair into the map
	 */
	static <T> Parameter of(final String name, final Supplier<T> value) {
		return ParameterFunction.parameter(name, value)::putInto;
	}

	/**
	 * Creates a {@link Parameter} for a single key-value pair, where the value is provided by a supplier.
	 *
	 * @param <T> the type of the name
	 * @param <U> the type of the value
	 *
	 * @param name the parameter name
	 * @param value the supplier of the parameter value
	 * @return a {@link Parameter} that inserts the key-value pair into the map
	 */
	static <T, U> Parameter of(final T name, final Supplier<U> value) {
		return ParameterFunction.parameter(name, value)::putInto;
	}

	/**
	 * Creates a {@link Parameter} for a single key-value pair, where both the key and value are provided by suppliers.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the value
	 *
	 * @param name the supplier of the parameter name
	 * @param value the supplier of the parameter value
	 * @return a {@link Parameter} that inserts the key-value pair into the map
	 */
	static <T, U> Parameter of(final Supplier<T> name, final Supplier<U> value) {
		return ParameterFunction.parameter(name, value)::putInto;
	}

	/**
	 * Creates a {@link Parameter} for a list of elements, which are joined into a single string.
	 *
	 * @param name the parameter name
	 * @param elements the list of elements to join
	 * @return a {@link Parameter} that inserts the key-value pair into the map
	 */
	static Parameter of(final String name, final List<String> elements) {
		return ParameterFunction.parameter(name, elements)::putInto;
	}

	/**
	 * Creates a {@link Parameter} for a list of elements, where both the key and elements are converted to strings.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the elements
	 *
	 * @param name the parameter name
	 * @param elements the list of elements to join
	 * @return a {@link Parameter} that inserts the key-value pair into the map
	 */
	static <T, U> Parameter of(final T name, final List<U> elements) {
		return ParameterFunction.parameter(name, elements)::putInto;
	}

	/**
	 * Creates a {@link Parameter} that delegates to another {@link Parameter}.
	 *
	 * @param parameter the delegate {@link Parameter}
	 * @return a {@link Parameter} that delegates to the provided parameter
	 */
	static Parameter of(final Parameter parameter) {
		return parameter;
	}

	/**
	 * Creates a {@link Parameter} that delegates to another {@link ParameterFunction}.
	 *
	 * @param parameter the delegate {@link Parameter}
	 * @return a {@link Parameter} that delegates to the provided function
	 */
	static Parameter of(final ParameterFunction parameter) {
		return parameter::putInto;
	}

	/**
	 * Creates a {@link Parameter} that inserts all entries from a map.
	 *
	 * @param map the map containing the entries to insert
	 * @return a {@link Parameter} that inserts all entries from the map
	 */
	static Parameter of(final Map<String, List<String>> map) {
		return ParameterFunction.parameters(map)::putInto;
	}

	/**
	 * Creates a {@link Parameter} that inserts all entries from more parameter functions.
	 *
	 * @param parameters the parameters containing the entries to insert
	 * @return a {@link Parameter} that inserts all entries from more parameter functions
	 */
	static Parameter of(final Parameter... parameters) {
		if (null == parameters) {
			return none();
		}
		return map -> {
			for (Parameter parameter : parameters) {
				parameter.putInto(map);
			}
		};
	}

	/**
	 * Returns an empty parameter function.
	 *
	 * @return an empty parameter function
	 */
	static Parameter none() {
		return EMPTY;
	}

	/**
	 * Creates a {@link Parameter} that conditionally inserts parameters based on a condition.
	 *
	 * @param condition the condition to evaluate
	 * @param parameters the {@link Parameter}s to execute if the condition is true
	 * @return a {@link Parameter} that conditionally inserts parameters
	 */
	static Parameter withCondition(final boolean condition, final Parameter... parameters) {
		if (condition) {
			return of(parameters);
		}
		return none();
	}

	/**
	 * Creates a {@link Parameter} that conditionally inserts parameters based on a condition. This is an alias for
	 * {@link #withCondition(boolean, Parameter...)}.
	 *
	 * @param condition the condition to evaluate
	 * @param parameters the {@link Parameter}s to execute if the condition is true
	 * @return a {@link Parameter} that conditionally inserts parameters
	 */
	static Parameter when(final boolean condition, final Parameter... parameters) {
		return withCondition(condition, parameters);
	}

	/**
	 * Creates a {@link Parameter} that conditionally inserts parameters if the provided object is non-null.
	 *
	 * @param <T> the type of the object
	 *
	 * @param obj the object to check for null
	 * @param parameters the {@link Parameter}s to execute if the object is non-null
	 * @return a {@link Parameter} that conditionally inserts parameters
	 */
	static <T> Parameter withNonNull(final T obj, final Parameter... parameters) {
		return withCondition(null != obj, parameters);
	}

	/**
	 * Creates a {@link Parameter} that conditionally inserts parameters based on a predicate.
	 *
	 * @param <T> the type of the object
	 *
	 * @param obj the object to evaluate
	 * @param predicate the predicate to test
	 * @param parameters the {@link Parameter}s to execute if the predicate is true
	 * @return a {@link Parameter} that conditionally inserts parameters
	 */
	static <T> Parameter withPredicate(final T obj, final Predicate<T> predicate, final Parameter... parameters) {
		return withCondition(predicate.test(obj), parameters);
	}

	/**
	 * Creates a {@link Parameter} that conditionally inserts parameters based on a predicate. This is an alias for
	 * {@link #withPredicate(Object, Predicate, Parameter...)}.
	 *
	 * @param <T> the type of the object
	 *
	 * @param obj the object to evaluate
	 * @param predicate the predicate to test
	 * @param parameters the {@link Parameter}s to execute if the predicate is true
	 * @return a {@link Parameter} that conditionally inserts parameters
	 */
	static <T> Parameter when(final T obj, final Predicate<T> predicate, final Parameter... parameters) {
		return withPredicate(obj, predicate, parameters);
	}
}
