package org.apiphany.security.oauth2;

import java.util.Map;

import org.apiphany.security.oauth2.client.OAuth2ClientRegistration;

/**
 * Configuration properties for OAuth2 authentication. Contains provider details and client registration information.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2Properties {

	/**
	 * The root property prefix for OAuth2 configuration.
	 */
	public static final String ROOT = "oauth2";

	/**
	 * Map of OAuth2 provider configurations keyed by provider name.
	 */
	private Map<String, OAuth2ProviderDetails> provider;

	/**
	 * Map of OAuth2 client registrations keyed by registration ID.
	 */
	private Map<String, OAuth2ClientRegistration> registration;

	/**
	 * Returns the map of OAuth2 provider configurations.
	 *
	 * @return map of provider configurations, keyed by provider name
	 */
	public Map<String, OAuth2ProviderDetails> getProvider() {
		return provider;
	}

	/**
	 * Sets the OAuth2 provider configurations.
	 *
	 * @param provider map of provider configurations, keyed by provider name
	 */
	public void setProvider(final Map<String, OAuth2ProviderDetails> provider) {
		this.provider = provider;
	}

	/**
	 * Returns the map of OAuth2 client registrations.
	 *
	 * @return map of client registrations, keyed by registration ID
	 */
	public Map<String, OAuth2ClientRegistration> getRegistration() {
		return registration;
	}

	/**
	 * Sets the OAuth2 client registrations.
	 *
	 * @param registration map of client registrations, keyed by registration ID
	 */
	public void setRegistration(final Map<String, OAuth2ClientRegistration> registration) {
		this.registration = registration;
	}
}
