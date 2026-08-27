package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.CircularRedirectException;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Tests for {@link AbstractSpringExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class AbstractSpringExchangeClientTest {

	@Nested
	class BuildRequestTests {

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldBuildRequestWithHeaders() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<String> request = mock(ApiRequest.class);
			when(request.getHeaders()).thenReturn(Map.of(
					HttpHeader.CONTENT_TYPE.value(), List.of("application/json")));
			when(request.getBody()).thenReturn("body");

			HttpEntity<String> entity = client.buildRequest(request);

			assertThat(entity.getHeaders().getContentType().toString(), equalTo("application/json"));
			assertThat(entity.getBody(), equalTo("body"));
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldBuildRequestWithNullBody() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<String> request = mock(ApiRequest.class);
			when(request.getHeaders()).thenReturn(Map.of());
			when(request.getBody()).thenReturn(null);

			HttpEntity<String> entity = client.buildRequest(request);

			assertThat(entity.getBody(), nullValue());
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildRequestWithNullHeaders() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<String> request = mock(ApiRequest.class);
			when(request.getHeaders()).thenReturn(null);
			when(request.getBody()).thenReturn(null);

			HttpEntity<String> entity = client.buildRequest(request);

			assertThat(entity.getHeaders(), notNullValue());
		}
	}

	@Nested
	class CreateHttpEntityTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateHttpEntityForString() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<String> request = mock(ApiRequest.class);
			when(request.getHeaders()).thenReturn(null);
			when(request.getBody()).thenReturn("hello");

			HttpEntity<String> entity = client.buildRequest(request);

			assertThat(entity.getBody(), equalTo("hello"));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateHttpEntityForByteArray() {
			TestSpringExchangeClient client = createClient();

			byte[] bytes = { 0x01, 0x02 };
			ApiRequest<byte[]> request = mock(ApiRequest.class);
			when(request.getHeaders()).thenReturn(null);
			when(request.getBody()).thenReturn(bytes);

			HttpEntity<byte[]> entity = client.buildRequest(request);

			assertThat(entity.getBody(), equalTo(bytes));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateHttpEntityForSerializable() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<Integer> request = mock(ApiRequest.class);
			when(request.getHeaders()).thenReturn(null);
			when(request.getBody()).thenReturn(42);

			HttpEntity<Integer> entity = client.buildRequest(request);

			assertThat(entity.getBody(), instanceOf(byte[].class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateHttpEntityForUnsupportedType() {
			TestSpringExchangeClient client = createClient();

			Object unsupportedBody = new Object() {
				@Override
				public String toString() {
					return "custom";
				}
			};

			ApiRequest<Object> request = mock(ApiRequest.class);
			when(request.getHeaders()).thenReturn(null);
			when(request.getBody()).thenReturn(unsupportedBody);

			HttpEntity<Object> entity = client.buildRequest(request);

			assertThat(entity.getBody(), instanceOf(String.class));
			assertThat(entity.getBody().toString(), equalTo("custom"));
		}
	}

	@Nested
	class BuildResponseTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildSuccessResponse() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<byte[]> request = mock(ApiRequest.class);
			doReturn(byte[].class).when(request).getClassResponseType();

			ResponseEntity<byte[]> responseEntity = ResponseEntity.ok("hello".getBytes());

			ApiResponse<byte[]> response = client.buildResponse(request, responseEntity);

			assertThat(response.isSuccessful(), equalTo(true));
			assertThat(response.getBody(), notNullValue());
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildErrorResponse() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<byte[]> request = mock(ApiRequest.class);

			ResponseEntity<byte[]> responseEntity = ResponseEntity.status(404)
					.body("not found".getBytes());

			ApiResponse<byte[]> response = client.buildResponse(request, responseEntity);

			assertThat(response.isSuccessful(), equalTo(false));
			assertThat(response.hasException(), equalTo(true));
		}
	}

	@Nested
	class GetResponseTypeTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldReturnInputStreamForStreamRequest() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<byte[]> request = mock(ApiRequest.class);
			when(request.isStream()).thenReturn(true);

			Class<?> responseType = client.getResponseType(request);

			assertThat(responseType, equalTo(InputStream.class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldReturnByteArrayForNonStreamRequest() {
			TestSpringExchangeClient client = createClient();

			ApiRequest<byte[]> request = mock(ApiRequest.class);
			when(request.isStream()).thenReturn(false);

			Class<?> responseType = client.getResponseType(request);

			assertThat(responseType, equalTo(byte[].class));
		}
	}

	@Nested
	class ExtractHttpStatusTests {

		@Test
		@SuppressWarnings("resource")
		void shouldExtractStatusFromHttpException() {
			TestSpringExchangeClient client = createClient();

			HttpException exception = HttpException.builder()
					.status(HttpStatus.NOT_FOUND)
					.build();

			HttpStatus status = client.extractHttpStatus(exception);

			assertThat(status, equalTo(HttpStatus.NOT_FOUND));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldExtractStatusFromHttpStatusCodeException() {
			TestSpringExchangeClient client = createClient();

			HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
			when(exception.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.FORBIDDEN);

			HttpStatus status = client.extractHttpStatus(exception);

			assertThat(status, equalTo(HttpStatus.FORBIDDEN));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnFoundForRedirectFailureWhenFollowRedirects() {
			ClientProperties properties = ClientProperties.defaults();
			properties.getConnection().setFollowRedirects(true);
			TestSpringExchangeClient client = createClient(properties);

			HttpStatus status = client.extractHttpStatus(new CircularRedirectException("redirect loop"));

			assertThat(status, equalTo(HttpStatus.FOUND));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNullForNonHttpExceptionWhenNotFollowingRedirects() {
			TestSpringExchangeClient client = createClient();

			HttpStatus status = client.extractHttpStatus(new RuntimeException("error"));

			assertThat(status, nullValue());
		}
	}

	@Nested
	class ExtractResponseBodyTests {

		@Test
		@SuppressWarnings("resource")
		void shouldExtractResponseBodyFromHttpException() {
			TestSpringExchangeClient client = createClient();

			HttpException exception = HttpException.builder()
					.responseBody("error body")
					.build();

			String body = client.extractResponseBody(exception);

			assertThat(body, equalTo("error body"));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldExtractResponseBodyFromHttpStatusCodeException() {
			TestSpringExchangeClient client = createClient();

			HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
			when(exception.getResponseBodyAsString()).thenReturn("error response");

			String body = client.extractResponseBody(exception);

			assertThat(body, equalTo("error response"));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNullForGenericException() {
			TestSpringExchangeClient client = createClient();

			String body = client.extractResponseBody(new RuntimeException("error"));

			assertThat(body, nullValue());
		}
	}

	@Nested
	class ExtractResponseHeadersTests {

		@Test
		@SuppressWarnings("resource")
		void shouldExtractHeadersFromHttpStatusCodeException() {
			TestSpringExchangeClient client = createClient();

			HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set("X-Custom", "value");
			when(exception.getResponseHeaders()).thenReturn(responseHeaders);

			Map<String, List<String>> headers = client.extractResponseHeaders(exception);

			assertThat(headers.get("X-Custom"), equalTo(List.of("value")));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNullForGenericExceptionHeaders() {
			TestSpringExchangeClient client = createClient();

			Map<String, List<String>> headers = client.extractResponseHeaders(new RuntimeException("error"));

			assertThat(headers, nullValue());
		}
	}

	@Nested
	class GetMessageConvertersTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnThreeDefaultConverters() {
			TestSpringExchangeClient client = createClient();

			List<?> converters = client.getMessageConverters();

			assertThat(converters.size(), equalTo(3));
		}
	}

	@Nested
	class CloseTests {

		@Test
		void shouldCloseClient() throws Exception {
			TestSpringExchangeClient client = createClient();

			client.close();

			assertTrue(client.isClosed());
		}
	}

	@Nested
	class RedirectLoopFailurePredicateTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNonEmptyPredicate() {
			TestSpringExchangeClient client = createClient();

			var predicate = client.getRedirectLoopFailurePredicate();

			assertThat(predicate, notNullValue());
		}
	}

	static class TestSpringExchangeClient extends AbstractSpringExchangeClient {

		private boolean closed = false;

		TestSpringExchangeClient(final ClientProperties clientProperties) {
			super(clientProperties);
		}

		@Override
		protected <T, U> ResponseEntity<U> sendRequest(final ApiRequest<T> apiRequest, final HttpEntity<T> httpEntity) {
			return null;
		}

		public boolean isClosed() {
			return closed;
		}

		@Override
		public void close() throws Exception {
			super.close();
			closed = true;
		}
	}

	private static TestSpringExchangeClient createClient() {
		return new TestSpringExchangeClient(ClientProperties.defaults());
	}

	private static TestSpringExchangeClient createClient(final ClientProperties properties) {
		return new TestSpringExchangeClient(properties);
	}
}
