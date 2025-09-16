package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.header.HeaderValues;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpStatus;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.JwtTokenValidator.TokenValidationException;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.server.SimpleHttpServer;
import org.apiphany.security.oauth2.server.SimpleOAuth2Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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

	private static final SimpleHttpServer API_SERVER = new SimpleHttpServer(API_SERVER_PORT, JWT_TOKEN_VALIDATOR);

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private OAuth2ApiClient oAuth2ApiClient;

	private SimpleApiClient simpleApiClient = new SimpleApiClient();

	private ExchangeClient exchangeClient = new JavaNetHttpExchangeClient();

	@BeforeEach
	void setUp() {
		String clientRegistrationJson = Strings.fromFile("/security/oauth2/oauth2-client-registration.json");
		clientRegistration = JsonBuilder.fromJson(clientRegistrationJson, OAuth2ClientRegistration.class);

		String providerDetailsJson = Strings.fromFile("/security/oauth2/oauth2-provider-details.json");
		providerDetails = JsonBuilder.fromJson(providerDetailsJson, OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(OAUTH2_SERVER.getUrl() + "/token");
	}

	@AfterEach
	void tearDown() throws Exception {
		oAuth2ApiClient.close();
		simpleApiClient.close();
		exchangeClient.close();
	}

	@AfterAll
	static void cleanup() throws Exception {
		OAUTH2_SERVER.close();
		API_SERVER.close();
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithSimpleOAuth2Server() throws TokenValidationException {
		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient);

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		assertThat(token, notNullValue());

		JWTClaimsSet claims = JWT_TOKEN_VALIDATOR.validateToken(token.getAccessToken());

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

	@SuppressWarnings("resource")
	@Test
	void shouldThrowExceptionIfExchangeClientThrowsWhileRetrievingToken() {
		HttpExchangeClient exchangeClientMock = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClientMock).getAuthenticationType();
		HttpException exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error");
		doThrow(exception).when(exchangeClientMock).exchange(any());

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClientMock);

		AuthenticationException e = assertThrows(AuthenticationException.class,
				() -> oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_BASIC));

		assertThat(e.getCause(), equalTo(exception));
	}

	static class SimpleApiClient extends ApiClient {

		protected SimpleApiClient() {
			super(with(JavaNetHttpExchangeClient.class));
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
