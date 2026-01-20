package org.apiphany.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.apiphany.ApiRequest;
import org.apiphany.header.Header;
import org.apiphany.header.Headers;
import org.apiphany.security.AuthenticationType;
import org.apiphany.utils.client.DummyExchangeClient;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExchangeClientTest {

	private final ExchangeClient exchangeClient = new DummyExchangeClient();

	@Test
	void shouldThrowExceptionOnGetClientProperties() {
		UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
				() -> exchangeClient.getClientProperties());

		assertEquals("getClientProperties", exception.getMessage());
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
		assertEquals(DummyExchangeClient.class.getSimpleName(), exchangeClient.getName());
	}

	@Test
	void shouldReturnHeadersAsStringFromApiMessage() {
		assertEquals(Collections.emptyList().toString(), exchangeClient.getHeadersAsString(new ApiRequest<String>()));
	}

	@Test
	void shouldReturnHeadersAsStringFromApiMessageWithHeaders() {
		ApiRequest<String> request = new ApiRequest<>();
		var headers = Headers.of(
				Header.of("Content-Type", "application/json"),
				Header.of("Accept", "application/json"));
		request.addHeaders(headers);

		String headersAsString = exchangeClient.getHeadersAsString(request);

		assertThat(headersAsString, containsString("Content-Type:[application/json]"));
		assertThat(headersAsString, containsString("Accept:[application/json]"));
	}

	@Test
	void shouldReturnAlwaysFalsePredicateOnIsSensitiveHeader() {
		assertEquals(false, exchangeClient.isSensitiveHeader().test("Any-Header-Value"));
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
	void shouldThrowExceptionOnGetCustomProperties() {
		UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
				() -> exchangeClient.getCustomProperties(String.class));

		assertEquals("getClientProperties", exception.getMessage());
	}

	@Test
	void shouldCreateExchangeClientBuilder() {
		ExchangeClientBuilder builder = ExchangeClient.builder();

		assertInstanceOf(ExchangeClientBuilder.class, builder);
	}
}
