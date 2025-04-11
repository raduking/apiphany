package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;
import org.apiphany.security.AuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AbstractTokenHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class AbstractTokenHttpExchangeClientTest {

	private static final Instant DEFAULT_EXPIRATION = Instant.now();

	private AbstractTokenHttpExchangeClient client;

	@BeforeEach
	void setUp() {
		client = new TokenClient(new HttpExchangeClient());
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

	static class TokenClient extends AbstractTokenHttpExchangeClient {

		protected TokenClient(final ExchangeClient exchangeClient) {
			super(exchangeClient);
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		@Override
		protected Instant getDefaultTokenExpiration() {
			return DEFAULT_EXPIRATION;
		}

	}

}
