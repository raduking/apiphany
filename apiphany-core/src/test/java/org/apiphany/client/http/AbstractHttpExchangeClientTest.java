package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AbstractHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class AbstractHttpExchangeClientTest {

	private final TestHttpExchangeClient exchangeClient = new TestHttpExchangeClient(ClientProperties.defaults());

	@Nested
	class EnsureContentLengthWithinLimitTests {

		@Test
		void shouldDoNothingWhenContentLengthHeaderIsMissing() {
			assertDoesNotThrow(() -> exchangeClient.callEnsureContentLengthWithinLimit(Map.of(), 10));
		}

		@Test
		void shouldDoNothingWhenContentLengthHeaderIsBlank() {
			assertDoesNotThrow(
					() -> exchangeClient.callEnsureContentLengthWithinLimit(Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("   ")), 10));
		}

		@Test
		void shouldDoNothingWhenContentLengthHeaderIsNotANumber() {
			assertDoesNotThrow(
					() -> exchangeClient.callEnsureContentLengthWithinLimit(Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("abc")), 10));
		}

		@Test
		void shouldDoNothingWhenContentLengthIsWithinLimit() {
			assertDoesNotThrow(() -> exchangeClient.callEnsureContentLengthWithinLimit(Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("10")), 10));
		}

		@Test
		void shouldThrowPayloadTooLargeWhenContentLengthExceedsLimit() {
			var headers = Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("11"));
			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.callEnsureContentLengthWithinLimit(headers, 10));

			assertThat(exception.getStatus(), equalTo(HttpStatus.PAYLOAD_TOO_LARGE));
			assertThat(exception.getMessage(), startsWith("[413 Payload Too Large] Response body exceeds configured max size"));
		}
	}

	@Nested
	class EnsureBodySizeWithinLimitTests {

		@Test
		void shouldDoNothingWhenBodyIsNotByteArray() {
			assertDoesNotThrow(() -> exchangeClient.callEnsureBodySizeWithinLimit("text", 3));
		}

		@Test
		void shouldDoNothingWhenByteArrayBodyIsWithinLimit() {
			assertDoesNotThrow(() -> exchangeClient.callEnsureBodySizeWithinLimit(new byte[3], 3));
		}

		@Test
		void shouldThrowPayloadTooLargeWhenByteArrayBodyExceedsLimit() {
			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.callEnsureBodySizeWithinLimit(new byte[4], 3));

			assertThat(exception.getStatus(), equalTo(HttpStatus.PAYLOAD_TOO_LARGE));
			assertThat(exception.getMessage(), startsWith("[413 Payload Too Large] Response body exceeds configured max size"));
		}
	}

	private static class TestHttpExchangeClient extends AbstractHttpExchangeClient {

		protected TestHttpExchangeClient(final ClientProperties clientProperties) {
			super(clientProperties);
		}

		@Override
		protected <T, U> ApiResponse<U> doExchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		@Override
		public void close() {
			// empty
		}

		void callEnsureContentLengthWithinLimit(final Map<String, List<String>> headers, final int maxBodySize) {
			ensureContentLengthWithinLimit(headers, maxBodySize);
		}

		void callEnsureBodySizeWithinLimit(final Object body, final int maxBodySize) {
			ensureBodySizeWithinLimit(body, maxBodySize);
		}
	}
}
