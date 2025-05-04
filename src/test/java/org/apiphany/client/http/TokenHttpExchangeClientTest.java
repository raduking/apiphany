package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
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
import org.apiphany.http.HttpHeader;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.BearerTokenProperties;
import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.JwtTokenValidator.TokenValidationException;
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
	private static final String TOKEN = Strings.fromFile("/access_token.txt");

	@Mock
	private ExchangeClient exchangeClient;

	private TokenHttpExchangeClient client;


	@BeforeEach
	void setUp() {
		client = new TokenHttpExchangeClient(exchangeClient);
		client.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);
	}

	@Test
	void shouldReturnTokenDefaultExpirationWhenTokenIsNull() {
		Instant expiration = client.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION));
	}

	@Test
	void shouldReturnTokenDefaultExpirationWhenTokenExpirationIsNull() {
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

		client.setAuthenticationToken(authenticationToken);

		Instant expiration = client.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN)));
	}

	@Test
	void shouldHaveTheTokenAuthenticationType() {
		AuthenticationType authenticationType = client.getAuthenticationType();

		assertThat(authenticationType, equalTo(AuthenticationType.TOKEN));
	}

	@Test
	void shouldAddTheAuthorizationHeaderToTheRequest() {
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
	void shouldAddTheAuthorizationHeaderToTheRequestSpecifiedInClientProperties() {
		BearerTokenProperties bearerTokenProperties = new BearerTokenProperties();
		bearerTokenProperties.setToken(TOKEN);

		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setCustomProperties(bearerTokenProperties);

		doReturn(clientProperties).when(exchangeClient).getClientProperties();

		client = new TokenHttpExchangeClient(exchangeClient);

		ApiRequest<Object> apiRequest = new ApiRequest<>();

		client.exchange(apiRequest);

		Map<String, List<String>> headers = apiRequest.getHeaders();
		String authorizationHeader = MapHeaderValues.get(HttpHeader.AUTHORIZATION, headers).getFirst();

		assertThat(authorizationHeader, equalTo(HeaderValues.value(HttpAuthScheme.BEARER, TOKEN)));
	}
}
