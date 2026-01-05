package org.apiphany.security;

import org.apiphany.ApiRequest;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpHeader;
import org.apiphany.lang.ScopedResource;

/**
 * Abstract authenticated HTTP exchange client that adds an Authorization header to each request.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractAuthorizationHttpExchangeClient extends AbstractAuthenticatedHttpExchangeClient implements AuthorizationHeaderProvider {

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param delegate actual exchange client making the request
	 */
	protected AbstractAuthorizationHttpExchangeClient(final ScopedResource<ExchangeClient> delegate) {
		super(delegate);
	}

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param delegate actual exchange client making the request
	 */
	protected AbstractAuthorizationHttpExchangeClient(final ExchangeClient delegate) {
		super(delegate);
	}

	/**
	 * @see #authenticate(ApiRequest)
	 */
	@Override
	protected final <T> void authenticate(final ApiRequest<T> apiRequest) {
		String headerValue = getAuthorizationHeader();
		Headers.addTo(apiRequest.getHeaders(), HttpHeader.AUTHORIZATION, headerValue);
	}
}
