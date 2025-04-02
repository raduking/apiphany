package org.apiphany.auth.oauth2.client;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apiphany.auth.oauth2.AuthorizationGrantType;
import org.apiphany.http.ContentType;
import org.apiphany.http.HttpHeader;
import org.apiphany.json.JsonBuilder;

/**
 * A representation of a client registration with an OAuth 2.0 or OpenID Connect 1.0 Provider.
 * <p>
 * See <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-2">Section 2 - Client Registration</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ClientRegistration {

	private String registrationId;

	private String clientId;

	private String clientSecret;

	private ClientAuthenticationMethod clientAuthenticationMethod;

	private AuthorizationGrantType authorizationGrantType;

	private String redirectUri;

	private Set<String> scopes = Collections.emptySet();

	private String clientName;

	private String provider;

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public String getRegistrationId() {
		return registrationId;
	}

	public void setRegistrationId(final String registrationId) {
		this.registrationId = registrationId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(final String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(final String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public ClientAuthenticationMethod getClientAuthenticationMethod() {
		return clientAuthenticationMethod;
	}

	public void setClientAuthenticationMethod(final ClientAuthenticationMethod clientAuthenticationMethod) {
		this.clientAuthenticationMethod = clientAuthenticationMethod;
	}

	public AuthorizationGrantType getAuthorizationGrantType() {
		return authorizationGrantType;
	}

	public void setAuthorizationGrantType(final AuthorizationGrantType authorizationGrantType) {
		this.authorizationGrantType = authorizationGrantType;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(final String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public Set<String> getScopes() {
		return scopes;
	}

	public void setScopes(final Set<String> scopes) {
		this.scopes = scopes;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(final String provider) {
		this.provider = provider;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(final String clientName) {
		this.clientName = clientName;
	}

	public Map<String, Object> getTokenRequestHeaders() {
		String credentials = String.join(":", getClientId(), getClientSecret());
		String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
		String authorizationHeaderValue = String.join(" ", getClientAuthenticationMethod().toString(), encodedCredentials);
		return Map.of(
				HttpHeader.CONTENT_TYPE.value(), ContentType.APPLICATION_FORM_URLENCODED.value(),
				HttpHeader.AUTHORIZATION.value(), authorizationHeaderValue);
	}

}
