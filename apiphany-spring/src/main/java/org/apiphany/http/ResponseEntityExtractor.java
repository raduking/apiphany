package org.apiphany.http;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apiphany.lang.Strings;
import org.morphix.lang.JavaObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.UnknownContentTypeException;

/**
 * Response extractor implementation that reads the response body and headers to a {@link ResponseEntity} of a given
 * type. This is a simplified version of Spring's {@link org.springframework.web.client.HttpMessageConverterExtractor}
 * that returns a {@link ResponseEntity} instead of just the body, and throws an {@link UnknownContentTypeException} if
 * no suitable message converter is found for the response content type.
 *
 * @param <T> the type of the response body
 *
 * @author Radu Sebastian LAZIN
 */
public class ResponseEntityExtractor<T> implements ResponseExtractor<ResponseEntity<T>> {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseEntityExtractor.class);

	/**
	 * The class of the response body to read.
	 */
	private final Class<T> responseClass;

	/**
	 * The list of HTTP message converters to use for reading the response body.
	 */
	private final List<HttpMessageConverter<?>> messageConverters;

	/**
	 * Constructor with response class and message converters.
	 *
	 * @param responseClass the class of the response body to read
	 * @param messageConverters the list of HTTP message converters to use for reading the response body
	 */
	public ResponseEntityExtractor(final Class<T> responseClass, final List<HttpMessageConverter<?>> messageConverters) {
		this.responseClass = Objects.requireNonNull(responseClass, "Response class must not be null");
		this.messageConverters = messageConverters;
	}

	/**
	 * @see ResponseExtractor#extractData(ClientHttpResponse)
	 */
	@Override
	public ResponseEntity<T> extractData(final ClientHttpResponse response) throws IOException { // NOSONAR
		T body = extractRawData(response);
		return ResponseEntity.status(response.getStatusCode())
				.headers(response.getHeaders())
				.body(body);
	}

	/**
	 * Extract the response body to the given type using the configured message converters. If no suitable converter is
	 * found, an {@link UnknownContentTypeException} is thrown.
	 *
	 * @param response the response to extract the body from
	 * @return the extracted response body
	 * @throws IOException if an I/O error occurs while reading the response body
	 */
	public T extractRawData(final ClientHttpResponse response) throws IOException {
		MediaType contentType = SpringHttpSupport.getContentType(response);
		try {
			for (HttpMessageConverter<?> messageConverter : messageConverters) {
				if (messageConverter.canRead(responseClass, contentType)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Reading to [{}] as {}", Strings.envelope("\"", responseClass.getName()), contentType);
					}
					HttpMessageConverter<T> converter = JavaObjects.cast(messageConverter);
					return converter.read(responseClass, response);
				}
			}
		} catch (IOException | HttpMessageNotReadableException ex) {
			throw new RestClientException("Error while extracting response for type [" +
					responseClass + "] and content type [" + contentType + "]", ex);
		}
		throw new UnknownContentTypeException(responseClass, contentType, response.getStatusCode(), // NOSONAR
				response.getStatusText(), response.getHeaders(), getResponseBody(response));
	}

	/**
	 * Read the response body to a byte array. This is used for error handling when no suitable message converter is found.
	 *
	 * @param response the response to read the body from
	 * @return the response body as a byte array, or an empty array if an error occurs while reading
	 */
	private static byte[] getResponseBody(final ClientHttpResponse response) {
		try {
			return FileCopyUtils.copyToByteArray(response.getBody());
		} catch (IOException ex) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Error while reading response body for error handling", ex);
			}
			return new byte[0];
		}
	}
}
