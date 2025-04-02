package org.apiphany.auth.oauth2;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apiphany.auth.oauth2.client.OAuth2ClientRegistration;
import org.apiphany.json.JsonBuilder;

/**
 * OAuth provider details.
 *
 * @see OAuth2ClientRegistration
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ProviderDetails {

	private String authorizationUri;

	private String tokenUri;

	private UserInfoEndpoint userInfoEndpoint = new UserInfoEndpoint();

	private String jwkSetUri;

	private String issuerUri;

	private Map<String, Serializable> configurationMetadata = Collections.emptyMap();

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public String getAuthorizationUri() {
		return authorizationUri;
	}

	public void setAuthorizationUri(final String authorizationUri) {
		this.authorizationUri = authorizationUri;
	}

	public String getTokenUri() {
		return tokenUri;
	}

	public void setTokenUri(final String tokenUri) {
		this.tokenUri = tokenUri;
	}

	public String getJwkSetUri() {
		return jwkSetUri;
	}

	public void setJwkSetUri(final String jwkSetUri) {
		this.jwkSetUri = jwkSetUri;
	}

	public String getIssuerUri() {
		return issuerUri;
	}

	public void setIssuerUri(final String issuerUri) {
		this.issuerUri = issuerUri;
	}

	public UserInfoEndpoint getUserInfoEndpoint() {
		return userInfoEndpoint;
	}

	public void setUserInfoEndpoint(final UserInfoEndpoint userInfoEndpoint) {
		this.userInfoEndpoint = userInfoEndpoint;
	}

	public Map<String, Serializable> getConfigurationMetadata() {
		return configurationMetadata;
	}

	public void setConfigurationMetadata(final Map<String, Serializable> configurationMetadata) {
		this.configurationMetadata = configurationMetadata;
	}

	public static class UserInfoEndpoint {

		private String uri;

		private AuthenticationMethod authenticationMethod = AuthenticationMethod.HEADER;

		private String userNameAttributeName;

		public String getUri() {
			return uri;
		}

		public void setUri(final String uri) {
			this.uri = uri;
		}

		public AuthenticationMethod getAuthenticationMethod() {
			return authenticationMethod;
		}

		public void setAuthenticationMethod(final AuthenticationMethod authenticationMethod) {
			this.authenticationMethod = authenticationMethod;
		}

		public String getUserNameAttributeName() {
			return userNameAttributeName;
		}

		public void setUserNameAttributeName(final String userNameAttributeName) {
			this.userNameAttributeName = userNameAttributeName;
		}
	}

}
