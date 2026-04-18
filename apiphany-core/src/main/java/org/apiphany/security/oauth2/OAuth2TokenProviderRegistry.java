package org.apiphany.security.oauth2;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.morphix.lang.function.Consumers;
import org.morphix.lang.function.Predicates;
import org.morphix.lang.resource.ScopedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for OAuth2 token providers. This class allows thread safe adding, retrieving, and managing OAuth2 token
 * providers by name. It supports automatic resource management and ensures proper cleanup of providers when the
 * registry is closed.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2TokenProviderRegistry implements AutoCloseable {

	/**
	 * The class logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2TokenProviderRegistry.class);

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
	 * Constructor.
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 */
	private OAuth2TokenProviderRegistry(final OAuth2Registry oAuth2Registry) {
		this.oAuth2Registry = Objects.requireNonNull(oAuth2Registry, "OAuth2 registry cannot be null");
	}

	/**
	 * Constructor with builder.
	 *
	 * @param builder the builder containing the configuration for the registry
	 */
	@SuppressWarnings("resource")
	private OAuth2TokenProviderRegistry(final Builder builder) {
		this(builder.oAuth2Registry);
		for (OAuth2ResolvedRegistration registration : oAuth2Registry.entries()) {
			String clientRegistrationName = registration.getClientRegistrationName();
			String providerName = builder.providerNameConverter.apply(clientRegistrationName);
			if (builder.providerNameFilter.test(providerName)) {
				OAuth2TokenProviderSpec.Builder specBuilder = OAuth2TokenProviderSpec.builder();
				builder.specCustomizer.accept(specBuilder);
				OAuth2TokenProvider provider = oAuth2Registry.tokenProvider(clientRegistrationName, specBuilder);
				add(providerName, ScopedResource.managed(provider));
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
	 * create the token providers the caller must provide a token client supplier and token provider name converter.
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 * @return an empty OAuth2 token provider registry
	 */
	public static OAuth2TokenProviderRegistry of(final OAuth2Registry oAuth2Registry) {
		return new OAuth2TokenProviderRegistry(oAuth2Registry);
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 registry. When building the token providers, the
	 * given token client supplier is used and when building the provider name the token provider name converter is used.
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 * @param tokenClientSupplier supplies a token provider client based on the client registration and provider details
	 * @param providerNameConverter a function that maps the client registration name to the token provider name
	 * @return an OAuth2 token provider registry based on the given OAuth2 registry
	 */
	public static OAuth2TokenProviderRegistry of(
			final OAuth2Registry oAuth2Registry,
			final OAuth2TokenClientSupplier tokenClientSupplier,
			final UnaryOperator<String> providerNameConverter) {
		return of(oAuth2Registry, tokenClientSupplier, providerNameConverter, Consumers.noBiConsumer());
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 registry. When building the token providers, the
	 * given token client supplier is used and when building the provider name the token provider name converter is used.
	 * <p>
	 * For controlling which providers to create, use the other method that accepts a provider name filter:
	 * {@code #of(OAuth2Registry, OAuth2TokenClientSupplier, UnaryOperator, Predicate, BiConsumer)} .
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 * @param tokenClientSupplier supplies a token provider client based on the client registration and provider details
	 * @param providerNameConverter a function that maps the client registration name to the token provider name
	 * @param createdProviderCustomizer a consumer that is called when a new provider is created
	 * @return an OAuth2 token provider registry based on the given OAuth2 registry
	 */
	public static OAuth2TokenProviderRegistry of(
			final OAuth2Registry oAuth2Registry,
			final OAuth2TokenClientSupplier tokenClientSupplier,
			final UnaryOperator<String> providerNameConverter,
			final BiConsumer<String, OAuth2TokenProvider> createdProviderCustomizer) {
		return of(oAuth2Registry, tokenClientSupplier, providerNameConverter, Predicates.acceptAll(), createdProviderCustomizer);
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 registry. When building the token providers, the
	 * given token client supplier is used and when building the provider name the token provider name converter is used.
	 * This method allows filtering which providers to create based on their converted name. When a provider name is
	 * filtered out, no token provider is created for the corresponding client registration.
	 * <p>
	 * Note: The filter is applied on the converted provider name.
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 * @param tokenClientSupplier supplies a token provider client based on the client registration and provider details
	 * @param providerNameConverter a function that maps the client registration name to the token provider name
	 * @param providerNameFilter a predicate to filter which providers to include by their converted name
	 * @param createdProviderCustomizer a consumer that is called when a new provider is created
	 * @return an OAuth2 token provider registry based on the given OAuth2 registry
	 */
	public static OAuth2TokenProviderRegistry of(
			final OAuth2Registry oAuth2Registry,
			final OAuth2TokenClientSupplier tokenClientSupplier,
			final UnaryOperator<String> providerNameConverter,
			final Predicate<String> providerNameFilter,
			final BiConsumer<String, OAuth2TokenProvider> createdProviderCustomizer) {
		return builder()
				.oAuth2Registry(oAuth2Registry)
				.customizeSpec(specBuilder -> specBuilder
						.tokenClientSupplier(tokenClientSupplier))
				.providerNameConverter(providerNameConverter)
				.providerNameFilter(providerNameFilter)
				.providerPostConstruct(createdProviderCustomizer)
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
	public static OAuth2TokenProviderRegistry of(
			final OAuth2Registry oAuth2Registry,
			final OAuth2TokenClientSupplier tokenClientSupplier) {
		return of(oAuth2Registry, tokenClientSupplier, UnaryOperator.identity());
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 properties. This will build the underlying OAuth2
	 * registry based on the given OAuth2 properties and then create the token providers using the given token client
	 * supplier and token provider name function for the provider name.
	 *
	 * @param oAuth2Properties the OAuth2 properties
	 * @param tokenClientSupplier supplies a token provider client based on the client registration and provider details
	 * @param providerNameConverter a function that maps the client registration name to the token provider name
	 * @return an OAuth2 token provider registry based on the given OAuth2 properties
	 */
	public static OAuth2TokenProviderRegistry of(
			final OAuth2Properties oAuth2Properties,
			final OAuth2TokenClientSupplier tokenClientSupplier,
			final UnaryOperator<String> providerNameConverter) {
		return of(OAuth2Registry.of(oAuth2Properties), tokenClientSupplier, providerNameConverter);
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
	public static OAuth2TokenProviderRegistry of(
			final OAuth2Properties oAuth2Properties,
			final OAuth2TokenClientSupplier tokenClientSupplier) {
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
	public void add(final String name, final ScopedResource<OAuth2TokenProvider> provider) {
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
	 * Returns the OAuth2 token provider registered with the given name.
	 *
	 * @param name the name of the OAuth2 token provider
	 * @return the OAuth2 token provider registered with the given name, or null if no provider is found
	 */
	@SuppressWarnings("resource")
	public OAuth2TokenProvider getProvider(final String name) {
		ScopedResource<OAuth2TokenProvider> scopedProvider = providers.get(name);
		if (null == scopedProvider) {
			LOGGER.warn("No OAuth2 token provider found with name: {}", name);
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
		 * The token provider specification builder that is used for building the token providers for the client registrations
		 * in the underlying OAuth2 registry. This builder is used as a base for building the token provider specifications for
		 * each client registration and can be customized with common configuration for all providers, such as the token refresh
		 * scheduler and token client supplier. The builder is initialized with default values, so it can be used without any
		 * customization if the defaults are sufficient.
		 */
		private Consumer<OAuth2TokenProviderSpec.Builder> specCustomizer = Consumers.noConsumer();

		/**
		 * A function that maps the client registration name to the token provider name. This is an optional field and if not
		 * set, the registry will use the client registration name as the token provider name.
		 */
		private UnaryOperator<String> providerNameConverter;

		/**
		 * A predicate to filter which providers to include by their converted name. This is an optional field and if not set,
		 * the registry will include all providers. The filter is applied on the converted provider name, so it can be used to
		 * control which providers to create based on their final name in the registry, not just based on the client
		 * registration name.
		 */
		private Predicate<String> providerNameFilter;

		/**
		 * A consumer that is called when a new provider is created. This is an optional field and if not set, the registry will
		 * not perform any additional actions when a provider is created. The consumer accepts the provider name and the created
		 * provider instance, so it can be used for additional initialization or logging after the provider is created.
		 */
		private BiConsumer<String, OAuth2TokenProvider> providerPostConstruct;

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
		 * Sets the token provider specification builder customizer to be used for building the token providers for the client
		 * registrations in the underlying OAuth2 registry. This builder is used as a base for building the token provider
		 * specifications for each client registration and can be customized with common configuration for all providers, such
		 * as the token refresh scheduler and token client supplier. The builder is initialized with default values, so it can
		 * be used without any customization if the defaults are sufficient.
		 *
		 * @param specCustomizer the token provider specification builder customizer to be used for building the token providers
		 *     for the client registrations in the underlying OAuth2 registry
		 * @return this builder instance for chaining
		 */
		public Builder customizeSpec(final Consumer<OAuth2TokenProviderSpec.Builder> specCustomizer) {
			this.specCustomizer = specCustomizer;
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
			this.providerNameConverter = providerNameConverter;
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
			this.providerNameFilter = providerNameFilter;
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
			this.providerPostConstruct = providerPostConstruct;
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
