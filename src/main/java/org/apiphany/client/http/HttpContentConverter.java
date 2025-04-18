package org.apiphany.client.http;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValuesChain;
import org.apiphany.http.HttpHeader;

/**
 * Content converter for HTTP clients.
 *
 * @param <T> the content converter generic type
 *
 * @author Radu Sebastian LAZIN
 */
public interface HttpContentConverter<T> extends ContentConverter<T> {

	/**
	 * Retrieves the values of the {@code Content-Type} header from the provided headers object. This method delegates to
	 * {@link #getHeaderValues(Object, Object, HeaderValuesChain)} to fetch the header values.
	 *
	 * @param <V> the type of the headers object (e.g., {@link HttpHeaders} or {@link Map}).
	 *
	 * @param headers the headers object from which to retrieve the {@code Content-Type} values. This can be an instance of
	 *     {@link HttpHeaders} or a {@link Map} of header names to lists of values.
	 * @param headerValuesChain chain of header values that will be used to get a specific header list
	 * @return a list of values for the {@code Content-Type} header. If the header is not found or the headers parameter is
	 * of an unsupported type, an empty list is returned.
	 */
	default <V> List<String> getContentTypes(final V headers, final HeaderValuesChain headerValuesChain) {
		return getHeaderValues(headers, HttpHeader.CONTENT_TYPE, headerValuesChain);
	}

}
