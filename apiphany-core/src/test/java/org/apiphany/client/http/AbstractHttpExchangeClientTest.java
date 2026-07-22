package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.resource.ScopedResource;

/**
 * Test class for {@link AbstractHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class AbstractHttpExchangeClientTest {

	private TestHttpExchangeClient exchangeClient;

	@AfterEach
	void tearDown() {
		ScopedResource.safeClose(exchangeClient, Consumers.noConsumer());
	}

	@Nested
	class EnsureContentLengthWithinLimitTests {

		@Test
		void shouldDoNothingWhenContentLengthHeaderIsMissing() {
			exchangeClient = newExchangeClient(false);

			assertDoesNotThrow(() -> exchangeClient.callEnsureContentLengthWithinLimit(Map.of(), 10));
		}

		@ParameterizedTest
		@MethodSource("provideContentLengthHeaderTestCases")
		void shouldDoNothingWhenContentLengthHeaderIsValid(final String contentLengthHeader) {
			exchangeClient = newExchangeClient(false);

			assertDoesNotThrow(
					() -> exchangeClient.callEnsureContentLengthWithinLimit(Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of(contentLengthHeader)), 10));
		}

		@Test
		void shouldThrowPayloadTooLargeWhenContentLengthExceedsLimit() {
			exchangeClient = newExchangeClient(false);

			var headers = Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("11"));
			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.callEnsureContentLengthWithinLimit(headers, 10));

			assertThat(exception.getStatus(), equalTo(HttpStatus.PAYLOAD_TOO_LARGE));
			assertThat(exception.getMessage(), startsWith("[413 Payload Too Large] Response body exceeds configured max size"));
		}

		private static Stream<Arguments> provideContentLengthHeaderTestCases() {
			return Stream.of(
					Arguments.of("   "),
					Arguments.of("abc"),
					Arguments.of("0"),
					Arguments.of("1"),
					Arguments.of("10")
			);
		}
	}

	@Nested
	class EnsureBodySizeWithinLimitTests {

		@Test
		void shouldDoNothingWhenBodyIsNotByteArray() {
			exchangeClient = newExchangeClient(false);

			assertDoesNotThrow(() -> exchangeClient.callEnsureBodySizeWithinLimit("text", 3));
		}

		@Test
		void shouldDoNothingWhenByteArrayBodyIsWithinLimit() {
			exchangeClient = newExchangeClient(false);

			assertDoesNotThrow(() -> exchangeClient.callEnsureBodySizeWithinLimit(new byte[3], 3));
		}

		@Test
		void shouldThrowPayloadTooLargeWhenByteArrayBodyExceedsLimit() {
			exchangeClient = newExchangeClient(false);

			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.callEnsureBodySizeWithinLimit(new byte[4], 3));

			assertThat(exception.getStatus(), equalTo(HttpStatus.PAYLOAD_TOO_LARGE));
			assertThat(exception.getMessage(), startsWith("[413 Payload Too Large] Response body exceeds configured max size"));
		}
	}

	@Nested
	class CustomizeHttpExceptionBuilderTests {

		@Test
		void shouldCustomizeAsRedirectLoopWhenFollowRedirectsIsEnabled() {
			exchangeClient = newExchangeClient(true);

			HttpException.Builder builder = HttpException.builder();

			exchangeClient.callCustomizeHttpExceptionBuilder(builder, new RuntimeException("too many redirects"));

			HttpException exception = builder.build();

			assertThat(exception.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
			assertThat(exception.getMessage(), startsWith("[500 Internal Server Error] Redirect loop detected"));
			assertThat(exception.getResponseBody(), nullValue());
		}

		@Test
		void shouldCustomizeUsingExtractedStatusBodyAndHeadersWhenFollowRedirectsIsDisabled() {
			exchangeClient = newExchangeClient(false);

			Map<String, List<String>> headers = Map.of("X-Test", List.of("test-value"));
			HttpException sourceException = HttpException.builder()
					.status(HttpStatus.BAD_GATEWAY)
					.responseBody("bad gateway body")
					.responseHeaders(headers)
					.build();

			HttpException.Builder builder = HttpException.builder();
			exchangeClient.callCustomizeHttpExceptionBuilder(builder, sourceException);

			HttpException exception = builder.build();

			assertThat(exception.getStatus(), equalTo(HttpStatus.BAD_GATEWAY));
			assertThat(exception.getResponseBody(), equalTo("bad gateway body"));
			assertThat(exception.getResponseHeaders(), equalTo(headers));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCustomizeUsingExtractedStatusBodyAndHeadersWhenFollowRedirectsIsEnabledButFailureIsNotRedirectLoop() {
			exchangeClient = newExchangeClient(true);

			Map<String, List<String>> headers = Map.of("X-Test", List.of("test-value"));
			HttpException sourceException = HttpException.builder()
					.status(HttpStatus.BAD_GATEWAY)
					.responseBody("bad gateway body")
					.responseHeaders(headers)
					.build();

			HttpException.Builder builder = HttpException.builder();
			exchangeClient.callCustomizeHttpExceptionBuilder(builder, sourceException);

			HttpException exception = builder.build();

			assertThat(exception.getStatus(), equalTo(HttpStatus.BAD_GATEWAY));
			assertThat(exception.getResponseBody(), equalTo("bad gateway body"));
			assertThat(exception.getResponseHeaders(), equalTo(headers));
		}
	}

	@Nested
	class RedirectLoopFailurePredicateTests {

		@ParameterizedTest
		@MethodSource("redirectLoopFailurePredicateTestCases")
		void shouldUseDefaultRedirectLoopFailurePredicateFromHttpMessages(final Exception exception, final boolean expectedResult) {
			exchangeClient = newExchangeClient(false);

			Predicate<Throwable> predicate = exchangeClient.callRedirectLoopFailurePredicate();

			assertThat(predicate.test(exception), equalTo(expectedResult));
		}

		private static Stream<Arguments> redirectLoopFailurePredicateTestCases() {
			return Stream.of(
					Arguments.of(new RuntimeException("circular redirect"), true),
					Arguments.of(new RuntimeException("too many redirects"), true),
					Arguments.of(new RuntimeException("connection reset"), false),
					Arguments.of(null, false)
			);
		}
	}

	private static TestHttpExchangeClient newExchangeClient(final boolean followRedirects) {
		ClientProperties clientProperties = ClientProperties.defaults();
		clientProperties.getConnection().setFollowRedirects(followRedirects);
		return new TestHttpExchangeClient(clientProperties);
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

		void callCustomizeHttpExceptionBuilder(final HttpException.Builder builder, final Throwable throwable) {
			customizeHttpExceptionBuilder(builder, throwable);
		}

		Predicate<Throwable> callRedirectLoopFailurePredicate() {
			return redirectLoopFailurePredicate();
		}
	}
}
