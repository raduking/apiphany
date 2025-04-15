package org.apiphany.security.oauth2.client;

import java.util.Base64;
import java.util.Collections;
import java.util.Set;

import org.apiphany.header.HeaderValues;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.oauth2.AuthorizationGrantType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a client registration with an OAuth 2.0 or OpenID Connect 1.0 Provider. Contains all necessary
 * configuration for client authentication and authorization.
 *
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-2">RFC 6749 Section 2 - Client
 * Registration</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ClientRegistration {

	/**
	 * Unique identifier for this client registration.
	 */
	private String registrationId;

	/**
	 * The client identifier issued by the authorization server.
	 */
	private String clientId;

	/**
	 * The client secret issued by the authorization server.
	 */
	private String clientSecret;

	/**
	 * The authentication method used by the client.
	 */
	private ClientAuthenticationMethod clientAuthenticationMethod;

	/**
	 * The grant type used for authorization.
	 */
	private AuthorizationGrantType authorizationGrantType;

	/**
	 * The redirect URI where authorization responses are sent.
	 */
	private String redirectUri;

	/**
	 * The scope(s) requested by the client during authorization.
	 */
	private Set<String> scopes = Collections.emptySet();

	/**
	 * A descriptive name for the client.
	 */
	private String clientName;

	/**
	 * Reference to the associated provider configuration.
	 */
	private String provider;

	/**
	 * Returns a JSON representation of this client registration.
	 *
	 * @return JSON string representation of this object
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the unique registration identifier.
	 *
	 * @return the registration ID
	 */
	public String getRegistrationId() {
		return registrationId;
	}

	/**
	 * Sets the unique registration identifier.
	 *
	 * @param registrationId the registration ID to set
	 */
	public void setRegistrationId(final String registrationId) {
		this.registrationId = registrationId;
	}

	/**
	 * Returns the client identifier.
	 *
	 * @return the client ID
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * Sets the client identifier.
	 *
	 * @param clientId the client ID to set
	 */
	public void setClientId(final String clientId) {
		this.clientId = clientId;
	}

	/**
	 * Returns the client secret.
	 *
	 * @return the client secret
	 */
	public String getClientSecret() {
		return clientSecret;
	}

	/**
	 * Sets the client secret.
	 *
	 * @param clientSecret the client secret to set
	 */
	public void setClientSecret(final String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/**
	 * Returns the client authentication method.
	 *
	 * @return the authentication method
	 */
	public ClientAuthenticationMethod getClientAuthenticationMethod() {
		return clientAuthenticationMethod;
	}

	/**
	 * Sets the client authentication method.
	 *
	 * @param clientAuthenticationMethod the authentication method to set
	 */
	public void setClientAuthenticationMethod(final ClientAuthenticationMethod clientAuthenticationMethod) {
		this.clientAuthenticationMethod = clientAuthenticationMethod;
	}

	/**
	 * Returns the authorization grant type.
	 *
	 * @return the authorization grant type
	 */
	public AuthorizationGrantType getAuthorizationGrantType() {
		return authorizationGrantType;
	}

	/**
	 * Sets the authorization grant type.
	 *
	 * @param authorizationGrantType the grant type to set
	 */
	public void setAuthorizationGrantType(final AuthorizationGrantType authorizationGrantType) {
		this.authorizationGrantType = authorizationGrantType;
	}

	/**
	 * Returns the redirect URI.
	 *
	 * @return the redirect URI
	 */
	public String getRedirectUri() {
		return redirectUri;
	}

	/**
	 * Sets the redirect URI.
	 *
	 * @param redirectUri the redirect URI to set
	 */
	public void setRedirectUri(final String redirectUri) {
		this.redirectUri = redirectUri;
	}

	/**
	 * Returns the set of requested scopes.
	 *
	 * @return the set of scopes
	 */
	public Set<String> getScopes() {
		return scopes;
	}

	/**
	 * Sets the requested scopes.
	 *
	 * @param scopes the set of scopes to request
	 */
	public void setScopes(final Set<String> scopes) {
		this.scopes = scopes;
	}

	/**
	 * Returns the provider reference.
	 *
	 * @return the provider name or identifier
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * Sets the provider reference.
	 *
	 * @param provider the provider name or identifier to set
	 */
	public void setProvider(final String provider) {
		this.provider = provider;
	}

	/**
	 * Returns the client name.
	 *
	 * @return the descriptive client name
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * Sets the client name.
	 *
	 * @param clientName the descriptive client name to set
	 */
	public void setClientName(final String clientName) {
		this.clientName = clientName;
	}

	/**
	 * Returns {@code base64(client_id:client_secret)}
	 *
	 * @return the properly formatted encoded credentials
	 */
	@JsonIgnore
	public String getEncodedCredentials() {
		String credentials = String.join(":", getClientId(), getClientSecret());
		return Base64.getEncoder().encodeToString(credentials.getBytes());
	}

	/**
	 * Returns the {@code Authorization} header value for {@link ClientAuthenticationMethod#CLIENT_SECRET_BASIC}.
	 *
	 * @return the authorization header value for client secret basic
	 */
	@JsonIgnore
	public String getClientSecretBasicHeaderValue() {
		return HeaderValues.value(HttpAuthScheme.BASIC, getEncodedCredentials());
	}

	/**
	 * Checks if this client registration has a client secret configured.
	 *
	 * @return true if a client secret is present, false otherwise
	 */
	public boolean hasClientSecret() {
		return Strings.isNotEmpty(clientSecret);
	}
}
