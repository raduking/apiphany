package org.apiphany.security.oauth2.client;

import org.apiphany.client.DecoratingExchangeClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.lang.ScopedResource;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ResolvedRegistration;
import org.apiphany.security.oauth2.OAuth2TokenProvider;
import org.apiphany.security.oauth2.OAuth2TokenProviderSpec;
import org.apiphany.security.token.client.TokenHttpExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 exchange client, this class decorates an existing {@link ExchangeClient} with automatic OAuth2 support.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2HttpExchangeClient extends TokenHttpExchangeClient {

	/**
	 * The class logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2HttpExchangeClient.class);

	/**
	 * The exchange client doing the token refresh.
	 */
	private final ScopedResource<ExchangeClient> tokenExchangeClient;

	/**
	 * All OAuth2 properties.
	 */
	private final OAuth2Properties oAuth2Properties;

	/**
	 * The resolved registration.
	 */
	private OAuth2ResolvedRegistration resolvedRegistration;

	/**
	 * The token provider.
	 */
	private OAuth2TokenProvider tokenProvider;

	/**
	 * Decorates an exchange client with OAuth2 authentication.
	 * <p>
	 * If no client registration name is provided, the default one will be used, but only if there is exactly one
	 * registration defined. Otherwise, an exception is thrown.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param tokenExchangeClient exchange client doing the token refresh
	 * @param clientRegistrationName the wanted client registration name
	 * @throws IllegalStateException if no valid client registration is found
	 */
	public OAuth2HttpExchangeClient(
			final ScopedResource<ExchangeClient> exchangeClient,
			final ScopedResource<ExchangeClient> tokenExchangeClient,
			final String clientRegistrationName) {
		super(exchangeClient);

		this.tokenExchangeClient = ScopedResource.ensureSingleManager(tokenExchangeClient, exchangeClient);
		this.oAuth2Properties = getClientProperties().getCustomProperties(OAuth2Properties.ROOT, OAuth2Properties.class);
		this.resolvedRegistration = OAuth2ResolvedRegistration.of(oAuth2Properties, clientRegistrationName);

		if (initialize()) {
			OAuth2TokenProviderSpec specification = OAuth2TokenProviderSpec.builder()
					.registration(resolvedRegistration)
					.tokenClientSupplier((clientRegistration, providerDetails) -> new OAuth2ApiClient(clientRegistration, providerDetails,
							tokenExchangeClient.unwrap()))
					.build();
			this.tokenProvider = OAuth2TokenProvider.of(specification);
		}
		setAuthenticationScheme(HttpAuthScheme.BEARER);
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param tokenExchangeClient exchange client doing the token refresh
	 * @param clientRegistrationName the wanted client registration name
	 */
	public OAuth2HttpExchangeClient(
			final ExchangeClient exchangeClient,
			final ExchangeClient tokenExchangeClient,
			final String clientRegistrationName) {
		this(ScopedResource.unmanaged(exchangeClient), ScopedResource.unmanaged(tokenExchangeClient), clientRegistrationName);
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param tokenExchangeClient exchange client doing the token refresh
	 */
	public OAuth2HttpExchangeClient(final ScopedResource<ExchangeClient> exchangeClient, final ScopedResource<ExchangeClient> tokenExchangeClient) {
		this(exchangeClient, tokenExchangeClient, null);
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param tokenExchangeClient exchange client doing the token refresh
	 */
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient, final ExchangeClient tokenExchangeClient) {
		this(exchangeClient, tokenExchangeClient, null);
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication. Uses the same exchange client for token requests.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param clientRegistrationName the wanted client registration name
	 */
	public OAuth2HttpExchangeClient(final ScopedResource<ExchangeClient> exchangeClient, final String clientRegistrationName) {
		this(exchangeClient, exchangeClient, clientRegistrationName);
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication. Uses the same exchange client for token requests.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param clientRegistrationName the wanted client registration name
	 */
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient, final String clientRegistrationName) {
		this(exchangeClient, exchangeClient, clientRegistrationName);
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication. Uses the same exchange client for token requests.
	 *
	 * @param exchangeClient decorated exchange client
	 */
	public OAuth2HttpExchangeClient(final ScopedResource<ExchangeClient> exchangeClient) {
		this(exchangeClient, (String) null);
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication. Uses the same exchange client for token requests.
	 *
	 * @param exchangeClient decorated exchange client
	 */
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient) {
		this(exchangeClient, (String) null);
	}

	/**
	 * Try to get an authentication token at startup. Returns true if the initialization of the properties was successful,
	 * false otherwise.
	 *
	 * @return true if the initialization was successful, false otherwise
	 */
	@SuppressWarnings("resource")
	private boolean initialize() { // NOSONAR we don't care about the parent class private method
		if (getExchangeClient().getClientProperties().isDisabled()) {
			LOGGER.warn("[{}] OAuth2 client is disabled!", getName());
			return false;
		}
		if (null == resolvedRegistration) {
			throw new IllegalStateException("No valid client registration found!");
		}
		return true;
	}

	/**
	 * @see DecoratingExchangeClient#close()
	 */
	@Override
	public void close() throws Exception {
		super.close();
		if (null != tokenProvider) {
			tokenProvider.close();
		}
		tokenExchangeClient.closeIfManaged();
	}

	/**
	 * @see ExchangeClient#getAuthenticationType()
	 */
	@Override
	public AuthenticationType getAuthenticationType() {
		return AuthenticationType.OAUTH2;
	}

	/**
	 * Returns the authentication token. If the current token is expired, it tries to retrieve a new one.
	 *
	 * @return the authentication token
	 */
	@Override
	public AuthenticationToken getAuthenticationToken() {
		if (null != tokenProvider) {
			return tokenProvider.getAuthenticationToken();
		}
		return super.getAuthenticationToken();
	}

	/**
	 * Returns the token client.
	 *
	 * @return the token client
	 */
	protected AuthenticationTokenProvider getTokenClient() {
		return tokenProvider.getTokenClient();
	}

	/**
	 * Returns the token provider.
	 *
	 * @return the token provider
	 */
	protected OAuth2TokenProvider getTokenProvider() {
		return tokenProvider;
	}
}
