package org.apiphany.security.oauth2.client;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.AbstractTokenHttpExchangeClient;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.morphix.lang.JavaObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 exchange client, this class decorates an existing {@link ExchangeClient} with automatic OAuth2 support.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2HttpExchangeClient extends AbstractTokenHttpExchangeClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2HttpExchangeClient.class);

	/**
	 * The exchange client doing the token refresh.
	 */
	private final ExchangeClient tokenExchangeClient;

	/**
	 * Underlying API client that retrieves the token.
	 */
	private OAuth2ApiClient tokenApiClient;

	/**
	 * Token retrieval scheduler.
	 */
	private final ScheduledExecutorService tokenRefreshScheduler;

	/**
	 * Scheduler enabled flag.
	 */
	private boolean schedulerEnabled;

	/**
	 * Client registration name.
	 */
	private String clientRegistrationName;

	/**
	 * All OAuth2 client registrations.
	 */
	private final Map<String, OAuth2ClientRegistration> clientRegistrations;

	/**
	 * All OAuth2 providers.
	 */
	private final Map<String, OAuth2ProviderDetails> providers;

	/**
	 * Decorates an exchange client with OAuth2 authentication.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param tokenExchangeClient exchange client doing the token refresh
	 */
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient, final ExchangeClient tokenExchangeClient) {
		super(exchangeClient);

		this.tokenExchangeClient = tokenExchangeClient;
		this.tokenRefreshScheduler = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());

		OAuth2Properties oAuth2Properties = getClientProperties().getCustomProperties(OAuth2Properties.ROOT, OAuth2Properties.class);
		this.clientRegistrations = oAuth2Properties.getRegistration();
		this.providers = oAuth2Properties.getProvider();

		setAuthenticationScheme(HttpAuthScheme.BEARER);

		setSchedulerEnabled(initialize());
		if (isSchedulerEnabled()) {
			refreshAuthenticationToken();
		}
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication. Uses the same exchange client for token requests.
	 *
	 * @param exchangeClient decorated exchange client
	 */
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient) {
		this(exchangeClient, exchangeClient);
	}

	/**
	 * Try to get an authentication token at startup. Returns true if the initialization of the properties was successful,
	 * false otherwise.
	 *
	 * @return true if the initialization was successful, false otherwise
	 */
	private boolean initialize() {
		if (exchangeClient.getClientProperties().isDisabled()) {
			LOGGER.warn("[{}] OAuth2 client is disabled!", getClass().getSimpleName());
			return false;
		}
		if (Maps.isEmpty(clientRegistrations)) {
			LOGGER.warn("[{}] No OAuth2 client registrations provided in: {}.registration",
					getClass().getSimpleName(), OAuth2Properties.ROOT);
			return false;
		}
		if (Maps.isEmpty(providers)) {
			LOGGER.warn("[{}] No OAuth2 providers provided in: {}.provider",
					getClass().getSimpleName(), OAuth2Properties.ROOT);
			return false;
		}
		// TODO: implement for multiple registrations
		String name = getClientRegistrationName();
		if (!initialize(name)) {
			return false;
		}
		setClientRegistrationName(name);

		return null != tokenApiClient;
	}

	/**
	 * Try to initialize OAuth2 properties and the OAuth2 API client based on the given client registration name. Returns
	 * true if the initialization of the properties was successful, false otherwise.
	 *
	 * @param clientRegistrationName client registration name
	 * @return true if the initialization was successful, false otherwise
	 */
	private boolean initialize(final String clientRegistrationName) {
		OAuth2ClientRegistration clientRegistration = clientRegistrations.get(clientRegistrationName);
		if (null == clientRegistration) {
			LOGGER.warn("[{}] No OAuth2 client provided for client registration in {}.registration.{}",
					getClass().getSimpleName(), OAuth2Properties.ROOT, clientRegistrationName);
			return false;
		}
		if (!clientRegistration.hasClientSecret()) {
			LOGGER.warn("[{}] No OAuth2 client-secret provided in {}.registration.{}",
					getClass().getSimpleName(), OAuth2Properties.ROOT, clientRegistrationName);
			return false;
		}
		OAuth2ProviderDetails provider = providers.get(clientRegistration.getProvider());
		if (null == provider) {
			LOGGER.warn("[{}] No OAuth2 provider named '{}' for found in in {}.provider",
					getClass().getSimpleName(), clientRegistration.getProvider(), OAuth2Properties.ROOT);
			return false;
		}

		this.tokenApiClient = new OAuth2ApiClient(clientRegistration, provider, tokenExchangeClient);
		return true;
	}

	/**
	 * @see #getAuthenticationType()
	 */
	@Override
	public AuthenticationType getAuthenticationType() {
		return AuthenticationType.OAUTH2;
	}

	/**
	 * Returns the client registration name, if the client registration property is blank a random one is selected from the
	 * registrations map.
	 *
	 * @return client registration name
	 */
	public String getClientRegistrationName() {
		return Strings.isNotEmpty(clientRegistrationName)
				? clientRegistrationName
				: clientRegistrations.keySet().iterator().next();
	}

	/**
	 * Sets the client registration name which selects which OAuth2 settings will be used for this client, by default if
	 * only one is provided this property doesn't need to be set.
	 *
	 * @param clientRegistrationName client registration name
	 */
	public void setClientRegistrationName(final String clientRegistrationName) {
		this.clientRegistrationName = clientRegistrationName;
	}

	/**
	 * Returns the authentication token. If the current token is expired it tries to retrieve a new one.
	 *
	 * @return the authentication token
	 */
	@Override
	public AuthenticationToken getAuthenticationToken() {
		if (isNewTokenNeeded()) {
			updateAuthenticationToken();
		}
		return super.getAuthenticationToken();
	}

	/**
	 * Refreshes the authentication token.
	 */
	private void refreshAuthenticationToken() {
		updateAuthenticationToken();
		if (isSchedulerDisabled()) {
			return;
		}
		// schedule new token checking
		Instant expiration = getTokenExpiration().minus(TOKEN_EXPIRATION_ERROR_MARGIN);
		Instant scheduled = JavaObjects.max(expiration, Instant.now());
		Duration delay = Duration.between(Instant.now(), scheduled);
		tokenRefreshScheduler.schedule(this::refreshAuthenticationToken, delay.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Updates the authentication token by retrieving a new one.
	 */
	private void updateAuthenticationToken() {
		LOGGER.debug("[{}] Token expired, requesting new token.", getClass().getSimpleName());
		Instant expiration = Instant.now();
		AuthenticationToken token = tokenApiClient.getAuthenticationToken();
		if (null == token) {
			LOGGER.error("[{}] Error retrieving token, retrieved token was null", getClass().getSimpleName());
			return;
		}
		token.setExpiration(expiration.plusSeconds(token.getExpiresIn()));
		setAuthenticationToken(token);
		LOGGER.debug("[{}] Successfully retrieved new token.", getClass().getSimpleName());
	}

	/**
	 * Returns true if the scheduler is enabled, false otherwise.
	 *
	 * @return true if the scheduler is enabled, false otherwise
	 */
	public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	/**
	 * Returns true if the scheduler is disabled, false otherwise.
	 *
	 * @return true if the scheduler is disabled, false otherwise
	 */
	public boolean isSchedulerDisabled() {
		return !isSchedulerEnabled();
	}

	/**
	 * Enables/disables the scheduler.
	 *
	 * @param schedulerEnabled scheduler enable flag
	 */
	public void setSchedulerEnabled(final boolean schedulerEnabled) {
		this.schedulerEnabled = schedulerEnabled;
	}

	/**
	 * Returns the refresh scheduler.
	 *
	 * @return the refresh scheduler
	 */
	protected ScheduledExecutorService getTokenRefreshScheduler() {
		return tokenRefreshScheduler;
	}

	/**
	 * Returns the token API client.
	 *
	 * @return the token API client
	 */
	protected OAuth2ApiClient getTokenApiClient() {
		return tokenApiClient;
	}
}
