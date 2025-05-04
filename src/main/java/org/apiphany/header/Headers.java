package org.apiphany.header;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apiphany.ApiMessage;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.apiphany.lang.collections.Maps;
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
		String stringHeaderName = Strings.safeToString(headerName);
		if (headerValue instanceof List<?> headerList) {
			if (existingHeaders.containsKey(headerName)) {
				List<String> existing = existingHeaders.computeIfAbsent(stringHeaderName, k -> new ArrayList<>());
				headerList.forEach(hv -> existing.add(Strings.safeToString(hv)));
			}
		} else if (null != headerValue) {
			existingHeaders.computeIfAbsent(stringHeaderName, k -> new ArrayList<>()).add(headerValue.toString());
		}
	}

	/**
	 * Adds a header to the headers of the given API message.
	 *
	 * @param <T> message body type
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param apiRequest API message object
	 * @param headerName header name
	 * @param headerValue header value
	 */
	static <T, N, H> void addTo(final ApiMessage<T> apiRequest, final N headerName, final H headerValue) {
		addTo(apiRequest.getHeaders(), headerName, headerValue);
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
	 * @param headerValuesFunction a function that return the header values for the given header name
	 * @return true if the given headers contain the given header with the given value, false otherwise
	 */
	static <N, V> boolean contains(final N headerName, final V headerValue, final Function<N, List<String>> headerValuesFunction) {
		List<String> headerValues = headerValuesFunction.apply(headerName);
		if (Lists.isEmpty(headerValues)) {
			return false;
		}
		String stringValue = Strings.safeToString(headerValue);
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
	 * Returns true if the API message contains the given header with the given value, false otherwise.
	 *
	 * @param <N> header name type
	 * @param <V> header value type
	 * @param <T> API message body type
	 *
	 * @param headerName header name
	 * @param headerValue header value
	 * @param apiMessage API message object
	 * @return true if the given headers contain the given header with the given value, false otherwise
	 */
	static <N, V, T> boolean contains(final N headerName, final V headerValue, final ApiMessage<T> apiMessage) {
		return contains(headerName, headerValue, apiMessage.getHeaders());
	}
}
