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
		String lookupName = Nullables.nonNullOrDefault(clientRegistrationName, "unknown");
		if (null == properties) {
			LOGGER.error("[{}] No OAuth2 properties provided in: {}", lookupName, OAuth2Properties.ROOT);
			return null;
		}
		if (Maps.isEmpty(properties.getRegistration())) {
			LOGGER.warn("[{}] No OAuth2 client registrations provided in: {}.registration", lookupName, OAuth2Properties.ROOT);
			return null;
		}
		String name = clientRegistrationName;
		if (Strings.isEmpty(name)) {
			Set<String> clientRegistrationNames = properties.getRegistration().keySet();
			if (clientRegistrationNames.size() > 1) {
				LOGGER.warn("[{}] Multiple OAuth2 client registrations provided in: {}.registration and the client registration name "
						+ "was not given to the provider.", lookupName, OAuth2Properties.ROOT);
			} else {
				name = clientRegistrationNames.iterator().next();
			}
		}
		if (Strings.isEmpty(name)) {
			return null;
		}
		if (Maps.isEmpty(properties.getProvider())) {
			LOGGER.warn("[{}] No OAuth2 providers provided in: {}.provider", clientRegistrationName, OAuth2Properties.ROOT);
			return null;
		}

		OAuth2ClientRegistration registration = properties.getClientRegistration(name);
		if (null == registration) {
			LOGGER.warn("[{}] No OAuth2 client provided for client registration in {}.registration.{}",
					clientRegistrationName, OAuth2Properties.ROOT, clientRegistrationName);
			return null;
		}
		if (!registration.hasClientId()) {
			LOGGER.warn("[{}] No OAuth2 client-id provided in {}.registration.{}",
					clientRegistrationName, OAuth2Properties.ROOT, clientRegistrationName);
			return null;
		}
		if (!registration.hasClientSecret()) {
			LOGGER.warn("[{}] No OAuth2 client-secret provided in {}.registration.{}",
					clientRegistrationName, OAuth2Properties.ROOT, clientRegistrationName);
			return null;
		}
		OAuth2ProviderDetails providerDetails = properties.getProviderDetails(registration);
		if (null == providerDetails) {
			LOGGER.warn("[{}] No OAuth2 provider named '{}' for found in in {}.provider",
					clientRegistrationName, registration.getProvider(), OAuth2Properties.ROOT);
			return null;
		}
		return OAuth2ResolvedRegistration.of(name, registration, providerDetails);
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
