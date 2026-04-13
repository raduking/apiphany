package org.apiphany.security.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import org.apiphany.logging.Slf4jLoggerAdapter;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.morphix.lang.Comparables;
import org.morphix.lang.Nullables;
import org.morphix.lang.retry.Retry;
import org.morphix.lang.retry.WaitCounter;
import org.morphix.lang.thread.ReschedulingTask;
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
	 * The self rescheduling task that handles the token refresh scheduling.
	 */
	private final ReschedulingTask selfReschedulingTask;

	/**
	 * The OAuth2 resolved registration for this provider.
	 */
	private final OAuth2ResolvedRegistration registration;

	/**
	 * The specific options for this provider.
	 */
	private final OAuth2TokenProviderProperties properties;

	/**
	 * The client doing the token refresh.
	 */
	private final AuthenticationTokenProvider tokenClient;

	/**
	 * The authentication token.
	 */
	private AuthenticationToken authenticationToken;

	/**
	 * Supplies the default token expiration.
	 */
	private final Supplier<Instant> defaultExpirationSupplier;

	/**
	 * Creates a new authentication token provider.
	 *
	 * @param specification the OAuth2 token provider specification
	 */
	public OAuth2TokenProvider(final OAuth2TokenProviderSpec specification) {
		this.properties = specification.getTokenProviderProperties();
		this.registration = specification.getResolvedRegistration();
		this.defaultExpirationSupplier = specification.getDefaultExpirationSupplier();

		if (null == registration) {
			LOGGER.warn("[{}] No registration provided, token retrieval will be disabled.", getName());
			this.tokenClient = null;
		} else {
			OAuth2TokenClientSupplier supplier = specification.getTokenClientSupplier();
			this.tokenClient = supplier.get(registration.getClientRegistration(), registration.getProviderDetails());
		}
		this.selfReschedulingTask = newReschedulingTask(specification);

		if (null != tokenClient) {
			enable();
		}
	}

	/**
	 * Builds a new self rescheduling task for this token provider.
	 *
	 * @param specification the OAuth2 token provider specification
	 * @return a new self rescheduling task for this token provider
	 */
	@SuppressWarnings("resource")
	private ReschedulingTask newReschedulingTask(final OAuth2TokenProviderSpec specification) {
		return ReschedulingTask.builder()
				.name(getName())
				.task(this::updateAuthenticationToken)
				.nextDelay(this::getNextUpdateDelay)
				.minDelay(properties.getMinRefreshInterval())
				.scheduler(specification.getTokenRefreshScheduler())
				.taskCancelRetry(Retry.of(WaitCounter.of(properties.getMaxTaskCloseAttempts(), properties.getCloseTaskRetryInterval())))
				.logger(Slf4jLoggerAdapter.of(LOGGER))
				.build();
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
		selfReschedulingTask.close();
		if (getTokenClient() instanceof AutoCloseable closeable) {
			closeable.close();
		}
	}

	/**
	 * Returns the client registration name.
	 *
	 * @return client registration name
	 */
	public String getClientRegistrationName() {
		return Nullables.whenNotNull(registration,
				OAuth2ResolvedRegistration::getClientRegistrationName,
				() -> OAuth2ResolvedRegistration.UNKNOWN_REGISTRATION_NAME);
	}

	/**
	 * Returns the name of this token provider, which is the same as the client registration name making this method a
	 * simple alias for {@link #getClientRegistrationName()}.
	 *
	 * @return the name of this token provider
	 */
	public String getName() {
		return getClientRegistrationName();
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
		if (null == authenticationToken) {
			LOGGER.warn("[{}] No authentication token available, using default expiration.", getName());
			return getDefaultTokenExpiration();
		}
		Instant expiration = authenticationToken.getExpiration();
		if (null == expiration) {
			LOGGER.warn("[{}] No expiration date in authentication token, using default expiration.", getName());
			return getDefaultTokenExpiration();
		}
		return expiration;
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
		String clientRegistrationName = getClientRegistrationName();
		LOGGER.debug("[{}] Token expired, requesting new token.", clientRegistrationName);
		Instant expiration = Instant.now();
		try {
			AuthenticationToken token = getAuthenticationTokenFromClient();
			token.setExpiration(expiration.plusSeconds(token.getExpiresIn()));
			setAuthenticationToken(token);
			LOGGER.debug("[{}] Successfully retrieved new token.", clientRegistrationName);
		} catch (Exception e) {
			LOGGER.error("[{}] Error retrieving new token.", clientRegistrationName, e);
		}
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
	 * Returns the delay until the next token update. The delay is calculated as the time until the token expiration minus
	 * the error margin defined in {@link OAuth2TokenProviderProperties}. If the calculated delay is negative, it returns
	 * the minimum refresh interval defined in {@link OAuth2TokenProviderProperties}.
	 *
	 * @return the delay until the next token update
	 */
	private Duration getNextUpdateDelay() {
		Instant expiration = getTokenExpiration().minus(getProperties().getExpirationErrorMargin());
		Instant scheduled = Comparables.max(expiration, Instant.now());
		Duration delay = Duration.between(Instant.now(), scheduled);
		return Comparables.max(delay, getProperties().getMinRefreshInterval());
	}

	/**
	 * Returns true if the scheduler is enabled, false otherwise.
	 *
	 * @return true if the scheduler is enabled, false otherwise
	 */
	public boolean isSchedulerEnabled() {
		return selfReschedulingTask.isEnabled();
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
	 * Enables the scheduler if it is not already enabled.
	 *
	 * @return true if the scheduler was enabled, false if it was already enabled
	 */
	public boolean enable() {
		return selfReschedulingTask.enable();
	}

	/**
	 * Disables the scheduler if it is not already disabled.
	 *
	 * @return true if the scheduler was disabled, false if it was already disabled
	 */
	public boolean disable() {
		return selfReschedulingTask.disable();
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
