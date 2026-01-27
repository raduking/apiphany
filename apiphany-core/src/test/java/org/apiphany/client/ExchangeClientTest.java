package org.apiphany.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.header.Header;
import org.apiphany.header.Headers;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExchangeClientTest {

	private final ExchangeClient exchangeClient = new ExchangeClient() {

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
			return null;
		}

		@Override
		public void close() {
			// empty
		}
	};

	@Test
	void shouldReturnNullClientProperties() {
		ClientProperties clientProperties = exchangeClient.getClientProperties();

		assertNull(clientProperties);
	}

	@Test
	void shouldThrowExceptionOnAsyncExchange() {
		UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
				() -> exchangeClient.asyncExchange(null));

		assertEquals("asyncExchange(ApiRequest)", exception.getMessage());
	}

	@Test
	void shouldReturnNoneAuthenticationType() {
		assertEquals(AuthenticationType.NONE, exchangeClient.getAuthenticationType());
	}

	@Test
	void shouldReturnClassNameAsClientName() {
		assertEquals(exchangeClient.getClass().getSimpleName(), exchangeClient.getName());
	}

	@Test
	void shouldReturnDisplayHeadersFromApiMessage() {
		assertEquals(Collections.emptyMap(), exchangeClient.getDisplayHeaders(new ApiRequest<String>()));
	}

	@Test
	void shouldReturnDisplayHeadersFromNullApiMessage() {
		assertEquals(Collections.emptyMap(), exchangeClient.getDisplayHeaders(null));
	}

	@Test
	void shouldReturnDisplayHeadersFromApiMessageWithHeaders() {
		ApiRequest<String> request = new ApiRequest<>();
		var headers = Headers.of(
				Header.of("Content-Type", "application/json"),
				Header.of("Accept", "application/json"));
		request.addHeaders(headers);

		Map<String, List<String>> headersForDisplay = exchangeClient.getDisplayHeaders(request);

		assertThat(headersForDisplay, equalTo(Map.of(
				"Content-Type", List.of("application/json"),
				"Accept", List.of("application/json"))));
	}

	@Test
	void shouldReturnAlwaysFalsePredicateOnIsSensitiveHeader() {
		assertFalse(exchangeClient.isSensitiveHeader().test("Any-Header-Value"));
	}

	@Test
	void shouldReturnEmptyMapOnGetCommonHeaders() {
		assertEquals(Collections.emptyMap(), exchangeClient.getCommonHeaders());
	}

	@Test
	void shouldReturnEmptyMapOnGetTracingHeaders() {
		assertEquals(Collections.emptyMap(), exchangeClient.getTracingHeaders());
	}

	@Test
	void shouldReturnNullOnGetCustomProperties() {
		String customProperties = exchangeClient.getCustomProperties(String.class);

		assertNull(customProperties);
	}

	@Test
	void shouldCreateExchangeClientBuilder() {
		ExchangeClientBuilder builder = ExchangeClient.builder();

		assertInstanceOf(ExchangeClientBuilder.class, builder);
	}
}
