package org.apiphany.header;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiRequest;
import org.apiphany.lang.Strings;
import org.morphix.lang.Nullables;

/**
 * Utility methods for headers map.
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
	public static <N, H> void addTo(final Map<String, List<String>> existingHeaders, final Map<N, H> headers) {
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
	public static <N, H> void addTo(final Map<String, List<String>> existingHeaders, final N headerName, final H headerValue) {
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
	 * Adds a header to the headers of the given request.
	 *
	 * @param <T> request body type
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param apiRequest API request object
	 * @param headerName header name
	 * @param headerValue header value
	 */
	public static <T, N, H> void addTo(final ApiRequest<T> apiRequest, final N headerName, final H headerValue) {
		addTo(apiRequest.getHeaders(), headerName, headerValue);
	}

}
