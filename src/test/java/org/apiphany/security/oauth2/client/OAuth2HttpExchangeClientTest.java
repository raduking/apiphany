package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.HttpException;
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
import org.junit.jupiter.api.AfterEach;
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

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private ManagedApiClientWithOAuth2 managedApiClientWithOAuth2;

	private ClientProperties clientProperties;

	private OAuth2Properties oAuth2Properties;

	@BeforeEach
	void setUp() {
		String clientRegistrationJson = Strings.fromFile("/security/oauth2/oauth2-client-registration.json");
		clientRegistration = JsonBuilder.fromJson(clientRegistrationJson, OAuth2ClientRegistration.class);
		clientRegistration.setProvider(PROVIDER_NAME);
		clientRegistration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		providerDetails = JsonBuilder.fromJson(Strings.fromFile("/security/oauth2/oauth2-provider-details.json"), OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(OAUTH2_SERVER.getUrl() + "/token");

		oAuth2Properties = new OAuth2Properties();
		oAuth2Properties.setProvider(Map.of(PROVIDER_NAME, providerDetails));
		oAuth2Properties.setRegistration(Map.of(MY_SIMPLE_APP, clientRegistration));

		clientProperties = new ClientProperties();
		clientProperties.setCustomProperties(oAuth2Properties);

		managedApiClientWithOAuth2 = new ManagedApiClientWithOAuth2(clientProperties);
	}

	@AfterEach
	void tearDown() throws Exception {
		managedApiClientWithOAuth2.close();
	}

	@AfterAll
	static void cleanup() throws Exception {
		OAUTH2_SERVER.close();
		API_SERVER.close();
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithManagedApiClientWithOAuth2() {
		String result = managedApiClientWithOAuth2.getName();

		assertThat(result, equalTo(SimpleHttpServer.NAME));
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

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v1() throws Exception {
		try (OAuth2v1ApiClient client = new OAuth2v1ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(SimpleHttpServer.NAME));
		}
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v2() throws Exception {
		try (OAuth2v2ApiClient client = new OAuth2v2ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(SimpleHttpServer.NAME));
		}
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v3() throws Exception {
		try (OAuth2v3ApiClient client = new OAuth2v3ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(SimpleHttpServer.NAME));
		}
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v4() throws Exception {
		try (OAuth2v4ApiClient client = new OAuth2v4ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(SimpleHttpServer.NAME));
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
	void shouldReturnValidAuthenticationTokenWithApiClientManagedResourcesWithOAuth2v5() throws Exception {
		try (OAuth2v5ApiClient client = new OAuth2v5ApiClient(clientProperties)) {
			String result = client.getName();

			assertThat(result, equalTo(SimpleHttpServer.NAME));
		}
	}

	@SuppressWarnings("resource")
	@Test
	void shouldReturnValidAuthenticationTokenWithManagedApiClientManagedWithOAuth2() throws Exception {
		try (JavaNetHttpExchangeClient tokenClient = spy(new JavaNetHttpExchangeClient())) {
			try (OAuth2UnmanagedTokenClientApiClient client = new OAuth2UnmanagedTokenClientApiClient(clientProperties, tokenClient)) {
				String result = client.getName();

				assertThat(result, equalTo(SimpleHttpServer.NAME));
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
	 * This is just a base class for the various API clients used in the tests so that they all have the same
	 * implementation for the {@link #getName()} method.
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

		@Override
		public ExchangeClient getExchangeClient(final AuthenticationType authenticationType) {
			return super.getExchangeClient(authenticationType);
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
			super(exchangeClient(JavaNetHttpExchangeClient.class)
					.properties(properties)
					.secureWith()
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
					.secureWith()
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
					.secureWith()
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
					.decorateWith(OAuth2HttpExchangeClientBuilder.class));
		}
	}

	/**
	 * In this client the caller manages the resources and we manage the decorator.
	 */
	static class OAuth2v6ApiClient extends BaseApiClient {

		@SuppressWarnings("resource")
		protected OAuth2v6ApiClient(final ExchangeClient exchangeClient) {
			super(ScopedResource.managed(new OAuth2HttpExchangeClient(exchangeClient)));
		}
	}

	/**
	 * In this client the {@link ApiClient} manages the resources except for the token retrieve client.
	 */
	static class OAuth2UnmanagedTokenClientApiClient extends BaseApiClient {

		protected OAuth2UnmanagedTokenClientApiClient(final ClientProperties properties, final ExchangeClient tokenClient) {
			super(with(JavaNetHttpExchangeClient.class)
					.properties(properties)
					.secureWith()
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
