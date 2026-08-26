package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.hc.client5.http.CircularRedirectException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.reflection.Fields;

/**
 * Test class for {@link ApacheHC5HttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class ApacheHC5HttpExchangeClientTest {

	private static final URI TEST_URI = URI.create("https://example.com/api");

	private static final String STRING_BODY = "test body";
	private static final byte[] BYTES_BODY = new byte[] { 0x01, 0x02, 0x03 };

	@Nested
	class ConstructorTests {

		@Test
		@SuppressWarnings("resource")
		void shouldBuildWithDefaultClientProperties() throws Exception {
			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient()) {
				assertNotNull(client.getHttpClient());
				assertNotNull(client.getConnectionManager());
				assertThat(client.getClientProperties(), notNullValue());
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldBuildWithExplicitClientProperties() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			properties.getConnection().setMaxPerRoute(5);

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				assertNotNull(client.getHttpClient());
				assertThat(client.getClientProperties().getConnection().getMaxPerRoute(), equalTo(5));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldDefaultHttpVersionWhenNotConfigured() throws Exception {
			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient()) {
				assertNotNull(client.getHttpClient());
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldSetHttpVersionFromProperties() throws Exception {
			ApacheHC5Properties properties = new ApacheHC5Properties();
			properties.getRequest().setProtocolVersion("2.0");

			ClientProperties clientProperties = ClientProperties.defaults();
			clientProperties.setCustomProperties(properties);

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(clientProperties)) {
				assertNotNull(client.getHttpClient());
			}
		}
	}

	@Nested
	class ToHttpUriRequestTests {

		@Test
		void shouldBuildGetRequest() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.GET);
			assertThat(request, instanceOf(HttpGet.class));
		}

		@Test
		void shouldBuildPostRequest() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.POST);
			assertThat(request, instanceOf(HttpPost.class));
		}

		@Test
		void shouldBuildPutRequest() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.PUT);
			assertThat(request, instanceOf(HttpPut.class));
		}

		@Test
		void shouldBuildDeleteRequest() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.DELETE);
			assertThat(request, instanceOf(HttpDelete.class));
		}

		@Test
		void shouldBuildPatchRequest() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.PATCH);
			assertThat(request, instanceOf(HttpPatch.class));
		}

		@Test
		void shouldBuildHeadRequest() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.HEAD);
			assertThat(request, instanceOf(HttpHead.class));
		}

		@Test
		void shouldBuildOptionsRequest() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.OPTIONS);
			assertThat(request, instanceOf(HttpOptions.class));
		}

		@Test
		void shouldBuildTraceRequest() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.TRACE);
			assertThat(request, instanceOf(HttpTrace.class));
		}

		@Test
		void shouldThrowExceptionForUnsupportedMethod() {
			assertThrows(UnsupportedOperationException.class,
					() -> ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, HttpMethod.CONNECT));
		}

		@Test
		void shouldBuildRequestFromStringMethod() {
			HttpUriRequest request = ApacheHC5HttpExchangeClient.toHttpUriRequest(TEST_URI, "GET");
			assertThat(request, instanceOf(HttpGet.class));
		}
	}

	@Nested
	class ToHttpHeadersMapTests {

		@Test
		void shouldConvertHeaderArrayToMap() {
			Header[] headers = new Header[] {
					new BasicHeader("X-Custom", "value1"),
					new BasicHeader("Accept", "application/json")
			};

			Map<String, List<String>> result = ApacheHC5HttpExchangeClient.toHttpHeadersMap(headers);

			assertThat(result.get("X-Custom"), equalTo(List.of("value1")));
			assertThat(result.get("Accept"), equalTo(List.of("application/json")));
		}

		@Test
		void shouldAccumulateMultipleValuesForSameHeader() {
			Header[] headers = new Header[] {
					new BasicHeader("Accept", "text/html"),
					new BasicHeader("Accept", "application/json")
			};

			Map<String, List<String>> result = ApacheHC5HttpExchangeClient.toHttpHeadersMap(headers);

			assertThat(result.get("Accept"), equalTo(List.of("text/html", "application/json")));
		}
	}

	@Nested
	class AddHeadersTests {

		@Test
		void shouldNotFailWhenHeadersAreNull() {
			HttpUriRequest request = new HttpGet(TEST_URI);

			ApacheHC5HttpExchangeClient.addHeaders(request, null);

			assertThat(request.getHeaders().length, equalTo(0));
		}

		@Test
		void shouldAddHeadersToRequest() {
			HttpUriRequest request = new HttpGet(TEST_URI);
			Map<String, List<String>> headers = Map.of(
					"X-Custom", List.of("value1"),
					"Authorization", List.of("Bearer token"));

			ApacheHC5HttpExchangeClient.addHeaders(request, headers);

			Header[] result = request.getHeaders();
			boolean foundCustom = false;
			boolean foundAuth = false;
			for (Header header : result) {
				if ("X-Custom".equals(header.getName()) && "value1".equals(header.getValue())) {
					foundCustom = true;
				}
				if ("Authorization".equals(header.getName()) && "Bearer token".equals(header.getValue())) {
					foundAuth = true;
				}
			}
			assertTrue(foundCustom, "X-Custom header not found");
			assertTrue(foundAuth, "Authorization header not found");
		}

		@Test
		void shouldNotFailWhenHeadersContainNullValuesList() {
			HttpUriRequest request = new HttpGet(TEST_URI);
			Map<String, List<String>> headers = new HashMap<>();
			headers.put("X-Null", null);

			ApacheHC5HttpExchangeClient.addHeaders(request, headers);

			assertThat(request.getHeaders().length, equalTo(0));
		}
	}

	@Nested
	class ExtractHttpStatusTests {

		@Test
		void shouldReturnFoundForCircularRedirectWhenFollowRedirectsEnabled() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			properties.getConnection().setFollowRedirects(true);

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				CircularRedirectException exception = new CircularRedirectException("circular redirect");
				HttpStatus status = client.extractHttpStatus(exception);

				assertThat(status, equalTo(HttpStatus.FOUND));
			}
		}

		@Test
		void shouldReturnNullForCircularRedirectWhenFollowRedirectsDisabled() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			properties.getConnection().setFollowRedirects(false);

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				CircularRedirectException exception = new CircularRedirectException("circular redirect");
				HttpStatus status = client.extractHttpStatus(exception);

				assertNull(status);
			}
		}

		@Test
		void shouldReturnNullForNonCircularRedirectException() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			properties.getConnection().setFollowRedirects(true);

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				RuntimeException exception = new RuntimeException("some error");
				HttpStatus status = client.extractHttpStatus(exception);

				assertNull(status);
			}
		}

		@Test
		void shouldExtractStatusFromHttpException() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				HttpException exception = HttpException.builder()
						.status(HttpStatus.BAD_GATEWAY)
						.build();
				HttpStatus status = client.extractHttpStatus(exception);

				assertThat(status, equalTo(HttpStatus.BAD_GATEWAY));
			}
		}
	}

	@Nested
	class GetRedirectLoopFailurePredicateTests {

		@Test
		void shouldMatchDirectCircularRedirectException() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				Predicate<Throwable> predicate = client.getRedirectLoopFailurePredicate();

				assertTrue(predicate.test(new CircularRedirectException("circular redirect")));
			}
		}

		@Test
		void shouldMatchWrappedCircularRedirectException() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				Predicate<Throwable> predicate = client.getRedirectLoopFailurePredicate();
				CircularRedirectException circular = new CircularRedirectException("circular redirect");
				IOException wrapped = new IOException("wrapped", circular);

				assertTrue(predicate.test(wrapped));
			}
		}

		@Test
		void shouldMatchCircularRedirectMessage() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				Predicate<Throwable> predicate = client.getRedirectLoopFailurePredicate();

				assertTrue(predicate.test(new RuntimeException("circular redirect")));
			}
		}

		@Test
		void shouldMatchTooManyRedirectsMessage() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				Predicate<Throwable> predicate = client.getRedirectLoopFailurePredicate();

				assertTrue(predicate.test(new RuntimeException("too many redirects")));
			}
		}

		@Test
		void shouldNotMatchNonRedirectException() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				Predicate<Throwable> predicate = client.getRedirectLoopFailurePredicate();

				assertEquals(false, predicate.test(new RuntimeException("connection reset")));
			}
		}
	}

	@Nested
	class BuildRequestTests {

		@Test
		@SuppressWarnings("unchecked")
		void shouldBuildGetRequestWithApiRequest() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(TEST_URI).when(apiRequest).getUri();
				doReturn(HttpMethod.GET).when(apiRequest).getMethod();
				doReturn(null).when(apiRequest).getHeaders();
				doReturn(false).when(apiRequest).hasBody();

				HttpUriRequest request = client.buildRequest(apiRequest);

				assertThat(request, instanceOf(HttpGet.class));
				assertThat(request.getUri().toString(), equalTo("https://example.com/api"));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildPostRequestWithBody() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ApiRequest<String> apiRequest = mock(ApiRequest.class);
				doReturn(TEST_URI).when(apiRequest).getUri();
				doReturn(HttpMethod.POST).when(apiRequest).getMethod();
				doReturn(null).when(apiRequest).getHeaders();
				doReturn(STRING_BODY).when(apiRequest).getBody();
				doReturn(true).when(apiRequest).hasBody();

				HttpUriRequest request = client.buildRequest(apiRequest);

				assertThat(request, instanceOf(HttpPost.class));
				assertNotNull(request.getEntity());
			}
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldSetHeadersOnRequest() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(TEST_URI).when(apiRequest).getUri();
				doReturn(HttpMethod.GET).when(apiRequest).getMethod();
				doReturn(Map.of("X-Custom", List.of("value1"))).when(apiRequest).getHeaders();
				doReturn(false).when(apiRequest).hasBody();

				HttpUriRequest request = client.buildRequest(apiRequest);

				boolean found = false;
				for (Header header : request.getHeaders()) {
					if ("X-Custom".equals(header.getName()) && "value1".equals(header.getValue())) {
						found = true;
					}
				}
				assertTrue(found, "Custom header not set on request");
			}
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldSetHttpVersionOnRequest() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(TEST_URI).when(apiRequest).getUri();
				doReturn(HttpMethod.GET).when(apiRequest).getMethod();
				doReturn(null).when(apiRequest).getHeaders();
				doReturn(false).when(apiRequest).hasBody();

				HttpUriRequest request = client.buildRequest(apiRequest);

				ProtocolVersion version = request.getVersion();
				assertNotNull(version);
			}
		}
	}

	@Nested
	class CreateHttpEntityTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityFromStringBody() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ApiRequest<String> apiRequest = mock(ApiRequest.class);
				doReturn(STRING_BODY).when(apiRequest).getBody();
				doReturn(null).when(apiRequest).getHeaders();

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
				assertThat(EntityUtils.toString(entity), equalTo(STRING_BODY));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityFromByteArrayBody() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ApiRequest<byte[]> apiRequest = mock(ApiRequest.class);
				doReturn(BYTES_BODY).when(apiRequest).getBody();
				doReturn(null).when(apiRequest).getHeaders();

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
				assertArrayEquals(BYTES_BODY, EntityUtils.toByteArray(entity));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityFromInputStream() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				InputStream inputStream = new ByteArrayInputStream(BYTES_BODY);
				ApiRequest<InputStream> apiRequest = mock(ApiRequest.class);
				doReturn(inputStream).when(apiRequest).getBody();
				doReturn(null).when(apiRequest).getHeaders();

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
				assertArrayEquals(BYTES_BODY, EntityUtils.toByteArray(entity));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityFromFile() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				File tempFile = File.createTempFile("hc5-test", ".txt");
				tempFile.deleteOnExit();
				java.nio.file.Files.writeString(tempFile.toPath(), "file content");

				ApiRequest<File> apiRequest = mock(ApiRequest.class);
				doReturn(tempFile).when(apiRequest).getBody();
				doReturn(null).when(apiRequest).getHeaders();

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
				assertThat(EntityUtils.toString(entity), equalTo("file content"));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityFromPath() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				Path tempPath = java.nio.file.Files.createTempFile("hc5-test", ".txt");
				tempPath.toFile().deleteOnExit();
				java.nio.file.Files.writeString(tempPath, "path content");

				ApiRequest<Path> apiRequest = mock(ApiRequest.class);
				doReturn(tempPath).when(apiRequest).getBody();
				doReturn(null).when(apiRequest).getHeaders();

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
				assertThat(EntityUtils.toString(entity), equalTo("path content"));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityFromSerializable() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ApiRequest<Serializable> apiRequest = mock(ApiRequest.class);
				doReturn("serializable string").when(apiRequest).getBody();
				doReturn(null).when(apiRequest).getHeaders();

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityFromJsonObject() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				TestDto dto = TestDto.of("id1", 42);
				Map<String, List<String>> headers = Map.of(HttpHeader.CONTENT_TYPE.value(), List.of("application/json"));

				ApiRequest<TestDto> apiRequest = mock(ApiRequest.class);
				doReturn(dto).when(apiRequest).getBody();
				doReturn(headers).when(apiRequest).getHeaders();
				doReturn(null).when(apiRequest).getCharset();
				doReturn(true).when(apiRequest).containsHeader(org.mockito.ArgumentMatchers.eq(HttpHeader.CONTENT_TYPE),
						org.mockito.ArgumentMatchers.any());

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
				String json = EntityUtils.toString(entity);
				assertTrue(json.contains("id1"));
				assertTrue(json.contains("42"));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityFromSupplier() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				Supplier<String> supplier = () -> "supplied value";
				ApiRequest<Supplier<?>> apiRequest = mock(ApiRequest.class);
				doReturn(supplier).when(apiRequest).getBody();
				doReturn(null).when(apiRequest).getHeaders();

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
				assertThat(EntityUtils.toString(entity), equalTo("supplied value"));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateEntityWithContentType() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				Map<String, List<String>> headers = Map.of(
						HttpHeader.CONTENT_TYPE.value(), List.of("text/plain"));

				ApiRequest<String> apiRequest = mock(ApiRequest.class);
				doReturn(STRING_BODY).when(apiRequest).getBody();
				doReturn(headers).when(apiRequest).getHeaders();
				doReturn(null).when(apiRequest).getCharset();

				HttpEntity entity = client.createHttpEntity(apiRequest);

				assertNotNull(entity);
				assertTrue(entity.getContentType().contains("text/plain"));
			}
		}
	}

	@Nested
	class BuildResponseTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildResponseFrom200Ok() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ClassicHttpResponse response = mock(ClassicHttpResponse.class);
				when(response.getCode()).thenReturn(200);
				when(response.getHeaders()).thenReturn(new Header[] {
						new BasicHeader("Content-Type", "text/plain")
				});

				BasicHttpEntity entity = new BasicHttpEntity(
						new ByteArrayInputStream("OK".getBytes()), ContentType.TEXT_PLAIN);
				when(response.getEntity()).thenReturn(entity);

				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(false).when(apiRequest).isStream();

				ApiResponse<?> result = client.buildResponse(apiRequest, response);

				assertThat(result.getStatus(), equalTo(HttpStatus.OK));
				assertThat(result.orNull(), equalTo("OK"));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildResponseWith204NoContent() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ClassicHttpResponse response = mock(ClassicHttpResponse.class);
				when(response.getCode()).thenReturn(204);
				when(response.getHeaders()).thenReturn(new Header[0]);
				when(response.getEntity()).thenReturn(null);

				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(false).when(apiRequest).isStream();

				ApiResponse<?> result = client.buildResponse(apiRequest, response);

				assertThat(result.getStatus(), equalTo(HttpStatus.NO_CONTENT));
				assertNull(result.orNull());
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildResponseWith304NotModified() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ClassicHttpResponse response = mock(ClassicHttpResponse.class);
				when(response.getCode()).thenReturn(304);
				when(response.getHeaders()).thenReturn(new Header[0]);
				when(response.getEntity()).thenReturn(null);

				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(false).when(apiRequest).isStream();

				ApiResponse<?> result = client.buildResponse(apiRequest, response);

				assertThat(result.getStatus(), equalTo(HttpStatus.NOT_MODIFIED));
				assertNull(result.orNull());
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildResponseWith500Error() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ClassicHttpResponse response = mock(ClassicHttpResponse.class);
				when(response.getCode()).thenReturn(500);
				when(response.getHeaders()).thenReturn(new Header[0]);

				BasicHttpEntity entity = new BasicHttpEntity(
						new ByteArrayInputStream("Internal Server Error".getBytes()), ContentType.TEXT_PLAIN);
				when(response.getEntity()).thenReturn(entity);

				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(false).when(apiRequest).isStream();

				ApiResponse<?> result = client.buildResponse(apiRequest, response);

				assertThat(result.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
				assertThat(((org.apiphany.http.HttpException) result.getException()).getResponseBody(),
						equalTo("Internal Server Error"));
			}
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldBuildResponseWithMultipleHeaders() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				ClassicHttpResponse response = mock(ClassicHttpResponse.class);
				when(response.getCode()).thenReturn(200);
				when(response.getHeaders()).thenReturn(new Header[] {
						new BasicHeader("X-Custom", "value1"),
						new BasicHeader("X-Custom", "value2"),
						new BasicHeader("Accept", "application/json")
				});

				BasicHttpEntity entity = new BasicHttpEntity(
						new ByteArrayInputStream("OK".getBytes()), ContentType.TEXT_PLAIN);
				when(response.getEntity()).thenReturn(entity);

				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(false).when(apiRequest).isStream();
				doReturn(String.class).when(apiRequest).getClassResponseType();

				ApiResponse<?> result = client.buildResponse(apiRequest, response);

				assertThat(result.getStatus(), equalTo(HttpStatus.OK));
				assertThat(result.getHeaders().get("X-Custom"), equalTo(List.of("value1", "value2")));
				assertThat(result.getHeaders().get("Accept"), equalTo(List.of("application/json")));
			}
		}
	}

	@Nested
	class CloseTests {

		@SuppressWarnings("resource")
		@Test
		void shouldCloseClientAndDrainConnectionPool() throws Exception {
			Queue<Closeable> closeables = null;

			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient()) {
				CloseableHttpClient httpClient = client.getHttpClient();
				closeables = Fields.IgnoreAccess.get(httpClient, "closeables");

				assertNotNull(closeables);
				assertThat(closeables, hasSize(1));
			}

			assertNotNull(closeables);
			assertThat(closeables, hasSize(0));
		}
	}

	@Nested
	class DoExchangeTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldDelegateToSendRequestForNonStream() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
			CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
			when(mockResponse.getCode()).thenReturn(200);
			when(mockResponse.getHeaders()).thenReturn(new Header[0]);

			BasicHttpEntity entity = new BasicHttpEntity(
					new ByteArrayInputStream("OK".getBytes()), ContentType.TEXT_PLAIN);
			when(mockResponse.getEntity()).thenReturn(entity);

			when(mockClient.execute(any(), any(HttpClientResponseHandler.class)))
					.thenAnswer(invocation -> {
						HttpClientResponseHandler<?> handler = invocation.getArgument(1);
						return handler.handleResponse(mockResponse);
					});

			HttpClient httpClient = null;
			try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient(properties)) {
				httpClient = client.getHttpClient();
				Fields.IgnoreAccess.set(client, "httpClient", mockClient);

				ApiRequest<Void> apiRequest = mock(ApiRequest.class);
				doReturn(TEST_URI).when(apiRequest).getUri();
				doReturn(HttpMethod.GET).when(apiRequest).getMethod();
				doReturn(null).when(apiRequest).getHeaders();
				doReturn(false).when(apiRequest).hasBody();
				doReturn(false).when(apiRequest).isStream();
				doReturn(String.class).when(apiRequest).getClassResponseType();

				ApiResponse<?> result = client.doExchange(apiRequest);

				assertThat(result.getStatus(), equalTo(HttpStatus.OK));
			} finally {
				if (httpClient instanceof Closeable) {
					((Closeable) httpClient).close();
				}
			}
		}
	}

	/**
	 * Local DTO for JSON serialization tests
	 */
	static class TestDto {

		private String id;
		private int count;

		static TestDto of(final String id, final int count) {
			TestDto result = new TestDto();
			result.id = id;
			result.count = count;
			return result;
		}

		public String getId() {
			return id;
		}

		public int getCount() {
			return count;
		}
	}
}
