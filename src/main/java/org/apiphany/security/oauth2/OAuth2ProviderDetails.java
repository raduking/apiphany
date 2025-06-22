package org.apiphany.security.oauth2;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.oauth2.client.OAuth2ClientRegistration;

/**
 * Contains configuration details for an OAuth 2.0 Provider. This includes endpoint URIs and other provider-specific
 * configurations.
 *
 * @see OAuth2ClientRegistration
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ProviderDetails {

	/**
	 * The authorization endpoint URI. This is where clients are redirected to initiate the authorization flow.
	 */
	private String authorizationUri;

	/**
	 * The token endpoint URI. Used by clients to exchange an authorization code for an access token.
	 */
	private String tokenUri;

	/**
	 * Configuration for the UserInfo endpoint. Contains details about how to access and authenticate with the UserInfo
	 * endpoint.
	 */
	private UserInfoEndpoint userInfoEndpoint = new UserInfoEndpoint();

	/**
	 * The JSON Web Key Set (JWKS) URI. Used to obtain the provider's public keys for token validation.
	 */
	private String jwkSetUri;

	/**
	 * The issuer URI. Used as the issuer identifier in tokens and discovery responses.
	 */
	private String issuerUri;

	/**
	 * Additional provider configuration metadata. Contains any extra provider-specific configuration parameters.
	 */
	private Map<String, Serializable> configurationMetadata = Collections.emptyMap();

	/**
	 * Default constructor.
	 */
	public OAuth2ProviderDetails() {
		// empty
	}

	/**
	 * Returns a JSON representation of this provider configuration.
	 *
	 * @return JSON string representation of this object
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the authorization endpoint URI.
	 *
	 * @return the authorization URI
	 */
	public String getAuthorizationUri() {
		return authorizationUri;
	}

	/**
	 * Sets the authorization endpoint URI.
	 *
	 * @param authorizationUri the authorization URI to set
	 */
	public void setAuthorizationUri(final String authorizationUri) {
		this.authorizationUri = authorizationUri;
	}

	/**
	 * Returns the token endpoint URI.
	 *
	 * @return the token URI
	 */
	public String getTokenUri() {
		return tokenUri;
	}

	/**
	 * Sets the token endpoint URI.
	 *
	 * @param tokenUri the token URI to set
	 */
	public void setTokenUri(final String tokenUri) {
		this.tokenUri = tokenUri;
	}

	/**
	 * Returns the JWK Set URI.
	 *
	 * @return the JWK Set URI
	 */
	public String getJwkSetUri() {
		return jwkSetUri;
	}

	/**
	 * Sets the JWK Set URI.
	 *
	 * @param jwkSetUri the JWK Set URI to set
	 */
	public void setJwkSetUri(final String jwkSetUri) {
		this.jwkSetUri = jwkSetUri;
	}

	/**
	 * Returns the issuer URI.
	 *
	 * @return the issuer URI
	 */
	public String getIssuerUri() {
		return issuerUri;
	}

	/**
	 * Sets the issuer URI.
	 *
	 * @param issuerUri the issuer URI to set
	 */
	public void setIssuerUri(final String issuerUri) {
		this.issuerUri = issuerUri;
	}

	/**
	 * Returns the UserInfo endpoint configuration.
	 *
	 * @return the UserInfo endpoint configuration
	 */
	public UserInfoEndpoint getUserInfoEndpoint() {
		return userInfoEndpoint;
	}

	/**
	 * Sets the UserInfo endpoint configuration.
	 *
	 * @param userInfoEndpoint the UserInfo endpoint configuration to set
	 */
	public void setUserInfoEndpoint(final UserInfoEndpoint userInfoEndpoint) {
		this.userInfoEndpoint = userInfoEndpoint;
	}

	/**
	 * Returns the additional configuration metadata.
	 *
	 * @return the configuration metadata map
	 */
	public Map<String, Serializable> getConfigurationMetadata() {
		return configurationMetadata;
	}

	/**
	 * Sets the additional configuration metadata.
	 *
	 * @param configurationMetadata the configuration metadata map to set
	 */
	public void setConfigurationMetadata(final Map<String, Serializable> configurationMetadata) {
		this.configurationMetadata = configurationMetadata;
	}

	/**
	 * Configuration for the OAuth2 UserInfo endpoint. Contains details needed to access user profile information.
	 */
	public static class UserInfoEndpoint {

		/**
		 * The UserInfo endpoint URI. Used to fetch the authenticated user's profile information.
		 */
		private String uri;

		/**
		 * The authentication method used when accessing the UserInfo endpoint. Defaults to HEADER (Bearer token
		 * authentication).
		 */
		private AuthenticationMethod authenticationMethod = AuthenticationMethod.HEADER;

		/**
		 * The name of the attribute to use as the user's name. Typically, "sub", "name", or "preferred_username".
		 */
		private String userNameAttributeName;

		/**
		 * Default constructor.
		 */
		public UserInfoEndpoint() {
			// empty
		}

		/**
		 * Returns the UserInfo endpoint URI.
		 *
		 * @return the UserInfo endpoint URI
		 */
		public String getUri() {
			return uri;
		}

		/**
		 * Sets the UserInfo endpoint URI.
		 *
		 * @param uri the UserInfo endpoint URI to set
		 */
		public void setUri(final String uri) {
			this.uri = uri;
		}

		/**
		 * Returns the authentication method for the UserInfo endpoint.
		 *
		 * @return the authentication method
		 */
		public AuthenticationMethod getAuthenticationMethod() {
			return authenticationMethod;
		}

		/**
		 * Sets the authentication method for the UserInfo endpoint.
		 *
		 * @param authenticationMethod the authentication method to set
		 */
		public void setAuthenticationMethod(final AuthenticationMethod authenticationMethod) {
			this.authenticationMethod = authenticationMethod;
		}

		/**
		 * Returns the user name attribute name.
		 *
		 * @return the name of the user name attribute
		 */
		public String getUserNameAttributeName() {
			return userNameAttributeName;
		}

		/**
		 * Sets the user name attribute name.
		 *
		 * @param userNameAttributeName the name of the user name attribute to set
		 */
		public void setUserNameAttributeName(final String userNameAttributeName) {
			this.userNameAttributeName = userNameAttributeName;
		}
	}
}
