package org.apiphany.security.token.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiRequest;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.HeaderValues;
import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.JwtTokenValidator.TokenValidationException;
import org.apiphany.security.token.TokenProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link TokenHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class TokenHttpExchangeClientTest {

	private static final int EXPIRES_IN = 300;

	private static final Instant DEFAULT_EXPIRATION = Instant.now();

	private static final String SECRET = "a-string-secret-at-least-256-bits-long";
	private static final String TOKEN = Strings.fromFile("/security/oauth2/access-token.txt");

	@Mock
	private ExchangeClient exchangeClient;

	private TokenHttpExchangeClient client;

	private ClientProperties clientProperties = new ClientProperties();

	@BeforeEach
	void setUp() {
		// empty
	}

	@AfterEach
	void tearDown() throws Exception {
		if (null != client) {
			client.close();
		}
	}

	@Test
	void shouldReturnTokenDefaultExpirationWhenTokenIsNull() {
		exchangeClientSetup(clientProperties);
		client = new TokenHttpExchangeClient(exchangeClient);
		client.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);

		Instant expiration = client.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION));
	}

	@Test
	void shouldReturnTokenDefaultExpirationWhenTokenExpirationIsNull() {
		exchangeClientSetup(clientProperties);
		client = new TokenHttpExchangeClient(exchangeClient);
		client.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);

		client.setAuthenticationToken(new AuthenticationToken());

		Instant expiration = client.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION));
	}

	@Test
	void shouldValidateTestTokenWithCorrectSecretAndReturnCorrectExpirationWhenTokenSet() throws TokenValidationException {
		JwtTokenValidator tokenValidator = new JwtTokenValidator(SECRET);
		tokenValidator.validateToken(TOKEN, false);

		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(TOKEN);
		authenticationToken.setExpiresIn(EXPIRES_IN);
		authenticationToken.setExpiration(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN));

		exchangeClientSetup(clientProperties);
		client = new TokenHttpExchangeClient(exchangeClient);
		client.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);

		client.setAuthenticationToken(authenticationToken);

		Instant expiration = client.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN)));
	}

	@Test
	void shouldHaveTheTokenAuthenticationType() {
		exchangeClientSetup(clientProperties);
		client = new TokenHttpExchangeClient(exchangeClient);
		client.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);

		AuthenticationType authenticationType = client.getAuthenticationType();

		assertThat(authenticationType, equalTo(AuthenticationType.TOKEN));
	}

	@Test
	void shouldAddTheAuthorizationHeaderToTheRequest() {
		exchangeClientSetup(clientProperties);
		client = new TokenHttpExchangeClient(exchangeClient);
		client.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);

		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(TOKEN);
		client.setAuthenticationToken(authenticationToken);
		client.setAuthenticationScheme(HttpAuthScheme.BEARER);

		ApiRequest<Object> apiRequest = new ApiRequest<>();

		client.exchange(apiRequest);

		Map<String, List<String>> headers = apiRequest.getHeaders();
		String authorizationHeader = MapHeaderValues.get(HttpHeader.AUTHORIZATION, headers).getFirst();

		assertThat(authorizationHeader, equalTo(HeaderValues.value(HttpAuthScheme.BEARER, TOKEN)));
	}

	@Test
	void shouldAddTheBearerAuthorizationHeaderToTheRequestWithTokenSpecifiedInClientProperties() {
		TokenProperties tokenProperties = new TokenProperties();
		tokenProperties.setValue(TOKEN);
		clientProperties.setCustomProperties(tokenProperties);

		exchangeClientSetup(clientProperties);
		client = new TokenHttpExchangeClient(exchangeClient);

		ApiRequest<Object> apiRequest = new ApiRequest<>();

		client.exchange(apiRequest);

		Map<String, List<String>> headers = apiRequest.getHeaders();
		String authorizationHeader = MapHeaderValues.get(HttpHeader.AUTHORIZATION, headers).getFirst();

		assertThat(authorizationHeader, equalTo(HeaderValues.value(HttpAuthScheme.BEARER, TOKEN)));
	}

	@Test
	void shouldAddTheAuthorizationHeaderToTheRequestWithAuthSchemeAndTokenSpecifiedInClientProperties() {
		TokenProperties tokenProperties = new TokenProperties();
		tokenProperties.setValue(TOKEN);
		tokenProperties.setAuthenticationScheme(HttpAuthScheme.BASIC.value());
		clientProperties.setCustomProperties(tokenProperties);

		exchangeClientSetup(clientProperties);
		client = new TokenHttpExchangeClient(exchangeClient);

		ApiRequest<Object> apiRequest = new ApiRequest<>();

		client.exchange(apiRequest);

		Map<String, List<String>> headers = apiRequest.getHeaders();
		String authorizationHeader = MapHeaderValues.get(HttpHeader.AUTHORIZATION, headers).getFirst();

		assertThat(authorizationHeader, equalTo(HeaderValues.value(HttpAuthScheme.BASIC, TOKEN)));
	}

	@Test
	void shouldThrowExceptionWhenRequestingTokenIfTokenWasNotInitialized() {
		exchangeClientSetup(null);

		client = new TokenHttpExchangeClient(exchangeClient);

		ApiRequest<Object> apiRequest = new ApiRequest<>();

		AuthenticationException e = assertThrows(AuthenticationException.class, () -> client.exchange(apiRequest));

		String expectedMessage = HttpException.exceptionMessage(HttpStatus.UNAUTHORIZED, "Missing authentication token");
		assertThat(e.getMessage(), equalTo(expectedMessage));
	}

	@Test
	void shouldThrowExceptionWhenRetrievingAuthenticationTokenIfTokenWasNotInitialized() {
		exchangeClientSetup(null);

		client = new TokenHttpExchangeClient(exchangeClient);

		AuthenticationException e = assertThrows(AuthenticationException.class, () -> client.getAuthenticationToken());

		String expectedMessage = HttpException.exceptionMessage(HttpStatus.UNAUTHORIZED, "Missing authentication token");
		assertThat(e.getMessage(), equalTo(expectedMessage));
	}

	@Test
	void shouldRequireNewAuthenticationTokenIfTokenWasNotInitialized() {
		exchangeClientSetup(null);

		client = new TokenHttpExchangeClient(exchangeClient);

		boolean newTokenNeeded = client.isNewTokenNeeded();

		assertTrue(newTokenNeeded);
	}

	@SuppressWarnings("resource")
	private void exchangeClientSetup(final ClientProperties clientProperties) {
		doReturn(clientProperties).when(exchangeClient).getClientProperties();
	}
}
