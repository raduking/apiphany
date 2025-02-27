package org.apiphany.http;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apiphany.lang.Strings;

/**
 * Filter encapsulation.
 * <p>
 * Builds a request parameter named <code>filter</code> and a value. By default, it helps as a builder for FIQL/RSQL
 * queries but can be extended to use any other operators for any queries.
 *
 * @param value filter value.
 *
 * @author Radu Sebastian LAZIN
 */
public record Filter(String value) implements RequestParameters.ParameterFunction {

	/**
	 * Basic filtering operations enumeration. Represents logical and comparison operators used in expressions or
	 * conditions. Each operator has a corresponding string representation.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public enum Operator {

		/**
		 * Equal operator.
		 */
		EQ("=="),

		/**
		 * Not equal operator.
		 */
		NE("!="),

		/**
		 * Greater than operator.
		 */
		GT(">"),

		/**
		 * Less than operator.
		 */
		LT("<"),

		/**
		 * Greater than or equal operator.
		 */
		GE(">="),

		/**
		 * Less than or equal operator.
		 */
		LE("<="),

		/**
		 * Logical OR operator.
		 */
		OR(","),

		/**
		 * Logical AND operator.
		 */
		AND(";");

		/**
		 * The string representation of the operator.
		 */
		private final String value;

		/**
		 * Constructs an {@link Operator} with the specified string representation.
		 *
		 * @param value the string representation of the operator.
		 */
		Operator(final String value) {
			this.value = value;
		}

		/**
		 * Returns the string representation of the operator.
		 *
		 * @return the string representation of the operator.
		 */
		@Override
		public String toString() {
			return getValue();
		}

		/**
		 * Returns the string representation of the operator.
		 *
		 * @return the string representation of the operator.
		 */
		public String getValue() {
			return value;
		}
	}

	/**
	 * Filtering parameter name.
	 */
	public static final String NAME = "filter";

	/**
	 * No filter object.
	 */
	private static final Filter NO_FILTER = new Filter(null);

	/**
	 * Returns the value of the filter.
	 *
	 * @return the value of the filter
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns an empty filter object.
	 *
	 * @return an empty filter object
	 */
	public static Filter none() {
		return NO_FILTER;
	}

	/**
	 * Creates a filtering object with the filter value as string: filter=value.
	 *
	 * @param value value of the filter
	 * @return filtering object
	 */
	public static Filter of(final String value) {
		if (null == value || value.isEmpty()) {
			return none();
		}
		return new Filter(value);
	}

	/**
	 * Creates a filtering object with the filter value as name + operation + value the resulting filter being:
	 * filter=concatenatedString(name, operation, value)
	 *
	 * @param name name of the filter
	 * @param operator filter operator as {@link String}
	 * @param value value of the filter
	 * @return filtering object
	 */
	public static Filter of(final String name, final String operator, final String value) {
		return of(name + operator + value);
	}

	/**
	 * Creates a filtering object with the filter value as name + operation + value the resulting filter being:
	 * filter=concatenatedString(name, operation, value)
	 *
	 * @param <O> operator type
	 * @param <V> value type
	 *
	 * @param name name of the filter
	 * @param operator filter operator
	 * @param value value of the filter
	 * @return filtering object
	 */
	public static <O, V> Filter of(final String name, final O operator, final V value) {
		return of(name + operator + value);
	}

	/**
	 * Creates a filtering object with the filter value as name + operation + value the resulting filter being:
	 * filter=concatenatedString(name, operation, value)
	 *
	 * @param <T> enum type
	 * @param <O> operator type
	 *
	 * @param name name of the filter
	 * @param operator filter operator
	 * @param value value of the filter
	 * @return filtering object
	 */
	public static <T extends Enum<T>, O> Filter of(final Enum<T> name, final O operator, final String value) {
		return of(Strings.safeToString(name) + operator + value);
	}

	/**
	 * Creates a filtering object with {@link Operator#EQ} as operator effectively creating a filter:
	 * <code>filter=name==value</code>.
	 *
	 * @param name name of the filter
	 * @param value value of the filter
	 * @return filtering object
	 * @see Filter#of(String, Object, Object)
	 */
	public static Filter equals(final String name, final String value) {
		return of(name, Operator.EQ, value);
	}

	/**
	 * Returns a composed filter from the given filter with or operator.
	 *
	 * <pre>
	 * (filter1 or filter2 or ... or filterN)
	 * </pre>
	 *
	 * @param filters filters to or
	 * @return composed filter
	 */
	public static Filter anyOf(final Filter... filters) {
		if (null == filters) {
			return none();
		}
		return anyOf(List.of(filters));
	}

	/**
	 * Returns a composed filter from the given filter with or operator.
	 *
	 * <pre>
	 * (filter1 or filter2 or ... or filterN)
	 * </pre>
	 *
	 * @param filters filters to or
	 * @return composed filter
	 */
	public static Filter anyOf(final Collection<Filter> filters) {
		Filter rv = none();
		for (Filter filter : filters) {
			rv = rv.or(filter);
		}
		return rv != none() ? embrace(rv) : rv;
	}

	/**
	 * Returns a filter composed of all the given filters with the or operator.
	 *
	 * <pre>
	 * (name operator value1 or name operator value2 or ... or name operator valueN)
	 * </pre>
	 *
	 * @param <T> list element type
	 *
	 * @param name left value for each of the values
	 * @param operator operator
	 * @param values right values
	 * @return composed filter
	 */
	public static <T> Filter anyOf(final String name, final String operator, final List<T> values) {
		return anyOf(name, (n, v) -> Filter.of(n, operator, v), values);
	}

	/**
	 * Returns a filter composed of all the given filters with the or operator.
	 *
	 * <pre>
	 * (name operator value1 or name operator value2 or ... or name operator valueN)
	 * </pre>
	 *
	 * @param <T> list element type
	 * @param <O> operator type
	 *
	 * @param name left value for each of the values
	 * @param operator operator
	 * @param values right values
	 * @return composed filter
	 */
	public static <T, O> Filter anyOf(final String name, final O operator, final List<T> values) {
		return anyOf(name, (n, v) -> Filter.of(n, operator, v), values);
	}

	/**
	 * Returns a filter composed of all the given filters with the or operator.
	 *
	 * <pre>
	 * (name operator value1 or name operator value2 or ... or name operator valueN)
	 * </pre>
	 *
	 * @param <T> enum type
	 * @param <O> operator type
	 * @param <U> list element type
	 *
	 * @param name left value enumeration for each of the values
	 * @param operator operator
	 * @param values right values
	 * @return composed filter
	 */
	public static <T extends Enum<T>, O, U> Filter anyOf(final T name, final O operator, final List<U> values) {
		return anyOf(Strings.safeToString(name), (n, v) -> Filter.of(n, operator, v), values);
	}

	/**
	 * Returns a filter composed of all the given filters with the or operator.
	 *
	 * <pre>
	 * (name operator value1 or name operator value2 or ... or name operator valueN)
	 * </pre>
	 *
	 * @param <T> enum type
	 * @param <O> operator type
	 * @param <U> array element type
	 *
	 * @param name left value enumeration for each of the values
	 * @param operator operator
	 * @param values right values
	 * @return composed filter
	 */
	@SafeVarargs
	public static <T extends Enum<T>, O, U> Filter anyOf(final T name, final O operator, final U... values) {
		return anyOf(Strings.safeToString(name), (n, v) -> Filter.of(n, operator, v), List.of(values));
	}

	/**
	 * Returns a filter composed of all the given filters with the or operator.
	 *
	 * <pre>
	 * (filterBuilder.get(name, value1) or filterBuilder.get(name, value2) or ... or filterBuilder.get(name, valueN))
	 * </pre>
	 *
	 * @param <T> list element type
	 *
	 * @param name left value for each of the values
	 * @param filterBuilder filter builder function
	 * @param values right values
	 * @return composed filter
	 */
	public static <T> Filter anyOf(final String name, final Filter.Supplier filterBuilder, final List<T> values) {
		List<Filter> filters = values.stream()
				.map(value -> filterBuilder.get(name, Strings.safeToString(value)))
				.toList();
		return anyOf(filters);
	}

	/**
	 * Returns a composed filter from the given filter with and operator.
	 *
	 * <pre>
	 * filter1 and filter2 and ... and filterN
	 * </pre>
	 *
	 * @param embrace flag to encompass in parentheses the and operation
	 * @param filters filters to and
	 * @return composed filter
	 */
	public static Filter allOf(final boolean embrace, final Filter... filters) {
		if (null == filters) {
			return none();
		}
		return allOf(embrace, List.of(filters));
	}

	/**
	 * Returns a composed filter from the given filter with and operator.
	 *
	 * <pre>
	 * filter1 and filter2 and ... and filterN
	 * </pre>
	 *
	 * @param filters filters to and
	 * @return composed filter
	 */
	public static Filter allOf(final Filter... filters) {
		return allOf(false, filters);
	}

	/**
	 * Returns a composed filter from the given filter with and operator.
	 *
	 * <pre>
	 * filter1 and filter2 and ... and filterN
	 * </pre>
	 *
	 * @param embrace flag to encompass in parentheses the and operation
	 * @param filters filters to or
	 * @return composed filter
	 */
	public static Filter allOf(final boolean embrace, final Collection<Filter> filters) {
		Filter rv = none();
		for (Filter filter : filters) {
			rv = rv.and(filter);
		}
		return rv != none() && embrace ? embrace(rv) : rv;
	}

	/**
	 * Returns a composed filter from the given filter with and operator.
	 *
	 * <pre>
	 * filter1 and filter2 and ... and filterN
	 * </pre>
	 *
	 * @param filters filters to or
	 * @return composed filter
	 */
	public static Filter allOf(final Collection<Filter> filters) {
		return allOf(false, filters);
	}

	/**
	 * Returns a filter composed of all the given filters with the and operator.
	 *
	 * <pre>
	 * name operator value1 and name operator value2 and ... and name operator valueN
	 * </pre>
	 *
	 * @param <T> list element type
	 *
	 * @param name left value for each of the values
	 * @param operator string operator
	 * @param values right values
	 * @return composed filter
	 */
	public static <T> Filter allOf(final String name, final String operator, final List<T> values) {
		return allOf(name, (n, v) -> Filter.of(n, operator, v), values);
	}

	/**
	 * Returns a filter composed of all the given filters with the and operator.
	 *
	 * <pre>
	 * name operator value1 and name operator value2 and ... and name operator valueN
	 * </pre>
	 *
	 * @param <T> list element type
	 * @param <O> operator type
	 *
	 * @param name left value for each of the values
	 * @param operator any operator
	 * @param values right values
	 * @return composed filter
	 */
	public static <T, O> Filter allOf(final String name, final O operator, final List<T> values) {
		return allOf(name, (n, v) -> Filter.of(n, operator, v), values);
	}

	/**
	 * Returns a filter composed of all the given filters with the and operator.
	 *
	 * <pre>
	 * name operator value1 and name operator value2 and ... and name operator valueN
	 * </pre>
	 *
	 * @param <T> name type
	 * @param <O> operator type
	 * @param <U> list element type
	 *
	 * @param name left value enumeration
	 * @param operator operator
	 * @param values right values
	 * @return composed filter
	 */
	public static <T extends Enum<T>, O, U> Filter allOf(final T name, final O operator, final List<U> values) {
		return allOf(Strings.safeToString(name), (n, v) -> Filter.of(n, operator, v), values);
	}

	/**
	 * Returns a filter composed of all the given filters with the and operator.
	 *
	 * <pre>
	 * (filterBuilder.get(name, value1) and filterBuilder.get(name, value2) and ... and filterBuilder.get(name, valueN))
	 * </pre>
	 *
	 * @param <T> list element type
	 *
	 * @param name left value
	 * @param filterBuilder filter builder function
	 * @param values right values
	 * @return composed filter
	 */
	public static <T> Filter allOf(final String name, final Filter.Supplier filterBuilder, final List<T> values) {
		List<Filter> filters = values.stream()
				.map(value -> filterBuilder.get(name, Strings.safeToString(value)))
				.toList();
		return allOf(filters);
	}

	/**
	 * Returns a new filter by applying the and operation between this and the given filter.
	 *
	 * @param filter filter to and this
	 * @return a new filter by applying the and operation between this and the given filter
	 */
	public Filter and(final Filter filter) {
		return and(filter, Operator.AND);
	}

	/**
	 * Returns a new filter by applying the and operation between this and the given filter.
	 *
	 * @param <O> operator type
	 *
	 * @param filter filter to and this
	 * @param and custom and operator
	 * @return a new filter by applying the and operation between this and the given filter
	 */
	public <O> Filter and(final Filter filter, final O and) {
		return operation(filter, and);
	}

	/**
	 * Returns a new filter by applying the or operation between this and the given filter.
	 *
	 * @param filter filter to or this
	 * @return a new filter by applying the or operation between this and the given filter
	 */
	public Filter or(final Filter filter) {
		return or(filter, Operator.OR);
	}

	/**
	 * Returns a new filter by applying the or operation between this and the given filter.
	 *
	 * @param <O> operator type
	 *
	 * @param filter filter to or this
	 * @param or custom or operator
	 * @return a new filter by applying the or operation between this and the given filter
	 */
	public <O> Filter or(final Filter filter, final O or) {
		return operation(filter, or);
	}

	/**
	 * Returns a new filter by applying the operation between this and the given filter.
	 *
	 * @param <O> operator type
	 *
	 * @param filter filter to apply the operator with this
	 * @param operator operator to apply
	 * @return a new filter by applying the operation between this and the given filter
	 */
	public <O> Filter operation(final Filter filter, final O operator) {
		if (none() == this) {
			return filter;
		}
		if (none() == filter) {
			return this;
		}
		return of(getValue(), operator, filter.getValue());
	}

	/**
	 * Returns the given filter with parentheses around it.
	 *
	 * @param filter filter to put parentheses around
	 * @return filter with parentheses around
	 */
	public static Filter embrace(final Filter filter) {
		return of("(" + filter.getValue() + ")");
	}

	/**
	 * Returns the filter if the given condition is true, {@link #none()} otherwise.
	 *
	 * @param condition condition to check
	 * @param filter filter to return when the condition is true
	 * @return the filter if the given condition is true
	 */
	public static Filter withCondition(final boolean condition, final Filter filter) {
		return condition ? filter : none();
	}

	/**
	 * Returns the filter if the given value is not {@code null}, {@link #none()} otherwise.
	 *
	 * @param <T> value type
	 *
	 * @param value value to check if null
	 * @param filter filter to return when the condition is true
	 * @return the filter if the given value is not {@code null}
	 */
	public static <T> Filter withNonNull(final T value, final Filter filter) {
		return withCondition(null != value, filter);
	}

	/**
	 * Puts the filter into the given map.
	 *
	 * @param map map to put the filter into
	 */
	@Override
	public void putInto(final Map<String, String> map) {
		if (none() != this) {
			map.put(NAME, getValue());
		}
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		if (none() == this) {
			return "";
		}
		String result = getValue();
		if (result.startsWith("(")) {
			result = result.substring(1, result.length() - 1);
		}
		return NAME + "=" + result;
	}

	/**
	 * Filter supplier functional interface.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	public interface Supplier {

		/**
		 * Returns a filter from a right and left value.
		 *
		 * @param rv right value
		 * @param lv left value
		 * @return filter object
		 */
		Filter get(String rv, String lv);
	}

}
