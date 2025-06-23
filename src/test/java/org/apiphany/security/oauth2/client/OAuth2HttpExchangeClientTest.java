package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.HttpException;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.server.SimpleHttpServer;
import org.apiphany.security.oauth2.server.SimpleOAuth2Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2HttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2HttpExchangeClientTest {

	private static final String CLIENT_SECRET = "apiphany-client-secret-more-than-32-characters";
	private static final String CLIENT_ID = "apiphany-client";
	private static final String PROVIDER_NAME = "my-provider-name";
	private static final String MY_SIMPLE_APP = "my-simple-app";

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final int OAUTH_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
	private static final int API_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	private static final SimpleOAuth2Server OAUTH2_SERVER = new SimpleOAuth2Server(OAUTH_SERVER_PORT, CLIENT_ID, CLIENT_SECRET);
	private static final JwtTokenValidator JWT_TOKEN_VALIDATOR = new JwtTokenValidator(CLIENT_ID, CLIENT_SECRET, OAUTH2_SERVER.getUrl());

	private static final SimpleHttpServer API_SERVER = new SimpleHttpServer(API_SERVER_PORT, JWT_TOKEN_VALIDATOR);

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private SimpleApiClientWithOAuth2 simpleApiClientWithOAuth2;

	private ClientProperties clientProperties;

	private OAuth2Properties oAuth2Properties;

	@BeforeEach
	void setUp() {
		clientRegistration = JsonBuilder.fromJson(Strings.fromFile("/oauth2-client-registration.json"), OAuth2ClientRegistration.class);
		clientRegistration.setProvider(PROVIDER_NAME);
		clientRegistration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		providerDetails = JsonBuilder.fromJson(Strings.fromFile("/oauth2-provider-details.json"), OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(OAUTH2_SERVER.getUrl() + "/token");

		oAuth2Properties = new OAuth2Properties();
		oAuth2Properties.setProvider(Map.of(PROVIDER_NAME, providerDetails));
		oAuth2Properties.setRegistration(Map.of(MY_SIMPLE_APP, clientRegistration));

		clientProperties = new ClientProperties();
		clientProperties.setCustomProperties(oAuth2Properties);

		simpleApiClientWithOAuth2 = new SimpleApiClientWithOAuth2(clientProperties);
	}

	@AfterEach
	void tearDown() throws Exception {
		simpleApiClientWithOAuth2.close();
	}

	@AfterAll
	static void cleanup() throws Exception {
		OAUTH2_SERVER.close();
		API_SERVER.close();
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithSimpleOAuth2Server() {
		String result = simpleApiClientWithOAuth2.getName();

		assertThat(result, equalTo(SimpleHttpServer.NAME));
	}

	@Test
	void shouldInitializeTokenRefreshScheduler() throws Exception {
		try (OAuth2HttpExchangeClient oAuth2HttpExchangeClient =
				new OAuth2HttpExchangeClient(new JavaNetHttpExchangeClient(clientProperties))) {
			@SuppressWarnings("resource")
			ScheduledExecutorService executorService = oAuth2HttpExchangeClient.getTokenRefreshScheduler();

			assertThat(executorService, notNullValue());
		}
	}

	@Test
	void shouldNotGetTheValueWithoutAToken() throws Exception {
		try (SimpleApiClient simpleApiClient = new SimpleApiClient(clientProperties)) {
			String result =  simpleApiClient.getName();

			assertThat(result, nullValue());
		}
	}

	@Test
	void shouldNotGetTheValueWithoutATokenAndThrowExceptionIfBleedExceptionIsSetToTrue() throws Exception {
		try (SimpleApiClient simpleApiClient = new SimpleApiClient(clientProperties)) {
			simpleApiClient.setBleedExceptions(true);

			HttpException httpException = assertThrows(HttpException.class, simpleApiClient::getName);

			assertThat(httpException.getMessage(), equalTo("Missing Authorization header."));
		}
	}

	static class SimpleApiClientWithOAuth2 extends ApiClient {

		@SuppressWarnings("resource")
		protected SimpleApiClientWithOAuth2(final ClientProperties properties) {
			super(new OAuth2HttpExchangeClient(new JavaNetHttpExchangeClient(properties)));
		}

		public String getName() {
			return client()
					.http()
					.get()
					.url("http://localhost:" + API_SERVER_PORT)
					.path(API, "name")
					.retrieve(String.class)
					.orNull();
		}
	}

	static class SimpleApiClient extends ApiClient {

		@SuppressWarnings("resource")
		protected SimpleApiClient(final ClientProperties properties) {
			super(new JavaNetHttpExchangeClient(properties));
		}

		public String getName() {
			return client()
					.http()
					.get()
					.url("http://localhost:" + API_SERVER_PORT)
					.path(API, "name")
					.retrieve(String.class)
					.orNull();
		}
	}

}
