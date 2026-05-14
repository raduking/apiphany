package org.apiphany.http;

import java.io.IOException;
import java.util.List;

import org.morphix.lang.collections.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;

/**
 * Request callback implementation that writes the body and headers of a given {@link HttpEntity} to the request.
 *
 * @param <T> the request body type
 *
 * @author Radu Sebastian LAZIN
 */
public class HttpEntityRequestCallback<T> implements RequestCallback {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpEntityRequestCallback.class);

	/**
	 * The HTTP request entity containing the body and headers to write to the request.
	 */
	private final HttpEntity<T> requestEntity;

	/**
	 * The list of HTTP message converters to use for writing the request body.
	 */
	private final List<HttpMessageConverter<?>> messageConverters;

	/**
	 * Constructor with request entity and message converters.
	 *
	 * @param requestEntity the HTTP request entity
	 * @param messageConverters the list of HTTP message converters to use for writing the request body
	 */
	public HttpEntityRequestCallback(final HttpEntity<T> requestEntity, final List<HttpMessageConverter<?>> messageConverters) {
		this.requestEntity = requestEntity;
		this.messageConverters = Lists.safe(messageConverters);
	}

	/**
	 * @see RequestCallback#doWithRequest(ClientHttpRequest)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doWithRequest(final ClientHttpRequest request) throws IOException {
		HttpHeaders requestHeaders = request.getHeaders();

		HttpHeaders requestEntityHeaders = requestEntity.getHeaders();
		MediaType requestContentType = requestEntityHeaders.getContentType();
		Object requestBody = requestEntity.getBody();
		if (null == requestBody) {
			SpringHttpRequests.copyHeaders(requestEntityHeaders, requestHeaders);
			return;
		}

		Class<T> requestBodyClass = (Class<T>) requestBody.getClass();
		for (HttpMessageConverter<?> messageConverter : messageConverters) {
			if (messageConverter.canWrite(requestBodyClass, requestContentType)) {
				SpringHttpRequests.copyHeaders(requestEntityHeaders, requestHeaders);
				logBody(requestBody, requestContentType, messageConverter);
				HttpMessageConverter<Object> converter = (HttpMessageConverter<Object>) messageConverter;
				converter.write(requestBody, requestContentType, request);
				return;
			}
		}
		String message = "No HttpMessageConverter for " + requestBodyClass.getName();
		if (requestContentType != null) {
			message += " and content type \"" + requestContentType + "\"";
		}
		throw new RestClientException(message);
	}

	/**
	 * Logs the request body and content type if debug logging is enabled.
	 *
	 * @param body the request body to log
	 * @param mediaType the content type of the request, may be null
	 * @param converter the HttpMessageConverter used to write the request body
	 */
	private static void logBody(final Object body, final MediaType mediaType, final HttpMessageConverter<?> converter) {
		if (LOGGER.isDebugEnabled()) {
			if (null != mediaType) {
				LOGGER.debug("Writing [{}] as \"{}\"", body, mediaType);
			} else {
				LOGGER.debug("Writing [{}] with {}", body, converter.getClass().getName());
			}
		}
	}
}
