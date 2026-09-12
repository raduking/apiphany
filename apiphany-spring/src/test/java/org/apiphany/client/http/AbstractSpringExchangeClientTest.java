package org.apiphany.client.http;

import static org.apiphany.http.HttpMethod.GET;
import static org.apiphany.http.HttpMethod.POST;
import static org.apiphany.http.HttpStatus.FORBIDDEN;
import static org.apiphany.http.HttpStatus.FOUND;
import static org.apiphany.http.HttpStatus.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.CircularRedirectException;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.CloseableHttpRequestFactory;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

/**
 * Tests for {@link AbstractSpringExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class AbstractSpringExchangeClientTest {

	@Nested
	class BuildRequestTests {

		private TestSpringExchangeClient client;

		@BeforeEach
		void setUp() {
			client = createClient();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldBuildRequestWithHeaders() {
			ApiRequest<String> request = mock(ApiRequest.class);
			doReturn(Map.of(HttpHeader.CONTENT_TYPE.value(), List.of("application/json"))).when(request).getHeaders();
			doReturn("body").when(request).getBody();

			HttpEntity<String> entity = client.buildRequest(request);

			assertThat(entity.getHeaders().getContentType().toString(), equalTo("application/json"));
			assertThat(entity.getBody(), equalTo("body"));
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldBuildRequestWithNullBody() {
			ApiRequest<String> request = mock(ApiRequest.class);
			doReturn(Map.of()).when(request).getHeaders();
			doReturn(null).when(request).getBody();

			HttpEntity<String> entity = client.buildRequest(request);

			assertThat(entity.getBody(), nullValue());
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldBuildRequestWithNullHeaders() {
			ApiRequest<String> request = mock(ApiRequest.class);
			doReturn(null).when(request).getHeaders();
			doReturn(null).when(request).getBody();

			HttpEntity<String> entity = client.buildRequest(request);

			assertThat(entity.getHeaders(), notNullValue());
		}
	}

	@Nested
	class CreateHttpEntityTests {

		private TestSpringExchangeClient client;

		@BeforeEach
		void setUp() {
			client = createClient();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldCreateHttpEntityForString() {
			ApiRequest<String> request = mock(ApiRequest.class);
			doReturn(null).when(request).getHeaders();
			doReturn("hello").when(request).getBody();

			HttpEntity<String> entity = client.buildRequest(request);

			assertThat(entity.getBody(), equalTo("hello"));
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldCreateHttpEntityForByteArray() {
			byte[] bytes = { 0x01, 0x02 };
			ApiRequest<byte[]> request = mock(ApiRequest.class);
			doReturn(null).when(request).getHeaders();
			doReturn(bytes).when(request).getBody();

			HttpEntity<byte[]> entity = client.buildRequest(request);

			assertThat(entity.getBody(), equalTo(bytes));
		}

		@Test
		void shouldCreateHttpEntityForSerializable() {
			ApiRequest<?> request = mock(ApiRequest.class);
			doReturn(null).when(request).getHeaders();
			doReturn(42).when(request).getBody();

			HttpEntity<?> entity = client.buildRequest(request);

			assertThat(entity.getBody(), instanceOf(byte[].class));
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldCreateHttpEntityForUnsupportedType() {
			Object unsupportedBody = new Object() {
				@Override
				public String toString() {
					return "custom";
				}
			};

			ApiRequest<Object> request = mock(ApiRequest.class);
			doReturn(null).when(request).getHeaders();
			doReturn(unsupportedBody).when(request).getBody();

			HttpEntity<Object> entity = client.buildRequest(request);

			assertThat(entity.getBody(), instanceOf(String.class));
			assertThat(entity.getBody().toString(), equalTo("custom"));
		}
	}

	@Nested
	class BuildResponseTests {

		private static final int STATUS_404 = 404;

		private TestSpringExchangeClient client;

		@BeforeEach
		void setUp() {
			client = createClient();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldBuildSuccessResponse() {
			ApiRequest<byte[]> request = mock(ApiRequest.class);
			doReturn(byte[].class).when(request).getClassResponseType();

			ResponseEntity<byte[]> responseEntity = ResponseEntity.ok("hello".getBytes());

			ApiResponse<byte[]> response = client.buildResponse(request, responseEntity);

			assertThat(response.isSuccessful(), equalTo(true));
			assertThat(response.getBody(), notNullValue());
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldBuildErrorResponse() {
			ApiRequest<byte[]> request = mock(ApiRequest.class);

			ResponseEntity<byte[]> responseEntity = ResponseEntity.status(STATUS_404)
					.body("not found".getBytes());

			ApiResponse<byte[]> response = client.buildResponse(request, responseEntity);

			assertThat(response.isSuccessful(), equalTo(false));
			assertThat(response.hasException(), equalTo(true));
		}
	}

	@Nested
	class GetResponseTypeTests {

		private TestSpringExchangeClient client;

		@BeforeEach
		void setUp() {
			client = createClient();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldReturnInputStreamForStreamRequest() {
			ApiRequest<byte[]> request = mock(ApiRequest.class);
			doReturn(true).when(request).isStream();

			Class<?> responseType = client.getResponseType(request);

			assertThat(responseType, equalTo(InputStream.class));
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldReturnByteArrayForNonStreamRequest() {
			ApiRequest<byte[]> request = mock(ApiRequest.class);
			doReturn(false).when(request).isStream();

			Class<?> responseType = client.getResponseType(request);

			assertThat(responseType, equalTo(byte[].class));
		}
	}

	@Nested
	class ExtractHttpStatusTests {

		private TestSpringExchangeClient client;
		private ClientProperties clientProperties;

		@BeforeEach
		void setUp() {
			clientProperties = ClientProperties.defaults();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		void shouldExtractStatusFromHttpException() {
			client = createClient(clientProperties);
			HttpException exception = HttpException.builder()
					.status(NOT_FOUND)
					.build();

			var status = client.extractHttpStatus(exception);

			assertThat(status, equalTo(NOT_FOUND));
		}

		@Test
		void shouldExtractStatusFromHttpStatusCodeException() {
			client = createClient(clientProperties);
			HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
			doReturn(HttpStatus.FORBIDDEN).when(exception).getStatusCode();

			var status = client.extractHttpStatus(exception);

			assertThat(status, equalTo(FORBIDDEN));
		}

		@Test
		void shouldReturnFoundForRedirectFailureWhenFollowRedirects() {
			clientProperties.getConnection().setFollowRedirects(true);
			client = createClient(clientProperties);

			var status = client.extractHttpStatus(new CircularRedirectException("redirect loop"));

			assertThat(status, equalTo(FOUND));
		}

		@Test
		void shouldReturnNullForNonHttpExceptionWhenNotFollowingRedirects() {
			client = createClient(clientProperties);

			var status = client.extractHttpStatus(new RuntimeException("error"));

			assertThat(status, nullValue());
		}
	}

	@Nested
	class ExtractResponseBodyTests {

		private TestSpringExchangeClient client;

		@BeforeEach
		void setUp() {
			client = createClient();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		void shouldExtractResponseBodyFromHttpException() {
			HttpException exception = HttpException.builder()
					.responseBody("error body")
					.build();

			String body = client.extractResponseBody(exception);

			assertThat(body, equalTo("error body"));
		}

		@Test
		void shouldExtractResponseBodyFromHttpStatusCodeException() {
			HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
			doReturn("error response").when(exception).getResponseBodyAsString();

			String body = client.extractResponseBody(exception);

			assertThat(body, equalTo("error response"));
		}

		@Test
		void shouldReturnNullForGenericException() {
			String body = client.extractResponseBody(new RuntimeException("error"));

			assertThat(body, nullValue());
		}
	}

	@Nested
	class ExtractResponseHeadersTests {

		private TestSpringExchangeClient client;

		@BeforeEach
		void setUp() {
			client = createClient();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		void shouldExtractHeadersFromHttpStatusCodeException() {
			HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set("X-Custom", "value");
			doReturn(responseHeaders).when(exception).getResponseHeaders();

			Map<String, List<String>> headers = client.extractResponseHeaders(exception);

			assertThat(headers.get("X-Custom"), equalTo(List.of("value")));
		}

		@Test
		void shouldReturnNullForGenericExceptionHeaders() {
			Map<String, List<String>> headers = client.extractResponseHeaders(new RuntimeException("error"));

			assertThat(headers, nullValue());
		}
	}

	@Nested
	class GetMessageConvertersTests {

		private TestSpringExchangeClient client;

		@BeforeEach
		void setUp() {
			client = createClient();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		void shouldReturnThreeDefaultConverters() {
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

		private TestSpringExchangeClient client;

		@BeforeEach
		void setUp() {
			client = createClient();
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		void shouldReturnNonEmptyPredicate() {
			var predicate = client.getRedirectLoopFailurePredicate();

			assertThat(predicate, notNullValue());
		}
	}

	@Nested
	class SendStreamRequestTests {

		private static final String TEST_URL = "https://api.example.com";
		private static final String REQUEST_BODY = "request body";
		private static final String RESPONSE_BODY = "response body";

		private TestStreamSpringExchangeClient client;
		private CloseableHttpRequestFactory requestFactory;
		private ClientHttpRequest request;
		private ClientHttpResponse response;

		@BeforeEach
		void setUp() {
			requestFactory = mock(CloseableHttpRequestFactory.class);
			client = new TestStreamSpringExchangeClient(ClientProperties.defaults(), requestFactory);
			request = mock(ClientHttpRequest.class);
			response = mock(ClientHttpResponse.class);
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldSendStreamRequestWithBody() throws Exception {
			doReturn(new HttpHeaders()).when(request).getHeaders();
			doReturn(new ByteArrayOutputStream()).when(request).getBody();
			doReturn(response).when(request).execute();
			doReturn(request).when(requestFactory).createRequest(any(URI.class), any(HttpMethod.class));
			stubSuccessResponse();
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(POST).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			doReturn(false).when(apiRequest).isStream();

			HttpEntity<String> httpEntity = new HttpEntity<>(REQUEST_BODY);

			ResponseEntity<byte[]> result = client.sendStreamRequest(apiRequest, httpEntity);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			assertThat(new String(result.getBody()), equalTo(RESPONSE_BODY));
			verify(requestFactory).createRequest(URI.create(TEST_URL), HttpMethod.POST);
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldSendStreamRequestWithoutBody() throws Exception {
			doReturn(new HttpHeaders()).when(request).getHeaders();
			doReturn(response).when(request).execute();
			doReturn(request).when(requestFactory).createRequest(any(URI.class), any(HttpMethod.class));
			stubSuccessResponse();
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(GET).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			doReturn(false).when(apiRequest).isStream();

			HttpEntity<String> httpEntity = new HttpEntity<>(null);

			ResponseEntity<byte[]> result = client.sendStreamRequest(apiRequest, httpEntity);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			assertThat(new String(result.getBody()), equalTo(RESPONSE_BODY));
			verify(request).execute();
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldSendStreamRequestWithInputStreamResponse() throws Exception {
			doReturn(new HttpHeaders()).when(request).getHeaders();
			doReturn(response).when(request).execute();
			doReturn(request).when(requestFactory).createRequest(any(URI.class), any(HttpMethod.class));
			stubSuccessResponse();
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(GET).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			doReturn(true).when(apiRequest).isStream();

			HttpEntity<String> httpEntity = new HttpEntity<>(null);

			ResponseEntity<InputStream> result = client.sendStreamRequest(apiRequest, httpEntity);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			assertThat(result.getBody(), instanceOf(InputStream.class));
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldCloseResponseAndWrapWhenExtractionFails() throws Exception {
			doReturn(new HttpHeaders()).when(request).getHeaders();
			doReturn(response).when(request).execute();
			doReturn(request).when(requestFactory).createRequest(any(URI.class), any(HttpMethod.class));
			doReturn(new HttpHeaders()).when(response).getHeaders();
			doThrow(new IOException("read failed")).when(response).getBody();
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(GET).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			doReturn(false).when(apiRequest).isStream();

			HttpEntity<String> httpEntity = new HttpEntity<>(null);

			HttpException exception = assertThrows(HttpException.class, () -> client.sendStreamRequest(apiRequest, httpEntity));

			verify(response).close();
			assertThat(exception.getCause(), instanceOf(RestClientException.class));
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldWrapIOExceptionWhenCreateRequestFails() throws Exception {
			doThrow(new IOException("connection refused")).when(requestFactory).createRequest(any(URI.class), any(HttpMethod.class));
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(GET).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();

			HttpEntity<String> httpEntity = new HttpEntity<>(null);

			HttpException exception = assertThrows(HttpException.class, () -> client.sendStreamRequest(apiRequest, httpEntity));

			assertThat(exception.getCause(), instanceOf(IOException.class));
		}

		@SuppressWarnings("resource")
		private void stubSuccessResponse() throws IOException {
			doReturn(HttpStatus.OK).when(response).getStatusCode();
			doReturn(new HttpHeaders()).when(response).getHeaders();
			doReturn(new ByteArrayInputStream(RESPONSE_BODY.getBytes())).when(response).getBody();
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

	static class TestStreamSpringExchangeClient extends AbstractSpringExchangeClient {

		TestStreamSpringExchangeClient(final ClientProperties clientProperties, final CloseableHttpRequestFactory requestFactory) {
			super(clientProperties, requestFactory);
		}

		@Override
		protected <T, U> ResponseEntity<U> sendRequest(final ApiRequest<T> apiRequest, final HttpEntity<T> httpEntity) {
			return null;
		}
	}

	private static TestSpringExchangeClient createClient() {
		return new TestSpringExchangeClient(ClientProperties.defaults());
	}

	private static TestSpringExchangeClient createClient(final ClientProperties properties) {
		return new TestSpringExchangeClient(properties);
	}
}
