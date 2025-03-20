package org.apiphany.client.http;

import java.net.http.HttpHeaders;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apiphany.client.ContentConverter;
import org.apiphany.http.HttpHeader;
import org.morphix.lang.JavaObjects;

/**
 * Content converter for HTTP clients.
 * <p>
 * TODO: maybe implement getHeaderValues with chain of responsibility pattern
 *
 * @param <T> the content converter generic type
 *
 * @author Radu Sebastian LAZIN
 */
public interface HttpContentConverter<T> extends ContentConverter<T> {

	/**
	 * Retrieves the values of a specific header from the provided headers object. This method supports headers provided as
	 * either {@link HttpHeaders} or a {@link Map}.
	 *
	 * @param <V> the type of the headers object (e.g., {@link HttpHeaders} or {@link Map}).
	 *
	 * @param headers the headers object from which to retrieve the values. This can be an instance of {@link HttpHeaders}
	 *     or a {@link Map} of header names to lists of values.
	 * @param header the name of the header whose values are to be retrieved.
	 * @return a list of values for the specified header. If the header is not found or the headers object is of an
	 * unsupported type, an empty list is returned.
	 */
	@Override
	default <V> List<String> getHeaderValues(final V headers, final String header) {
		if (headers instanceof HttpHeaders httpHeadersInstance) {
			return httpHeadersInstance.allValues(header);
		}
		if (Map.class.isAssignableFrom(headers.getClass())) {
			Map<String, List<String>> headersMap = JavaObjects.cast(headers);
			return HttpHeaders.of(headersMap, (name, value) -> true)
					.allValues(header);
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieves the values of the {@code Content-Type} header from the provided headers object. This method delegates to
	 * {@link #getHeaderValues(Object, String)} to fetch the header values.
	 *
	 * @param <V> the type of the headers object (e.g., {@link HttpHeaders} or {@link Map}).
	 *
	 * @param headers the headers object from which to retrieve the {@code Content-Type} values. This can be an instance of
	 *     {@link HttpHeaders} or a {@link Map} of header names to lists of values.
	 * @return a list of values for the {@code Content-Type} header. If the header is not found or the headers object is of
	 * an unsupported type, an empty list is returned.
	 */
	default <V> List<String> getContentTypes(final V headers) {
		return getHeaderValues(headers, HttpHeader.CONTENT_TYPE.value());
	}
}
