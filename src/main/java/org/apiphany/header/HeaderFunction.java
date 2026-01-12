package org.apiphany.header;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apiphany.ParameterFunction;

/**
 * Functional interface for defining how headers are inserted into a headers map. This interface together with
 * {@link Headers#of(HeaderFunction...)} is used to build header maps dynamically.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface HeaderFunction extends Consumer<Map<String, List<String>>> {

	/**
	 * Inserts headers into the given map.
	 *
	 * @param map the map to insert headers into
	 */
	void addTo(Map<String, List<String>> map);

	/**
	 * @see Consumer#accept(Object)
	 */
	@Override
	default void accept(final Map<String, List<String>> map) {
		addTo(map);
	}

	/**
	 * Creates a {@link ParameterFunction} for a single key-value pair.
	 *
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
	 */
	static HeaderFunction header(final String name, final String value) {
		return map -> Headers.addTo(map, name, value);
	}

	/**
	 * Creates a {@link HeaderFunction} for a single key-value pair, where both the key and value are converted to strings.
	 *
	 * @param <T> the type of the key
	 * @param <U> the type of the value
	 *
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return a {@link ParameterFunction} that inserts the key-value pair into the map
	 */
	static <T, U> HeaderFunction header(final T name, final U value) {
		return header(String.valueOf(name), String.valueOf(value));
	}
}
