package org.apiphany.header;

/**
 * Placeholder for Header related functionalities.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Header extends HeaderFunction {

	/**
	 * Creates a {@link Header} for a single key-value pair.
	 *
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return a {@link Header} that inserts the key-value pair into the map
	 */
	static Header of(final String name, final String value) {
		return HeaderFunction.header(name, value)::addTo;
	}

	/**
	 * Creates a {@link Header} for a single key-value pair, where both the key and value are converted to strings.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the value
	 *
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return a {@link Header} that inserts the key-value pair into the map
	 */
	static <T, U> Header of(final T name, final U value) {
		return HeaderFunction.header(name, value)::addTo;
	}

	/**
	 * Builds the value for the header for the request by concatenating the two parameters with whitespace between them.
	 *
	 * @param <L> left value type
	 * @param <R> right value type
	 *
	 * @param lvalue left value
	 * @param rvalue right value
	 * @return the header value
	 */
	public static <L, R> String value(final L lvalue, final R rvalue) {
		return HeaderValues.value(lvalue, rvalue);
	}

	/**
	 * Builds the value for the header for the request by concatenating the two parameters with separator between them.
	 *
	 * @param <L> left value type
	 * @param <R> right value type
	 *
	 * @param lvalue left value
	 * @param rvalue right value
	 * @param separator the separator to use between the two values
	 * @return the header value
	 */
	public static <L, R> String value(final L lvalue, final R rvalue, final String separator) {
		return HeaderValues.value(lvalue, rvalue, separator);
	}
}
