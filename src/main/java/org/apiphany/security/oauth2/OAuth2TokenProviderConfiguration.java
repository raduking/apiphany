package org.apiphany.security.oauth2;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.morphix.lang.Nullables;

/**
 * OAuth2 token provider configuration.
 * <ul>
 * <li>If no properties are provided, default properties are used</li>
 * <li>If no scheduler is provided, a virtual-thread scheduler is created and owned by the provider</li>
 * <li>If no token client supplier is provided, a supplier that always supplies a {@code null} authentication token
 * provider is used</li>
 * <li>If no default expiration supplier is provided, the current instant supplier is used</li>
 * </ul>
 *
 * @param properties the OAuth2 token provider properties
 * @param registration the OAuth2 resolved registration for this provider
 * @param tokenRefreshScheduler the token refresh scheduler
 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
 *
 * @author Radu Sebastian LAZIN
 */
public record OAuth2TokenProviderConfiguration(
		OAuth2TokenProviderProperties properties,
		OAuth2ResolvedRegistration registration,
		ScheduledExecutorService tokenRefreshScheduler,
		OAuth2TokenClientSupplier tokenClientSupplier,
		Supplier<Instant> defaultExpirationSupplier) {

	/**
	 * Compact constructor.
	 */
	@SuppressWarnings("resource")
	public OAuth2TokenProviderConfiguration {
		properties = Nullables.nonNullOrDefault(properties, () -> OAuth2TokenProviderProperties.defaults());
		tokenRefreshScheduler = Nullables.nonNullOrDefault(tokenRefreshScheduler, () -> defaultSchedulerExecutor());
		tokenClientSupplier = Nullables.nonNullOrDefault(tokenClientSupplier, () -> (clientRegistration, providerDetails) -> null);
		defaultExpirationSupplier = Nullables.nonNullOrDefault(defaultExpirationSupplier, () -> Instant::now);
	}

	/**
	 * Constructor with builder.
	 *
	 * @param builder
	 */
	private OAuth2TokenProviderConfiguration(final Builder builder) {
		this(
				builder.properties,
				builder.registration,
				builder.tokenRefreshScheduler,
				builder.tokenClientSupplier,
				builder.defaultExpirationSupplier);
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
	 * The default scheduler executor. Creates a scheduled executor service using virtual threads.
	 *
	 * @return the scheduled executor service
	 */
	public static ScheduledExecutorService defaultSchedulerExecutor() {
		return Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
	}

	/**
	 * Builder for OAuth2 token provider configuration.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Builder {

		/**
		 * The properties.
		 */
		private OAuth2TokenProviderProperties properties;

		/**
		 * The registration.
		 */
		private OAuth2ResolvedRegistration registration;

		/**
		 * The token refresh scheduler.
		 */
		private ScheduledExecutorService tokenRefreshScheduler;

		/**
		 * The token client supplier.
		 */
		private OAuth2TokenClientSupplier tokenClientSupplier;

		/**
		 * The default expiration supplier.
		 */
		private Supplier<Instant> defaultExpirationSupplier;

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
			this.properties = properties;
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
			this.registration = OAuth2ResolvedRegistration.of(oAuth2Properties, clientRegistrationName);
			return this;
		}

		/**
		 * Sets the registration from the given OAuth2 properties.
		 *
		 * @param oAuth2Properties the OAuth2 properties
		 * @return the builder
		 */
		public Builder registration(final OAuth2Properties oAuth2Properties) {
			this.registration = OAuth2ResolvedRegistration.of(oAuth2Properties, null);
			return this;
		}

		/**
		 * Sets the token refresh scheduler.
		 *
		 * @param tokenRefreshScheduler the token refresh scheduler
		 * @return the builder
		 */
		public Builder tokenRefreshScheduler(final ScheduledExecutorService tokenRefreshScheduler) {
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
			this.tokenClientSupplier = tokenClientSupplier;
			return this;
		}

		/**
		 * Sets the default expiration supplier.
		 *
		 * @param defaultExpirationSupplier the default expiration supplier
		 * @return the builder
		 */
		public Builder defaultExpirationSupplier(final Supplier<Instant> defaultExpirationSupplier) {
			this.defaultExpirationSupplier = defaultExpirationSupplier;
			return this;
		}

		/**
		 * Builds the OAuth2 token provider configuration.
		 *
		 * @return the OAuth2 token provider configuration
		 */
		public OAuth2TokenProviderConfiguration build() {
			return new OAuth2TokenProviderConfiguration(this);
		}
	}
}
