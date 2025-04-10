package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.client.JwtTokenValidator.TokenValidationException;
import org.apiphany.security.oauth2.server.NimbusOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.lang.thread.Threads;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Test class for {@link OAuth2ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ApiClientTest {

	private static final NimbusOAuth2Server OAUTH2_SERVER = new NimbusOAuth2Server();
	private static final JwtTokenValidator JWT_TOKEN_VALIDATOR = new JwtTokenValidator(OAUTH2_SERVER.getClientSecret(), OAUTH2_SERVER.getUrl());

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	@BeforeEach
	void setUp() {
		clientRegistration =
				JsonBuilder.fromJson(Strings.fromFile("/oauth2-client-registration.json", Threads.noConsumer()), OAuth2ClientRegistration.class);
		providerDetails = JsonBuilder.fromJson(Strings.fromFile("/oauth2-provider-details.json", Threads.noConsumer()), OAuth2ProviderDetails.class);
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithNimbusOAuth2Server() throws TokenValidationException {
		providerDetails.setTokenUri(OAUTH2_SERVER.getUrl() + "/token");

		OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, new HttpExchangeClient());

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		assertThat(token, notNullValue());

		JWTClaimsSet claims = JWT_TOKEN_VALIDATOR.validateToken(token.getAccessToken());

		assertThat(claims, notNullValue());
	}

}
