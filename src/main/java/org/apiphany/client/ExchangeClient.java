package org.apiphany.client;

import java.util.concurrent.CompletableFuture;

import org.apiphany.ApiMethod;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.RequestMethod;
import org.apiphany.auth.AuthenticationType;

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
	 * @param request request properties
	 * @return response
	 */
	<T, U> ApiResponse<U> exchange(final ApiRequest<T> request);

	/**
	 * Basic rest template like async exchange method.
	 *
	 * @param <T> request/response body type
	 * @param <U> response body type
	 *
	 * @param request request properties
	 * @return response
	 */
	default <T, U> CompletableFuture<ApiResponse<U>> asyncExchange(final ApiRequest<T> request) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the authentication type.
	 *
	 * @return the authentication type
	 */
	default AuthenticationType getAuthenticationType() {
		return AuthenticationType.NO_AUTHENTICATION;
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
