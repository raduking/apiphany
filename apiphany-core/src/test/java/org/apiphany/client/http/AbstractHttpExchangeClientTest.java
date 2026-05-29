package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
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
			exchangeClient.callEnsureContentLengthWithinLimit(Map.of(), 10);
		}

		@Test
		void shouldDoNothingWhenContentLengthHeaderIsBlank() {
			exchangeClient.callEnsureContentLengthWithinLimit(Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("   ")), 10);
		}

		@Test
		void shouldDoNothingWhenContentLengthHeaderIsNotANumber() {
			exchangeClient.callEnsureContentLengthWithinLimit(Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("abc")), 10);
		}

		@Test
		void shouldDoNothingWhenContentLengthIsWithinLimit() {
			exchangeClient.callEnsureContentLengthWithinLimit(Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("10")), 10);
		}

		@Test
		void shouldThrowPayloadTooLargeWhenContentLengthExceedsLimit() {
			HttpException exception = assertThrows(HttpException.class,
					() -> exchangeClient.callEnsureContentLengthWithinLimit(
							Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("11")), 10));

			assertThat(exception.getStatus(), equalTo(HttpStatus.PAYLOAD_TOO_LARGE));
			assertThat(exception.getMessage(), startsWith("[413 Payload Too Large] Response body exceeds configured max size"));
		}
	}

	@Nested
	class EnsureBodySizeWithinLimitTests {

		@Test
		void shouldDoNothingWhenBodyIsNotByteArray() {
			exchangeClient.callEnsureBodySizeWithinLimit("text", 3);
		}

		@Test
		void shouldDoNothingWhenByteArrayBodyIsWithinLimit() {
			exchangeClient.callEnsureBodySizeWithinLimit(new byte[3], 3);
		}

		@Test
		void shouldThrowPayloadTooLargeWhenByteArrayBodyExceedsLimit() {
			HttpException exception = assertThrows(HttpException.class,
					() -> exchangeClient.callEnsureBodySizeWithinLimit(new byte[4], 3));

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
