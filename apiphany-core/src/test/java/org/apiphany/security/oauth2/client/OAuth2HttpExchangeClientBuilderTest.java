package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Map;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.ScopedResource;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.oauth2.AuthorizationGrantType;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2HttpExchangeClientBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2HttpExchangeClientBuilderTest {

	private static final String REGISTRATION = "test-registration";
	private static final String PROVIDER = "test-provider";
	private static final String CLIENT = "test-client";

	private static final String TOKEN_URI = "http://localhost:1234/token";

	private static final int EXPIRES_IN = 300;
	private static final Instant DEFAULT_EXPIRATION = Instant.now();

	static class DummyExchangeClient implements ExchangeClient {

		private final ClientProperties properties;

		public DummyExchangeClient() {
			this(null);
		}

		public DummyExchangeClient(final ClientProperties properties) {
			this.properties = properties;
		}

		@Override
		@SuppressWarnings("unchecked")
		public ClientProperties getClientProperties() {
			return properties;
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
			return null;
		}

		@Override
		public void close() {
			// empty
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldAlwaysBuildManagedOAuth2HttpExchangeClientWithTheGivenClient() throws Exception {
		ScopedResource<ExchangeClient> oauth2Client = OAuth2HttpExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.build();
		oauth2Client.closeIfManaged();

		assertNotNull(oauth2Client);
		assertTrue(oauth2Client.isManaged());

		OAuth2HttpExchangeClient client = (OAuth2HttpExchangeClient) oauth2Client.unwrap();
		assertNull(client.getTokenProvider(), "Token provider should be null because registration is not defined");
		assertNull(client.getTokenClient(), "Token client should be null because registration is not defined");
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildOAuth2HttpExchangeClientWithTheGivenTokenClient() throws Exception {
		ScopedResource<ExchangeClient> tokenClient = new ScopedResource<>(new DummyExchangeClient());
		ScopedResource<ExchangeClient> oauth2Client = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.securedWith()
				.oAuth2(builder -> builder.tokenClient(tokenClient.unwrap()))
				.build();
		oauth2Client.closeIfManaged();

		assertNotNull(oauth2Client);
		assertTrue(oauth2Client.isManaged());

		OAuth2HttpExchangeClient client = (OAuth2HttpExchangeClient) oauth2Client.unwrap();

		assertThat(client.getTokenExchangeClient(), sameInstance(tokenClient.unwrap()));
		assertThat(client.getTokenExchangeClient(), not(sameInstance(client.getExchangeClient())));

		assertNull(client.getTokenProvider(), "Token provider should be null because registration is not defined");
		assertNull(client.getTokenClient(), "Token client should not be null because it was provided");
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildAndThrowExceptionOnGetAuthenticationTokenWithGivenTokenExchangeClientIfTheClientReturnsNull() throws Exception {
		ClientProperties clientProperties = new ClientProperties();
		OAuth2Properties oauth2Properties = new OAuth2Properties();
		oauth2Properties.setRegistration(Map.of(REGISTRATION, buildRegistration(CLIENT, PROVIDER)));
		oauth2Properties.setProvider(Map.of(PROVIDER, buildProvider(TOKEN_URI)));
		clientProperties.setCustomProperties(oauth2Properties);

		ExchangeClient tokenExchangeClient = mock(HttpExchangeClient.class);
		doReturn(clientProperties).when(tokenExchangeClient).getClientProperties();
		doReturn(AuthenticationType.NONE).when(tokenExchangeClient).getAuthenticationType();

		ScopedResource<ExchangeClient> oauth2Client = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.properties(clientProperties)
				.securedWith()
				.oAuth2(builder -> builder
						.tokenClient(tokenExchangeClient)
						.registrationName(REGISTRATION))
				.build();

		try {
			OAuth2HttpExchangeClient client = (OAuth2HttpExchangeClient) oauth2Client.unwrap();

			AuthenticationException e = assertThrows(AuthenticationException.class, client::getAuthenticationToken);

			assertThat(e.getMessage(), equalTo("Missing authentication token"));
		} finally {
			oauth2Client.closeIfManaged();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildAndRetrieveTokenWithGivenTokenExchangeClient() throws Exception {
		AuthenticationToken expectedToken = createToken("test-token");

		ClientProperties clientProperties = new ClientProperties();
		OAuth2Properties oauth2Properties = new OAuth2Properties();
		oauth2Properties.setRegistration(Map.of(REGISTRATION, buildRegistration(CLIENT, PROVIDER)));
		oauth2Properties.setProvider(Map.of(PROVIDER, buildProvider(TOKEN_URI)));
		clientProperties.setCustomProperties(oauth2Properties);

		ApiResponse<AuthenticationToken> tokenResponse = ApiResponse.create(expectedToken)
				.status(HttpStatus.OK)
				.build();

		ExchangeClient tokenExchangeClient = mock(HttpExchangeClient.class);
		doReturn(clientProperties).when(tokenExchangeClient).getClientProperties();
		doReturn(AuthenticationType.NONE).when(tokenExchangeClient).getAuthenticationType();
		doReturn(tokenResponse).when(tokenExchangeClient).exchange(any());

		ScopedResource<ExchangeClient> oauth2Client = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.properties(clientProperties)
				.securedWith()
				.oAuth2(builder -> builder
						.tokenClient(tokenExchangeClient)
						.registrationName(REGISTRATION))
				.build();

		try {
			OAuth2HttpExchangeClient client = (OAuth2HttpExchangeClient) oauth2Client.unwrap();
			AuthenticationToken token = client.getAuthenticationToken();

			assertThat(token, equalTo(expectedToken));
		} finally {
			oauth2Client.closeIfManaged();
		}
	}

	private static OAuth2ClientRegistration buildRegistration(final String client, final String provider) {
		OAuth2ClientRegistration registration = new OAuth2ClientRegistration();
		registration.setClientId(client + "Id");
		registration.setClientSecret(client + "Secret");
		registration.setProvider(provider);
		registration.setAuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
		registration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
		return registration;
	}

	private static OAuth2ProviderDetails buildProvider(final String tokenUri) {
		OAuth2ProviderDetails provider = new OAuth2ProviderDetails();
		provider.setTokenUri(TOKEN_URI + "/" + tokenUri);
		return provider;
	}

	private static AuthenticationToken createToken(final String token) {
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(token);
		authenticationToken.setExpiresIn(EXPIRES_IN);
		authenticationToken.setExpiration(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN));
		return authenticationToken;
	}
}
