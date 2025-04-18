package org.apiphany.client;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.apiphany.ApiMessage;
import org.apiphany.ApiMethod;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.RequestMethod;
import org.apiphany.header.HeaderValues;
import org.apiphany.lang.collections.Maps;
import org.apiphany.security.AuthenticationType;

/**
 * Interface for exchange clients.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ExchangeClient {

	/**
	 * Basic rest template like exchange method.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest request properties
	 * @return response
	 */
	<T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest);

	/**
	 * Returns the client properties.
	 *
	 * @param <T> client properties type
	 *
	 * @return the client properties
	 */
	default <T extends ClientProperties> T getClientProperties() {
		throw new UnsupportedOperationException("getClientProperties");
	}

	/**
	 * Basic rest template like async exchange method.
	 *
	 * @param <T> request/response body type
	 * @param <U> response body type
	 *
	 * @param apiRequest request properties
	 * @return response
	 */
	default <T, U> CompletableFuture<ApiResponse<U>> asyncExchange(final ApiRequest<T> apiRequest) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the authentication type. By default it returns {@link AuthenticationType#NONE}.
	 *
	 * @return the authentication type
	 */
	default AuthenticationType getAuthenticationType() {
		return AuthenticationType.NONE;
	}

	/**
	 * Returns the client name.
	 *
	 * @return the client name
	 */
	default String getName() {
		return getClass().getName();
	}

	/**
	 * Returns all headers as {@link String}. This method helps log the headers of a message and subsequent implementations
	 * can override this method to show only the wanted headers.
	 *
	 * @param <T> the message body type
	 *
	 * @param apiMessage the API message containing the headers
	 * @return string representation of the message headers
	 */
	default <T> String getHeadersAsString(final ApiMessage<T> apiMessage) {
		return Maps.safe(apiMessage.getHeaders()).entrySet().stream().map(entry -> {
			String headerName = entry.getKey();
			List<String> headerValues = getRedactedHeaderPredicate().test(headerName)
					? Collections.singletonList(HeaderValues.REDACTED)
					: entry.getValue();

			StringBuilder sb = new StringBuilder();
			sb.append(headerName).append(":");
			sb.append("\"");
			sb.append(String.join(", ", headerValues));
			sb.append("\"");
			return sb.toString();
		}).toList().toString();
	}

	/**
	 * Returns a predicate for the headers that should be redacted. By default nothing is redacted.
	 *
	 * @return a predicate for the headers that should be redacted
	 */
	default Predicate<String> getRedactedHeaderPredicate() {
		return s -> false;
	}

	/**
	 * Returns the GET request method.
	 *
	 * @return the GET request method
	 */
	default RequestMethod get() {
		return ApiMethod.UNDEFINED;
	}

	/**
	 * Returns the PUT request method.
	 *
	 * @return the PUT request method
	 */
	default RequestMethod put() {
		return ApiMethod.UNDEFINED;
	}

	/**
	 * Returns the POST request method.
	 *
	 * @return the POST request method
	 */
	default RequestMethod post() {
		return ApiMethod.UNDEFINED;
	}

	/**
	 * Returns the DELETE request method.
	 *
	 * @return the DELETE request method
	 */
	default RequestMethod delete() {
		return ApiMethod.UNDEFINED;
	}

	/**
	 * Returns the PATCH request method.
	 *
	 * @return the PATCH request method
	 */
	default RequestMethod patch() {
		return ApiMethod.UNDEFINED;
	}

	/**
	 * Returns the HEAD request method.
	 *
	 * @return the HEAD request method
	 */
	default RequestMethod head() {
		return ApiMethod.UNDEFINED;
	}

	/**
	 * Returns the TRACE request method.
	 *
	 * @return the TRACE request method
	 */
	default RequestMethod trace() {
		return ApiMethod.UNDEFINED;
	}

}
