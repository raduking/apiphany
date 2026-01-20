package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.apiphany.security.token.client.TokenHttpExchangeClient;
import org.apiphany.utils.security.JwtTokenValidator;
import org.apiphany.utils.security.JwtTokenValidator.TokenValidationException;
import org.junit.jupiter.api.AfterEach;
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

	private static final long EXPIRES_IN = 300;
	private static final long NEGATIVE_EXPIRES_IN = -10;
	private static final Instant DEFAULT_EXPIRATION = Instant.now();

	private static final String SECRET = "a-string-secret-at-least-256-bits-long";
	private static final String TOKEN = Strings.fromFile("security/oauth2/access-token.txt");

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

	@AfterEach
	void tearDown() throws Exception {
		if (null != tokenProvider) {
			tokenProvider.close();
		}
	}

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
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationsMapIsEmpty() {
		doReturn(Collections.emptyMap()).when(oAuth2Properties).getRegistration();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, (cr, pd) -> null);

		assertTrue(tokenProvider.isSchedulerDisabled());
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
	void shouldNotInitializeSchedulerIfTheRegistrationIsMissingTheRequiredRegistrationMapIsEmpty() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, "missing-registration", (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationIsMissingTheClientId() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationIsMissingTheClientSecret() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationIsMissingProviderDetails() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> tokenClient);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfThereAreMultipleRegistrationsAndClientRegistrationNameIsNotProvidedInConstructor() {
		OAuth2ClientRegistration clientRegistration2 = mock(OAuth2ClientRegistration.class);
		Map<String, OAuth2ClientRegistration> registration = Map.of(
				CLIENT_REGISTRATION_NAME, clientRegistration,
				"clientRegistrationName2", clientRegistration2);
		doReturn(registration).when(oAuth2Properties).getRegistration();
		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, (cr, pd) -> null);

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldInitializeSchedulerIfOAuth2PropertiesContainsRelevantData() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		AuthenticationToken authenticationToken = createToken();
		doReturn(authenticationToken).when(tokenClient).getAuthenticationToken();

		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> tokenClient);

		assertTrue(tokenProvider.isSchedulerEnabled());
		assertFalse(tokenProvider.isSchedulerDisabled());
		assertThat(tokenProvider.getTokenClient(), equalTo(tokenClient));
		assertThat(tokenProvider.getClientRegistration(), equalTo(clientRegistration));
		assertThat(tokenProvider.getProviderDetails(), equalTo(providerDetails));
		assertThat(tokenProvider.getClientRegistrationName(), equalTo(CLIENT_REGISTRATION_NAME));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldInitializeSchedulerAndScheduleNewTokenRetrievalWithDefaultErrorMarginAndMinRefreshInterval() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		AuthenticationToken authenticationToken = mock(AuthenticationToken.class);
		Instant expiration = mock(Instant.class);
		doReturn(expiration).when(authenticationToken).getExpiration();
		doReturn(EXPIRES_IN).when(authenticationToken).getExpiresIn();
		doReturn(authenticationToken).when(tokenClient).getAuthenticationToken();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
		ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
		doReturn(true).when(scheduledFuture).cancel(false);
		doReturn(scheduledFuture).when(scheduledExecutorService).schedule(any(Runnable.class), anyLong(), any());

		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, scheduledExecutorService, (cr, pd) -> tokenClient);

		assertTrue(tokenProvider.isSchedulerEnabled());
		verify(expiration).minus(AuthenticationToken.EXPIRATION_ERROR_MARGIN);
		verify(scheduledExecutorService).schedule(any(Runnable.class),
				eq(OAuth2TokenProviderProperties.Default.MIN_REFRESH_INTERVAL.toMillis()), eq(TimeUnit.MILLISECONDS));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldInitializeSchedulerAndScheduleNewTokenRetrievalWithConfiguredErrorMarginAndMinRefreshInterval() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		AuthenticationToken authenticationToken = mock(AuthenticationToken.class);
		Instant expiration = mock(Instant.class);
		doReturn(expiration).when(authenticationToken).getExpiration();
		doReturn(EXPIRES_IN).when(authenticationToken).getExpiresIn();
		doReturn(authenticationToken).when(tokenClient).getAuthenticationToken();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
		ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
		doReturn(true).when(scheduledFuture).cancel(false);
		doReturn(scheduledFuture).when(scheduledExecutorService).schedule(any(Runnable.class), anyLong(), any());

		Duration expirationErrorMargin = AuthenticationToken.EXPIRATION_ERROR_MARGIN.plusSeconds(1);
		Duration minRefreshInterval = OAuth2TokenProviderProperties.Default.MIN_REFRESH_INTERVAL.plusMillis(66);

		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		properties.setExpirationErrorMargin(expirationErrorMargin);
		properties.setMinRefreshInterval(minRefreshInterval);

		tokenProvider = new OAuth2TokenProvider(properties, oAuth2Properties, CLIENT_REGISTRATION_NAME,
				scheduledExecutorService, (cr, pd) -> tokenClient);

		assertTrue(tokenProvider.isSchedulerEnabled());
		verify(expiration).minus(expirationErrorMargin);
		verify(scheduledExecutorService).schedule(any(Runnable.class), eq(minRefreshInterval.toMillis()), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	void shouldThrowExceptionWhenGettingTokenIfTokenRetrievalThrowsException() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		doThrow(new RuntimeException("Error getting token")).when(tokenClient).getAuthenticationToken();

		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> tokenClient);

		AuthenticationException e = assertThrows(AuthenticationException.class, tokenProvider::getAuthenticationToken);
		assertThat(e.getMessage(), equalTo("Missing authentication token"));
	}

	@Test
	void shouldThrowExceptionWhenGettingTokenIfTokenRetrievalReturnsNull() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		doReturn(null).when(tokenClient).getAuthenticationToken();

		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> tokenClient);

		AuthenticationException e = assertThrows(AuthenticationException.class, tokenProvider::getAuthenticationToken);
		assertThat(e.getMessage(), equalTo("Missing authentication token"));
	}

	@Test
	void shouldThrowExceptionWhenGettingTokenIfTokenRetrievalReturnsInvalidExpiration() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		AuthenticationToken invalidToken = new AuthenticationToken();
		invalidToken.setExpiresIn(NEGATIVE_EXPIRES_IN);
		doReturn(invalidToken).when(tokenClient).getAuthenticationToken();

		tokenProvider = new OAuth2TokenProvider(oAuth2Properties, CLIENT_REGISTRATION_NAME, (cr, pd) -> tokenClient);

		AuthenticationException e = assertThrows(AuthenticationException.class, tokenProvider::getAuthenticationToken);
		assertThat(e.getMessage(), equalTo("Missing authentication token"));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldGetTokenAndCloseTheTokenProviderResource() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		AuthenticationToken authenticationToken = createToken();
		TokenHttpExchangeClient localTokenClient = mock(TokenHttpExchangeClient.class);
		doReturn(authenticationToken).when(localTokenClient).getAuthenticationToken();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
		ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
		doReturn(true).when(scheduledFuture).cancel(false);
		doReturn(scheduledFuture).when(scheduledExecutorService).schedule(any(Runnable.class), anyLong(), any());

		AuthenticationToken token = null;
		try (OAuth2TokenProvider localTokenProvider =
				new OAuth2TokenProvider(oAuth2Properties, null, scheduledExecutorService, (cr, pd) -> localTokenClient)) {
			token = localTokenProvider.getAuthenticationToken();
		}

		assertThat(token, notNullValue());
		verify(scheduledFuture).cancel(false);
		verify(scheduledExecutorService).close();
		verify(localTokenClient).close();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCallShutdownOnSchedulerExecutorWhenScheduledTaskCannotBeClosed() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		AuthenticationToken authenticationToken = createToken();
		TokenHttpExchangeClient localTokenClient = mock(TokenHttpExchangeClient.class);
		doReturn(authenticationToken).when(localTokenClient).getAuthenticationToken();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
		ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
		doReturn(false).when(scheduledFuture).cancel(false);
		doReturn(scheduledFuture).when(scheduledExecutorService).schedule(any(Runnable.class), anyLong(), any());

		int maxAttempts = 2;
		OAuth2TokenProviderProperties properties = OAuth2TokenProviderProperties.defaults();
		properties.setMaxTaskCloseAttempts(maxAttempts);

		try (OAuth2TokenProvider localTokenProvider =
				new OAuth2TokenProvider(properties, oAuth2Properties, null, scheduledExecutorService, (cr, pd) -> localTokenClient)) {
			// empty
		}

		verify(scheduledFuture, times(maxAttempts)).cancel(false);
		verify(scheduledExecutorService).shutdownNow();
		verify(localTokenClient).close();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldNotCloseTheScheduledFutureIfNotInitialized() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);

		try (OAuth2TokenProvider localTokenProvider =
				new OAuth2TokenProvider(oAuth2Properties, null, scheduledExecutorService, (cr, pd) -> null)) {
			// empty
		}

		verify(scheduledExecutorService).close();
		verifyNoMoreInteractions(scheduledExecutorService);
	}

	@SuppressWarnings("resource")
	@Test
	void shouldThrowExceptionOnGettingTokenIfNotInitialized() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);

		AuthenticationException e = null;
		try (OAuth2TokenProvider localTokenProvider =
				new OAuth2TokenProvider(oAuth2Properties, null, scheduledExecutorService, (cr, pd) -> null)) {
			e = assertThrows(AuthenticationException.class, localTokenProvider::getAuthenticationToken);
		}

		assertThat(e.getMessage(), equalTo("Missing authentication token"));
	}

	private static AuthenticationToken createToken() {
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(TOKEN);
		authenticationToken.setExpiresIn(EXPIRES_IN);
		authenticationToken.setExpiration(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN));
		return authenticationToken;
	}
}
