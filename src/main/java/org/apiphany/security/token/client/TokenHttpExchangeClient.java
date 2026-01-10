package org.apiphany.security.token.client;

import java.time.Instant;
import java.util.function.Supplier;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.DecoratingExchangeClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.HeaderValues;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.lang.ScopedResource;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.AuthorizationHeaderProvider;
import org.apiphany.security.client.http.AuthorizationHttpExchangeClient;
import org.apiphany.security.token.TokenProperties;
import org.morphix.lang.Nullables;

/**
 * Authorizes all requests with the provided token and authorization scheme in the client properties. Base class for all
 * clients that need to add authorization/authentication headers to the request. It delegates all calls to the
 * underlying exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public class TokenHttpExchangeClient extends DecoratingExchangeClient implements AuthorizationHttpExchangeClient, AuthenticationTokenProvider {

	/**
	 * The authentication token.
	 */
	private AuthenticationToken authenticationToken;

	/**
	 * The authentication scheme, defaults to {@code Bearer} if missing.
	 */
	private HttpAuthScheme authenticationScheme;

	/**
	 * Supplies the default token expiration.
	 */
	private Supplier<Instant> defaultExpirationSupplier;

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param exchangeClient actual exchange client making the request
	 */
	protected TokenHttpExchangeClient(final ScopedResource<ExchangeClient> exchangeClient) {
		super(exchangeClient);

		this.defaultExpirationSupplier = Instant::now;
		initialize();
	}

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param exchangeClient actual exchange client making the request
	 */
	protected TokenHttpExchangeClient(final ExchangeClient exchangeClient) {
		this(ScopedResource.unmanaged(exchangeClient));
	}

	/**
	 * Initializes the client. We don't care if we have private methods with the same name in the base class.
	 */
	private void initialize() { // NOSONAR
		ClientProperties clientProperties = getClientProperties();
		if (null == clientProperties) {
			return;
		}
		TokenProperties tokenProperties = clientProperties.getCustomProperties(TokenProperties.class);
		if (null == tokenProperties) {
			return;
		}
		AuthenticationToken authToken = new AuthenticationToken();
		authToken.setAccessToken(tokenProperties.getValue());
		setAuthenticationToken(authToken);

		HttpAuthScheme httpAuthScheme = Nullables.notNull(tokenProperties.getAuthenticationScheme())
				.thenYield(HttpAuthScheme::fromString)
				.orElse(HttpAuthScheme.BEARER);
		setAuthenticationScheme(httpAuthScheme);
	}

	/**
	 * @see ExchangeClient#getAuthenticationType()
	 */
	@Override
	public AuthenticationType getAuthenticationType() {
		return AuthenticationType.TOKEN;
	}

	/**
	 * @see AuthorizationHeaderProvider#getAuthorizationHeader()
	 */
	@Override
	public String getAuthorizationHeader() {
		AuthenticationToken token = getAuthenticationToken();
		return HeaderValues.value(getAuthenticationScheme(), token.getAccessToken());
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
	 * Returns the default token expiration date.
	 *
	 * @return the default token expiration date
	 */
	protected Instant getDefaultTokenExpiration() {
		return defaultExpirationSupplier.get();
	}

	/**
	 * Sets the default token expiration supplier to supply the value for {@link #getDefaultTokenExpiration()}.
	 *
	 * @param defaultExpirationSupplier default token expiration supplier
	 */
	protected void setDefaultTokenExpirationSupplier(final Supplier<Instant> defaultExpirationSupplier) {
		this.defaultExpirationSupplier = defaultExpirationSupplier;
	}

	/**
	 * Returns the authentication token.
	 *
	 * @return the authentication token
	 */
	@Override
	public AuthenticationToken getAuthenticationToken() {
		return AuthenticationTokenProvider.valid(authenticationToken);
	}

	/**
	 * Sets the authentication token.
	 *
	 * @param authenticationToken authentication token object
	 */
	public void setAuthenticationToken(final AuthenticationToken authenticationToken) {
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
	public void setAuthenticationScheme(final HttpAuthScheme authenticationScheme) {
		this.authenticationScheme = authenticationScheme;
	}
}
