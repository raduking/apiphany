package org.apiphany.security.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 registry which holds all the registrations and providers defined by {@link OAuth2Properties}.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2Registry {

	/**
	 * The class logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Registry.class);

	/**
	 * Resolved registrations entries map.
	 */
	private final Map<String, OAuth2ResolvedRegistration> entries;

	/**
	 * Constructor with OAuth2 properties.
	 *
	 * @param properties the properties to build the registrations on
	 */
	private OAuth2Registry(final OAuth2Properties properties) {
		this.entries = buildResolvedRegistrations(properties);
	}

	/**
	 * Returns an OAuth2 registry based on the given properties.
	 *
	 * @param properties the OAuth2 properties
	 * @return an OAuth2 registry based on the given properties
	 */
	public static OAuth2Registry of(final OAuth2Properties properties) {
		return new OAuth2Registry(properties);
	}

	/**
	 * Returns an empty OAuth2 registry.
	 *
	 * @return an empty OAuth2 registry
	 */
	public static OAuth2Registry of() {
		return of(null);
	}

	/**
	 * Builds the resolved registration entries based on the given properties.
	 *
	 * @param properties the OAuth2 properties
	 * @return the resolved registration entries
	 */
	private static Map<String, OAuth2ResolvedRegistration> buildResolvedRegistrations(final OAuth2Properties properties) {
		if (properties == null || properties.getRegistration() == null) {
			return Collections.emptyMap();
		}
		Map<String, OAuth2ResolvedRegistration> resolved = new HashMap<>();
		properties.getRegistration().forEach((name, registration) -> {
			OAuth2ProviderDetails provider = properties.getProviderDetails(registration);
			if (provider != null) {
				resolved.put(name, OAuth2ResolvedRegistration.of(name, registration, provider));
			}
		});
		return Collections.unmodifiableMap(resolved);
	}

	/**
	 * Returns a resolved registration based on the given client registration name.
	 *
	 * @param clientRegistrationName the client registration name
	 * @return a resolved registration based on the given client registration name
	 */
	public OAuth2ResolvedRegistration get(final String clientRegistrationName) {
		OAuth2ResolvedRegistration registration =
				entries.get(Objects.requireNonNull(clientRegistrationName, "clientRegistrationName must not be null"));
		if (null == registration) {
			LOGGER.warn("[{}] No OAuth2 client provided for client registration in: {}.registration.{}",
					clientRegistrationName, OAuth2Properties.ROOT, clientRegistrationName);
		}
		return registration;
	}

	/**
	 * Returns the resolved registration entries.
	 *
	 * @return the resolved registration entries
	 */
	public Collection<OAuth2ResolvedRegistration> entries() {
		return entries.values();
	}

	/**
	 * Returns a new OAuth2 token provider based on the given parameters. The caller is responsible for handling the life
	 * cycle of the returned token provider.
	 *
	 * @param clientRegistrationName the client registration name
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 * @return a new OAuth2 token provider
	 */
	public OAuth2TokenProvider tokenProvider(final String clientRegistrationName, final OAuth2TokenClientSupplier tokenClientSupplier) {
		OAuth2ResolvedRegistration registration = get(clientRegistrationName);
		OAuth2TokenProviderSpec specification = OAuth2TokenProviderSpec.builder()
				.registration(registration)
				.tokenClientSupplier(tokenClientSupplier)
				.build();
		return OAuth2TokenProvider.of(specification);
	}

	/**
	 * Returns a list of new OAuth2 token providers based on this registry. The caller is responsible for handling the life
	 * cycle of the returned token providers.
	 *
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 * @return a list of OAuth2 token providers based on this registry
	 */
	public List<OAuth2TokenProvider> tokenProviders(final OAuth2TokenClientSupplier tokenClientSupplier) {
		return entries.keySet().stream().map(registrationName -> tokenProvider(registrationName, tokenClientSupplier)).toList();
	}
}
