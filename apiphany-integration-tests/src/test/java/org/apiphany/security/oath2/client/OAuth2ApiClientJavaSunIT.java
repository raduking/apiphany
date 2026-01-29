package org.apiphany.security.oath2.client;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.header.Header;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpHeader;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.JwtTokenValidator.TokenValidationException;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.ITWithJavaSunOAuth2Server;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.client.OAuth2ApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Test class for {@link OAuth2ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ApiClientJavaSunIT extends ITWithJavaSunOAuth2Server {

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private OAuth2ApiClient oAuth2ApiClient;

	private final SimpleApiClient simpleApiClient = new SimpleApiClient();

	private final ExchangeClient exchangeClient = new JavaNetHttpExchangeClient();

	@BeforeEach
	@SuppressWarnings("resource")
	void setUp() {
		String clientRegistrationJson = Strings.fromFile("security/oauth2/oauth2-client-registration.json");
		clientRegistration = JsonBuilder.fromJson(clientRegistrationJson, OAuth2ClientRegistration.class);

		String providerDetailsJson = Strings.fromFile("security/oauth2/oauth2-provider-details.json");
		providerDetails = JsonBuilder.fromJson(providerDetailsJson, OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(oAuth2Server().getUrl() + "/token");
	}

	@AfterEach
	void tearDown() throws Exception {
		oAuth2ApiClient.close();
		simpleApiClient.close();
		exchangeClient.close();
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithSimpleOAuth2Server() throws TokenValidationException {
		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient);

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		assertThat(token, notNullValue());

		JWTClaimsSet claims = tokenValidator().validateToken(token.getAccessToken());

		assertThat(claims, notNullValue());
	}

	@Test
	void shouldAuthorizeRequestWithValidToken() {
		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient);

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		String result = simpleApiClient.getName(token);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldAuthorizeRequestWithValidTokenWithClientBuilderConstructor() {
		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails,
				ExchangeClientBuilder.create().client(JavaNetHttpExchangeClient.class));

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		String result = simpleApiClient.getName(token);

		assertThat(result, notNullValue());
	}

	static class SimpleApiClient extends ApiClient {

		protected SimpleApiClient() {
			super(with(JavaNetHttpExchangeClient.class));
		}

		@SuppressWarnings("resource")
		public String getName(final AuthenticationToken token) {
			return client()
					.http()
					.get()
					.url("http://localhost:" + apiServer().getPort())
					.path(API, "name")
					.header(HttpHeader.AUTHORIZATION, Header.value(HttpAuthScheme.BEARER, token.getAccessToken()))
					.retrieve(String.class)
					.orNull();
		}
	}
}
