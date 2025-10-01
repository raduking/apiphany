package org.apiphany.security.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;

import org.apiphany.lang.collections.Maps;
import org.apiphany.security.AuthenticationTokenProvider;
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
		OAuth2ResolvedRegistration registration = entries.get(clientRegistrationName);
		if (null == registration) {
			LOGGER.warn("[{}] No OAuth2 client provided for client registration in {}.registration.{}",
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
	 * Returns a new OAuth2 token provider based on the given parameters.
	 *
	 * @param clientRegistrationName the client registration name
	 * @param options the OAuth2 token provider options
	 * @param tokenRefreshScheduler the token refresh scheduler
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 * @return a new OAuth2 token provider
	 */
	public OAuth2TokenProvider tokenProvider(
			final String clientRegistrationName,
			final OAuth2TokenProviderOptions options,
			final ScheduledExecutorService tokenRefreshScheduler,
			final BiFunction<OAuth2ClientRegistration, OAuth2ProviderDetails, AuthenticationTokenProvider> tokenClientSupplier) {
		OAuth2ResolvedRegistration registration = get(clientRegistrationName);
		return new OAuth2TokenProvider(options, registration, tokenRefreshScheduler, tokenClientSupplier);
	}

	/**
	 * Returns a new OAuth2 token provider based on the given parameters.
	 *
	 * @param clientRegistrationName the client registration name
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 * @return a new OAuth2 token provider
	 */
	@SuppressWarnings("resource")
	public OAuth2TokenProvider tokenProvider(
			final String clientRegistrationName,
			final BiFunction<OAuth2ClientRegistration, OAuth2ProviderDetails, AuthenticationTokenProvider> tokenClientSupplier) {
		OAuth2TokenProviderOptions options = OAuth2TokenProviderOptions.defaults();
		ScheduledExecutorService tokenRefreshScheduler = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
		return tokenProvider(clientRegistrationName, options, tokenRefreshScheduler, tokenClientSupplier);
	}

	/**
	 * Returns a list of OAuth2 token providers based on this registry.
	 *
	 * @param tokenClientSupplier the supplier for the client that will make the actual token requests
	 * @return a list of OAuth2 token providers based on this registry
	 */
	public List<OAuth2TokenProvider> tokenProviders(
			final BiFunction<OAuth2ClientRegistration, OAuth2ProviderDetails, AuthenticationTokenProvider> tokenClientSupplier) {
		return entries.keySet().stream().map(key -> tokenProvider(key, tokenClientSupplier)).toList();
	}
}
