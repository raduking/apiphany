package org.apiphany.spring.http;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
 * type.
 *
 * @param <T> the type of the response body
 *
 * @author Radu Sebastian LAZIN
 */
public class ResponseEntityResponseExtractor<T> implements ResponseExtractor<ResponseEntity<T>> {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseEntityResponseExtractor.class);

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
	public ResponseEntityResponseExtractor(final Class<T> responseClass, final List<HttpMessageConverter<?>> messageConverters) {
		this.responseClass = Objects.requireNonNull(responseClass, "Response class must not be null");
		this.messageConverters = messageConverters;
	}

	/**
	 * @see ResponseExtractor#extractData(ClientHttpResponse)
	 */
	@Override
	public ResponseEntity<T> extractData(final ClientHttpResponse response) throws IOException {
		T body = extractRawData(response);
		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(body);
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
		MediaType contentType = getContentType(response);
		try {
			for (HttpMessageConverter<?> messageConverter : messageConverters) {
				if (this.responseClass != null && messageConverter.canRead(responseClass, contentType)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Reading to [{}] as \"{}\"", responseClass.getName(), contentType);
					}
					@SuppressWarnings("unchecked")
					HttpMessageConverter<T> converter = (HttpMessageConverter<T>) messageConverter;
					return converter.read(this.responseClass, response);
				}
			}
		} catch (IOException | HttpMessageNotReadableException ex) {
			throw new RestClientException("Error while extracting response for type [" +
					responseClass + "] and content type [" + contentType + "]", ex);
		}
		throw new UnknownContentTypeException(responseClass, contentType,
				response.getStatusCode(), response.getStatusText(),
				response.getHeaders(), getResponseBody(response));
	}

	/**
	 * Determine the Content-Type of the response based on the "Content-Type" header or otherwise default to
	 * {@link MediaType#APPLICATION_OCTET_STREAM}.
	 *
	 * @param response the response
	 * @return the MediaType, or "application/octet-stream"
	 */
	protected MediaType getContentType(final ClientHttpResponse response) {
		MediaType contentType = response.getHeaders().getContentType();
		if (contentType == null) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("No content-type, using 'application/octet-stream'");
			}
			contentType = MediaType.APPLICATION_OCTET_STREAM;
		}
		return contentType;
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
			// ignore
		}
		return new byte[0];
	}
}
