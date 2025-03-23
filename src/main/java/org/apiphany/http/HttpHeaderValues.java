package org.apiphany.http;

import java.net.http.HttpHeaders;
import java.util.List;

import org.apiphany.header.HeaderValues;

/**
 * A {@link HeaderValues} implementation specifically designed to handle {@link HttpHeaders} objects. This concrete
 * implementation processes HTTP headers from standard HTTP message objects, delegating to the next handler in the chain
 * if the input is not an {@link HttpHeaders} instance.
 * <p>
 * This class is typically used in HTTP processing pipelines to extract header values from HTTP request/response objects
 * while maintaining the chain of responsibility pattern.
 */
public class HttpHeaderValues extends HeaderValues {

	/**
	 * Retrieves header values from an {@link HttpHeaders} object or delegates to the next handler. If the input headers
	 * object is an instance of {@link HttpHeaders}, this method extracts values for the specified header using
	 * {@link HttpMessages#getHeaderValues(String, HttpHeaders)}. Otherwise, it passes the request to the next
	 * {@link HeaderValues} in the chain.
	 *
	 * @param header the name of the header to retrieve (typically case-insensitive according to HTTP standards)
	 * @param headers the headers object, expected to be an {@link HttpHeaders} instance or any other type that subsequent
	 *     handlers in the chain might process
	 * @return a list of values for the specified header if headers is an HttpHeaders instance and contains the header,
	 * otherwise the result from the next handler in the chain
	 * @throws ClassCastException if headers cannot be properly processed by the next handler in the chain
	 */
	@Override
	public List<String> get(final String header, final Object headers) {
		if (headers instanceof HttpHeaders httpHeaders) {
			return HttpMessages.getHeaderValues(header, httpHeaders);
		}
		return getNext().get(header, headers);
	}
}
