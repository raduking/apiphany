package org.apiphany.security;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.DecoratingHttpExchangeClient;
import org.apiphany.lang.ScopedResource;

/**
 * Abstract authenticated HTTP exchange client, enforcing authentication before performing the exchange.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractAuthenticatedHttpExchangeClient extends DecoratingHttpExchangeClient {

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param delegate actual exchange client making the request
	 */
	protected AbstractAuthenticatedHttpExchangeClient(final ScopedResource<ExchangeClient> delegate) {
		super(delegate);
	}

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param delegate actual exchange client making the request
	 */
	protected AbstractAuthenticatedHttpExchangeClient(final ExchangeClient delegate) {
		super(delegate);
	}

	/**
	 * Subclasses must implement this method to authenticate the request.
	 *
	 * @param <T> the request body type
	 *
	 * @param apiRequest the request to authenticate
	 */
	protected abstract <T> void authenticate(ApiRequest<T> apiRequest);

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
	public final <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
		authenticate(request);
		return super.exchange(request);
	}
}
