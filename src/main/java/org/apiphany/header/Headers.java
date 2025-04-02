package org.apiphany.header;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	 * @param existingHeaders existing headers
	 * @param headers headers map
	 */
	public static <H> void addTo(final Map<String, List<String>> existingHeaders, final Map<String, H> headers) {
		Nullables.whenNotNull(headers).then(hdrs -> {
			for (Map.Entry<String, H> header : hdrs.entrySet()) {
				String headerName = header.getKey();
				Object headerValue = Nullables.nonNullOrDefault(header.getValue(), "");
				addTo(existingHeaders, headerName, headerValue);
			}
		});
	}

	/**
	 * Adds a header to existing headers.
	 *
	 * @param existingHeaders existing headers
	 * @param headerName header name
	 * @param headerValue header value
	 */
	public static <H> void addTo(final Map<String, List<String>> existingHeaders, final String headerName, final H headerValue) {
		if (headerValue instanceof List<?> headerList) {
			if (existingHeaders.containsKey(headerName)) {
				List<String> existing = existingHeaders.computeIfAbsent(headerName, k -> new ArrayList<>());
				headerList.forEach(hv -> existing.add(Strings.safeToString(hv)));
			}
		} else {
			existingHeaders.computeIfAbsent(headerName, k -> new ArrayList<>()).add(headerValue.toString());
		}
	}

}
