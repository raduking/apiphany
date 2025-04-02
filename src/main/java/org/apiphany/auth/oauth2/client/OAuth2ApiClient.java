package org.apiphany.auth.oauth2.client;

import static org.apiphany.RequestParameters.ParameterFunction.parameter;

import java.time.Duration;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.RequestParameters;
import org.apiphany.auth.AuthenticationToken;
import org.apiphany.auth.oauth2.OAuth2ProviderDetails;
import org.apiphany.client.ExchangeClient;

public class OAuth2ApiClient extends ApiClient {

	private Duration expiresIn = OAuth2HttpExchangeClient.DEFAULT_EXPIRES_IN;

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	public OAuth2ApiClient(final OAuth2ClientRegistration clientRegistration, final OAuth2ProviderDetails providerDetails, final ExchangeClient exchangeClient) {
		super(NO_BASE_URL, exchangeClient);
		this.clientRegistration = clientRegistration;
		this.providerDetails = providerDetails;
	}

	public AuthenticationToken getAuthenticationToken() {
		Map<String, String> params = RequestParameters.of(
				parameter("grant_type", clientRegistration.getAuthorizationGrantType()),
				parameter("expires_in", expiresIn.toSeconds())
		);
		String body = RequestParameters.asString(RequestParameters.encode(params));

		return client()
				.post()
				.url(providerDetails.getTokenUri())
				.body(body)
				.headers(clientRegistration.getTokenRequestHeaders())
				.retrieve(AuthenticationToken.class)
				.orNull();
	}

}
