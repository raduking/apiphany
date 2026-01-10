package org.apiphany.security.client.http;

import org.apiphany.ApiRequest;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpHeader;
import org.apiphany.security.AuthorizationHeaderProvider;
import org.apiphany.security.client.AuthenticatedExchangeClient;

/**
 * Authenticated HTTP exchange client that adds an Authorization header to each request.
 *
 * @author Radu Sebastian LAZIN
 */
public interface AuthorizationHttpExchangeClient extends HttpExchangeClient, AuthenticatedExchangeClient, AuthorizationHeaderProvider {

	/**
	 * @see AuthenticatedExchangeClient#authenticate(ApiRequest)
	 */
	@Override
	default <T> void authenticate(final ApiRequest<T> apiRequest) {
		String headerValue = getAuthorizationHeader();
		Headers.addTo(apiRequest.getHeaders(), HttpHeader.AUTHORIZATION, headerValue);
	}
}
