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
import java.util.function.Predicate;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.Parameter;
import org.apiphany.RequestParameters;
import org.apiphany.header.Header;
import org.apiphany.header.HeaderValues;
import org.apiphany.header.Headers;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.Sensitive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

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
		public Predicate<String> isSensitiveHeader() {
			return "My-Authorization"::equalsIgnoreCase;
		}

		@Override
		public Predicate<String> isSensitiveParam() {
			return "password"::equalsIgnoreCase;
		}

		@Override
		public void close() {
			// empty
		}
	};

	@AfterEach
	void tearDown() throws Exception {
		exchangeClient.close();
	}

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
	void shouldRedactSensitiveDisplayHeadersUsingIsSensitiveParamPredicate() {
		ApiRequest<String> request = new ApiRequest<>();
		var headers = Headers.of(
				Header.of("Content-Type", "application/json"),
				Header.of("My-Authorization", "Test"));
		request.addHeaders(headers);

		Map<String, List<String>> headersForDisplay = exchangeClient.getDisplayHeaders(request);

		assertThat(headersForDisplay, equalTo(Map.of(
				"Content-Type", List.of("application/json"),
				"My-Authorization", List.of(HeaderValues.REDACTED))));
	}

	@Test
	void shouldReturnAlwaysFalsePredicateOnIsSensitiveHeader() {
		assertFalse(exchangeClient.isSensitiveHeader().test("Any-Header-Value"));
	}

	@Test
	void shouldReturnDisplayParamsFromNullApiRequest() {
		assertEquals(Collections.emptyMap(), exchangeClient.getDisplayParams(null));
	}

	@Test
	void shouldReturnDisplayParamsFromApiRequestWithParams() {
		ApiRequest<String> request = new ApiRequest<>();
		var params = RequestParameters.of(
				Parameter.of("page", "1"),
				Parameter.of("limit", "10"));
		Fields.IgnoreAccess.set(request, "params", params);

		Map<String, List<String>> paramsForDisplay = exchangeClient.getDisplayParams(request);

		assertThat(paramsForDisplay, equalTo(Map.of(
				"page", List.of("1"),
				"limit", List.of("10"))));
	}

	@Test
	void shouldRedactSensitiveDisplayParamsUsingIsSensitiveParamPredicate() {
		ApiRequest<String> request = new ApiRequest<>();
		var params = RequestParameters.of(
				Parameter.of("username", "john"),
				Parameter.of("password", "secret"));
		Fields.IgnoreAccess.set(request, "params", params);

		Map<String, List<String>> paramsForDisplay = exchangeClient.getDisplayParams(request);

		assertThat(paramsForDisplay, equalTo(Map.of(
				"username", List.of("john"),
				"password", List.of(Sensitive.Value.REDACTED))));
	}

	@Test
	void shouldReturnAlwaysFalsePredicateOnIsSensitiveParam() {
		assertFalse(exchangeClient.isSensitiveParam().test("Any-Param-Value"));
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
