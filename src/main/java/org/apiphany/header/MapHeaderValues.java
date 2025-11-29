package org.apiphany.header;

import java.util.List;
import java.util.Map;

import org.morphix.lang.JavaObjects;

/**
 * A {@link HeaderValues} implementation that extracts header values from a {@link Map}. This concrete implementation
 * handles cases where the object containing the headers is a Map structure, typically mapping header names to lists of
 * values. If the input headers are not a Map, it delegates the request to the next {@link HeaderValues} in the chain.
 * <p>
 * This class is particularly useful for processing HTTP headers stored in map structures, following the Chain of
 * Responsibility pattern established by {@link HeaderValues}.
 *
 * @author Radu Sebastian LAZIN
 */
public class MapHeaderValues extends HeaderValues {

	/**
	 * Default constructor.
	 */
	public MapHeaderValues() {
		// empty
	}

	/**
	 * Retrieves header values from a Map structure or delegates to the next handler in the chain. If the input headers
	 * object is a {@link Map}, this method extracts values for the specified header using {@link Headers#get(Object, Map)}.
	 * Otherwise, it passes the request to the next {@link HeaderValues} in the chain.
	 *
	 * @param <N> header name type
	 *
	 * @param header the name of the header to retrieve (case sensitivity depends on implementation)
	 * @param headers the object containing the headers, expected to be a {@link Map} of header names to values, or any
	 *     other type that subsequent handlers in the chain might process
	 * @return a list of values for the specified header if headers is a Map and contains the header, otherwise the result
	 * from the next handler in the chain
	 * @throws ClassCastException if headers is a Map but cannot be cast to the expected type
	 */
	@Override
	public <N> List<String> get(final N header, final Object headers) {
		return switch (headers) {
			case Map<?, ?> map -> Headers.get(header, JavaObjects.<Map<String, List<String>>>cast(map));
			default -> getNext().get(header, headers);
		};
	}

	/**
	 * Checks if the specified header with the given value exists in a Map structure or delegates to the next handler in the
	 * chain. If the input headers object is a {@link Map}, this method checks for the presence of the header and its value
	 * using {@link Headers#contains(Object, Object, Map)}. Otherwise, it passes the request to the next
	 * {@link HeaderValues} in the chain.
	 *
	 * @param <N> header name type
	 * @param <V> header value type
	 *
	 * @param headerName header name
	 * @param headerValue header value
	 * @param headers the object containing the headers, expected to be a {@link Map} of header names to values, or any
	 *     other type that subsequent handlers in the chain might process
	 * @return true if the specified header with the given value exists in the Map, otherwise the result from the next
	 * handler in the chain
	 * @throws ClassCastException if headers is a Map but cannot be cast to the expected type
	 */
	@Override
	public <N, V> boolean contains(final N headerName, final V headerValue, final Object headers) {
		return switch (headers) {
			case Map<?, ?> map -> Headers.contains(headerName, headerValue, JavaObjects.<Map<String, List<String>>>cast(map));
			default -> getNext().contains(headerName, headerValue, headers);
		};
	}

	/**
	 * Retrieves the values of a specific header from the provided {@link Map} of headers.
	 *
	 * @param <N> header name type
	 *
	 * @param header the name of the header whose values are to be retrieved.
	 * @param headers the map of headers, where each key is a header name and the value is a list of header values.
	 * @return a list of values for the specified header. If the header is not found, an empty list is returned.
	 */
	public static <N> List<String> get(final N header, final Map<String, List<String>> headers) {
		return Headers.get(header, headers);
	}

	/**
	 * Returns true if the headers contain the given header, false otherwise.
	 *
	 * @param <N> header name type
	 *
	 * @param headerName header name
	 * @param headers existing headers
	 * @return true if the headers contain the given header, false otherwise
	 */
	public static <N> boolean contains(final N headerName, final Map<String, List<String>> headers) {
		return Headers.contains(headerName, headers);
	}
}
