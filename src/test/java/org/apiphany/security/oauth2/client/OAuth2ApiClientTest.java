package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import org.apiphany.ApiClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.header.HeaderValues;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpHeader;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.JwtTokenValidator.TokenValidationException;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.server.SimpleHttpServer;
import org.apiphany.security.oauth2.server.SimpleOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Test class for {@link OAuth2ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ApiClientTest {

	private static final String CLIENT_SECRET = "apiphany-client-secret-more-than-32-characters";
	private static final String CLIENT_ID = "apiphany-client";

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final int OAUTH_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
	private static final int API_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	private static final SimpleOAuth2Server OAUTH2_SERVER = new SimpleOAuth2Server(OAUTH_SERVER_PORT, CLIENT_ID, CLIENT_SECRET);
	private static final JwtTokenValidator JWT_TOKEN_VALIDATOR = new JwtTokenValidator(CLIENT_ID, CLIENT_SECRET, OAUTH2_SERVER.getUrl());

	@SuppressWarnings("unused")
	private static final SimpleHttpServer API_SERVER = new SimpleHttpServer(API_SERVER_PORT, JWT_TOKEN_VALIDATOR);

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private OAuth2ApiClient oAuth2ApiClient;

	private SimpleApiClient simpleApiClient = new SimpleApiClient();

	@BeforeEach
	void setUp() {
		clientRegistration = JsonBuilder.fromJson(Strings.fromFile("/oauth2-client-registration.json"), OAuth2ClientRegistration.class);
		providerDetails = JsonBuilder.fromJson(Strings.fromFile("/oauth2-provider-details.json"), OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(OAUTH2_SERVER.getUrl() + "/token");
		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, new JavaNetHttpExchangeClient());
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithSimpleOAuth2Server() throws TokenValidationException {
		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		assertThat(token, notNullValue());

		JWTClaimsSet claims = JWT_TOKEN_VALIDATOR.validateToken(token.getAccessToken());

		assertThat(claims, notNullValue());
	}

	@Test
	void shouldAuthorizeRequestWithValidToken() {
		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		String result = simpleApiClient.getName(token);

		assertThat(result, notNullValue());
	}

	static class SimpleApiClient extends ApiClient {

		protected SimpleApiClient() {
			super(new JavaNetHttpExchangeClient());
		}

		public String getName(final AuthenticationToken token) {
			return client()
					.http()
					.get()
					.url("http://localhost:" + API_SERVER_PORT)
					.path(API, "name")
					.header(HttpHeader.AUTHORIZATION, HeaderValues.value(HttpAuthScheme.BEARER, token.getAccessToken()))
					.retrieve(String.class)
					.orNull();
		}
	}
}
