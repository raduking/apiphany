package org.apiphany.security.oauth2;

import java.util.Set;

import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.morphix.lang.Nullables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing a complete OAuth2 client registration information. This is basically a wrapper over the client
 * registration and provider details objects together with the registration name.
 * <p>
 * Given the following properties:
 *
 * <pre>
 * oauth2:
 *   registration:
 *     my-client:
 *       client-id: my-client-id
 *       client-secret: my-client-secret
 *       provider: my-provider
 *   provider:
 *     my-provider:
 *       authorization-uri: https://example.com/oauth2/authorize
 *       token-uri: https://example.com/oauth2/token
 *       user-info-uri: https://example.com/oauth2/userinfo
 *       jwk-set-uri: https://example.com/oauth2/keys
 * </pre>
 *
 * an instance of this class can be built for the registration name {@code my-client} which will contain both the client
 * registration and provider details.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ResolvedRegistration {

	/**
	 * The class logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2ResolvedRegistration.class);

	/**
	 * The unknown registration name constant.
	 */
	public static final String UNKNOWN_REGISTRATION_NAME = "<unknown>";

	/**
	 * The client registration name.
	 */
	private final String clientRegistrationName;

	/**
	 * The configuration for the OAuth2 client registration.
	 */
	private final OAuth2ClientRegistration clientRegistration;

	/**
	 * The configuration details for the OAuth2 provider
	 */
	private final OAuth2ProviderDetails providerDetails;

	/**
	 * Constructor with all fields.
	 *
	 * @param clientRegistrationName the client registration name
	 * @param clientRegistration the configuration for the OAuth2 client registration
	 * @param providerDetails the configuration details for the OAuth2 provider
	 */
	private OAuth2ResolvedRegistration(
			final String clientRegistrationName,
			final OAuth2ClientRegistration clientRegistration,
			final OAuth2ProviderDetails providerDetails) {
		this.clientRegistrationName = clientRegistrationName;
		this.clientRegistration = clientRegistration;
		this.providerDetails = providerDetails;
	}

	/**
	 * Builds a new OAuth2 resolved registration with the given parameters.
	 *
	 * @param clientRegistrationName the client registration name
	 * @param clientRegistration the configuration for the OAuth2 client registration
	 * @param providerDetails the configuration details for the OAuth2 provider
	 * @return a new OAuth2 registry entry
	 */
	public static OAuth2ResolvedRegistration of(
			final String clientRegistrationName,
			final OAuth2ClientRegistration clientRegistration,
			final OAuth2ProviderDetails providerDetails) {
		return new OAuth2ResolvedRegistration(clientRegistrationName, clientRegistration, providerDetails);
	}

	/**
	 * Builds a new OAuth2 resolved registration from the given properties. If the entry cannot be extracted the result is
	 * {@code null}.
	 *
	 * @param properties the OAuth2 properties
	 * @param clientRegistrationName the client registration name
	 * @return a new OAuth2 registry entry, or null if validation fails
	 */
	public static OAuth2ResolvedRegistration of(final OAuth2Properties properties, final String clientRegistrationName) {
		String lookupName = Nullables.nonNullOrDefault(clientRegistrationName, UNKNOWN_REGISTRATION_NAME);
		if (null == properties) {
			LOGGER.error("[{}] No OAuth2 properties provided in: {}", lookupName, OAuth2Properties.ROOT);
			return null;
		}
		if (Maps.isEmpty(properties.getRegistration())) {
			LOGGER.warn("[{}] No OAuth2 client registrations provided in: {}.registration", lookupName, OAuth2Properties.ROOT);
			return null;
		}
		String resolvedName = clientRegistrationName;
		if (Strings.isEmpty(resolvedName)) {
			Set<String> clientRegistrationNames = properties.getRegistration().keySet();
			if (clientRegistrationNames.size() > 1) {
				LOGGER.warn("[{}] Multiple OAuth2 client registrations present in: {}.registration, but the client registration name"
						+ " was not specified (when building the OAuth2TokenProvider).", lookupName, OAuth2Properties.ROOT);
			} else {
				resolvedName = clientRegistrationNames.iterator().next();
			}
		}
		if (Strings.isEmpty(resolvedName)) {
			return null;
		}
		if (Maps.isEmpty(properties.getProvider())) {
			LOGGER.warn("[{}] No OAuth2 provider provided in: {}.provider for: {}.registration.{}",
					resolvedName, OAuth2Properties.ROOT, OAuth2Properties.ROOT, resolvedName);
			return null;
		}

		OAuth2ClientRegistration registration = properties.getClientRegistration(resolvedName);
		if (null == registration) {
			LOGGER.warn("[{}] No OAuth2 client provided for client registration in: {}.registration.{}",
					resolvedName, OAuth2Properties.ROOT, resolvedName);
			return null;
		}
		if (!registration.hasClientId()) {
			LOGGER.warn("[{}] No OAuth2 client-id provided in: {}.registration.{}",
					resolvedName, OAuth2Properties.ROOT, resolvedName);
			return null;
		}
		if (!registration.hasClientSecret()) {
			LOGGER.warn("[{}] No OAuth2 client-secret provided in: {}.registration.{}",
					resolvedName, OAuth2Properties.ROOT, resolvedName);
			return null;
		}
		OAuth2ProviderDetails providerDetails = properties.getProviderDetails(registration);
		if (null == providerDetails) {
			String provider = registration.getProvider();
			LOGGER.warn("[{}] No OAuth2 provider named '{}' for found in in: {}.provider for: {}.registration.{}",
					resolvedName, provider, OAuth2Properties.ROOT, OAuth2Properties.ROOT, resolvedName);
			return null;
		}
		return OAuth2ResolvedRegistration.of(resolvedName, registration, providerDetails);
	}

	/**
	 * Returns the client registration name.
	 *
	 * @return the clientRegistrationName
	 */
	public String getClientRegistrationName() {
		return clientRegistrationName;
	}

	/**
	 * Returns the client registration.
	 *
	 * @return the clientRegistration
	 */
	public OAuth2ClientRegistration getClientRegistration() {
		return clientRegistration;
	}

	/**
	 * Returns the provider details.
	 *
	 * @return the providerDetails
	 */
	public OAuth2ProviderDetails getProviderDetails() {
		return providerDetails;
	}
}
