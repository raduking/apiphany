package org.apiphany.security.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apiphany.logging.Slf4jLoggerAdapter;
import org.morphix.lang.function.LoggerAdapter;

/**
 * OAuth2 registry which holds all the resolved registrations defined by {@link OAuth2Properties}.
 * <p>
 * This class also provides utility methods to create {@link OAuth2TokenProvider} instances based on the resolved
 * registrations. The caller is responsible for handling the life cycle of the returned token providers.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2Registry {

	/**
	 * The class logger.
	 */
	private static final LoggerAdapter LOGGER = Slf4jLoggerAdapter.of(OAuth2Registry.class);

	/**
	 * Resolved registrations entries map.
	 */
	private final Map<String, OAuth2ResolvedRegistration> registrations;

	/**
	 * Constructor with OAuth2 properties.
	 *
	 * @param properties the properties to build the registrations on
	 */
	private OAuth2Registry(final OAuth2Properties properties) {
		this.registrations = resolve(properties);
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
	private static Map<String, OAuth2ResolvedRegistration> resolve(final OAuth2Properties properties) {
		if (null == properties || null == properties.getRegistration()) {
			return Collections.emptyMap();
		}
		Map<String, OAuth2ResolvedRegistration> resolved = new HashMap<>();
		properties.getRegistration().forEach((name, registration) -> {
			OAuth2ProviderDetails provider = properties.getProviderDetails(registration);
			if (null != provider) {
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
				registrations.get(Objects.requireNonNull(clientRegistrationName, "clientRegistrationName must not be null"));
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
		return registrations.values();
	}
}
