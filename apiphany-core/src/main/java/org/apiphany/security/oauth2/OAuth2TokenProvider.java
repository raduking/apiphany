package org.apiphany.security.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiphany.logging.Slf4jLoggerAdapter;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.morphix.lang.Comparables;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ExecutionWrapper;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.resource.ScopedResource;
import org.morphix.lang.retry.Retry;
import org.morphix.lang.retry.WaitCounter;
import org.morphix.lang.thread.ReschedulingTask;

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
	private static final LoggerAdapter LOGGER = Slf4jLoggerAdapter.of(OAuth2TokenProvider.class);

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
	 * @param builder the OAuth2 token provider builder
	 */
	public OAuth2TokenProvider(final Builder builder) {
		this.properties = builder.properties;
		this.registration = builder.registration;
		this.defaultExpirationSupplier = builder.defaultExpirationSupplier;

		if (null == registration) {
			LOGGER.warn("[{}] No registration provided, token retrieval will be disabled.", getName());
			this.tokenClient = null;
		} else {
			OAuth2TokenClientSupplier supplier = builder.tokenClientSupplier;
			this.tokenClient = supplier.get(registration.getClientRegistration(), registration.getProviderDetails());
		}
		this.selfReschedulingTask = newReschedulingTask(builder);

		if (null != tokenClient) {
			enable();
		} else {
			LOGGER.warn("[{}] No token client provided, token retrieval will be disabled.", getName());
		}
	}

	/**
	 * Builds a new self rescheduling task for this token provider.
	 *
	 * @param builder the OAuth2 token provider builder
	 * @return a new self rescheduling task for this token provider
	 */
	@SuppressWarnings("resource")
	private ReschedulingTask newReschedulingTask(final Builder builder) {
		ScopedResource<ScheduledExecutorService> scheduler =
				Nullables.nonNullOrDefault(builder.tokenRefreshScheduler, () -> ScopedResource.managed(defaultScheduler()));
		return ReschedulingTask.builder()
				.name(getName())
				.task(this::updateAuthenticationToken)
				.nextDelay(this::getNextUpdateDelay)
				.minDelay(properties.getMinRefreshInterval())
				.scheduler(scheduler)
				.taskCancelRetry(Retry.of(WaitCounter.of(properties.getMaxTaskCloseAttempts(), properties.getCloseTaskRetryInterval())))
				.terminationTimeout(properties.getSchedulerTerminationTimeout())
				.executionWrapper(builder.updateTokenWrapperFunction.apply(getName()))
				.logger(LOGGER)
				.build();
	}

	/**
	 * Creates a new builder for OAuth2 token provider configuration.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
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

	/**
	 * Returns a new default scheduler executor. Creates a scheduled executor service using virtual threads.
	 * <p>
	 * The caller is responsible for shutting down the executor.
	 *
	 * @return the scheduled executor service
	 */
	public static ScheduledExecutorService defaultScheduler() {
		return Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
	}

	/**
	 * OAuth2 token provider builder.
	 * <p>
	 * This class uses the builder pattern for construction. The following default values are used:
	 * <ul>
	 * <li>If no properties are provided, default properties are used</li>
	 * <li>If no scheduler is provided, a virtual-thread scheduler is created and owned by the provider</li>
	 * <li>If no token client supplier is provided, a supplier that always supplies a {@code null} authentication token
	 * provider is used</li>
	 * <li>If no default expiration supplier is provided, the current instant supplier is used</li>
	 * </ul>
	 * WARNING: The caller is responsible for shutting down the scheduler if a custom unmanaged one is provided.
	 * <p>
	 * The token provider needs a single OAuth2 resolved registration to function. If multiple registrations are needed,
	 * multiple token providers must be created.
	 * <p>
	 * The resolved registration can be provided directly or built from OAuth2 properties and/or a client registration name.
	 * If no client registration name is provided, the properties must contain only one client registration.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Builder {

		/**
		 * The properties.
		 */
		private OAuth2TokenProviderProperties properties = OAuth2TokenProviderProperties.defaults();

		/**
		 * The registration.
		 */
		private OAuth2ResolvedRegistration registration;

		/**
		 * The token client supplier.
		 */
		private OAuth2TokenClientSupplier tokenClientSupplier = OAuth2TokenClientSupplier.supplyNull();

		/**
		 * The token refresh scheduler.
		 */
		private ScopedResource<ScheduledExecutorService> tokenRefreshScheduler;

		/**
		 * The default expiration supplier.
		 */
		private Supplier<Instant> defaultExpirationSupplier = Instant::now;

		/**
		 * The wrapper for the token update execution. This can be used to add retry logic, logging, etc. around the token
		 * update process.
		 */
		private Function<String, ExecutionWrapper<Void>> updateTokenWrapperFunction = name -> ExecutionWrapper.identity();

		/**
		 * Hidden constructor.
		 */
		private Builder() {
			// hidden constructor
		}

		/**
		 * Sets the properties.
		 *
		 * @param properties the properties
		 * @return the builder
		 */
		public Builder properties(final OAuth2TokenProviderProperties properties) {
			this.properties = Objects.requireNonNull(properties, "OAuth2 token provider properties cannot be null");
			return this;
		}

		/**
		 * Sets the registration.
		 *
		 * @param registration the registration
		 * @return the builder
		 */
		public Builder registration(final OAuth2ResolvedRegistration registration) {
			this.registration = registration;
			return this;
		}

		/**
		 * Sets the registration from the given OAuth2 properties and client registration name.
		 *
		 * @param oAuth2Properties the OAuth2 properties
		 * @param clientRegistrationName the client registration name
		 * @return the builder
		 */
		public Builder registration(final OAuth2Properties oAuth2Properties, final String clientRegistrationName) {
			return registration(OAuth2ResolvedRegistration.of(oAuth2Properties, clientRegistrationName));
		}

		/**
		 * Sets the registration from the given OAuth2 properties. The OAuth2 properties must contain only one client
		 * registration. If multiple registrations are present use {@link #registration(OAuth2Properties, String)} instead.
		 *
		 * @param oAuth2Properties the OAuth2 properties
		 * @return the builder
		 */
		public Builder registration(final OAuth2Properties oAuth2Properties) {
			return registration(oAuth2Properties, null);
		}

		/**
		 * Sets the token refresh scheduler. The caller will be responsible for shutting down the scheduler.
		 *
		 * @param tokenRefreshScheduler the token refresh scheduler
		 * @return the builder
		 */
		@SuppressWarnings("resource")
		public Builder tokenRefreshScheduler(final ScheduledExecutorService tokenRefreshScheduler) {
			return tokenRefreshScheduler(ScopedResource.unmanaged(tokenRefreshScheduler));
		}

		/**
		 * Sets the token refresh scheduler resource. The caller must specify if the scheduler is managed by the provider or
		 * not. If the scheduler is managed, the provider will be responsible for shutting it down otherwise the caller will be
		 * responsible.
		 *
		 * @param tokenRefreshScheduler the token refresh scheduler
		 * @return the builder
		 */
		public Builder tokenRefreshScheduler(final ScopedResource<ScheduledExecutorService> tokenRefreshScheduler) {
			this.tokenRefreshScheduler = tokenRefreshScheduler;
			return this;
		}

		/**
		 * Sets the token client supplier.
		 *
		 * @param tokenClientSupplier the token client supplier
		 * @return the builder
		 */
		public Builder tokenClientSupplier(final OAuth2TokenClientSupplier tokenClientSupplier) {
			this.tokenClientSupplier = Objects.requireNonNull(tokenClientSupplier, "Token client supplier cannot be null");
			return this;
		}

		/**
		 * Sets the default expiration supplier.
		 *
		 * @param defaultExpirationSupplier the default expiration supplier
		 * @return the builder
		 */
		public Builder defaultExpirationSupplier(final Supplier<Instant> defaultExpirationSupplier) {
			this.defaultExpirationSupplier = Objects.requireNonNull(defaultExpirationSupplier, "Default expiration supplier cannot be null");
			return this;
		}

		/**
		 * Sets the wrapper function for the token update execution. This can be used to add retry logic, logging, etc. around
		 * the token update process. The function receives the token provider name as a parameter and must return the execution
		 * wrapper to be used for that provider.
		 *
		 * @param updateTokenWrapperFunction the wrapper function for the token update execution
		 * @return the builder
		 */
		public Builder updateTokenWrapper(final Function<String, ExecutionWrapper<Void>> updateTokenWrapperFunction) {
			Objects.requireNonNull(updateTokenWrapperFunction, "Update token execution wrapper function cannot be null");
			this.updateTokenWrapperFunction = updateTokenWrapperFunction;
			return this;
		}

		/**
		 * Builds the OAuth2 token provider.
		 *
		 * @return the OAuth2 token provider
		 */
		public OAuth2TokenProvider build() {
			return new OAuth2TokenProvider(this);
		}
	}
}
