package org.apiphany.spring.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.io.OneShotInputStreamSupplier;
import org.apiphany.spring.collections.ExtendedMaps;
import org.morphix.lang.Nullables;
import org.morphix.reflection.Constructors;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility methods for HTTP requests in a Spring context.
 *
 * @author Radu Sebastian LAZIN
 */
public class SpringHttpRequests {

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
	public static HttpEntity<InputStreamResource> createHttpEntity(final InputStream content, final HttpHeaders headers) {
		return new HttpEntity<>(new InputStreamResource(new OneShotInputStreamSupplier(content).get()), headers);
	}

	/**
	 * Hide constructor.
	 */
	private SpringHttpRequests() {
		throw Constructors.unsupportedOperationException();
	}
}
