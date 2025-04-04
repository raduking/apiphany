package org.apiphany.auth.oauth2.client;

import static org.apiphany.RequestParameters.ParameterFunction.parameter;
import static org.apiphany.RequestParameters.ParameterFunction.when;

import java.time.Duration;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.RequestParameters;
import org.apiphany.auth.AuthenticationToken;
import org.apiphany.auth.oauth2.AuthenticationMethod;
import org.apiphany.auth.oauth2.OAuth2ProviderDetails;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.ContentType;
import org.apiphany.http.HttpHeader;

/**
 * Specialized {@link ApiClient} for OAuth2 authentication flows. Handles token acquisition and management for OAuth2
 * protected APIs.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ApiClient extends ApiClient {

	/**
	 * Default expiration duration for access tokens.
	 */
	private Duration expiresIn = OAuth2HttpExchangeClient.DEFAULT_EXPIRES_IN;

	/**
	 * Configuration for the OAuth2 client registration.
	 */
	private OAuth2ClientRegistration clientRegistration;

	/**
	 * Configuration details for the OAuth2 provider.
	 */
	private OAuth2ProviderDetails providerDetails;

	/**
	 * Constructs a new OAuth2 API client with the specified configurations.
	 *
	 * @param clientRegistration the OAuth2 client registration details
	 * @param providerDetails the OAuth2 provider configuration details
	 * @param exchangeClient the HTTP exchange client to use for requests
	 */
	public OAuth2ApiClient(final OAuth2ClientRegistration clientRegistration, final OAuth2ProviderDetails providerDetails,
			final ExchangeClient exchangeClient) {
		super(NO_BASE_URL, exchangeClient);
		this.clientRegistration = clientRegistration;
		this.providerDetails = providerDetails;
	}

	/**
	 * Retrieves a standard authentication token.
	 *
	 * @return the authentication token, or null if the request fails
	 */
	public AuthenticationToken getAuthenticationToken() {
		return getAuthenticationToken(providerDetails.getUserInfoEndpoint().getAuthenticationMethod());
	}

	/**
	 * Retrieves a standard authentication token.
	 *
	 * @param authenticationMethod token request authentication method
	 * @return the authentication token, or null if the request fails
	 */
	public AuthenticationToken getAuthenticationToken(final AuthenticationMethod authenticationMethod) {
		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Form.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Form.EXPIRES_IN, expiresIn.toSeconds()),
				when(AuthenticationMethod.FORM == authenticationMethod,
						parameter(OAuth2Form.CLIENT_ID, clientRegistration.getClientId()),
						parameter(OAuth2Form.CLIENT_SECRET, clientRegistration.getClientSecret())
				)
		);
		return client()
				.post()
				.url(providerDetails.getTokenUri())
				.body(RequestParameters.asString(RequestParameters.encode(params)))
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED)
				.headerWhen(AuthenticationMethod.HEADER == authenticationMethod, HttpHeader.AUTHORIZATION, clientRegistration.getAuthorizationHeaderValue())
				.retrieve(AuthenticationToken.class)
				.orNull();
	}

}
