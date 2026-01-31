package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.RequestMethod;
import org.apiphany.header.Header;
import org.apiphany.header.HeaderValues;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

/**
 * Test class for {@link HttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpExchangeClientTest {

	@Test
	void shouldReturnAllRequestMethods() throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();

		RequestMethod getMethod = client.get();
		RequestMethod putMethod = client.put();
		RequestMethod postMethod = client.post();
		RequestMethod deleteMethod = client.delete();
		RequestMethod patchMethod = client.patch();
		RequestMethod headMethod = client.head();
		RequestMethod optionsMethod = client.options();
		RequestMethod traceMethod = client.trace();

		client.close();

		assertThat(getMethod, equalTo(HttpMethod.GET));
		assertThat(putMethod, equalTo(HttpMethod.PUT));
		assertThat(postMethod, equalTo(HttpMethod.POST));
		assertThat(deleteMethod, equalTo(HttpMethod.DELETE));
		assertThat(patchMethod, equalTo(HttpMethod.PATCH));
		assertThat(headMethod, equalTo(HttpMethod.HEAD));
		assertThat(optionsMethod, equalTo(HttpMethod.OPTIONS));
		assertThat(traceMethod, equalTo(HttpMethod.TRACE));
	}

	@Test
	void shouldBeAutoCloseable() {
		DummyHttpExchangeClient client = new DummyHttpExchangeClient();
		try (client) {
			// empty
		}

		assertTrue(client.getClosed());
	}

	@ParameterizedTest
	@ValueSource(strings = { "Authorization", "authorization", "AuTHoRiZaTiOn" })
	void shouldReturnAuthorizationHeaderAsSensitive(final String headerName) throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		Predicate<String> isSensitive = client.isSensitiveHeader();

		assertTrue(isSensitive.test(headerName));
	}

	@ParameterizedTest
	@EnumSource(HttpHeader.class)
	void shouldReturnNonAuthorizationHeadersAsNonSensitive(final HttpHeader header) throws Exception {
		if (header == HttpHeader.AUTHORIZATION) {
			return;
		}
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		Predicate<String> isSensitive = client.isSensitiveHeader();

		assertFalse(isSensitive.test(header.name()));
	}

	@Test
	void shouldReturnEmptyTracingHeadersWhenNoValuesInMDC() throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		Map<String, List<String>> tracingHeaders = client.getTracingHeaders();

		assertThat(tracingHeaders, equalTo(Collections.emptyMap()));
	}

	@Test
	void shouldReturnTracingHeadersWhenThereAreValuesInMDC() throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		try {
			MDC.put("traceId", "some-trace-id");
			MDC.put("spanId", "some-span-id");
			Map<String, List<String>> tracingHeaders = client.getTracingHeaders();

			assertThat(tracingHeaders.size(), equalTo(2));
		} finally {
			MDC.clear();
		}
	}

	@Test
	void shouldReturnEmptyCommonHeadersByDefault() throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		Map<String, List<String>> commonHeaders = client.getCommonHeaders();

		assertThat(commonHeaders, equalTo(Collections.emptyMap()));
	}

	@Test
	void shouldReturnNoneAsAuthenticationTypeByDefault() throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		assertThat(client.getAuthenticationType(), equalTo(AuthenticationType.NONE));
	}

	@Test
	void shouldReturnClassNameAsClientNameByDefault() throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		assertThat(client.getName(), equalTo(DummyHttpExchangeClient.class.getSimpleName()));
	}

	@Test
	void shouldReturnDisplayHeadersWithRedactedAuthorizationFromApiMessageWithHeaders() throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		ApiRequest<String> request = new ApiRequest<>();
		var headers = Headers.of(
				Header.of("Content-Type", "application/json"),
				Header.of("Authorization", "1234567890"));
		request.addHeaders(headers);

		Map<String, List<String>> headersForDisplay = client.getDisplayHeaders(request);

		assertThat(headersForDisplay, equalTo(Map.of(
				"Content-Type", List.of("application/json"),
				"Authorization", List.of(HeaderValues.REDACTED))));
	}

	static class DummyHttpExchangeClient implements HttpExchangeClient {

		private boolean closed = false;

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		@Override
		public void close() {
			closed = true;
		}

		boolean getClosed() {
			return closed;
		}
	}

}
