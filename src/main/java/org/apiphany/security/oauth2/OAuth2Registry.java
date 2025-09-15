package org.apiphany.security.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apiphany.lang.collections.Maps;

/**
 * OAuth2 registry which holds all the registrations and providers defined by {@link OAuth2Properties}.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2Registry {

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
		Map<String, OAuth2ResolvedRegistration> resolved = new HashMap<>();
		Maps.safe(properties.getRegistration()).forEach((name, registration) -> {
			OAuth2ProviderDetails provider = properties.getProviderDetails(registration);
			if (provider != null) {
				resolved.put(name, OAuth2ResolvedRegistration.of(name, registration, provider));
			}
		});
		this.entries = Collections.unmodifiableMap(resolved);
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
	 * Returns a resolved registration based on the given client registration name.
	 *
	 * @param clientRegistrationName the client registration name
	 * @return a resolved registration based on the given client registration name
	 */
	public OAuth2ResolvedRegistration get(final String clientRegistrationName) {
		return entries.get(clientRegistrationName);
	}

	/**
	 * Returns the resolved registration entries.
	 *
	 * @return the resolved registration entries
	 */
	public Collection<OAuth2ResolvedRegistration> entries() {
		return entries.values();
	}
}
