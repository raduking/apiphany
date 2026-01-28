package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.HttpStatus;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ScopedResource;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.OAuth2TokenProvider;
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

	private static final String PROVIDER_NAME = "my-provider-name";
	private static final String MY_SIMPLE_APP = "my-simple-app";

	private static final String MAIN_EXCHANGE_CLIENT = "main-exchange-client";
	private static final String TOKEN_EXCHANGE_CLIENT = "token-exchange-client";

	private ClientProperties clientProperties;

	@BeforeEach
	void setUp() {
		String clientRegistrationJson = Strings.fromFile("security/oauth2/oauth2-client-registration.json");
		OAuth2ClientRegistration clientRegistration = JsonBuilder.fromJson(clientRegistrationJson, OAuth2ClientRegistration.class);
		clientRegistration.setProvider(PROVIDER_NAME);
		clientRegistration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		OAuth2ProviderDetails providerDetails =
				JsonBuilder.fromJson(Strings.fromFile("security/oauth2/oauth2-provider-details.json"), OAuth2ProviderDetails.class);

		OAuth2Properties oAuth2Properties = OAuth2Properties.of(Map.of(MY_SIMPLE_APP, clientRegistration), Map.of(PROVIDER_NAME, providerDetails));

		clientProperties = new ClientProperties();
		clientProperties.setCustomProperties(oAuth2Properties);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotCloseAnyUnmanagedClient() throws Exception {
		JavaNetHttpExchangeClient tokenExchangeClient = mock(JavaNetHttpExchangeClient.class);
		doReturn(AuthenticationType.NONE).when(tokenExchangeClient).getAuthenticationType();
		doReturn(TOKEN_EXCHANGE_CLIENT).when(tokenExchangeClient).getName();

		AuthenticationToken token = new AuthenticationToken();
		token.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(token).status(HttpStatus.OK).build();
		doReturn(apiResponse).when(tokenExchangeClient).exchange(any());

		JavaNetHttpExchangeClient exchangeClient = mock(JavaNetHttpExchangeClient.class);
		doReturn(clientProperties).when(exchangeClient).getClientProperties();
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();

		try (OAuth2HttpExchangeClient client = new OAuth2HttpExchangeClient(exchangeClient, tokenExchangeClient, MY_SIMPLE_APP)) {
			// empty
		}

		verify(exchangeClient, never()).close();
		verify(tokenExchangeClient, never()).close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldInitializeTokenRefreshScheduler() throws Exception {
		JavaNetHttpExchangeClient exchangeClient = mock(JavaNetHttpExchangeClient.class);
		doReturn(clientProperties).when(exchangeClient).getClientProperties();
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();

		AuthenticationToken token = new AuthenticationToken();
		token.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(token).status(HttpStatus.OK).build();
		doReturn(apiResponse).when(exchangeClient).exchange(any());

		try (OAuth2HttpExchangeClient oAuth2HttpExchangeClient =
				new OAuth2HttpExchangeClient(exchangeClient)) {
			OAuth2TokenProvider tokenProvider = JavaObjects.cast(oAuth2HttpExchangeClient.getTokenProvider());
			ScheduledExecutorService executorService = Fields.IgnoreAccess.get(tokenProvider, "tokenRefreshScheduler");

			assertThat(executorService, notNullValue());
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildExchangeClientWithDifferentExchangeAndTokenExchangeClientsAndCloseResources() throws Exception {
		JavaNetHttpExchangeClient tokenExchangeClient = mock(JavaNetHttpExchangeClient.class);
		doReturn(AuthenticationType.NONE).when(tokenExchangeClient).getAuthenticationType();
		doReturn(TOKEN_EXCHANGE_CLIENT).when(tokenExchangeClient).getName();

		AuthenticationToken token = new AuthenticationToken();
		token.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(token).status(HttpStatus.OK).build();
		doReturn(apiResponse).when(tokenExchangeClient).exchange(any());

		JavaNetHttpExchangeClient exchangeClient = mock(JavaNetHttpExchangeClient.class);
		doReturn(clientProperties).when(exchangeClient).getClientProperties();
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();

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
		JavaNetHttpExchangeClient exchangeClient = mock(JavaNetHttpExchangeClient.class);
		doReturn(clientProperties).when(exchangeClient).getClientProperties();
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();

		AuthenticationToken token = new AuthenticationToken();
		token.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(token).status(HttpStatus.OK).build();
		doReturn(apiResponse).when(exchangeClient).exchange(any());

		try (OAuth2HttpExchangeClient client = new OAuth2HttpExchangeClient(ScopedResource.managed(exchangeClient))) {
			assertThat(client.getTokenClient(), notNullValue());
		}

		verify(exchangeClient).close();
	}
}
