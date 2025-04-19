package org.apiphany.security.oauth2;

import java.util.Map;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.collections.Maps;
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
	 * Map of OAuth2 client registrations keyed by registration ID.
	 */
	private Map<String, OAuth2ClientRegistration> registration;

	/**
	 * Map of OAuth2 provider configurations keyed by provider name.
	 */
	private Map<String, OAuth2ProviderDetails> provider;

	/**
	 * Returns the string representation of this object as JSON.
	 *
	 * @return the string representation of this object as JSON
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
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
	 * Returns the client registration for the given name.
	 *
	 * @param name client registration name
	 * @return the client registration for the given name
	 */
	public OAuth2ClientRegistration getClientRegistration(final String name) {
		return Maps.isEmpty(registration) ? null : registration.get(name);
	}

	/**
	 * Returns the provider details for the given name.
	 *
	 * @param name provider name
	 * @return the provider details for the given name
	 */
	public OAuth2ProviderDetails getProviderDetails(final String name) {
		return Maps.isEmpty(provider) ? null : provider.get(name);
	}

	/**
	 * Returns the provider details for the given client registration.
	 *
	 * @param clientRegistration the client registration
	 * @return the provider details for the given client registration
	 */
	public OAuth2ProviderDetails getProviderDetails(final OAuth2ClientRegistration clientRegistration) {
		return getProviderDetails(clientRegistration.getProvider());
	}
}
