package org.apiphany.http;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.apiphany.io.OneShotInputStreamSupplier;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

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
	 * Creates a new {@link HttpEntity} with the given content and headers.
	 *
	 * @param <T> the type of the content in the HTTP entity
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
	 * Creates a new {@link HttpEntity} with the given file and headers.
	 *
	 * @param file the content file in the HTTP entity
	 * @param headers the HTTP headers to include in the HTTP entity
	 * @return a new {@link HttpEntity} with the given content and headers
	 */
	public static HttpEntity<FileSystemResource> createHttpEntity(final File file, final HttpHeaders headers) {
		return createHttpEntity(new FileSystemResource(file), headers);
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
	 * Returns the request factory configured on the given {@link RestClient.Builder}, or {@code null} if none is set.
	 *
	 * @param restClientBuilder the REST client builder
	 * @return the request factory, or {@code null} if the builder has none
	 */
	public static ClientHttpRequestFactory getRequestFactory(final RestClient.Builder restClientBuilder) {
		if (null == restClientBuilder) {
			return null;
		}
		return Fields.IgnoreAccess.get(restClientBuilder, "requestFactory");
	}

	/**
	 * Returns the request factory configured on the given {@link RestTemplateBuilder}, or {@code null} if none is set
	 * explicitly. A default builder with classpath detection enabled is not treated as having a factory.
	 *
	 * @param restTemplateBuilder the REST template builder
	 * @return the request factory, or {@code null} if the builder has none
	 */
	public static ClientHttpRequestFactory getRequestFactory(final RestTemplateBuilder restTemplateBuilder) {
		if (!hasRequestFactory(restTemplateBuilder)) {
			return null;
		}
		return restTemplateBuilder.buildRequestFactory();
	}

	/**
	 * Returns {@code true} when the given {@link RestTemplateBuilder} has an explicit request factory, {@code false}
	 * otherwise.
	 *
	 * @param restTemplateBuilder the REST template builder
	 * @return {@code true} if a request factory has been configured on the builder
	 */
	public static boolean hasRequestFactory(final RestTemplateBuilder restTemplateBuilder) {
		if (null == restTemplateBuilder) {
			return false;
		}
		return null != Fields.IgnoreAccess.get(restTemplateBuilder, "requestFactoryBuilder");
	}

	/**
	 * Hide constructor.
	 */
	private SpringHttpSupport() {
		throw Constructors.unsupportedOperationException();
	}
}
