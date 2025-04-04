package org.apiphany.header;

import java.util.List;
import java.util.Map;

import org.apiphany.http.HttpMessages;
import org.morphix.lang.JavaObjects;

/**
 * A {@link HeaderValues} implementation that extracts header values from a {@link Map}. This concrete implementation
 * handles cases where the headers object is a Map structure, typically mapping header names to lists of values. If the
 * input headers are not a Map, it delegates the request to the next {@link HeaderValues} in the chain.
 * <p>
 * This class is particularly useful for processing HTTP headers stored in map structures, following the Chain of
 * Responsibility pattern established by {@link HeaderValues}.
 *
 * @author Radu Sebastian LAZIN
 */
public class MapHeaderValues extends HeaderValues { // NOSONAR singleton since it has no state

	/**
	 * Retrieves header values from a Map structure or delegates to the next handler in the chain. If the input headers
	 * object is a {@link Map}, this method extracts values for the specified header using
	 * {@link HttpMessages#getHeaderValues(Object, Map)}. Otherwise, it passes the request to the next {@link HeaderValues}
	 * in the chain.
	 *
	 * @param <N> header name type
	 * @param header the name of the header to retrieve (case sensitivity depends on implementation)
	 * @param headers the headers object, expected to be a {@link Map} of header names to values, or any other type that
	 *     subsequent handlers in the chain might process
	 * @return a list of values for the specified header if headers is a Map and contains the header, otherwise the result
	 * from the next handler in the chain
	 * @throws ClassCastException if headers is a Map but cannot be cast to the expected type
	 */
	@Override
	public <N> List<String> get(final N header, final Object headers) {
		if (null != headers && Map.class.isAssignableFrom(headers.getClass())) {
			Map<String, List<String>> headersMap = JavaObjects.cast(headers);
			return HttpMessages.getHeaderValues(header, headersMap);
		}
		return getNext().get(header, headers);
	}

	/**
	 * Returns the instance (new instances can still be created with {@code new}).
	 *
	 * @return the singleton {@link MapHeaderValues} instance
	 */
	public static MapHeaderValues getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Actual {@link MapHeaderValues} singleton instance
		 */
		private static final MapHeaderValues INSTANCE = new MapHeaderValues();
	}
}
