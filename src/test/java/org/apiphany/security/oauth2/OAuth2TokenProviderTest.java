package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.JwtTokenValidator.TokenValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link OAuth2TokenProvider}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class OAuth2TokenProviderTest {

	private static final int EXPIRES_IN = 300;

	private static final Instant DEFAULT_EXPIRATION = Instant.now();

	private static final String SECRET = "a-string-secret-at-least-256-bits-long";
	private static final String TOKEN = Strings.fromFile("/security/oauth2/access-token.txt");

	private static final String CLIENT_REGISTRATION_NAME = "bubu";
	private static final String PROVIDER_NAME = "mumu";

	@Mock
	private AuthenticationTokenProvider tokenClient;

	@Mock
	private OAuth2ClientRegistration clientRegistration;

	@Mock
	private OAuth2ProviderDetails providerDetails;

	@Mock
	private OAuth2Properties oAuth2Properties;

	private OAuth2TokenProvider tokenProvider;

	@Test
	void shouldReturnTokenDefaultExpirationWhenTokenIsNull() {
		tokenProvider = new OAuth2TokenProvider(null, null, (cr, pd) -> null);
		tokenProvider.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);

		Instant expiration = tokenProvider.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION));
	}

	@Test
	void shouldReturnTokenDefaultExpirationWhenTokenExpirationIsNull() {
		tokenProvider = new OAuth2TokenProvider(null, null, (cr, pd) -> null);
		tokenProvider.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);

		tokenProvider.setAuthenticationToken(new AuthenticationToken());

		Instant expiration = tokenProvider.getTokenExpiration();

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

		tokenProvider = new OAuth2TokenProvider(null, null, (cr, pd) -> null);
		tokenProvider.setDefaultTokenExpirationSupplier(() -> DEFAULT_EXPIRATION);

		tokenProvider.setAuthenticationToken(authenticationToken);

		Instant expiration = tokenProvider.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN)));
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationsMapIsNull() {
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, null, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationsMapIsEmpty() {
		doReturn(Collections.emptyMap()).when(oAuth2Properties).getRegistration();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, null, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheProvidersMapIsNull() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, null, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheProvidersMapIsEmpty() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(Collections.emptyMap()).when(oAuth2Properties).getProvider();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationsIsMissingTheRequiredRegistrationMapIsEmpty() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, "missing-registration", (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationsIsMissingClientSecret() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationsIsMissingProviderDetails() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> tokenClient);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldInitializeSchedulerIfOAuth2PropertiesContainsRelevantData() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(TOKEN);
		authenticationToken.setExpiresIn(EXPIRES_IN);
		authenticationToken.setExpiration(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN));
		doReturn(authenticationToken).when(tokenClient).getAuthenticationToken();

		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> tokenClient);

		assertTrue(tokenProvider.isSchedulerEnabled());
	}
}
