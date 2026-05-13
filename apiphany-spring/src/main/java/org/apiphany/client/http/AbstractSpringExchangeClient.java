package org.apiphany.client.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpStatus;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.collections.Maps;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Abstract exchange client implemented with Spring.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractSpringExchangeClient extends AbstractHttpExchangeClient {

	/**
	 * Constructor with client properties.
	 *
	 * @param clientProperties client properties
	 */
	protected AbstractSpringExchangeClient(final ClientProperties clientProperties) {
		super(clientProperties);
	}

	/**
	 * Sends the HTTP request and returns the response entity.
	 *
	 * @param <T> request entity type
	 * @param <U> response entity type
	 *
	 * @param apiRequest the API request object
	 * @param httpEntity the HTTP request entity
	 * @return the HTTP response entity
	 */
	protected abstract <T, U> ResponseEntity<U> sendRequest(final ApiRequest<T> apiRequest, final HttpEntity<T> httpEntity);

	/**
	 * @see ExchangeClient#exchange(ApiRequest)
	 */
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		apiRequest.addHeaders(getTracingHeaders());
		apiRequest.addHeaders(getCommonHeaders());

		HttpEntity<T> httpEntity = buildRequest(apiRequest);
		ResponseEntity<U> responseEntity = sendRequest(apiRequest, httpEntity);
		return buildResponse(apiRequest, responseEntity);
	}

	/**
	 * Builds the HTTP request entity from the API request object. This method also adds: {@link HttpHeaders#CONTENT_TYPE}
	 * as {@link MediaType#APPLICATION_JSON} and {@link HttpHeaders#ACCEPT} with {@link MediaType#APPLICATION_JSON} if none
	 * are present in the request.
	 *
	 * @param <T> request entity type
	 *
	 * @param apiRequest the request object
	 * @return the request entity
	 */
	protected <T> HttpEntity<T> buildRequest(final ApiRequest<T> apiRequest) {
		HttpHeaders headers = new HttpHeaders();
		Map<String, List<String>> existingHeaders = apiRequest.getHeaders();
		if (Maps.isNotEmpty(existingHeaders)) {
			existingHeaders.forEach(headers::addAll);
		}
		return new HttpEntity<>(apiRequest.getBody(), headers);
	}

	/**
	 * Builds the API response object from the API request and the HTTP response entity.
	 *
	 * @param <T> request entity type
	 * @param <U> response entity type
	 *
	 * @param apiRequest the request object
	 * @param responseEntity the HTTP response entity
	 * @return the API response object
	 */
	protected <T, U> ApiResponse<U> buildResponse(final ApiRequest<T> apiRequest, final ResponseEntity<U> responseEntity) {
		Map<String, List<String>> headers = responseEntity.getHeaders();
		HttpStatus httpStatus = HttpStatus.fromCode(responseEntity.getStatusCode().value());
		U responseBody = responseEntity.getBody();

		List<String> encodings = getHeaderValues(HttpHeader.CONTENT_ENCODING, headers);
		List<ContentEncoding> contentEncodings = ContentEncoding.parseAll(encodings);
		U decompressedBody = ContentEncoding.decodeBody(responseBody, contentEncodings);

		List<String> contentTypeHeaders = headers.get(HttpHeader.CONTENT_TYPE.value());
		HttpContentType contentType = HttpContentType.parse(contentTypeHeaders);

		return buildResponse(apiRequest, httpStatus, headers, contentType, decompressedBody);
	}

	/**
	 * Determines the response type based on whether the API request is a stream or not. If the request is a stream, the
	 * response type will be {@link InputStream}, otherwise it will be {@code byte[]}.
	 *
	 * @param <T> request entity type
	 * @param <U> response entity type
	 *
	 * @param apiRequest the request object
	 * @return the response type class
	 */
	protected <T, U> Class<U> getResponseType(final ApiRequest<T> apiRequest) {
		Class<?> responseType;
		if (apiRequest.isStream()) {
			responseType = InputStream.class;
		} else {
			responseType = byte[].class;
		}
		return JavaObjects.cast(responseType);
	}
}
