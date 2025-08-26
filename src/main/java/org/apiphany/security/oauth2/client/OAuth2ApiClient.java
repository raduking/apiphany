package org.apiphany.security.oauth2.client;

import static org.apiphany.ParameterFunction.parameter;

import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.RequestParameters;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.ContentType;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.apiphany.security.oauth2.AuthorizationGrantType;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2Parameter;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.token.client.TokenHttpExchangeClient;

/**
 * Specialized {@link ApiClient} for OAuth2 authentication flows. Handles token acquisition and management for OAuth2
 * protected APIs.
 * <p>
 * TODO: implement full functionality.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ApiClient extends ApiClient implements AuthenticationTokenProvider {

	/**
	 * Configuration for the OAuth2 client registration.
	 */
	private final OAuth2ClientRegistration clientRegistration;

	/**
	 * Configuration details for the OAuth2 provider.
	 */
	private final OAuth2ProviderDetails providerDetails;

	/**
	 * Constructs a new OAuth2 API client with the specified configurations.
	 *
	 * @param clientRegistration the OAuth2 client registration details
	 * @param providerDetails the OAuth2 provider configuration details
	 * @param httpExchangeClient the HTTP exchange client to use for requests
	 */
	public OAuth2ApiClient(final OAuth2ClientRegistration clientRegistration, final OAuth2ProviderDetails providerDetails,
			final ExchangeClient httpExchangeClient) {
		super(httpExchangeClient);
		this.clientRegistration = clientRegistration;
		this.providerDetails = providerDetails;
	}

	/**
	 * Constructs a new OAuth2 API client with the specified configurations.
	 *
	 * @param clientRegistration the OAuth2 client registration details
	 * @param providerDetails the OAuth2 provider configuration details
	 * @param exchangeClientBuilder the HTTP exchange client to use for requests
	 */
	public OAuth2ApiClient(final OAuth2ClientRegistration clientRegistration, final OAuth2ProviderDetails providerDetails,
			final ExchangeClientBuilder exchangeClientBuilder) {
		super(exchangeClientBuilder);
		this.clientRegistration = clientRegistration;
		this.providerDetails = providerDetails;
	}

	/**
	 * Retrieves a standard authentication token.
	 *
	 * @return the authentication token, or null if the request fails
	 */
	@Override
	public AuthenticationToken getAuthenticationToken() {
		return getAuthenticationToken(clientRegistration.getClientAuthenticationMethod());
	}

	/**
	 * Retrieves a standard authentication token.
	 *
	 * @param method OAuth2 client authentication method
	 * @return the authentication token, or null if the request fails
	 */
	public AuthenticationToken getAuthenticationToken(final ClientAuthenticationMethod method) {
		AuthorizationGrantType grantType = clientRegistration.getAuthorizationGrantType();
		if (AuthorizationGrantType.CLIENT_CREDENTIALS != grantType) {
			throw new UnsupportedOperationException("Unsupported authorization grant type: " + grantType);
		}
		return switch (method) {
			case CLIENT_SECRET_BASIC -> getTokenWithClientSecretBasic();
			case CLIENT_SECRET_POST -> getTokenWithClientSecretPost();
			default -> throw new UnsupportedOperationException("Unsupported client authentication method: " + method);
		};
	}

	/**
	 * Returns the authentication token with {@link ClientAuthenticationMethod#CLIENT_SECRET_BASIC} method.
	 *
	 * @return the authentication token with `client_secret_basic` method
	 */
	private AuthenticationToken getTokenWithClientSecretBasic() {
		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Parameter.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Parameter.EXPIRES_IN, TokenHttpExchangeClient.DEFAULT_EXPIRES_IN.toSeconds()));
		return client()
				.http()
				.post()
				.url(providerDetails.getTokenUri())
				.body(RequestParameters.asString(RequestParameters.encode(params)))
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED)
				.header(HttpHeader.AUTHORIZATION, clientRegistration.getAuthorizationHeaderValue(HttpAuthScheme.BASIC))
				.retrieve(AuthenticationToken.class)
				.orRethrow(AuthenticationException::new);
	}

	/**
	 * Returns the authentication token with {@link ClientAuthenticationMethod#CLIENT_SECRET_POST} method.
	 *
	 * @return the authentication token with `client_secret_post` method
	 */
	private AuthenticationToken getTokenWithClientSecretPost() {
		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Parameter.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Parameter.EXPIRES_IN, TokenHttpExchangeClient.DEFAULT_EXPIRES_IN.toSeconds()),
				parameter(OAuth2Parameter.CLIENT_ID, clientRegistration.getClientId()),
				parameter(OAuth2Parameter.CLIENT_SECRET, clientRegistration.getClientSecret()));
		return client()
				.http()
				.post()
				.url(providerDetails.getTokenUri())
				.body(RequestParameters.asString(RequestParameters.encode(params)))
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED)
				.retrieve(AuthenticationToken.class)
				.orRethrow(AuthenticationException::new);
	}
}
