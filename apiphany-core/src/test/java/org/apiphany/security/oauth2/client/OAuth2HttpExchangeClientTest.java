package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.HttpStatus;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ScopedResource;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.OAuth2TokenProvider;
import org.apiphany.utils.security.JwtTokenValidator;
import org.apiphany.utils.security.oauth2.server.SimpleHttpServer;
import org.apiphany.utils.security.oauth2.server.SimpleOAuth2Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Fields;

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

	private ClientProperties clientProperties;

	@BeforeEach
	void setUp() {
		String clientRegistrationJson = Strings.fromFile("security/oauth2/oauth2-client-registration.json");
		OAuth2ClientRegistration clientRegistration = JsonBuilder.fromJson(clientRegistrationJson, OAuth2ClientRegistration.class);
		clientRegistration.setProvider(PROVIDER_NAME);
		clientRegistration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		OAuth2ProviderDetails providerDetails =
				JsonBuilder.fromJson(Strings.fromFile("security/oauth2/oauth2-provider-details.json"), OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(OAUTH2_SERVER.getUrl() + "/token");

		OAuth2Properties oAuth2Properties = OAuth2Properties.of(Map.of(MY_SIMPLE_APP, clientRegistration), Map.of(PROVIDER_NAME, providerDetails));

		clientProperties = new ClientProperties();
		clientProperties.setCustomProperties(oAuth2Properties);
	}

	@AfterAll
	static void cleanup() throws Exception {
		OAUTH2_SERVER.close();
		API_SERVER.close();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldNotCloseAnyUnmanagedClient() throws Exception {
		JavaNetHttpExchangeClient tokenExchangeClient = mock(JavaNetHttpExchangeClient.class);
		doReturn(AuthenticationType.NONE).when(tokenExchangeClient).getAuthenticationType();
		doReturn("token-client").when(tokenExchangeClient).getName();

		AuthenticationToken token = new AuthenticationToken();
		token.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(token).status(HttpStatus.OK).build();
		doReturn(apiResponse).when(tokenExchangeClient).exchange(any());

		JavaNetHttpExchangeClient exchangeClient = mock(JavaNetHttpExchangeClient.class);
		doReturn(clientProperties).when(exchangeClient).getClientProperties();
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();

		try (OAuth2HttpExchangeClient client = new OAuth2HttpExchangeClient(exchangeClient, tokenExchangeClient, MY_SIMPLE_APP)) {
			// empty
		}

		verify(exchangeClient, times(0)).close();
		verify(tokenExchangeClient, times(0)).close();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldInitializeTokenRefreshScheduler() throws Exception {
		try (OAuth2HttpExchangeClient oAuth2HttpExchangeClient =
				new OAuth2HttpExchangeClient(new JavaNetHttpExchangeClient(clientProperties))) {
			OAuth2TokenProvider tokenProvider = JavaObjects.cast(oAuth2HttpExchangeClient.getTokenProvider());
			ScheduledExecutorService executorService = Fields.IgnoreAccess.get(tokenProvider, "tokenRefreshScheduler");

			assertThat(executorService, notNullValue());
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildExchangeClientWithDifferentExchangeAndTokenExchangeClientsAndCloseResources() throws Exception {
		ExchangeClient exchangeClient = spy(new JavaNetHttpExchangeClient(clientProperties));
		ExchangeClient tokenExchangeClient = spy(new JavaNetHttpExchangeClient());

		try (OAuth2HttpExchangeClient oAuth2ExchangeClient = new OAuth2HttpExchangeClient(
				ScopedResource.managed(exchangeClient), ScopedResource.managed(tokenExchangeClient))) {
			assertThat(oAuth2ExchangeClient.getTokenClient(), notNullValue());
		}

		verify(tokenExchangeClient).close();
		verify(exchangeClient).close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildExchangeClientWithEqualExchangeAndTokenExchangeClientsAndCloseResourcesOnlyOnce() throws Exception {
		ExchangeClient exchangeClient = spy(new JavaNetHttpExchangeClient(clientProperties));

		try (OAuth2HttpExchangeClient client = new OAuth2HttpExchangeClient(ScopedResource.managed(exchangeClient))) {
			assertThat(client.getTokenClient(), notNullValue());
		}

		verify(exchangeClient).close();
	}
}
