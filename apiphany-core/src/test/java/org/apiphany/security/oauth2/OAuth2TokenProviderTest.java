package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apiphany.lang.Strings;
import org.apiphany.logging.Slf4jLoggerAdapter;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.apiphany.security.token.client.TokenHttpExchangeClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.Holder;
import org.morphix.lang.function.ExecutionWrapper;
import org.morphix.lang.function.ExecutionWrappers;
import org.morphix.lang.function.LoggerAdapter.LoggingLevel;
import org.morphix.lang.resource.ScopedResource;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link OAuth2TokenProvider}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class OAuth2TokenProviderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2TokenProviderTest.class);

	private static final long EXPIRES_IN = 300;
	private static final long NEGATIVE_EXPIRES_IN = -10;
	private static final Instant DEFAULT_EXPIRATION = Instant.now();

	private static final String TOKEN = Strings.fromFile("security/oauth2/access-token.txt");

	private static final String CLIENT_REGISTRATION_NAME = "bubu";
	private static final String PROVIDER_NAME = "mumu";

	private static final AtomicInteger COUNTER = new AtomicInteger(0);

	@Mock
	private AuthenticationTokenProvider tokenClient;

	@Mock
	private OAuth2ClientRegistration clientRegistration;

	@Mock
	private OAuth2ProviderDetails providerDetails;

	@Mock
	private OAuth2Properties oAuth2Properties;

	private OAuth2TokenProvider tokenProvider;

	private String clientRegistrationName = unique(CLIENT_REGISTRATION_NAME);

	@AfterEach
	void tearDown() throws Exception {
		if (null != tokenProvider) {
			tokenProvider.close();
		}
	}

	@Test
	void shouldReturnTokenDefaultExpirationWhenTokenIsNull() {
		tokenProvider = OAuth2TokenProvider.builder()
				.defaultExpirationSupplier(() -> DEFAULT_EXPIRATION)
				.build();

		Instant expiration = tokenProvider.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION));
	}

	@Test
	void shouldReturnTokenDefaultExpirationWhenAuthenticationTokenExpirationIsNull() {
		tokenProvider = OAuth2TokenProvider.builder()
				.defaultExpirationSupplier(() -> DEFAULT_EXPIRATION)
				.build();

		tokenProvider.setAuthenticationToken(new AuthenticationToken());

		Instant expiration = tokenProvider.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION));
	}

	@Test
	void shouldReturnCorrectExpirationWhenTokenSet() {
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(TOKEN);
		authenticationToken.setExpiresIn(EXPIRES_IN);
		authenticationToken.setExpiration(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN));

		tokenProvider = OAuth2TokenProvider.builder()
				.defaultExpirationSupplier(() -> DEFAULT_EXPIRATION)
				.build();

		tokenProvider.setAuthenticationToken(authenticationToken);

		Instant expiration = tokenProvider.getTokenExpiration();

		assertThat(expiration, equalTo(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN)));
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationsMapIsNull() {
		tokenProvider = OAuth2TokenProvider.builder().build();

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfBuiltWithNullOAuth2Properties() {
		tokenProvider = OAuth2TokenProvider.builder()
				.registration((OAuth2Properties) null)
				.build();

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationsMapIsEmpty() {
		doReturn(Collections.emptyMap()).when(oAuth2Properties).getRegistration();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties)
				.build();

		assertTrue(tokenProvider.isSchedulerDisabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheProvidersMapIsNull() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties)
				.build();

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheProvidersMapIsEmpty() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(Collections.emptyMap()).when(oAuth2Properties).getProvider();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.build();

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationIsMissingTheRequiredRegistrationMapIsEmpty() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, "missing-registration")
				.build();

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationIsMissingTheClientId() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.build();

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationIsMissingTheClientSecret() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.build();

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfTheRegistrationIsMissingProviderDetails() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

		assertFalse(tokenProvider.isSchedulerEnabled());
	}

	@Test
	void shouldNotInitializeSchedulerIfThereAreMultipleRegistrationsAndClientRegistrationNameIsNotProvidedInConstructor() {
		OAuth2ClientRegistration clientRegistration2 = mock(OAuth2ClientRegistration.class);
		Map<String, OAuth2ClientRegistration> registration = Map.of(
				CLIENT_REGISTRATION_NAME, clientRegistration,
				"clientRegistrationName2", clientRegistration2);
		doReturn(registration).when(oAuth2Properties).getRegistration();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties)
				.build();

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

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

		assertTrue(tokenProvider.isSchedulerEnabled());
		assertFalse(tokenProvider.isSchedulerDisabled());
		assertThat(tokenProvider.getTokenClient(), equalTo(tokenClient));
		assertThat(tokenProvider.getClientRegistration(), equalTo(clientRegistration));
		assertThat(tokenProvider.getProviderDetails(), equalTo(providerDetails));
		assertThat(tokenProvider.getClientRegistrationName(), equalTo(CLIENT_REGISTRATION_NAME));
	}

	@Test
	@SuppressWarnings("resource")
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

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenRefreshScheduler(ScopedResource.managed(scheduledExecutorService))
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

		assertTrue(tokenProvider.isSchedulerEnabled());
		verify(expiration).minus(AuthenticationToken.EXPIRATION_ERROR_MARGIN);
		verify(scheduledExecutorService).schedule(any(Runnable.class),
				eq(OAuth2TokenProviderProperties.Default.MIN_REFRESH_INTERVAL.toMillis()), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	@SuppressWarnings("resource")
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

		tokenProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenRefreshScheduler(ScopedResource.managed(scheduledExecutorService))
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

		assertTrue(tokenProvider.isSchedulerEnabled());
		verify(expiration).minus(expirationErrorMargin);
		verify(scheduledExecutorService).schedule(any(Runnable.class), eq(minRefreshInterval.toMillis()), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldInitializeSchedulerAndUseBoundedExponentialBackoffForRefreshFailures() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		doThrow(new RuntimeException("boom")).when(tokenClient).getAuthenticationToken();

		Duration minRefreshInterval = Duration.ofMillis(100);
		Duration maxRefreshInterval = Duration.ofMillis(800);

		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		properties.setMinRefreshInterval(minRefreshInterval);
		properties.setMaxRefreshInterval(maxRefreshInterval);
		properties.setRefreshFailureDelayMultiplier(2.0d);

		List<Long> delays = Collections.synchronizedList(new ArrayList<>());
		AtomicInteger scheduleCalls = new AtomicInteger(0);

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
		ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
		doReturn(true).when(scheduledFuture).cancel(false);
		doAnswer(answer -> {
			Runnable runnable = answer.getArgument(0);
			long delayMillis = answer.getArgument(1);
			delays.add(delayMillis);
			if (scheduleCalls.incrementAndGet() <= 4) {
				runnable.run();
			}
			return scheduledFuture;
		}).when(scheduledExecutorService).schedule(any(Runnable.class), anyLong(), any());

		tokenProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenRefreshScheduler(ScopedResource.managed(scheduledExecutorService))
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

		assertThat(delays, equalTo(List.of(100L, 200L, 400L, 800L, 800L)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldInitializeSchedulerAndAwaitWithConfiguredTerminationTimeoutOnClose() throws Exception {
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
		doReturn(false).when(scheduledFuture).cancel(false);
		doReturn(scheduledFuture).when(scheduledExecutorService).schedule(any(Runnable.class), anyLong(), any());

		Duration schedulerTerminationTimeout = OAuth2TokenProviderProperties.Default.SCHEDULER_TERMINATION_TIMEOUT.plusMillis(66);

		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		properties.setSchedulerTerminationTimeout(schedulerTerminationTimeout);
		properties.setMaxTaskCloseAttempts(0);
		properties.setCloseTaskRetryInterval(Duration.ofMillis(10));

		tokenProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenRefreshScheduler(ScopedResource.managed(scheduledExecutorService))
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();
		tokenProvider.close();

		verify(scheduledExecutorService).shutdownNow();
		verify(scheduledExecutorService).awaitTermination(schedulerTerminationTimeout.toMillis(), TimeUnit.MILLISECONDS);
	}

	@Test
	void shouldThrowExceptionWhenGettingTokenIfTokenRetrievalThrowsException() {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		doThrow(new RuntimeException("BOOM! Error getting token")).when(tokenClient).getAuthenticationToken();

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

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

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

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

		tokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

		AuthenticationException e = assertThrows(AuthenticationException.class, tokenProvider::getAuthenticationToken);
		assertThat(e.getMessage(), equalTo("Missing authentication token"));
	}

	@Test
	@SuppressWarnings("resource")
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

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenRefreshScheduler(ScopedResource.managed(scheduledExecutorService))
				.tokenClientSupplier((cr, pd) -> localTokenClient)
				.build();

		AuthenticationToken token = null;
		try (localTokenProvider) {
			token = localTokenProvider.getAuthenticationToken();
		}

		assertThat(token, notNullValue());
		verify(scheduledFuture).cancel(false);
		verify(scheduledExecutorService).close();
		verify(localTokenClient).close();
	}

	@Test
	@SuppressWarnings("resource")
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

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties)
				.tokenRefreshScheduler(ScopedResource.managed(scheduledExecutorService))
				.tokenClientSupplier((cr, pd) -> localTokenClient)
				.build();

		try (localTokenProvider) {
			// empty
		}

		verify(scheduledFuture, times(maxAttempts)).cancel(false);
		verify(scheduledExecutorService).shutdownNow();
		verify(localTokenClient).close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotCloseTheScheduledFutureIfNotInitialized() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties)
				.tokenRefreshScheduler(ScopedResource.managed(scheduledExecutorService))
				.build();

		try (localTokenProvider) {
			// empty
		}

		verify(scheduledExecutorService).close();
		verifyNoMoreInteractions(scheduledExecutorService);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotCloseTheSchedulerIfNotManaged() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties)
				.tokenRefreshScheduler(scheduledExecutorService)
				.build();

		try (localTokenProvider) {
			// empty
		}

		verify(scheduledExecutorService, never()).close();
		verifyNoMoreInteractions(scheduledExecutorService);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowExceptionOnGettingTokenIfNotInitialized() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();

		ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.registration(oAuth2Properties)
				.tokenRefreshScheduler(scheduledExecutorService)
				.build();

		AuthenticationException e = null;
		try (localTokenProvider) {
			e = assertThrows(AuthenticationException.class, localTokenProvider::getAuthenticationToken);
		}

		assertNotNull(e);
		assertThat(e.getMessage(), equalTo("Missing authentication token"));
	}

	@Test
	@Timeout(5)
	void shouldInitializeSchedulerAndRetrieveMultipleTokensWithDefaultScheduler() throws Exception {
		String registrationName = getTaskName();
		doReturn(Map.of(registrationName, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(registrationName);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		int retrievals = 5;
		Duration tokenValidity = Duration.ofSeconds(1);

		CountDownLatch tokenRetrievalLatch = new CountDownLatch(retrievals);
		AtomicInteger tokenRetrievalCount = new AtomicInteger(0);
		doAnswer(answer -> {
			AuthenticationToken token = createToken(tokenValidity);
			int count = tokenRetrievalCount.incrementAndGet();
			LOGGER.info("Token retrieval count: {}", count);
			tokenRetrievalLatch.countDown();
			return token;
		}).when(tokenClient).getAuthenticationToken();

		Duration expirationErrorMargin = tokenValidity.minusMillis(10);
		Duration minRefreshInterval = Duration.ofMillis(5);

		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		properties.setExpirationErrorMargin(expirationErrorMargin);
		properties.setMinRefreshInterval(minRefreshInterval);
		properties.setMaxTaskCloseAttempts(1);

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties, registrationName)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

		boolean reachedRetrievals = false;
		try (localTokenProvider) {
			reachedRetrievals = Threads.safeWait(tokenRetrievalLatch, Duration.ofSeconds(3));
		}

		assertTrue(reachedRetrievals);
		assertThat(tokenRetrievalCount.get(), equalTo(retrievals));

		verify(tokenClient, times(retrievals)).getAuthenticationToken();
	}

	@Test
	@Timeout(5)
	@SuppressWarnings("resource")
	void shouldInitializeSchedulerAndRetrieveMultipleTokensWithCustomScheduler() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		int retrievals = 5;
		Duration tokenValidity = Duration.ofSeconds(1);

		AtomicInteger tokenRetrievalCount = new AtomicInteger(0);
		doAnswer(answer -> {
			AuthenticationToken token = createToken(tokenValidity);
			int count = tokenRetrievalCount.incrementAndGet();
			LOGGER.info("Token retrieval count: {}", count);
			return token;
		}).when(tokenClient).getAuthenticationToken();

		Duration expirationErrorMargin = tokenValidity.minusMillis(10);
		Duration minRefreshInterval = Duration.ofMillis(20);
		Duration pollingInterval = Duration.ofMillis(10);

		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		properties.setExpirationErrorMargin(expirationErrorMargin);
		properties.setMinRefreshInterval(minRefreshInterval);

		boolean reachedRetrievals = false;
		try (OAuth2TokenProvider localProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.tokenRefreshScheduler(ScopedResource.managed(Executors.newSingleThreadScheduledExecutor()))
				.build()) {
			reachedRetrievals = Threads.waitUntil(() -> tokenRetrievalCount.get() >= retrievals, Duration.ZERO, pollingInterval);
		}

		assertTrue(reachedRetrievals);
		verify(tokenClient, atLeast(retrievals)).getAuthenticationToken();
		// check that we didn't retrieve excessive tokens (e.g., due to scheduling issues)
		verify(tokenClient, atMost(retrievals + 2)).getAuthenticationToken();
	}

	@Test
	@Timeout(5)
	@SuppressWarnings("resource")
	void shouldInitializeSchedulerAndNotRetrieveTokensAfterDefaultSchedulerIsDisabled() throws Exception {
		doReturn(Map.of(clientRegistrationName, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(clientRegistrationName);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		Duration tokenValidity = Duration.ofSeconds(1);
		AtomicInteger tokenRetrievalCount = new AtomicInteger(0);

		Duration expirationErrorMargin = tokenValidity.minusMillis(10);
		Duration minRefreshInterval = Duration.ofMillis(10);

		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		properties.setExpirationErrorMargin(expirationErrorMargin);
		properties.setMinRefreshInterval(minRefreshInterval);

		Holder<OAuth2TokenProvider> tokenProviderHolder = Holder.empty();
		doAnswer(answer -> {
			AuthenticationToken token = createToken(tokenValidity);
			int count = tokenRetrievalCount.incrementAndGet();
			LOGGER.info("Token retrieval count: {}", count);
			if (count > 1) {
				tokenProviderHolder.getValue().disable();
			}
			return token;
		}).when(tokenClient).getAuthenticationToken();

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties, clientRegistrationName)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.build();

		tokenProviderHolder.setValue(localTokenProvider);

		boolean reachedRetrievals = false;
		try (localTokenProvider) {
			reachedRetrievals = Threads.waitUntil(() -> tokenRetrievalCount.get() >= 2);
		}

		assertTrue(reachedRetrievals);
		verify(tokenClient, times(2)).getAuthenticationToken();
	}

	@Test
	@Timeout(5)
	void shouldInitializeSchedulerAndRetrieveMultipleTokensWithDefaultSchedulerAndExecutionWrapper() throws Exception {
		doReturn(Map.of(CLIENT_REGISTRATION_NAME, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(CLIENT_REGISTRATION_NAME);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		int retrievals = 5;
		Duration tokenValidity = Duration.ofSeconds(1);

		CountDownLatch tokenRetrievalLatch = new CountDownLatch(retrievals);
		AtomicInteger tokenRetrievalCount = new AtomicInteger(0);
		doAnswer(answer -> {
			AuthenticationToken token = createToken(tokenValidity);
			int count = tokenRetrievalCount.incrementAndGet();
			LOGGER.info("Token retrieval count: {}", count);
			tokenRetrievalLatch.countDown();
			return token;
		}).when(tokenClient).getAuthenticationToken();

		Duration expirationErrorMargin = tokenValidity.minusMillis(10);
		Duration minRefreshInterval = Duration.ofMillis(10);

		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		properties.setExpirationErrorMargin(expirationErrorMargin);
		properties.setMinRefreshInterval(minRefreshInterval);

		AtomicInteger executionWrapperCount = new AtomicInteger(0);
		ExecutionWrapper<Void> count = s -> {
			LOGGER.info("Execution wrapper invocation count: {}", executionWrapperCount.incrementAndGet());
			return s;
		};
		ExecutionWrapper<Void> log = ExecutionWrappers.log(Slf4jLoggerAdapter.of(LOGGER), LoggingLevel.WARN, CLIENT_REGISTRATION_NAME + "-wrapper");

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties, CLIENT_REGISTRATION_NAME)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.updateTokenWrapper(name -> count.andThen(log))
				.build();

		boolean reachedRetrievals = false;
		try (localTokenProvider) {
			reachedRetrievals = Threads.safeWait(tokenRetrievalLatch, Duration.ofSeconds(3));
		}

		assertTrue(reachedRetrievals);
		assertThat(tokenRetrievalCount.get(), equalTo(retrievals));
		assertThat(executionWrapperCount.get(), equalTo(retrievals));

		verify(tokenClient, times(retrievals)).getAuthenticationToken();
	}

	@Test
	@Timeout(5)
	void shouldInitializeSchedulerAndRetrieveMultipleTokensWithDefaultSchedulerAndNamedExecutionWrapper() throws Exception {
		String registrationName = getTaskName();
		doReturn(Map.of(registrationName, clientRegistration)).when(oAuth2Properties).getRegistration();
		doReturn(clientRegistration).when(oAuth2Properties).getClientRegistration(registrationName);
		doReturn(true).when(clientRegistration).hasClientId();
		doReturn(true).when(clientRegistration).hasClientSecret();
		doReturn(Map.of(PROVIDER_NAME, providerDetails)).when(oAuth2Properties).getProvider();
		doReturn(providerDetails).when(oAuth2Properties).getProviderDetails(clientRegistration);

		int retrievals = 5;
		Duration tokenValidity = Duration.ofSeconds(1);

		CountDownLatch tokenRetrievalLatch = new CountDownLatch(retrievals);
		AtomicInteger tokenRetrievalCount = new AtomicInteger(0);
		doAnswer(answer -> {
			AuthenticationToken token = createToken(tokenValidity);
			int count = tokenRetrievalCount.incrementAndGet();
			LOGGER.info("Token retrieval count: {}", count);
			tokenRetrievalLatch.countDown();
			return token;
		}).when(tokenClient).getAuthenticationToken();

		Duration expirationErrorMargin = tokenValidity.minusMillis(10);
		Duration minRefreshInterval = Duration.ofMillis(10);

		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		properties.setExpirationErrorMargin(expirationErrorMargin);
		properties.setMinRefreshInterval(minRefreshInterval);
		properties.setMaxTaskCloseAttempts(1);

		List<String> namesAdded = new ArrayList<>();
		ExecutionWrapper<Void> log = ExecutionWrappers.log(Slf4jLoggerAdapter.of(LOGGER), LoggingLevel.WARN, registrationName + "-wrapper");

		OAuth2TokenProvider localTokenProvider = OAuth2TokenProvider.builder()
				.properties(properties)
				.registration(oAuth2Properties, registrationName)
				.tokenClientSupplier((cr, pd) -> tokenClient)
				.updateTokenWrapper(name -> getExecutionWrapper(name, namesAdded).andThen(log))
				.build();

		boolean reachedRetrievals = false;
		try (localTokenProvider) {
			reachedRetrievals = Threads.safeWait(tokenRetrievalLatch, Duration.ofSeconds(5));
		}

		assertTrue(reachedRetrievals);
		assertThat(tokenRetrievalCount.get(), equalTo(retrievals));
		assertThat(namesAdded.size(), equalTo(retrievals));
		for (String name : namesAdded) {
			assertThat(name, equalTo(registrationName));
		}

		verify(tokenClient, times(retrievals)).getAuthenticationToken();
	}

	private static AuthenticationToken createToken() {
		AuthenticationToken authenticationToken = createToken(Duration.ofSeconds(EXPIRES_IN));
		authenticationToken.setExpiration(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN));
		return authenticationToken;
	}

	private static AuthenticationToken createToken(final Duration expiresIn) {
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(TOKEN);
		authenticationToken.setExpiresIn(expiresIn.toSeconds());
		return authenticationToken;
	}

	private static String unique(final String prefix) {
		return prefix + String.format("%02d", COUNTER.incrementAndGet());
	}

	private static ExecutionWrapper<Void> getExecutionWrapper(final String name, final List<String> namesAdded) {
		return s -> {
			namesAdded.add(name);
			LOGGER.info("Execution wrapper '{}' invoked", name);
			return s;
		};
	}

	private static String getTaskName() {
		String name = Methods.getCallerMethodName((clsName, methodName) -> methodName).orElse("unknown");
		return "o2-" + name.replaceAll("[^A-Z]", "").toLowerCase();
	}
}
