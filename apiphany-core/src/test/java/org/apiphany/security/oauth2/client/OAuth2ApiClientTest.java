package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.header.Header;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpStatus;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link OAuth2ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class OAuth2ApiClientTest {

	private static final String MAIN_EXCHANGE_CLIENT = "main-exchange-client";
	private static final String HTTP_LOCALHOST = "http://localhost";
	private static final String NAME = "name";

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private OAuth2ApiClient oAuth2ApiClient;

	@Mock
	private JavaNetHttpExchangeClient exchangeClient;

	@BeforeEach
	void setUp() {
		String clientRegistrationJson = Strings.fromFile("security/oauth2/oauth2-client-registration.json");
		clientRegistration = JsonBuilder.fromJson(clientRegistrationJson, OAuth2ClientRegistration.class);

		String providerDetailsJson = Strings.fromFile("security/oauth2/oauth2-provider-details.json");
		providerDetails = JsonBuilder.fromJson(providerDetailsJson, OAuth2ProviderDetails.class);
	}

	@AfterEach
	void tearDown() throws Exception {
		oAuth2ApiClient.close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnAuthenticationToken() {
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();

		AuthenticationToken expectedToken = new AuthenticationToken();
		expectedToken.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(expectedToken).status(HttpStatus.OK).build();
		doReturn(apiResponse).when(exchangeClient).exchange(any());

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient);

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		assertThat(token, notNullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldAuthorizeRequestWithValidToken() throws Exception {
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();

		AuthenticationToken expectedToken = new AuthenticationToken();
		expectedToken.setExpiresIn(300);
		ApiResponse<AuthenticationToken> tokenApiResponse = ApiResponse.create(expectedToken).status(HttpStatus.OK).build();
		ApiResponse<String> nameApiResponse = ApiResponse.create(NAME).status(HttpStatus.OK).build();
		doReturn(tokenApiResponse)
				.doReturn(nameApiResponse)
				.when(exchangeClient).exchange(any());

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient);

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		try (SimpleApiClient simpleApiClient = new SimpleApiClient(exchangeClient)) {
			String result = simpleApiClient.getName(token);
			assertThat(result, notNullValue());
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldAuthorizeRequestWithValidTokenWithClientBuilderConstructor() throws Exception {
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();

		AuthenticationToken expectedToken = new AuthenticationToken();
		expectedToken.setExpiresIn(300);
		ApiResponse<AuthenticationToken> tokenApiResponse = ApiResponse.create(expectedToken).status(HttpStatus.OK).build();
		ApiResponse<String> nameApiResponse = ApiResponse.create(NAME).status(HttpStatus.OK).build();
		doReturn(tokenApiResponse)
				.doReturn(nameApiResponse)
				.when(exchangeClient).exchange(any());

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails,
				ExchangeClientBuilder.create().client(exchangeClient));

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		try (SimpleApiClient simpleApiClient = new SimpleApiClient(exchangeClient)) {
			String result = simpleApiClient.getName(token);
			assertThat(result, notNullValue());
		}
	}

	@SuppressWarnings("resource")
	@Test
	void shouldThrowExceptionIfExchangeClientThrowsWhileRetrievingToken() {
		HttpExchangeClient exchangeClientMock = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClientMock).getAuthenticationType();
		HttpException exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Expected server error");
		doThrow(exception).when(exchangeClientMock).exchange(any());

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClientMock);

		AuthenticationException e = assertThrows(AuthenticationException.class,
				() -> oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_BASIC));

		assertThat(e.getCause(), equalTo(exception));
	}

	static class SimpleApiClient extends ApiClient {

		protected SimpleApiClient(final ExchangeClient exchangeClient) {
			super(with(exchangeClient));
		}

		public String getName(final AuthenticationToken token) {
			return client()
					.http()
					.get()
					.url(HTTP_LOCALHOST)
					.path(API, "name")
					.header(HttpHeader.AUTHORIZATION, Header.value(HttpAuthScheme.BEARER, token.getAccessToken()))
					.retrieve(String.class)
					.orNull();
		}
	}
}
