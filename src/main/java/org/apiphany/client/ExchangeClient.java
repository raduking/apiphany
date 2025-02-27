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
	 * @param <T> response body type
	 *
	 * @param request request properties
	 * @return response
	 */
	<T> ApiResponse<T> exchange(final ApiRequest request);

	/**
	 * Basic rest template like async exchange method.
	 *
	 * @param <T> response body type
	 *
	 * @param request request properties
	 * @return response
	 */
	default <T> CompletableFuture<ApiResponse<T>> asyncExchange(final ApiRequest request) {
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
