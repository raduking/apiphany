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
}
