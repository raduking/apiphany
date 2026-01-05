package org.apiphany.security.oauth2;

import org.apiphany.security.AuthenticationTokenProvider;

/**
 * Functional interface for supplying an {@link AuthenticationTokenProvider} which is the provider that retrieves the
 * token based on OAuth2 client registration and provider details (usually by performing token requests to the OAuth2
 * authorization server).
 * <p>
 * This client supplier is used to supply the client that performs token requests.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface OAuth2TokenClientSupplier {

	/**
	 * Returns an {@link AuthenticationTokenProvider} based on the given client registration and provider details.
	 *
	 * @param clientRegistration the OAuth2 client registration
	 * @param providerDetails the OAuth2 provider details
	 * @return an AuthenticationTokenProvider based on the given client registration and provider details
	 */
	AuthenticationTokenProvider get(OAuth2ClientRegistration clientRegistration, OAuth2ProviderDetails providerDetails);

}
