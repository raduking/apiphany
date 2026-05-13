package org.apiphany.client.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.InputStreamSupplier;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.spring.http.SpringHttpRequests;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.collections.Maps;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

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

		return HttpException.ifThrows(() -> {
			HttpEntity<T> httpEntity = buildRequest(apiRequest);
			ResponseEntity<U> responseEntity = sendRequest(apiRequest, httpEntity);
			return buildResponse(apiRequest, responseEntity);
		}, this::customizeHttpException);
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
		T body = apiRequest.getBody();
		if (null == body) {
			return new HttpEntity<>(headers);
		}
		return createHttpEntity(apiRequest, body, headers);
	}

	/**
	 * Creates an HTTP entity from the given headers and body. This method handles different types of request bodies, such
	 * as strings, byte arrays, input streams, and suppliers of input streams. If the body is a supplier, it will be
	 * evaluated to get the actual body value. For other types of bodies, it will be converted to a string using
	 * {@link Strings#safeToString}.
	 *
	 * @param <T> request entity type
	 *
	 * @param apiRequest the API request object
	 * @param body the body of the request
	 * @param headers the HTTP headers to include in the entity
	 * @return the created HTTP entity
	 */
	protected <T> HttpEntity<T> createHttpEntity(final ApiRequest<T> apiRequest, final T body, final HttpHeaders headers) {
		HttpEntity<?> httpEntity = switch (body) {
			case String str -> new HttpEntity<>(str, headers);
			case byte[] bytes -> new HttpEntity<>(bytes, headers);
			case InputStream inputStream -> SpringHttpRequests.createHttpEntity(inputStream, headers);
			case InputStreamSupplier inputStreamSupplier -> SpringHttpRequests.createHttpEntity(inputStreamSupplier.get(), headers);
			case Supplier<?> supplier -> createHttpEntity(apiRequest, JavaObjects.cast(supplier.get()), headers);
			case Object obj when isContentJson(apiRequest) -> new HttpEntity<>(JsonBuilder.toJson(body), headers);
			default -> new HttpEntity<>(Strings.safeToString(body), headers);
		};
		return JavaObjects.cast(httpEntity);
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

	/**
	 * Extracts the HTTP status from the given throwable. If the throwable is an instance of {@link HttpException}, it
	 * returns the status from the exception. If the throwable is an instance of {@link HttpStatusCodeException}, it returns
	 * the status code from the exception. Otherwise, it returns {@link HttpStatus#INTERNAL_SERVER_ERROR}.
	 *
	 * @param throwable the throwable to extract the status from
	 * @return the extracted HTTP status
	 */
	@Override
	protected HttpStatus extractHttpStatus(final Throwable throwable) {
		return switch (throwable) {
			case HttpStatusCodeException httpStatusCodeException -> HttpStatus.fromCode(httpStatusCodeException.getStatusCode().value());
			default -> super.extractHttpStatus(throwable);
		};
	}

	/**
	 * Extracts the response body from the given throwable. If the throwable is an instance of {@link HttpException}, it
	 * returns the response body from the exception. If the throwable is an instance of {@link HttpStatusCodeException}, it
	 * returns the response body as a string from the exception. Otherwise, it returns {@code null}.
	 *
	 * @param throwable the throwable to extract the response body from
	 * @return the extracted response body, or {@code null} if not applicable
	 */
	@Override
	protected String extractResponseBody(final Throwable throwable) {
		return switch (throwable) {
			case HttpStatusCodeException httpStatusCodeException -> httpStatusCodeException.getResponseBodyAsString();
			default -> super.extractResponseBody(throwable);
		};
	}
}
