package org.apiphany.client;

import java.util.concurrent.CompletableFuture;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
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
	AuthenticationType getType();

	/**
	 * Returns the client name.
	 *
	 * @return the client name
	 */
	default String getName() {
		return getClass().getName();
	}
}
