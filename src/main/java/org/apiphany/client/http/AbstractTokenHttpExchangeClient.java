package org.apiphany.client.http;

import java.time.Duration;
import java.time.Instant;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.HeaderValues;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpHeader;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.morphix.lang.Nullables;

/**
 * Base class for all clients that need to add authorization/authentication headers to the request.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractTokenHttpExchangeClient extends AbstractHttpExchangeClient {

	/**
	 * Default value for token expiration (request) - 30 minutes.
	 */
	public static final Duration DEFAULT_EXPIRES_IN = Duration.ofMinutes(30);

	/**
	 * Duration for error margin when checking token expiration - 1 second.
	 */
	protected static final Duration TOKEN_EXPIRATION_ERROR_MARGIN = Duration.ofSeconds(1);

	/**
	 * The actual exchange client doing the request.
	 */
	protected final ExchangeClient exchangeClient;

	/**
	 * The authentication token.
	 */
	private AuthenticationToken authenticationToken;

	/**
	 * The authentication scheme (Ex: Bearer).
	 */
	private HttpAuthScheme authenticationScheme;

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param exchangeClient actual exchange client making the request
	 */
	protected AbstractTokenHttpExchangeClient(final ExchangeClient exchangeClient) {
		super(exchangeClient.getClientProperties());
		this.exchangeClient = exchangeClient;
	}

	/**
	 * @see #exchange(ApiRequest)
	 */
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		AuthenticationToken token = getAuthenticationToken();
		if (null == token) {
			throw new AuthenticationException("Missing authentication token");
		}
		String headerValue = HeaderValues.value(getAuthenticationScheme(), token.getAccessToken());
		Headers.addTo(apiRequest, HttpHeader.AUTHORIZATION, headerValue);
		return exchangeClient.exchange(apiRequest);
	}

	/**
	 * Returns the expiration date for the given token.
	 *
	 * @return the expiration date
	 */
	protected Instant getTokenExpiration() {
		return Nullables.notNull(authenticationToken)
				.andNotNull(AuthenticationToken::getExpiration)
				.valueOrDefault(this::getDefaultTokenExpiration);
	}

	/**
	 * Returns true if a new token is needed, false otherwise.
	 *
	 * @return true if a new token is needed, false otherwise
	 */
	public boolean isNewTokenNeeded() {
		if (null == authenticationToken) {
			return true;
		}
		Instant expiration = getTokenExpiration();
		return expiration.isBefore(Instant.now().minus(TOKEN_EXPIRATION_ERROR_MARGIN));
	}

	/**
	 * Returns the default token expiration date.
	 *
	 * @return the default token expiration date
	 */
	protected Instant getDefaultTokenExpiration() {
		return Instant.now();
	}

	/**
	 * Returns the authentication token.
	 *
	 * @return the authentication token
	 */
	public AuthenticationToken getAuthenticationToken() {
		return authenticationToken;
	}

	/**
	 * Sets the authentication token.
	 *
	 * @param authenticationToken authentication token object
	 */
	protected void setAuthenticationToken(final AuthenticationToken authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	/**
	 * Returns the authentication scheme.
	 *
	 * @return the authentication scheme
	 */
	public HttpAuthScheme getAuthenticationScheme() {
		return authenticationScheme;
	}

	/**
	 * Sets the authentication scheme.
	 *
	 * @param authenticationScheme the authentication scheme to set
	 */
	protected void setAuthenticationScheme(final HttpAuthScheme authenticationScheme) {
		this.authenticationScheme = authenticationScheme;
	}
}
