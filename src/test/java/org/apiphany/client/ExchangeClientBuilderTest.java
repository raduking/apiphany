package org.apiphany.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.junit.jupiter.api.Test;

/**
 * Test class for ExchangeClientBuilder.
 *
 * @author Radu Sebastian LAZIN
 */
class ExchangeClientBuilderTest {

	@Test
	void shouldNotAllowBothExchangeClientAndExchangeClientClassToBeNonNull() throws Exception {
		ExchangeClient exchangeClient = new ExchangeClient() {
			@Override
			public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
				return null;
			}

			@Override
			public void close() throws Exception {
				// empty
			}
		};
		try (exchangeClient) {
			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(ExchangeClient.class)
					.client(exchangeClient);

			IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

			assertThat(exception.getMessage(), equalTo("Cannot set both exchange client instance and exchange client class"));
		}
	}

}
