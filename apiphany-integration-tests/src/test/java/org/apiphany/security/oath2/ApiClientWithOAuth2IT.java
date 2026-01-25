package org.apiphany.security.oath2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.HttpException;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ScopedResource;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.client.OAuth2ApiClient;
import org.apiphany.security.oauth2.client.OAuth2HttpExchangeClient;
import org.apiphany.security.oauth2.client.OAuth2HttpExchangeClientBuilder;
import org.apiphany.security.oauth2.server.JavaSunHttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;

/**
 * Test class for {@link ApiClient} with {@link OAuth2HttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithOAuth2IT extends ITWithJavaSunOAuth2Server {

	private static final String PROVIDER_NAME = "my-provider-name";
	private static final String MY_SIMPLE_APP = "my-simple-app";

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private ClientProperties clientProperties;

	private OAuth2Properties oAuth2Properties;

	@BeforeEach
	@SuppressWarnings("resource")
	void setUp() {
		String clientRegistrationJson = Strings.fromFile("security/oauth2/oauth2-client-registration.json");
		clientRegistration = JsonBuilder.fromJson(clientRegistrationJson, OAuth2ClientRegistration.class);
		clientRegistration.setProvider(PROVIDER_NAME);
		clientRegistration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		providerDetails = JsonBuilder.fromJson(Strings.fromFile("security/oauth2/oauth2-provider-details.json"), OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(oAuth2Server().getUrl() + "/token");

		oAuth2Properties = OAuth2Properties.of(Map.of(MY_SIMPLE_APP, clientRegistration), Map.of(PROVIDER_NAME, providerDetails));

		clientProperties = new ClientProperties();
		clientProperties.setCustomProperties(oAuth2Properties);
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithManagedApiClientWithOAuth2() throws Exception {
		String result = null;
		FullyManagedApiClientWithOAuth2 client = new FullyManagedApiClientWithOAuth2(clientProperties);
		try (client) {
			result = client.getName();
		}

		assertThat(result, equalTo(JavaSunHttpServer.NAME));

		verify(client.exchangeClient).close();
		verify(client.tokenClient).close();
		verify(client.oAuth2ExchangeClient).close();
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithFullyManagedApiClientWithOAuth2AndCloseAllResources() throws Exception {
		String result = null;
		try (ManagedApiClientWithOAuth2 managedApiClientWithOAuth2 = new ManagedApiClientWithOAuth2(clientProperties)) {
			result = managedApiClientWithOAuth2.getName();
		}

		assertThat(result, equalTo(JavaSunHttpServer.NAME));
	}

	@Test
	void shouldNotGetTheValueWithoutATokenWithManagedClient() throws Exception {
		try (ManagedApiClient simpleApiClient = new ManagedApiClient(clientProperties)) {
			String result = simpleApiClient.getName();

			assertThat(result, nullValue());
		}
	}

	@Test
	void shouldNotGetTheValueWithoutATokenWithManagedClientAndThrowExceptionIfBleedExceptionIsSetToTrue() throws Exception {
		try (ManagedApiClient simpleApiClient = new ManagedApiClient(clientProperties)) {
			simpleApiClient.setBleedExceptions(true);

			HttpException httpException = assertThrows(HttpException.class, simpleApiClient::getName);

			assertThat(httpException.getMessage(), equalTo(HttpException.message(httpException.getStatus())
					+ " Missing Authorization header."));
		}
	}

	@Test
	void shouldNotGetTheValueWithoutATokenWithSimpleClient() throws Exception {
		try (SimpleApiClient simpleApiClient = new SimpleApiClient(clientProperties)) {
			String result = simpleApiClient.getName();

			assertThat(result, nullValue());
		}
	}

	@Test
	void shouldNotGetTheValueWithoutATokenWithSimpleClientAndThrowExceptionIfBleedExceptionIsSetToTrue() throws Exception {
		try (SimpleApiClient simpleApiClient = new SimpleApiClient(clientProperties)) {
			simpleApiClient.setBleedExceptions(true);

			HttpException httpException = assertThrows(HttpException.class, simpleApiClient::getName);

			assertThat(httpException.getMessage(), equalTo(HttpException.message(httpException.getStatus())
					+ " Missing Authorization header."));
		}
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v1() throws Exception {
		try (OAuth2v1ApiClient client = new OAuth2v1ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(JavaSunHttpServer.NAME));
		}
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v2() throws Exception {
		try (OAuth2v2ApiClient client = new OAuth2v2ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(JavaSunHttpServer.NAME));
		}
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v3() throws Exception {
		try (OAuth2v3ApiClient client = new OAuth2v3ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(JavaSunHttpServer.NAME));
		}
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v4() throws Exception {
		try (OAuth2v4ApiClient client = new OAuth2v4ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(JavaSunHttpServer.NAME));
		}
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCloseAllResourcesWithApiClientManagedResourcesWithOAuth2v4() throws Exception {
		JavaNetHttpExchangeClient exchangeClient = spy(new JavaNetHttpExchangeClient(clientProperties));
		try (OAuth2v4ApiClient client = new OAuth2v4ApiClient(exchangeClient)) {
			// empty
		}

		verify(exchangeClient).close();
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v5() throws Exception {
		try (OAuth2v5ApiClient client = new OAuth2v5ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(JavaSunHttpServer.NAME));
		}
	}

	@SuppressWarnings("resource")
	@Test
	void shouldNotAutoCloseAllResourcesWithApiClientManagedResourcesWithOAuth2v6() throws Exception {
		JavaNetHttpExchangeClient exchangeClient = spy(new JavaNetHttpExchangeClient(clientProperties));
		try (exchangeClient) {
			try (OAuth2v6ApiClient client = new OAuth2v6ApiClient(exchangeClient)) {
				// empty
			}
			verify(exchangeClient, times(0)).close();
		}
		verify(exchangeClient).close();
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v7() throws Exception {
		try (OAuth2v7ApiClient client = new OAuth2v7ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(JavaSunHttpServer.NAME));
		}
	}

	@SuppressWarnings("resource")
	@Test
	void shouldNotAutoCloseAllResourcesWithApiClientManagedResourcesWithOAuth2v8() throws Exception {
		JavaNetHttpExchangeClient exchangeClient = spy(new JavaNetHttpExchangeClient(clientProperties));
		try (exchangeClient) {
			try (OAuth2v8ApiClient client = new OAuth2v8ApiClient(exchangeClient)) {
				// empty
			}
			verify(exchangeClient, times(0)).close();
		}
		verify(exchangeClient).close();
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v9() throws Exception {
		IllegalStateException exception = null;
		try (OAuth2v9ApiClient client = new OAuth2v9ApiClient(clientProperties)) {
			// empty
		} catch (IllegalStateException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertThat(exception.getMessage(), equalTo("Client not secured with any mechanism"));
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesAndMultipleRegistrationsOAuth2v10() throws Exception {
		oAuth2Properties.setProvider(Map.of(PROVIDER_NAME, providerDetails, "another-provider", providerDetails));
		oAuth2Properties.setRegistration(Map.of(MY_SIMPLE_APP, clientRegistration, "another-app", clientRegistration));
		clientProperties.setCustomProperties(oAuth2Properties);

		try (OAuth2v10ApiClient client = new OAuth2v10ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(JavaSunHttpServer.NAME));
		}
	}

	@Test
	void shouldFailWithApiClientManagedResourcesAndMultipleRegistrationsWhenRegistrationNotProvidedOAuth2v11() throws Exception {
		oAuth2Properties.setProvider(Map.of(PROVIDER_NAME, providerDetails, "another-provider", providerDetails));
		oAuth2Properties.setRegistration(Map.of(MY_SIMPLE_APP, clientRegistration, "another-app", clientRegistration));
		clientProperties.setCustomProperties(oAuth2Properties);

		IllegalStateException exception = null;
		try (OAuth2v11ApiClient client = new OAuth2v11ApiClient(clientProperties)) {
			// empty
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertNotNull(exception);
		assertThat(exception.getMessage(), equalTo("No valid client registration found!"));
	}

	@Test
	void shouldFailToAuthorizeWhenClientWasSecuredWithMultipleOAuth2WithApiClientManagedResourcesWithOAuth2v12() throws Exception {
		HttpException exception = null;
		try (OAuth2v12ApiClient client = new OAuth2v12ApiClient(clientProperties)) {
			client.setBleedExceptions(true);
			client.getName();
		} catch (HttpException e) {
			exception = e;
		}

		assertNotNull(exception);
		// this is what our SimpleHttpServer returns when multiple Authorization headers are sent
		assertThat(exception.getMessage(), equalTo("[401 Unauthorized] Only one Authorization header value accepted, got 2."));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldReturnValidAuthenticationTokenWithManagedApiClientManagedWithOAuth2() throws Exception {
		try (JavaNetHttpExchangeClient tokenClient = spy(new JavaNetHttpExchangeClient())) {
			try (OAuth2UnmanagedTokenClientApiClient client = new OAuth2UnmanagedTokenClientApiClient(clientProperties, tokenClient)) {
				String result = client.getName();

				assertThat(result, equalTo(JavaSunHttpServer.NAME));
			}

			verify(tokenClient, times(0)).close();
		}
	}

	@Test
	void shouldNotReturnValidAuthenticationTokenWithApiClientManagedWithOAuth2v1IfClientIsDisabled() throws Exception {
		clientProperties.setEnabled(false);
		try (ManagedApiClientWithOAuth2 client = new ManagedApiClientWithOAuth2(clientProperties)) {
			String result = client.getName();

			@SuppressWarnings("resource")
			OAuth2HttpExchangeClient oAuth2HttpExchangeClient = JavaObjects.cast(client.getExchangeClient(AuthenticationType.OAUTH2));
			AuthenticationToken token = new AuthenticationToken();
			oAuth2HttpExchangeClient.setAuthenticationToken(token);
			AuthenticationToken resultToken = oAuth2HttpExchangeClient.getAuthenticationToken();

			assertThat(result, nullValue());
			assertSame(token, resultToken);
		}
	}

	/**
	 * This is just a base class for the various API clients used in the tests so that they all have the same implementation
	 * for the {@link #getName()} method.
	 */
	static class BaseApiClient extends ApiClient {

		protected BaseApiClient(final ExchangeClient exchangeClient) {
			super(exchangeClient);
		}

		protected BaseApiClient(final ExchangeClientBuilder exchangeClientBuilder) {
			super(exchangeClientBuilder);
		}

		protected BaseApiClient(final ScopedResource<ExchangeClient> exchangeClientResource) {
			super(exchangeClientResource);
		}

		@SuppressWarnings("resource")
		public String getName() {
			return client()
					.http()
					.get()
					.url("http://localhost:" + apiServer().getPort())
					.path(API, "name")
					.retrieve(String.class)
					.orNull();
		}
	}

	/**
	 * This class manages the client resources.
	 */
	static class ManagedApiClientWithOAuth2 extends BaseApiClient {

		@SuppressWarnings("resource")
		protected ManagedApiClientWithOAuth2(final ClientProperties properties) {
			super(new OAuth2HttpExchangeClient(new JavaNetHttpExchangeClient(properties)));
		}

		@Override
		public void close() throws Exception {
			super.close();
			getExchangeClient(AuthenticationType.OAUTH2).close();
		}
	}

	/**
	 * This class manages the client resources with separate exchange clients for API and token retrieval.
	 */
	static class FullyManagedApiClientWithOAuth2 extends BaseApiClient {

		AutoCloseable tokenClient;
		AutoCloseable oAuth2ExchangeClient;
		AutoCloseable exchangeClient;

		@SuppressWarnings("resource")
		protected FullyManagedApiClientWithOAuth2(final ClientProperties properties) {
			super(spy(new OAuth2HttpExchangeClient(spy(new JavaNetHttpExchangeClient(properties)), spy(new JavaNetHttpExchangeClient()))));
		}

		@Override
		public void close() throws Exception {
			super.close();
			exchangeClient = getExchangeClient(AuthenticationType.OAUTH2);
			if (exchangeClient instanceof OAuth2HttpExchangeClient oAuth2HttpExchangeClient) {
				oAuth2ExchangeClient = oAuth2HttpExchangeClient;
				AuthenticationTokenProvider tokenProvider = oAuth2HttpExchangeClient.getTokenClient();
				if (tokenProvider instanceof OAuth2ApiClient apiClient) {
					tokenClient = apiClient.getExchangeClient(AuthenticationType.NONE);
					tokenClient.close();
				}
				oAuth2HttpExchangeClient.getExchangeClient().close();
			}
			exchangeClient.close();
		}
	}

	/**
	 * This class manages the client resources.
	 */
	static class ManagedApiClient extends BaseApiClient {

		@SuppressWarnings("resource")
		protected ManagedApiClient(final ClientProperties properties) {
			super(new JavaNetHttpExchangeClient(properties));
		}

		@Override
		public void close() throws Exception {
			super.close();
			getExchangeClient(AuthenticationType.NONE).close();
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v1ApiClient extends BaseApiClient {

		protected OAuth2v1ApiClient(final ClientProperties properties) {
			super(withClient(JavaNetHttpExchangeClient.class)
					.properties(properties)
					.securedWith()
					.oAuth2());
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v2ApiClient extends BaseApiClient {

		protected OAuth2v2ApiClient(final ClientProperties properties) {
			super(with(JavaNetHttpExchangeClient.class)
					.properties(properties)
					.securedWith()
					.oAuth2());
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v3ApiClient extends BaseApiClient {

		protected OAuth2v3ApiClient(final ClientProperties properties) {
			super(with(JavaNetHttpExchangeClient.class)
					.properties(properties)
					.securedWith()
					.oAuth2(oauth2 -> oauth2.tokenClient(JavaNetHttpExchangeClient.class)));
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v4ApiClient extends BaseApiClient {

		@SuppressWarnings("resource")
		protected OAuth2v4ApiClient(final ClientProperties properties) {
			super(ScopedResource.managed(new OAuth2HttpExchangeClient(
					ScopedResource.managed(new JavaNetHttpExchangeClient(properties)))));
		}

		@SuppressWarnings("resource")
		protected OAuth2v4ApiClient(final ExchangeClient exchangeClient) {
			super(ScopedResource.managed(new OAuth2HttpExchangeClient(
					ScopedResource.managed(exchangeClient))));
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v5ApiClient extends BaseApiClient {

		protected OAuth2v5ApiClient(final ClientProperties properties) {
			super(with(JavaNetHttpExchangeClient.class)
					.properties(properties)
					.decoratedWithBuilder(OAuth2HttpExchangeClientBuilder.class));
		}
	}

	/**
	 * In this client the caller manages the resources, and we manage the decorator.
	 */
	static class OAuth2v6ApiClient extends BaseApiClient {

		@SuppressWarnings("resource")
		protected OAuth2v6ApiClient(final ExchangeClient exchangeClient) {
			super(ScopedResource.managed(new OAuth2HttpExchangeClient(exchangeClient)));
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v7ApiClient extends BaseApiClient {

		protected OAuth2v7ApiClient(final ClientProperties properties) {
			super(with(JavaNetHttpExchangeClient.class)
					.properties(properties)
					.decoratedWith(OAuth2HttpExchangeClient.class));
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v8ApiClient extends BaseApiClient {

		protected OAuth2v8ApiClient(final ExchangeClient exchangeClient) {
			super(with(exchangeClient)
					.decoratedWith(OAuth2HttpExchangeClient.class));
		}
	}

	/**
	 * In this client is invalid since no security mechanism is defined.
	 */
	static class OAuth2v9ApiClient extends BaseApiClient {

		protected OAuth2v9ApiClient(final ClientProperties properties) {
			super(with(properties)
					.securedWith());
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v10ApiClient extends BaseApiClient {

		protected OAuth2v10ApiClient(final ClientProperties properties) {
			super(with(properties)
					.securedWith()
					.oAuth2(oauth2 -> oauth2.registrationName(MY_SIMPLE_APP)));
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v11ApiClient extends BaseApiClient {

		protected OAuth2v11ApiClient(final ClientProperties properties) {
			super(with(properties)
					.securedWith()
					.oAuth2());
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class OAuth2v12ApiClient extends BaseApiClient {

		protected OAuth2v12ApiClient(final ClientProperties properties) {
			super(with(properties)
					.securedWith()
					.oAuth2()
					.securedWith()
					.oAuth2());
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources except for the token retrieve client.
	 */
	static class OAuth2UnmanagedTokenClientApiClient extends BaseApiClient {

		protected OAuth2UnmanagedTokenClientApiClient(final ClientProperties properties, final ExchangeClient tokenClient) {
			super(with(JavaNetHttpExchangeClient.class)
					.properties(properties)
					.securedWith()
					.oAuth2(oauth2 -> oauth2.tokenClient(tokenClient)));
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources, no need for {@link #close()}.
	 */
	static class SimpleApiClient extends BaseApiClient {

		protected SimpleApiClient(final ClientProperties properties) {
			super(ExchangeClient.builder()
					.client(JavaNetHttpExchangeClient.class)
					.properties(properties));
		}
	}

}
