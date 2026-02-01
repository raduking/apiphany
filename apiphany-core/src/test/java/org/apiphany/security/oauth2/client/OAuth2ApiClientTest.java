package org.apiphany.security.oauth2.client;

import static org.apiphany.ParameterFunction.parameter;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.security.interfaces.RSAPrivateKey;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.RequestParameters;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.header.Header;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ScopedResource;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.JwsAlgorithm;
import org.apiphany.security.keys.RSAKeys;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2Parameter;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

	@Test
	@SuppressWarnings("resource")
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

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	void shouldReturnAuthenticationWithClientSecretBasic() throws Exception {
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();
		doReturn(HttpMethod.POST).when(exchangeClient).post();

		AuthenticationToken expectedToken = new AuthenticationToken();
		expectedToken.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(expectedToken).status(HttpStatus.OK).build();

		ArgumentCaptor<ApiRequest<String>> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
		doReturn(apiResponse).when(exchangeClient).exchange(requestCaptor.capture());

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, ScopedResource.managed(exchangeClient));

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);

		assertThat(token, notNullValue());

		ApiRequest<String> capturedRequest = requestCaptor.getValue();

		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Parameter.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Parameter.EXPIRES_IN, OAuth2Parameter.Default.EXPIRES_IN.toSeconds()));

		String body = RequestParameters.asString(RequestParameters.encode(params));
		String expectedAuthHeader = clientRegistration.getAuthorizationHeaderValue(HttpAuthScheme.BASIC);
		var headers = capturedRequest.getHeaders();

		assertThat(capturedRequest.getBody(), equalTo(body));
		assertThat(headers.size(), equalTo(2));
		assertThat(Headers.get(HttpHeader.AUTHORIZATION, headers).getFirst(), equalTo(expectedAuthHeader));
		assertThat(Headers.get(HttpHeader.CONTENT_TYPE, headers).getFirst(), equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED));
		assertThat(capturedRequest.getUrl().toString(), equalTo(providerDetails.getTokenUri()));
		assertThat(capturedRequest.getMethod(), equalTo(HttpMethod.POST));
	}

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	void shouldReturnAuthenticationWithClientSecretPost() {
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();
		doReturn(HttpMethod.POST).when(exchangeClient).post();

		AuthenticationToken expectedToken = new AuthenticationToken();
		expectedToken.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(expectedToken).status(HttpStatus.OK).build();

		ArgumentCaptor<ApiRequest<String>> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
		doReturn(apiResponse).when(exchangeClient).exchange(requestCaptor.capture());

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient);

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		assertThat(token, notNullValue());

		ApiRequest<String> capturedRequest = requestCaptor.getValue();

		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Parameter.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Parameter.EXPIRES_IN, OAuth2Parameter.Default.EXPIRES_IN.toSeconds()),
				parameter(OAuth2Parameter.CLIENT_ID, clientRegistration.getClientId()),
				parameter(OAuth2Parameter.CLIENT_SECRET, clientRegistration.getClientSecret()));

		String body = RequestParameters.asString(RequestParameters.encode(params));
		var headers = capturedRequest.getHeaders();

		assertThat(capturedRequest.getBody(), equalTo(body));
		assertThat(headers.size(), equalTo(1));
		assertThat(Headers.get(HttpHeader.CONTENT_TYPE, headers).getFirst(), equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED));
		assertThat(capturedRequest.getUrl().toString(), equalTo(providerDetails.getTokenUri()));
		assertThat(capturedRequest.getMethod(), equalTo(HttpMethod.POST));
	}

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	void shouldReturnAuthenticationWithClientSecretJwt() {
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();
		doReturn(HttpMethod.POST).when(exchangeClient).post();

		AuthenticationToken expectedToken = new AuthenticationToken();
		expectedToken.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(expectedToken).status(HttpStatus.OK).build();

		ArgumentCaptor<ApiRequest<String>> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
		doReturn(apiResponse).when(exchangeClient).exchange(requestCaptor.capture());

		String clientRegistrationJsonString = Strings.fromFile("security/oauth2/oauth2-client-registration-jwt.json");
		clientRegistration = JsonBuilder.fromJson(clientRegistrationJsonString, OAuth2ClientRegistration.class);

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient);

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_JWT);

		assertThat(token, notNullValue());

		ApiRequest<String> capturedRequest = requestCaptor.getValue();

		Map<String, String> bodyParams = RequestParameters.from(capturedRequest.getBody());
		var headers = capturedRequest.getHeaders();

		assertThat(bodyParams.get(OAuth2Parameter.GRANT_TYPE.value()), equalTo(clientRegistration.getAuthorizationGrantType().value()));
		assertThat(bodyParams.get(OAuth2Parameter.CLIENT_ASSERTION_TYPE.value()), equalTo("urn:ietf:params:oauth:client-assertion-type:jwt-bearer"));
		assertThat(bodyParams.get(OAuth2Parameter.EXPIRES_IN.value()), equalTo(String.valueOf(OAuth2Parameter.Default.EXPIRES_IN.toSeconds())));
		assertThat(headers.size(), equalTo(1));
		assertThat(Headers.get(HttpHeader.CONTENT_TYPE, headers).getFirst(), equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED));
		assertThat(capturedRequest.getUrl().toString(), equalTo(providerDetails.getTokenUri()));
		assertThat(capturedRequest.getMethod(), equalTo(HttpMethod.POST));
	}

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	void shouldReturnAuthenticationWithClientSecretPK() {
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		doReturn(MAIN_EXCHANGE_CLIENT).when(exchangeClient).getName();
		doReturn(HttpMethod.POST).when(exchangeClient).post();

		AuthenticationToken expectedToken = new AuthenticationToken();
		expectedToken.setExpiresIn(300);
		ApiResponse<AuthenticationToken> apiResponse = ApiResponse.create(expectedToken).status(HttpStatus.OK).build();

		ArgumentCaptor<ApiRequest<String>> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
		doReturn(apiResponse).when(exchangeClient).exchange(requestCaptor.capture());

		String clientRegistrationJsonString = Strings.fromFile("security/oauth2/oauth2-client-registration-pk.json");
		clientRegistration = JsonBuilder.fromJson(clientRegistrationJsonString, OAuth2ClientRegistration.class);

		RSAPrivateKey privateKey = RSAKeys.loadPEMPrivateKey("security/oauth2/rsa_private.pem");
		JwsAlgorithm jwsAlgorithm = JwsAlgorithm.fromString("RS256");

		oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, privateKey, jwsAlgorithm, exchangeClient);

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.PRIVATE_KEY_JWT);

		assertThat(token, notNullValue());

		ApiRequest<String> capturedRequest = requestCaptor.getValue();

		Map<String, String> bodyParams = RequestParameters.from(capturedRequest.getBody());
		var headers = capturedRequest.getHeaders();

		assertThat(bodyParams.get(OAuth2Parameter.GRANT_TYPE.value()), equalTo(clientRegistration.getAuthorizationGrantType().value()));
		assertThat(bodyParams.get(OAuth2Parameter.CLIENT_ASSERTION_TYPE.value()), equalTo("urn:ietf:params:oauth:client-assertion-type:jwt-bearer"));
		assertThat(bodyParams.get(OAuth2Parameter.EXPIRES_IN.value()), equalTo(String.valueOf(OAuth2Parameter.Default.EXPIRES_IN.toSeconds())));
		assertThat(headers.size(), equalTo(1));
		assertThat(Headers.get(HttpHeader.CONTENT_TYPE, headers).getFirst(), equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED));
		assertThat(capturedRequest.getUrl().toString(), equalTo(providerDetails.getTokenUri()));
		assertThat(capturedRequest.getMethod(), equalTo(HttpMethod.POST));
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
