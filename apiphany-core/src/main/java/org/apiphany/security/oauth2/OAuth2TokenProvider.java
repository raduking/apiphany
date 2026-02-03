package org.apiphany.security.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.apiphany.lang.ScopedResource;
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
 * time minus an error margin defined in {@link OAuth2TokenProviderProperties}).
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
	private final AuthenticationTokenProvider tokenClient;

	/**
	 * Token retrieval scheduler.
	 */
	private final ScopedResource<ScheduledExecutorService> tokenRefreshScheduler;

	/**
	 * Scheduler enabled flag.
	 */
	private final AtomicBoolean schedulerEnabled = new AtomicBoolean(false);

	/**
	 * The OAuth2 resolved registration for this provider.
	 */
	private final OAuth2ResolvedRegistration registration;

	/**
	 * Supplies the default token expiration.
	 */
	private final Supplier<Instant> defaultExpirationSupplier;

	/**
	 * The specific options for this provider.
	 */
	private final OAuth2TokenProviderProperties properties;

	/**
	 * The scheduled future to stop.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * The authentication token.
	 */
	private AuthenticationToken authenticationToken;

	/**
	 * Creates a new authentication token provider.
	 *
	 * @param specification the OAuth2 token provider specification
	 */
	public OAuth2TokenProvider(final OAuth2TokenProviderSpec specification) {
		this.properties = specification.getTokenProviderProperties();
		this.registration = specification.getResolvedRegistration();
		this.tokenRefreshScheduler = specification.getTokenRefreshScheduler();
		this.defaultExpirationSupplier = specification.getDefaultExpirationSupplier();

		if (null == registration) {
			LOGGER.warn("No registration provided for OAuth2TokenProvider, token retrieval will be disabled.");
			this.tokenClient = null;
		} else {
			OAuth2TokenClientSupplier supplier = specification.getTokenClientSupplier();
			this.tokenClient = supplier.get(registration.getClientRegistration(), registration.getProviderDetails());
		}
		if (null != tokenClient) {
			setSchedulerEnabled(true);
			updateAuthenticationToken();
		}
	}

	/**
	 * Builds a new OAuth2 token provider from the given specification.
	 *
	 * @param specification the OAuth2 token provider specification
	 * @return a new OAuth2 token provider
	 */
	public static OAuth2TokenProvider of(final OAuth2TokenProviderSpec specification) {
		return new OAuth2TokenProvider(specification);
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
	@SuppressWarnings("resource")
	private void closeTokenRefreshScheduler() {
		if (tokenRefreshScheduler.isNotManaged()) {
			return;
		}
		ScheduledExecutorService scheduler = tokenRefreshScheduler.unwrap();
		boolean cancelled = null == scheduledFuture;
		if (!cancelled) {
			Retry retry = Retry.of(WaitCounter.of(getProperties().getMaxTaskCloseAttempts(), getProperties().getCloseTaskRetryInterval()));
			cancelled = retry.until(() -> scheduledFuture.cancel(false), Boolean::booleanValue);
		}
		if (cancelled) {
			scheduler.close();
		} else {
			List<Runnable> runningTasks = scheduler.shutdownNow();
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
	@SuppressWarnings("resource")
	private void scheduleTokenUpdate() {
		Instant expiration = getTokenExpiration().minus(getProperties().getExpirationErrorMargin());
		Instant scheduled = Comparables.max(expiration, Instant.now());
		Duration delay = Duration.between(Instant.now(), scheduled);
		delay = Comparables.max(delay, getProperties().getMinRefreshInterval());
		scheduledFuture = tokenRefreshScheduler.unwrap().schedule(this::updateAuthenticationToken, delay.toMillis(), TimeUnit.MILLISECONDS);
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
	public OAuth2TokenProviderProperties getProperties() {
		return properties;
	}
}
