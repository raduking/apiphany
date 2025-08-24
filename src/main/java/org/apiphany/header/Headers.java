package org.apiphany.header;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.apiphany.lang.collections.Maps;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;

/**
 * Utility methods for headers map and string header values.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Headers {

	/**
	 * Adds headers to existing headers.
	 *
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param existingHeaders existing headers
	 * @param headers headers map
	 */
	static <N, H> void addTo(final Map<String, List<String>> existingHeaders, final Map<N, H> headers) {
		Nullables.whenNotNull(headers).then(hdrs -> {
			for (Map.Entry<N, H> header : hdrs.entrySet()) {
				N headerName = header.getKey();
				Object headerValue = Nullables.nonNullOrDefault(header.getValue(), "");
				addTo(existingHeaders, headerName, headerValue);
			}
		});
	}

	/**
	 * Adds a header to existing headers.
	 *
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param existingHeaders existing headers
	 * @param headerName header name
	 * @param headerValue header value
	 */
	static <N, H> void addTo(final Map<String, List<String>> existingHeaders, final N headerName, final H headerValue) {
		if (null == headerName) {
			return;
		}
		Collection<?> headerCollection = null;
		if (headerValue instanceof Collection<?>) {
			headerCollection = JavaObjects.cast(headerValue);
		} else {
			headerCollection = Collections.singletonList(headerValue);
		}
		List<String> existing = existingHeaders.computeIfAbsent(headerName.toString(), k -> new ArrayList<>());
		headerCollection.forEach(hv -> {
			String stringValue = Strings.safeToString(hv);
			if (Strings.isNotEmpty(stringValue)) {
				existing.add(stringValue);
			}
		});
	}

	/**
	 * Returns true if the headers returned by the given header values function contain the given header with the given
	 * value, false otherwise.
	 *
	 * @param <N> header name type
	 * @param <V> header value type
	 *
	 * @param headerName header name
	 * @param headerValue header value
	 * @param headerValuesFunction a function that returns the header values for the given header name
	 * @return true if the given headers contain the given header with the given value, false otherwise
	 */
	static <N, V> boolean contains(final N headerName, final V headerValue, final Function<N, List<String>> headerValuesFunction) {
		List<String> headerValues = headerValuesFunction.apply(headerName);
		if (Lists.isEmpty(headerValues)) {
			return false;
		}
		String stringValue = Strings.safeToString(headerValue);
		if (Strings.isEmpty(stringValue)) {
			return true;
		}
		for (String value : headerValues) {
			if (value.contains(stringValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the headers contain the given header with the given value, false otherwise.
	 *
	 * @param <N> header name type
	 * @param <V> header value type
	 *
	 * @param headerName header name
	 * @param headerValue header value
	 * @param headers existing headers
	 * @return true if the given headers contain the given header with the given value, false otherwise
	 */
	static <N, V> boolean contains(final N headerName, final V headerValue, final Map<String, List<String>> headers) {
		if (Maps.isEmpty(headers)) {
			return false;
		}
		return contains(headerName, headerValue, hn -> MapHeaderValues.get(hn, headers));
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
	static <N> boolean contains(final N headerName, final Map<String, List<String>> headers) {
		return MapHeaderValues.contains(headerName, headers);
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
		return MapHeaderValues.get(header, headers);
	}
}
