package org.apiphany.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.io.OneShotInputStreamSupplier;
import org.apiphany.spring.collections.ExtendedMaps;
import org.morphix.lang.Nullables;
import org.morphix.reflection.Constructors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility methods for HTTP requests/responses in a Spring context.
 *
 * @author Radu Sebastian LAZIN
 */
public class SpringHttpSupport {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringHttpSupport.class);

	/**
	 * Returns a Spring HTTP method {@link HttpMethod} object.
	 *
	 * @param method string HTTP method
	 * @return HTTP method
	 */
	public static HttpMethod getHttpMethod(final String method) {
		return HttpMethod.valueOf(method);
	}

	/**
	 * Copies/adds the headers from the source to the target headers.
	 *
	 * @param source the headers to copy from
	 * @param target the headers to copy to
	 */
	public static void copyHeaders(final HttpHeaders source, final HttpHeaders target) {
		if (!source.isEmpty()) {
			source.forEach((key, values) -> target.put(key, new ArrayList<>(values)));
		}
	}

	/**
	 * Returns the {@link UriComponentsBuilder} with the give url and request parameters.
	 *
	 * @param url URL
	 * @param requestParams request parameters
	 * @return a new {@link UriComponentsBuilder}
	 */
	public static UriComponentsBuilder getUriComponentsBuilder(final String url, final Map<String, List<String>> requestParams) {
		Map<String, List<String>> queryParams = Nullables.apply(requestParams, HashMap::new, HashMap::new);
		return UriComponentsBuilder.fromUriString(url)
				.queryParams(ExtendedMaps.multiValueMap(queryParams));
	}

	/**
	 * Creates a new {@link HttpEntity} with the given content and headers.
	 *
	 * @param content the content to include in the HTTP entity
	 * @param headers the HTTP headers to include in the HTTP entity
	 * @return a new {@link HttpEntity} with the given content and headers
	 */
	public static <T> HttpEntity<T> createHttpEntity(final T content, final HttpHeaders headers) {
		return new HttpEntity<>(content, headers);
	}

	/**
	 * Creates a new {@link HttpEntity} with the given input stream and headers.
	 *
	 * @param inputStream the content input stream in the HTTP entity
	 * @param headers the HTTP headers to include in the HTTP entity
	 * @return a new {@link HttpEntity} with the given content and headers
	 */
	@SuppressWarnings("resource")
	public static HttpEntity<InputStreamResource> createHttpEntity(final InputStream inputStream, final HttpHeaders headers) {
		InputStream oneShotInputStream = new OneShotInputStreamSupplier(inputStream).get();
		InputStreamResource resource = new InputStreamResource(oneShotInputStream);
		return createHttpEntity(resource, headers);
	}

	/**
	 * Determine the Content-Type of the response based on the "Content-Type" header or otherwise default to
	 * {@link MediaType#APPLICATION_OCTET_STREAM}.
	 *
	 * @param response the response
	 * @return the MediaType, or "application/octet-stream"
	 */
	public static MediaType getContentType(final ClientHttpResponse response) {
		MediaType contentType = response.getHeaders().getContentType();
		if (null == contentType) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("No content-type, using '{}'", MediaType.APPLICATION_OCTET_STREAM_VALUE);
			}
			contentType = MediaType.APPLICATION_OCTET_STREAM;
		}
		return contentType;
	}

	/**
	 * Hide constructor.
	 */
	private SpringHttpSupport() {
		throw Constructors.unsupportedOperationException();
	}
}
