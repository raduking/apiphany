package org.apiphany.security.client;

import org.apiphany.ApiRequest;
import org.apiphany.client.ExchangeClient;

/**
 * Authenticated exchange client interface.
 *
 * @author Radu Sebastian LAZIN
 */
public interface AuthenticatedExchangeClient extends ExchangeClient {

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

}
