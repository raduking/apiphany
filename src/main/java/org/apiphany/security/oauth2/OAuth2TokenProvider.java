package org.apiphany.security.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 token provider component, this class provides a non-expired token by requesting a new one on expiration time.
 * <p>
 * TODO: implement refresh token functionality<br/>
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2TokenProvider implements AuthenticationTokenProvider, AutoCloseable {

	/**
	 * The logger object.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2TokenProvider.class);

	/**
	 * The client doing the token refresh.
	 */
	private AuthenticationTokenProvider tokenClient;

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
	 * Configuration for the OAuth2 client registration.
	 */
	private OAuth2ClientRegistration clientRegistration;

	/**
	 * Configuration details for the OAuth2 provider.
	 */
	private OAuth2ProviderDetails providerDetails;

	/**
	 * The scheduled future to stop.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * The authentication token.
	 */
	private AuthenticationToken authenticationToken;

	/**
	 * Supplies the default token expiration.
	 */
	private Supplier<Instant> defaultExpirationSupplier;

	/**
	 * The configuration.
	 */
	private final OAuth2TokenProviderConfiguration configuration;

	/**
	 * Creates a new authentication token provider.
	 *
	 * @param configuration the OAuth2 token provider configuration
	 * @param oAuth2Properties the OAuth2 properties
	 * @param clientRegistrationName the wanted client registration name
	 * @param tokenRefreshScheduler the token refresh scheduler
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 */
	public OAuth2TokenProvider(
			final OAuth2TokenProviderConfiguration configuration,
			final OAuth2Properties oAuth2Properties,
			final String clientRegistrationName,
			final ScheduledExecutorService tokenRefreshScheduler,
			final BiFunction<OAuth2ClientRegistration, OAuth2ProviderDetails, AuthenticationTokenProvider> tokenClientSupplier) {
		this.configuration = configuration;
		this.tokenRefreshScheduler = tokenRefreshScheduler;
		this.defaultExpirationSupplier = Instant::now;
		this.clientRegistrationName = clientRegistrationName;

		setSchedulerEnabled(initialize(oAuth2Properties));
		if (isSchedulerEnabled()) {
			this.tokenClient = tokenClientSupplier.apply(clientRegistration, providerDetails);
			updateAuthenticationToken();
		}
	}

	/**
	 * Creates a new authentication token provider.
	 *
	 * @param oAuth2Properties the OAuth2 properties
	 * @param clientRegistrationName the wanted client registration name
	 * @param tokenRefreshScheduler the token refresh scheduler
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 */
	public OAuth2TokenProvider(
			final OAuth2Properties oAuth2Properties,
			final String clientRegistrationName,
			final ScheduledExecutorService tokenRefreshScheduler,
			final BiFunction<OAuth2ClientRegistration, OAuth2ProviderDetails, AuthenticationTokenProvider> tokenClientSupplier) {
		this(OAuth2TokenProviderConfiguration.defaults(), oAuth2Properties, clientRegistrationName, tokenRefreshScheduler, tokenClientSupplier);
	}

	/**
	 * Creates a new authentication token provider. The scheduler will use virtual threads for the scheduled tasks.
	 *
	 * @param oAuth2Properties the OAuth2 properties
	 * @param clientRegistrationName the wanted client registration name
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 */
	@SuppressWarnings("resource")
	public OAuth2TokenProvider(
			final OAuth2Properties oAuth2Properties,
			final String clientRegistrationName,
			final BiFunction<OAuth2ClientRegistration, OAuth2ProviderDetails, AuthenticationTokenProvider> tokenClientSupplier) {
		this(oAuth2Properties, clientRegistrationName,
				Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory()), tokenClientSupplier);
	}

	/**
	 * Try to get an authentication token at startup. Returns true if the initialization of the properties was successful,
	 * false otherwise.
	 *
	 * @param properties the OAuth2 properties
	 * @return true if the initialization was successful, false otherwise
	 */
	private boolean initialize(final OAuth2Properties properties) {
		if (null == properties) {
			LOGGER.error("[{}] No OAuth2 properties provided in: {}", getName(), OAuth2Properties.ROOT);
			return false;
		}
		if (Maps.isEmpty(properties.getRegistration())) {
			LOGGER.warn("[{}] No OAuth2 client registrations provided in: {}.registration", getName(), OAuth2Properties.ROOT);
			return false;
		}
		if (Maps.isEmpty(properties.getProvider())) {
			LOGGER.warn("[{}] No OAuth2 providers provided in: {}.provider", getName(), OAuth2Properties.ROOT);
			return false;
		}
		// TODO: implement for multiple registrations
		String name = Strings.isEmpty(clientRegistrationName)
				? properties.getRegistration().keySet().iterator().next()
				: clientRegistrationName;
		if (!initialize(properties, name)) {
			return false;
		}
		setClientRegistrationName(name);
		return true;
	}

	/**
	 * Try to initialize OAuth2 properties and the OAuth2 API client based on the given client registration name. Returns
	 * true if the initialization of the properties was successful, false otherwise.
	 *
	 * @param properties the OAuth2 properties
	 * @param clientRegistrationName client registration name
	 * @return true if the initialization was successful, false otherwise
	 */
	private boolean initialize(final OAuth2Properties properties, final String clientRegistrationName) {
		this.clientRegistration = properties.getClientRegistration(clientRegistrationName);
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
		this.providerDetails = properties.getProviderDetails(clientRegistration);
		if (null == providerDetails) {
			LOGGER.warn("[{}] No OAuth2 provider named '{}' for found in in {}.provider",
					getName(), clientRegistration.getProvider(), OAuth2Properties.ROOT);
			return false;
		}
		return true;
	}

	/**
	 * @see #close()
	 */
	@Override
	public void close() throws Exception {
		closeTokenRefreshScheduler();
		if (getTokenClient() instanceof AutoCloseable closeable) {
			closeable.close();
		}
	}

	/**
	 * Safely closes the token refresh scheduler.
	 */
	private void closeTokenRefreshScheduler() {
		boolean cancelled = null == scheduledFuture;
		if (!cancelled) {
			Retry retry = Retry.of(WaitCounter.of(getConfiguration().getMaxTaskCloseAttempts(), Duration.ofMillis(200)));
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
	 * Returns the authentication token. If the current token is expired, it tries to retrieve a new one.
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
	protected void setAuthenticationToken(final AuthenticationToken authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	/**
	 * Updates the authentication token.
	 */
	private void updateAuthenticationToken() {
		LOGGER.debug("[{}] Token expired, requesting new token.", getName());
		Instant expiration = Instant.now();
		try {
			AuthenticationToken token = getTokenClient().getAuthenticationToken();
			token.setExpiration(expiration.plusSeconds(token.getExpiresIn()));
			setAuthenticationToken(token);
			LOGGER.debug("[{}] Successfully retrieved new token.", getName());
		} catch (Exception e) {
			LOGGER.error("[{}] Error retrieving new token.", getName(), e);
		}
		scheduleTokenUpdate();
	}

	/**
	 * Schedules the token update.
	 */
	private void scheduleTokenUpdate() {
		Instant expiration = getTokenExpiration().minus(getConfiguration().getExpirationErrorMargin());
		Instant scheduled = JavaObjects.max(expiration, Instant.now());
		Duration delay = Duration.between(Instant.now(), scheduled);
		delay = JavaObjects.max(delay, getConfiguration().getMinRefreshInterval());
		scheduledFuture = tokenRefreshScheduler.schedule(this::updateAuthenticationToken, delay.toMillis(), TimeUnit.MILLISECONDS);
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
	 * Returns the client that actually does the token requests.
	 *
	 * @return the client that actually does the token requests
	 */
	public AuthenticationTokenProvider getTokenClient() {
		return tokenClient;
	}

	/**
	 * Returns the name of the token provider.
	 *
	 * @return the name of the token provider
	 */
	private String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Returns the client registration.
	 *
	 * @return the client registration
	 */
	public OAuth2ClientRegistration getClientRegistration() {
		return clientRegistration;
	}

	/**
	 * Returns the provider details.
	 *
	 * @return the provider details
	 */
	public OAuth2ProviderDetails getProviderDetails() {
		return providerDetails;
	}

	/**
	 * Returns the configuration object to be able to change the configuration properties dynamically.
	 *
	 * @return the configuration
	 */
	public OAuth2TokenProviderConfiguration getConfiguration() {
		return configuration;
	}
}
