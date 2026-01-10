package org.apiphany.security.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.morphix.lang.Comparables;
import org.morphix.lang.Nullables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 token provider component, this class provides a non-expired token by requesting a new one on expiration time.
 * When a new token is requested, it uses the provided {@link AuthenticationTokenProvider} client to do the actual token
 * retrieval. The client is supplied using the {@link OAuth2TokenClientSupplier} functional interface.
 * <p>
 * The token refresh is done using a scheduled task that runs when the token is about to expire (based on the expiration
 * time minus an error margin defined in {@link OAuth2TokenProviderOptions}).
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
	 * The OAuth2 resolved registration for this provider.
	 */
	private final OAuth2ResolvedRegistration registration;

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
	 * The specific options for this provider.
	 */
	private final OAuth2TokenProviderOptions options;

	/**
	 * Creates a new authentication token provider.
	 *
	 * @param options the OAuth2 token provider options
	 * @param registration the OAuth2 resolved registration for this provider
	 * @param tokenRefreshScheduler the token refresh scheduler
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 */
	public OAuth2TokenProvider(
			final OAuth2TokenProviderOptions options,
			final OAuth2ResolvedRegistration registration,
			final ScheduledExecutorService tokenRefreshScheduler,
			final OAuth2TokenClientSupplier tokenClientSupplier) {
		this.options = options;
		this.tokenRefreshScheduler = tokenRefreshScheduler;
		this.defaultExpirationSupplier = Instant::now;
		this.registration = registration;

		if (null != registration) {
			setSchedulerEnabled(true);
			this.tokenClient = tokenClientSupplier.get(registration.getClientRegistration(), registration.getProviderDetails());
			updateAuthenticationToken();
		}
	}

	/**
	 * Creates a new authentication token provider.
	 *
	 * @param options the OAuth2 token provider options
	 * @param oAuth2Properties the OAuth2 properties
	 * @param clientRegistrationName the wanted client registration name
	 * @param tokenRefreshScheduler the token refresh scheduler
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 */
	public OAuth2TokenProvider(
			final OAuth2TokenProviderOptions options,
			final OAuth2Properties oAuth2Properties,
			final String clientRegistrationName,
			final ScheduledExecutorService tokenRefreshScheduler,
			final OAuth2TokenClientSupplier tokenClientSupplier) {
		this(options, OAuth2ResolvedRegistration.of(oAuth2Properties, clientRegistrationName), tokenRefreshScheduler, tokenClientSupplier);
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
			final OAuth2TokenClientSupplier tokenClientSupplier) {
		this(OAuth2TokenProviderOptions.defaults(), oAuth2Properties, clientRegistrationName, tokenRefreshScheduler, tokenClientSupplier);
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
			final OAuth2TokenClientSupplier tokenClientSupplier) {
		this(oAuth2Properties, clientRegistrationName,
				Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory()), tokenClientSupplier);
	}

	/**
	 * Creates a new authentication token provider. The scheduler will use virtual threads for the scheduled tasks. This
	 * will initialize the provider only if there is only one registration defined in OAuth2 properties.
	 *
	 * @param oAuth2Properties the OAuth2 properties
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 */
	public OAuth2TokenProvider(
			final OAuth2Properties oAuth2Properties,
			final OAuth2TokenClientSupplier tokenClientSupplier) {
		this(oAuth2Properties, null, tokenClientSupplier);
	}

	/**
	 * @see AutoCloseable#close()
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
			Retry retry = Retry.of(WaitCounter.of(getOptions().getMaxTaskCloseAttempts(), Duration.ofMillis(200)));
			cancelled = retry.until(() -> scheduledFuture.cancel(false), Boolean::booleanValue);
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
		return registration.getClientRegistrationName();
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
		LOGGER.debug("[{}] Token expired, requesting new token.", getClientRegistrationName());
		Instant expiration = Instant.now();
		try {
			AuthenticationToken token = getAuthenticationTokenFromClient();
			token.setExpiration(expiration.plusSeconds(token.getExpiresIn()));
			setAuthenticationToken(token);
			LOGGER.debug("[{}] Successfully retrieved new token.", getClientRegistrationName());
		} catch (Exception e) {
			LOGGER.error("[{}] Error retrieving new token.", getClientRegistrationName(), e);
		}
		scheduleTokenUpdate();
	}

	/**
	 * Retrieves the authentication token from the client.
	 *
	 * @return the authentication token
	 */
	private AuthenticationToken getAuthenticationTokenFromClient() {
		AuthenticationToken token = getTokenClient().getAuthenticationToken();
		if (null == token) {
			throw new AuthenticationException("Received null token from token client");
		}
		long expiresIn = token.getExpiresIn();
		if (expiresIn <= 0) {
			throw new AuthenticationException("Received token with invalid expiration: " + expiresIn);
		}
		return token;
	}

	/**
	 * Schedules the token update.
	 */
	private void scheduleTokenUpdate() {
		Instant expiration = getTokenExpiration().minus(getOptions().getExpirationErrorMargin());
		Instant scheduled = Comparables.max(expiration, Instant.now());
		Duration delay = Duration.between(Instant.now(), scheduled);
		delay = Comparables.max(delay, getOptions().getMinRefreshInterval());
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
	 * Returns the client registration.
	 *
	 * @return the client registration
	 */
	public OAuth2ClientRegistration getClientRegistration() {
		return registration.getClientRegistration();
	}

	/**
	 * Returns the provider details.
	 *
	 * @return the provider details
	 */
	public OAuth2ProviderDetails getProviderDetails() {
		return registration.getProviderDetails();
	}

	/**
	 * Returns the options object to be able to change the provider options dynamically.
	 *
	 * @return the options for this token provider
	 */
	public OAuth2TokenProviderOptions getOptions() {
		return options;
	}
}
