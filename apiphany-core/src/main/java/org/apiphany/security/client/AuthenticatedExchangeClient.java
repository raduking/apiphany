package org.apiphany.security.client;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.DelegatingExchangeClient;

/**
 * Authenticated exchange client interface.
 *
 * @author Radu Sebastian LAZIN
 */
public interface AuthenticatedExchangeClient extends DelegatingExchangeClient {

	/**
	 * Authenticates the given API request.
	 * <p>
	 * Implementations should be idempotent and may overwrite existing authentication-related headers.
	 *
	 * @param <T> the request body type
	 *
	 * @param apiRequest the request to authenticate
	 */
	<T> void authenticate(ApiRequest<T> apiRequest);

	/**
	 * Template method enforcing authentication.
	 *
	 * @param <T> the request body type
	 * @param <U> the response body type
	 *
	 * @param request the request to exchange
	 * @return the response
	 */
	@Override
	default <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
		authenticate(request);
		return DelegatingExchangeClient.super.exchange(request);
	}
}
