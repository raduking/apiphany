package org.apiphany.security.oauth2;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.apiphany.lang.ScopedResource;
import org.morphix.lang.Nullables;

/**
 * OAuth2 token provider specification. Holds the configuration needed to create an OAuth2 token provider.
 * <p>
 * This class uses the builder pattern for construction. The following default values are used:
 * <ul>
 * <li>If no properties are provided, default properties are used</li>
 * <li>If no scheduler is provided, a virtual-thread scheduler is created and owned by the provider</li>
 * <li>If no token client supplier is provided, a supplier that always supplies a {@code null} authentication token
 * provider is used</li>
 * <li>If no default expiration supplier is provided, the current instant supplier is used</li>
 * </ul>
 * WARNING: The caller is responsible for shutting down the scheduler if a custom one is provided.
 * <p>
 * The token provider needs a single OAuth2 resolved registration to function. If multiple registrations are needed,
 * multiple token providers must be created.
 * <p>
 * The resolved registration can be provided directly or built from OAuth2 properties and/or a client registration name.
 * If no client registration name is provided, the properties must contain only one client registration.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2TokenProviderSpec {

	/**
	 * the OAuth2 token provider properties.
	 */
	private final OAuth2TokenProviderProperties properties;

	/**
	 * The OAuth2 resolved registration.
	 */
	private final OAuth2ResolvedRegistration registration;

	/**
	 * The token client supplier.
	 */
	private final OAuth2TokenClientSupplier tokenClientSupplier;

	/**
	 * The token refresh scheduler.
	 */
	private final ScopedResource<ScheduledExecutorService> tokenRefreshScheduler;

	/**
	 * The default expiration supplier.
	 */
	private final Supplier<Instant> defaultExpirationSupplier;

	/**
	 * Constructor with builder.
	 *
	 * @param builder the builder
	 */
	private OAuth2TokenProviderSpec(final Builder builder) {
		this.properties = Nullables.nonNullOrDefault(builder.properties, OAuth2TokenProviderProperties::defaults);
		this.registration = builder.registration;
		this.tokenClientSupplier = Nullables.nonNullOrDefault(builder.tokenClientSupplier,
				() -> (clientRegistration, providerDetails) -> null);
		this.tokenRefreshScheduler = Nullables.nonNullOrDefault(builder.tokenRefreshScheduler,
				() -> ScopedResource.managed(OAuth2TokenProviderSpec.defaultSchedulerExecutor()));
		this.defaultExpirationSupplier = Nullables.nonNullOrDefault(builder.defaultExpirationSupplier,
				() -> Instant::now);
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
	 * Returns a new default scheduler executor. Creates a scheduled executor service using virtual threads.
	 * <p>
	 * The caller is responsible for shutting down the executor.
	 *
	 * @return the scheduled executor service
	 */
	public static ScheduledExecutorService defaultSchedulerExecutor() {
		return Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
	}

	/**
	 * Returns the OAuth2 token provider properties.
	 *
	 * @return the properties
	 */
	public OAuth2TokenProviderProperties getTokenProviderProperties() {
		return properties;
	}

	/**
	 * Returns the OAuth2 resolved registration.
	 *
	 * @return the registration
	 */
	public OAuth2ResolvedRegistration getResolvedRegistration() {
		return registration;
	}

	/**
	 * Returns the token client supplier.
	 *
	 * @return the token client supplier
	 */
	public OAuth2TokenClientSupplier getTokenClientSupplier() {
		return tokenClientSupplier;
	}

	/**
	 * Returns the token refresh scheduler.
	 *
	 * @return the token refresh scheduler
	 */
	public ScopedResource<ScheduledExecutorService> getTokenRefreshScheduler() {
		return tokenRefreshScheduler;
	}

	/**
	 * Returns the default expiration supplier.
	 *
	 * @return the default expiration supplier
	 */
	public Supplier<Instant> getDefaultExpirationSupplier() {
		return defaultExpirationSupplier;
	}

	/**
	 * Builder for OAuth2 token provider specification.
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
		 * The token client supplier.
		 */
		private OAuth2TokenClientSupplier tokenClientSupplier;

		/**
		 * The token refresh scheduler.
		 */
		private ScopedResource<ScheduledExecutorService> tokenRefreshScheduler;

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
		 * Sets the token refresh scheduler.
		 *
		 * @param tokenRefreshScheduler the token refresh scheduler
		 * @return the builder
		 */
		public Builder tokenRefreshScheduler(final ScheduledExecutorService tokenRefreshScheduler) {
			return tokenRefreshScheduler(ScopedResource.unmanaged(tokenRefreshScheduler));
		}

		/**
		 * Sets the token refresh scheduler.
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
		 * Builds the OAuth2 token provider specification.
		 *
		 * @return the OAuth2 token provider specification
		 */
		public OAuth2TokenProviderSpec build() {
			return new OAuth2TokenProviderSpec(this);
		}
	}
}
