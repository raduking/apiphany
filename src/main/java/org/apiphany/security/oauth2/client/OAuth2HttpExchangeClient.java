package org.apiphany.security.oauth2.client;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.TokenHttpExchangeClient;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.morphix.lang.JavaObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 exchange client, this class decorates an existing {@link ExchangeClient} with automatic OAuth2 support.
 * <p>
 * TODO: implement refresh token functionality<br/>
 * TODO: implement token exchange client resource management (currently the resource is managed)
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2HttpExchangeClient extends TokenHttpExchangeClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2HttpExchangeClient.class);

	/**
	 * The maximum number of attempts the {@link #close()} method tries to close the scheduled task.
	 */
	private static final int MAX_CLOSE_ATTEMPTS = 10;

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
	private final AtomicBoolean schedulerEnabled = new AtomicBoolean(false);

	/**
	 * Client registration name.
	 */
	private String clientRegistrationName;

	/**
	 * All OAuth2 properties.
	 */
	private final OAuth2Properties oAuth2Properties;

	/**
	 * The scheduled future to stop.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * Decorates an exchange client with OAuth2 authentication.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param tokenExchangeClient exchange client doing the token refresh
	 * @param clientRegistrationName the wanted client registration name
	 */
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient, final ExchangeClient tokenExchangeClient, final String clientRegistrationName) {
		super(exchangeClient);

		this.tokenExchangeClient = Objects.requireNonNull(tokenExchangeClient, "tokenExchangeClient cannot be null");
		this.tokenRefreshScheduler = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());

		this.oAuth2Properties = getClientProperties().getCustomProperties(OAuth2Properties.ROOT, OAuth2Properties.class);
		this.clientRegistrationName = clientRegistrationName;

		setAuthenticationScheme(HttpAuthScheme.BEARER);

		setSchedulerEnabled(initialize());
		if (isSchedulerEnabled()) {
			refreshAuthenticationToken();
		}
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
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient, final String clientRegistrationName) {
		this(exchangeClient, exchangeClient, clientRegistrationName);
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
	private boolean initialize() { // NOSONAR we don't care about the parent class private method
		if (exchangeClient.getClientProperties().isDisabled()) {
			LOGGER.warn("[{}] OAuth2 client is disabled!", getName());
			return false;
		}
		if (Maps.isEmpty(oAuth2Properties.getRegistration())) {
			LOGGER.warn("[{}] No OAuth2 client registrations provided in: {}.registration", getName(), OAuth2Properties.ROOT);
			return false;
		}
		if (Maps.isEmpty(oAuth2Properties.getProvider())) {
			LOGGER.warn("[{}] No OAuth2 providers provided in: {}.provider", getName(), OAuth2Properties.ROOT);
			return false;
		}
		// TODO: implement for multiple registrations
		String name = Strings.isNotEmpty(clientRegistrationName)
				? clientRegistrationName
				: oAuth2Properties.getRegistration().keySet().iterator().next();
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
		OAuth2ClientRegistration clientRegistration = oAuth2Properties.getClientRegistration(clientRegistrationName);
		if (null == clientRegistration) {
			LOGGER.warn("[{}] No OAuth2 client provided for client registration in {}.registration.{}",
					getName(), OAuth2Properties.ROOT, clientRegistrationName);
			return false;
		}
		if (!clientRegistration.hasClientSecret()) {
			LOGGER.warn("[{}] No OAuth2 client-secret provided in {}.registration.{}",
					getName(), OAuth2Properties.ROOT, clientRegistrationName);
			return false;
		}
		OAuth2ProviderDetails providerDetails = oAuth2Properties.getProviderDetails(clientRegistration);
		if (null == providerDetails) {
			LOGGER.warn("[{}] No OAuth2 provider named '{}' for found in in {}.provider",
					getName(), clientRegistration.getProvider(), OAuth2Properties.ROOT);
			return false;
		}

		this.tokenApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, tokenExchangeClient);
		return true;
	}

	/**
	 * @see #close()
	 */
	@Override
	public void close() throws Exception {
		super.close();
		closeTokenRefreshScheduler();
		if (exchangeClient != tokenExchangeClient) {
			tokenExchangeClient.close();
		}
		if (null != tokenApiClient) {
			tokenApiClient.close();
		}
	}

	/**
	 * Safely closes the token refresh scheduler.
	 */
	private void closeTokenRefreshScheduler() {
		boolean cancelled = true;
		if (null != scheduledFuture) {
			Retry retry = Retry.of(WaitCounter.of(MAX_CLOSE_ATTEMPTS, Duration.ofMillis(200)));
			cancelled = retry.when(() -> scheduledFuture.cancel(false), Boolean::booleanValue);
		}
		if (cancelled) {
			tokenRefreshScheduler.close();
		} else {
			List<Runnable> runningTasks = tokenRefreshScheduler.shutdownNow();
			LOGGER.warn("Still running tasks count: {}", runningTasks.size());
		}
	}

	/**
	 * @see #getAuthenticationType()
	 */
	@Override
	public AuthenticationType getAuthenticationType() {
		return AuthenticationType.OAUTH2;
	}

	/**
	 * Returns the client registration name.
	 *
	 * @return client registration name
	 */
	public String getClientRegistrationName() {
		return clientRegistrationName;
	}

	/**
	 * Sets the client registration name which selects which OAuth2 settings will be used for this client, by default if
	 * only one is provided this property doesn't need to be set.
	 *
	 * @param clientRegistrationName client registration name
	 */
	protected void setClientRegistrationName(final String clientRegistrationName) {
		this.clientRegistrationName = clientRegistrationName;
	}

	/**
	 * Returns the authentication token. If the current token is expired, it tries to retrieve a new one.
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
		scheduledFuture = tokenRefreshScheduler.schedule(this::refreshAuthenticationToken, delay.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Updates the authentication token by retrieving a new one.
	 */
	private void updateAuthenticationToken() {
		LOGGER.debug("[{}] Token expired, requesting new token.", getName());
		Instant expiration = Instant.now();
		AuthenticationToken token = tokenApiClient.getAuthenticationToken();
		if (null == token) {
			LOGGER.error("[{}] Error retrieving token, retrieved token was null", getName());
			return;
		}
		token.setExpiration(expiration.plusSeconds(token.getExpiresIn()));
		setAuthenticationToken(token);
		LOGGER.debug("[{}] Successfully retrieved new token.", getName());
	}

	/**
	 * Returns true if the scheduler is enabled, false otherwise.
	 *
	 * @return true if the scheduler is enabled, false otherwise
	 */
	public boolean isSchedulerEnabled() {
		return schedulerEnabled.get();
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
		this.schedulerEnabled.set(schedulerEnabled);
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
