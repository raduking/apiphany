package org.apiphany.security.oauth2;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.apiphany.logging.Slf4jLoggerAdapter;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.function.Predicates;
import org.morphix.lang.resource.ScopedResource;

/**
 * Registry for OAuth2 token providers. This class allows thread safe adding, retrieving, and managing OAuth2 token
 * providers by name. It supports automatic resource management and ensures proper cleanup of providers when the
 * registry is closed.
 * <p>
 * When constructing the token provider registry prefer using the builder method for more complex configurations, as it
 * provides a more fluent API and better readability when multiple optional parameters are involved, such as the
 * provider name converter, provider name filter, and created provider customizer.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2TokenProviderRegistry implements AutoCloseable {

	/**
	 * The class logger.
	 */
	private static final LoggerAdapter LOGGER = Slf4jLoggerAdapter.of(OAuth2TokenProviderRegistry.class);

	/**
	 * The registered providers map.
	 */
	private final Map<String, ScopedResource<OAuth2TokenProvider>> providers = new ConcurrentHashMap<>();

	/**
	 * Flag indicating whether the registry is closing.
	 */
	private final AtomicBoolean closing = new AtomicBoolean(false);

	/**
	 * The OAuth2 registry this token provider registry is based on.
	 */
	private final OAuth2Registry oAuth2Registry;

	/**
	 * Constructor with builder.
	 *
	 * @param builder the builder containing the configuration for the registry
	 */
	@SuppressWarnings("resource")
	private OAuth2TokenProviderRegistry(final Builder builder) {
		this.oAuth2Registry = Objects.requireNonNull(builder.oAuth2Registry, "OAuth2 registry cannot be null");
		for (OAuth2ResolvedRegistration registration : oAuth2Registry.entries()) {
			String clientRegistrationName = registration.getClientRegistrationName();
			String providerName = builder.providerNameConverter.apply(clientRegistrationName);
			if (builder.providerNameFilter.test(providerName)) {
				OAuth2TokenProvider.Builder providerBuilder = OAuth2TokenProvider.builder().registration(registration);
				builder.providerBuilderCustomizer.accept(providerBuilder);
				OAuth2TokenProvider provider = providerBuilder.build();
				addProvider(providerName, ScopedResource.managed(provider));
				builder.providerPostConstruct.accept(providerName, provider);
			} else {
				LOGGER.info("Skipping OAuth2 token provider creation for client registration: '{}' "
						+ "as the provider name: '{}' was filtered out.", clientRegistrationName, providerName);
			}
		}
	}

	/**
	 * Creates a new builder for constructing an OAuth2 token provider registry.
	 *
	 * @return a new builder for constructing an OAuth2 token provider registry
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 registry. This registry will be empty because to
	 * create the token providers the caller must provide at least a token client supplier.
	 * <p>
	 * When this method is used the caller will have to manually create and add the token providers to the registry using
	 * the {@link #addProvider(String, ScopedResource)} method.
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 * @return an empty OAuth2 token provider registry
	 */
	public static OAuth2TokenProviderRegistry of(final OAuth2Registry oAuth2Registry) {
		return builder()
				.oAuth2Registry(oAuth2Registry)
				.build();
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 registry. When building the token providers, the
	 * given token client supplier is used.
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 * @param tokenClientSupplier supplies a token provider client based on the client registration and provider details
	 * @return an OAuth2 token provider registry based on the given OAuth2 registry
	 */
	public static OAuth2TokenProviderRegistry of(final OAuth2Registry oAuth2Registry, final OAuth2TokenClientSupplier tokenClientSupplier) {
		return builder()
				.oAuth2Registry(oAuth2Registry)
				.customizeProviderBuilder(providerBuilder -> providerBuilder.tokenClientSupplier(tokenClientSupplier))
				.build();
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 properties. This will build the underlying OAuth2
	 * registry based on the given OAuth2 properties and then create the token providers using the given token client
	 * supplier.
	 *
	 * @param oAuth2Properties the OAuth2 properties
	 * @param tokenClientSupplier supplies a token provider client based on the client registration and provider details
	 * @return an OAuth2 token provider registry based on the given OAuth2 properties
	 */
	public static OAuth2TokenProviderRegistry of(final OAuth2Properties oAuth2Properties, final OAuth2TokenClientSupplier tokenClientSupplier) {
		return of(OAuth2Registry.of(oAuth2Properties), tokenClientSupplier);
	}

	/**
	 * Adds a new OAuth2 token provider to the registry.
	 *
	 * @param name the name of the OAuth2 token provider
	 * @param provider the OAuth2 token provider
	 * @throws IllegalStateException if an OAuth2 token provider with the given name is already registered or if the
	 *     registry is closing
	 */
	@SuppressWarnings("resource")
	public void addProvider(final String name, final ScopedResource<OAuth2TokenProvider> provider) {
		if (closing.get()) {
			provider.closeIfManaged(
					e -> LOGGER.error("Error closing OAuth2 token provider: '{}' when adding to a closing registry.", name, e));
			throw new IllegalStateException("Cannot add new OAuth2 token provider " + name + " to a closing registry.");
		}
		ScopedResource<OAuth2TokenProvider> existing =
				providers.putIfAbsent(name, Objects.requireNonNull(provider, "OAuth2 token provider cannot be null"));
		if (null != existing) {
			provider.closeIfManaged(
					e -> LOGGER.error("Error closing candidate OAuth2 token provider: '{}' when a provider with the same name exists.", name, e));
			throw new IllegalStateException("An OAuth2 token provider with name '" + name + "' is already registered.");
		}
	}

	/**
	 * Returns an unmodifiable list of all registered OAuth2 token providers.
	 *
	 * @return an unmodifiable list of all registered OAuth2 token providers
	 */
	public List<OAuth2TokenProvider> getProviders() {
		return providers.values().stream()
				.map(ScopedResource::unwrap)
				.toList();
	}

	/**
	 * Returns an unmodifiable list of all registered OAuth2 token provider names.
	 *
	 * @return an unmodifiable list of all registered OAuth2 token provider names
	 */
	public List<String> getProviderNames() {
		return List.copyOf(providers.keySet());
	}

	/**
	 * Returns the OAuth2 token provider registered with the given name, or null if no provider is found. The returned
	 * provider is not a scoped resource, so it should not be closed by the caller.
	 *
	 * @param name the name of the OAuth2 token provider
	 * @return the OAuth2 token provider registered with the given name, or null if no provider is found
	 */
	public OAuth2TokenProvider getProvider(final String name) {
		return getProvider(name, providerName -> {
			LOGGER.warn("No OAuth2 token provider found with name: {}", providerName);
			return null;
		});
	}

	/**
	 * Returns the OAuth2 token provider registered with the given name, or throws an exception if no provider is found.
	 * <p>
	 * If the provider is not found, the given function is called with the provider name to determine the exception to
	 * throw. If the function returns null, this method will return null instead of throwing an exception. This allows for
	 * flexible error handling based on the context of the call, as the caller can choose to throw an exception or return
	 * null when a provider is not found. For example, in some cases it might be acceptable to return null when a provider
	 * is not found, while in other cases it might be more appropriate to throw an exception.
	 * <p>
	 * The returned provider is not a scoped resource, so it should not be closed by the caller and the registry is
	 * responsible for managing its lifecycle.
	 *
	 * @param name the name of the OAuth2 token provider
	 * @param missingProvider a function that accepts the provider name and returns the exception to throw when no provider
	 *     is found with the given name or returns null to indicate that this method should return null instead of throwing
	 *     an exception
	 * @return the OAuth2 token provider registered with the given name, or null if no provider is found and the missing
	 * provider function returns null
	 * @throws IllegalStateException if no provider is found with the given name and the missing
	 */
	@SuppressWarnings("resource")
	public OAuth2TokenProvider getProvider(final String name, final Function<String, ? extends Exception> missingProvider) {
		ScopedResource<OAuth2TokenProvider> scopedProvider = providers.get(name);
		if (null == scopedProvider) {
			Exception exception = missingProvider.apply(name);
			if (null != exception) {
				throw new IllegalStateException("No OAuth2 token provider found with name: " + name, exception);
			}
			return null;
		}
		return scopedProvider.unwrap();
	}

	/**
	 * Returns the OAuth2 registry this token provider registry is based on.
	 *
	 * @return the OAuth2 registry this token provider registry is based on
	 */
	public OAuth2Registry getOAuth2Registry() {
		return oAuth2Registry;
	}

	/**
	 * Closes all registered OAuth2 token providers.
	 *
	 * @see AutoCloseable#close()
	 */
	@Override
	@SuppressWarnings("resource")
	public void close() throws Exception {
		if (!closing.compareAndSet(false, true)) {
			LOGGER.warn("Registry is closing or already closed, skipping close operation.");
			return;
		}
		for (Map.Entry<String, ScopedResource<OAuth2TokenProvider>> entry : providers.entrySet()) {
			ScopedResource<OAuth2TokenProvider> provider = entry.getValue();
			provider.closeIfManaged(e -> LOGGER.error("Error closing OAuth2 token provider: {} when closing registry.", entry.getKey(), e));
		}
	}

	/**
	 * Builder for constructing an OAuth2 token provider registry with a fluent API.
	 * <p>
	 * When building the token providers, the given token client supplier is used and when building the provider name the
	 * token provider name converter is used.
	 * <p>
	 * The builder allows filtering which providers to create based on their converted name. When a provider name is
	 * filtered out, no token provider is created for the corresponding client registration.
	 * <p>
	 * Note: The filter is applied on the converted provider name.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Builder {

		/**
		 * The OAuth2 registry this token provider registry is based on. This is a required field and must be set before
		 * building the registry.
		 */
		private OAuth2Registry oAuth2Registry;

		/**
		 * The token provider builder customizer that is used for building the token providers for the client registrations in
		 * the underlying OAuth2 registry. This builder is used as a base for building the token providers for each client
		 * registration and can be customized with common configuration for all providers, such as the token refresh scheduler
		 * and token client supplier. The builder is initialized with default values, so it can be used without any
		 * customization if the defaults are sufficient.
		 */
		private Consumer<OAuth2TokenProvider.Builder> providerBuilderCustomizer = Consumers.noConsumer();

		/**
		 * A function that maps the client registration name to the token provider name. This is an optional field and if not
		 * set, the registry will use the client registration name as the token provider name.
		 */
		private UnaryOperator<String> providerNameConverter = UnaryOperator.identity();

		/**
		 * A predicate to filter which providers to include by their converted name. This is an optional field and if not set,
		 * the registry will include all providers. The filter is applied on the converted provider name, so it can be used to
		 * control which providers to create based on their final name in the registry, not just based on the client
		 * registration name.
		 */
		private Predicate<String> providerNameFilter = Predicates.acceptAll();

		/**
		 * A consumer that is called when a new provider is created. This is an optional field and if not set, the registry will
		 * not perform any additional actions when a provider is created. The consumer accepts the provider name and the created
		 * provider instance, so it can be used for additional initialization or logging after the provider is created.
		 */
		private BiConsumer<String, OAuth2TokenProvider> providerPostConstruct = Consumers.noBiConsumer();

		/**
		 * Default constructor.
		 */
		private Builder() {
			// empty
		}

		/**
		 * Sets the OAuth2 registry this token provider registry is based on. This is a required field and must be set before
		 * building the registry.
		 *
		 * @param oAuth2Properties the OAuth2 properties to build the underlying registry on
		 * @return this builder instance for chaining
		 */
		public Builder oAuth2Properties(final OAuth2Properties oAuth2Properties) {
			return oAuth2Registry(OAuth2Registry.of(oAuth2Properties));
		}

		/**
		 * Sets the OAuth2 registry this token provider registry is based on. This is a required field and must be set before
		 * building the registry.
		 *
		 * @param oAuth2Registry the OAuth2 registry this token provider registry is based on
		 * @return this builder instance for chaining
		 */
		public Builder oAuth2Registry(final OAuth2Registry oAuth2Registry) {
			this.oAuth2Registry = oAuth2Registry;
			return this;
		}

		/**
		 * Sets the token provider builder customizer to be used for building the token providers for the client registrations
		 * in the underlying OAuth2 registry. This builder is used as a base for building the token providers for each client
		 * registration and can be customized with common configuration for all providers, such as the token refresh scheduler
		 * and token client supplier. The builder is initialized with default values, so it can be used without any
		 * customization if the defaults are sufficient.
		 *
		 * @param providerBuilderCustomizer the token provider builder customizer to be used for building the token providers
		 *     for the client registrations in the underlying OAuth2 registry
		 * @return this builder instance for chaining
		 */
		public Builder customizeProviderBuilder(final Consumer<OAuth2TokenProvider.Builder> providerBuilderCustomizer) {
			this.providerBuilderCustomizer = Objects.requireNonNull(providerBuilderCustomizer, "Provider builder customizer cannot be null");
			return this;
		}

		/**
		 * Sets the provider name converter to be used for converting client registration names to token provider names. This is
		 * an optional field and if not set, the registry will use the client registration name as the token provider name.
		 *
		 * @param providerNameConverter the provider name converter to be used for converting client registration names to token
		 *     provider names
		 * @return this builder instance for chaining
		 */
		public Builder providerNameConverter(final UnaryOperator<String> providerNameConverter) {
			this.providerNameConverter = Objects.requireNonNull(providerNameConverter, "Provider name converter cannot be null");
			return this;
		}

		/**
		 * Sets the provider name filter to be used for filtering which providers to include by their converted name. This is an
		 * optional field and if not set, the registry will include all providers. The filter is applied on the converted
		 * provider name, so it can be used to control which providers to create based on their final name in the registry, not
		 * just based on the client registration name.
		 *
		 * @param providerNameFilter the provider name filter to be used for filtering which providers to include by their
		 *     converted name
		 * @return this builder instance for chaining
		 */
		public Builder providerNameFilter(final Predicate<String> providerNameFilter) {
			this.providerNameFilter = Objects.requireNonNull(providerNameFilter, "Provider name filter cannot be null");
			return this;
		}

		/**
		 * Sets the provider post construct consumer to be called when a new provider is created. This is an optional field and
		 * if not set, the registry will not perform any additional actions when a provider is created. The consumer accepts the
		 * provider name and the created provider instance, so it can be used for additional initialization or logging after the
		 * provider is created.
		 *
		 * @param providerPostConstruct the provider post construct consumer to be called when a new provider is created
		 * @return this builder instance for chaining
		 */
		public Builder providerPostConstruct(final BiConsumer<String, OAuth2TokenProvider> providerPostConstruct) {
			this.providerPostConstruct = Objects.requireNonNull(providerPostConstruct, "Provider post construct cannot be null");
			return this;
		}

		/**
		 * Builds the OAuth2 token provider registry based on the provided configuration. This method will create token
		 * providers for all client registrations in the underlying OAuth2 registry that pass the provider name filter and will
		 * use the provided token client supplier and provider name converter for building the providers.
		 *
		 * @return a new OAuth2 token provider registry based on the provided configuration
		 */
		public OAuth2TokenProviderRegistry build() {
			return new OAuth2TokenProviderRegistry(this);
		}
	}
}
