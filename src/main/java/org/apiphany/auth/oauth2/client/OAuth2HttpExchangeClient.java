package org.apiphany.auth.oauth2.client;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.auth.AuthenticationToken;
import org.apiphany.auth.oauth2.OAuth2Properties;
import org.apiphany.auth.oauth2.OAuth2ProviderDetails;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.AbstractHttpExchangeClient;
import org.apiphany.header.Headers;
import org.apiphany.http.AuthorizationHeaderValues;
import org.apiphany.http.HttpHeader;
import org.morphix.lang.JavaObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2HttpExchangeClient extends AbstractHttpExchangeClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2HttpExchangeClient.class);

	/**
	 * Default value for token expiration (request) - 30 minutes.
	 */
	public static final Duration DEFAULT_EXPIRES_IN = Duration.ofMinutes(30);

	/**
	 * Duration for token refresh on token retrieve error - 500 milliseconds.
	 */
	protected static final Duration ERROR_TOKEN_REFRESH_DURATION = Duration.ofMillis(500);

	/**
	 * Duration for error margin when checking token expiration - 1 second.
	 */
	protected static final Duration TOKEN_EXPIRATION_ERROR_MARGIN = Duration.ofMillis(1000);

	/**
	 * The actual exchange client doing the request.
	 */
	private final ExchangeClient exchangeClient;

	/**
	 * The exchange client doing the token refresh.
	 */
	private final ExchangeClient tokenExchangeClient;

	/**
	 * Underlying API client that retrieves the token.
	 */
	private OAuth2ApiClient tokenApiClient;

	/**
	 * The authentication token.
	 */
	private AuthenticationToken authenticationToken;

	/**
	 * Token retrieval scheduler.
	 */
	private final ScheduledExecutorService tokenRefreshScheduler;

	/**
	 * Scheduler enabled flag.
	 */
	private boolean schedulerEnabled;

	/**
	 * Client registration name.
	 */
	private String clientRegistrationName;

	/**
	 * All OAuth2 client registrations.
	 */
	private final Map<String, OAuth2ClientRegistration> clientRegistrations;

	/**
	 * All OAuth2 providers.
	 */
	private final Map<String, OAuth2ProviderDetails> providers;

	/**
	 * Decorates an exchange client with OAuth2 authentication.
	 *
	 * @param exchangeClient decorated exchange client
	 * @param tokenExchangeClient exchange client doing the token refresh
	 */
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient, final ExchangeClient tokenExchangeClient) {
		super(exchangeClient.getClientProperties());

		this.exchangeClient = exchangeClient;
		this.tokenExchangeClient = tokenExchangeClient;
		this.tokenRefreshScheduler = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());

		OAuth2Properties oAuth2Properties = getClientProperties().getCustomProperties(OAuth2Properties.ROOT, OAuth2Properties.class);
		this.clientRegistrations = oAuth2Properties.getRegistration();
		this.providers = oAuth2Properties.getProvider();

		setSchedulerEnabled(initialize());
		if (isSchedulerEnabled()) {
			refreshAuthenticationToken();
		}
	}

	/**
	 * Decorates an exchange client with OAuth2 authentication. Uses the same exchange client for token requests.
	 *
	 * @param exchangeClient decorated exchange client
	 */
	public OAuth2HttpExchangeClient(final ExchangeClient exchangeClient) {
		this(exchangeClient, exchangeClient);
	}

	/**
	 * Try to get an authentication token at startup. Returns true if the initialization of the properties was successful,
	 * false otherwise.
	 *
	 * @return true if the initialization was successful
	 */
	private boolean initialize() {
		if (exchangeClient.getClientProperties().isDisabled()) {
			LOGGER.warn("[{}] OAuth2 client is disabled!", getClass().getSimpleName());
			return false;
		}
		if (CollectionUtils.isEmpty(clientRegistrations.values())) {
			LOGGER.warn("[{}] No OAuth2 client registrations provided in: {}.registration",
					getClass().getSimpleName(), OAuth2Properties.ROOT);
			return false;
		}
		if (CollectionUtils.isEmpty(providers.values())) {
			LOGGER.warn("[{}] No OAuth2 providers provided in: {}.provider",
					getClass().getSimpleName(), OAuth2Properties.ROOT);
			return false;
		}
		// TODO: implement for multiple registrations
		setClientRegistrationName(getClientRegistrationName());

		return null != tokenApiClient;
	}

	/**
	 * Returns the client registration name, if the client registration property is blank a random one is selected from the
	 * registrations map.
	 *
	 * @return client registration name
	 */
	public String getClientRegistrationName() {
		return StringUtils.isNotBlank(clientRegistrationName)
				? clientRegistrationName
				: clientRegistrations.keySet().iterator().next();
	}

	/**
	 * Sets the client registration name which selects which OAuth2 settings will be used for this client, by default if
	 * only one is provided this property doesn't need to be set.
	 *
	 * @param clientRegistrationName client registration name
	 */
	public void setClientRegistrationName(final String clientRegistrationName) {
		this.clientRegistrationName = clientRegistrationName;

		OAuth2ClientRegistration clientRegistration = clientRegistrations.get(clientRegistrationName);
		if (StringUtils.isBlank(clientRegistration.getClientSecret())) {
			LOGGER.warn("[{}] No OAuth2 client-secret provided in {}.registration.{}",
					getClass().getSimpleName(), OAuth2Properties.ROOT, clientRegistrationName);
			return;
		}
		OAuth2ProviderDetails provider = providers.get(clientRegistration.getProvider());

		this.tokenApiClient = new OAuth2ApiClient(clientRegistration, provider, tokenExchangeClient);
	}

	/**
	 * Returns the expiration date for the given token.
	 *
	 * @return the expiration date
	 */
	protected Instant getTokenExpirationDate() {
		if (null == authenticationToken) {
			return Instant.now().plus(ERROR_TOKEN_REFRESH_DURATION);
		}
		Instant expiration = authenticationToken.getExpiration();
		if (null == expiration) {
			return Instant.now().plus(DEFAULT_EXPIRES_IN);
		}
		return expiration;
	}

	/**
	 * Returns the authentication token. If the current token is expired it tries to retrieve a new one.
	 *
	 * @return the authentication token
	 */
	public AuthenticationToken getAuthenticationToken() {
		if (isNewTokenNeeded()) {
			updateAuthenticationToken();
		}
		return authenticationToken;
	}

	/**
	 * Refreshes the authentication token.
	 */
	private void refreshAuthenticationToken() {
		updateAuthenticationToken();
		if (isSchedulerDisabled()) {
			return;
		}
		// schedule new token checking
		Instant expirationDate = getTokenExpirationDate().minus(TOKEN_EXPIRATION_ERROR_MARGIN);
		Instant scheduledDate = JavaObjects.max(expirationDate, Instant.now());
		Duration delay = Duration.between(scheduledDate, Instant.now());
		tokenRefreshScheduler.schedule(this::refreshAuthenticationToken, delay.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Returns true if a new token is needed, false otherwise.
	 *
	 * @return true if a new token is needed
	 */
	public boolean isNewTokenNeeded() {
		if (null == authenticationToken) {
			return true;
		}
		Instant expirationDate = authenticationToken.getExpiration();
		return expirationDate.isBefore(Instant.now().minus(TOKEN_EXPIRATION_ERROR_MARGIN));
	}

	/**
	 * Updates the authentication token by retrieving a new one.
	 */
	private void updateAuthenticationToken() {
		try {
			LOGGER.debug("[{}] Token expired, requesting new token.", getClass().getSimpleName());
			Instant expiration = Instant.now();

			AuthenticationToken token = tokenApiClient.getAuthenticationToken();
			if (null == token) {
				throw new IllegalStateException("Retrieved token was null");
			}
			token.setExpiration(expiration.plusSeconds(token.getExpiresIn()));
			authenticationToken = token;

			LOGGER.debug("[{}] Successfully retrieved new token.", getClass().getSimpleName());
		} catch (Exception e) {
			LOGGER.error("[{}] Failed to get authentication token: {}", getClass().getSimpleName(), e.getMessage(), e);
		}
	}

	/**
	 * @see #exchange(ApiRequest)
	 */
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		if (null != getAuthenticationToken()) {
			String headerValue = AuthorizationHeaderValues.bearerHeaderValue(authenticationToken.getAccessToken());
			Headers.addTo(apiRequest.getHeaders(), HttpHeader.AUTHORIZATION.value(), headerValue);
		}
		return exchangeClient.exchange(apiRequest);
	}

	/**
	 * Returns true if the scheduler is enabled, false otherwise.
	 *
	 * @return true if the scheduler is enabled, false otherwise
	 */
	public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	/**
	 * Returns true if the scheduler is disabled, false otherwise.
	 *
	 * @return true if the scheduler is disabled, false otherwise
	 */
	public boolean isSchedulerDisabled() {
		return !isSchedulerEnabled();
	}

	/**
	 * Enables/disables the scheduler.
	 *
	 * @param schedulerEnabled scheduler enable flag
	 */
	public void setSchedulerEnabled(final boolean schedulerEnabled) {
		this.schedulerEnabled = schedulerEnabled;
	}

}
