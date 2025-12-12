package org.apiphany.security.oauth2;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.apiphany.lang.ScopedResource;
import org.apiphany.security.AuthenticationTokenProvider;
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
	 * Functional interface for handling errors when closing OAuth2 token providers.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	interface CloseErrorHandler extends BiConsumer<String, Exception> {
		// empty
	}

	/**
	 * Constructor.
	 */
	private OAuth2TokenProviderRegistry(final OAuth2Registry oAuth2Registry) {
		this.oAuth2Registry = oAuth2Registry;
	}

	/**
	 * Creates an empty OAuth2 token provider registry.
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 * @return an empty OAuth2 token provider registry
	 */
	public static OAuth2TokenProviderRegistry of(final OAuth2Registry oAuth2Registry) {
		return new OAuth2TokenProviderRegistry(Objects.requireNonNull(oAuth2Registry, "OAuth2 registry cannot be null"));
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 registry.
	 *
	 * @param oAuth2Registry the OAuth2 registry must not be null
	 * @param tokenClientSupplier a function that creates an authentication token provider based on the client registration
	 *     and provider details
	 * @param tokenProviderNameFunction a function that maps the client registration name to the token provider name
	 * @return an OAuth2 token provider registry based on the given OAuth2 registry
	 */
	public static OAuth2TokenProviderRegistry of(
			final OAuth2Registry oAuth2Registry,
			final BiFunction<OAuth2ClientRegistration, OAuth2ProviderDetails, AuthenticationTokenProvider> tokenClientSupplier,
			final UnaryOperator<String> tokenProviderNameFunction) {
		OAuth2TokenProviderRegistry registry = of(oAuth2Registry);
		List<OAuth2TokenProvider> tokenProviders = oAuth2Registry.tokenProviders(tokenClientSupplier);
		for (OAuth2TokenProvider provider : tokenProviders) {
			String providerName = tokenProviderNameFunction.apply(provider.getClientRegistrationName());
			registry.add(providerName, ScopedResource.managed(provider));
		}
		return registry;
	}

	/**
	 * Creates an OAuth2 token provider registry based on the given OAuth2 properties.
	 *
	 * @param oAuth2Properties the OAuth2 properties
	 * @param tokenClientSupplier a function that creates an authentication token provider based on the client registration
	 *     and provider details
	 * @param tokenProviderNameFunction a function that maps the client registration name to the token provider name
	 * @return an OAuth2 token provider registry based on the given OAuth2 properties
	 */
	public static OAuth2TokenProviderRegistry of(
			final OAuth2Properties oAuth2Properties,
			final BiFunction<OAuth2ClientRegistration, OAuth2ProviderDetails, AuthenticationTokenProvider> tokenClientSupplier,
			final UnaryOperator<String> tokenProviderNameFunction) {
		return of(OAuth2Registry.of(oAuth2Properties), tokenClientSupplier, tokenProviderNameFunction);
	}

	/**
	 * Adds a new OAuth2 token provider to the registry.
	 *
	 * @param name the name of the OAuth2 token provider
	 * @param provider the OAuth2 token provider
	 * @throws IllegalStateException if an OAuth2 token provider with the given name is already registered or if the
	 *     registry is closing
	 */
	public void add(final String name, final ScopedResource<OAuth2TokenProvider> provider) {
		if (closing.get()) {
			closeIfManaged(name, provider,
					(n, e) -> LOGGER.error("Error closing OAuth2 token provider: '{}' when adding to a closing registry.", n, e));
			throw new IllegalStateException("Cannot add new OAuth2 token provider " + name + " to a closing registry.");
		}
		ScopedResource<OAuth2TokenProvider> existing =
				providers.putIfAbsent(name, Objects.requireNonNull(provider, "OAuth2 token provider cannot be null"));
		if (null != existing) {
			closeIfManaged(name, provider,
					(n, e) -> LOGGER.error("Error closing candidate OAuth2 token provider: '{}' when a provider with the same name exists.", n, e));
			throw new IllegalStateException("An OAuth2 token provider with name '" + name + "' is already registered.");
		}
	}

	/**
	 * Closes the given OAuth2 token provider if it is managed, logging any errors.
	 *
	 * @param name the name of the OAuth2 token provider
	 * @param provider the OAuth2 token provider
	 */
	private static void closeIfManaged(final String name, final ScopedResource<OAuth2TokenProvider> provider, final CloseErrorHandler onError) {
		try {
			provider.closeIfManaged();
		} catch (Exception e) {
			onError.accept(name, e);
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
	public void close() throws Exception {
		if (!closing.compareAndSet(false, true)) {
			return;
		}
		for (Map.Entry<String, ScopedResource<OAuth2TokenProvider>> entry : providers.entrySet()) {
			closeIfManaged(entry.getKey(), entry.getValue(),
					(n, e) -> LOGGER.error("Error closing OAuth2 token provider: {} when closing registry.", n, e));
		}
	}
}
