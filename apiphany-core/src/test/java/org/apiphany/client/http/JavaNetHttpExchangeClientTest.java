package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.Supplier;

import org.apiphany.ApiClient;
import org.apiphany.ApiClientFluentAdapter;
import org.apiphany.ApiMethod;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.Status;
import org.apiphany.client.ClientCustomization;
import org.apiphany.client.ClientProperties;
import org.apiphany.header.Header;
import org.apiphany.header.Headers;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.ByteBufferSubscriber;
import org.apiphany.io.ContentType;
import org.apiphany.io.InputStreamSupplier;
import org.apiphany.io.gzip.GZip;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.utils.TestDto;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.convert.MapConversions;
import org.morphix.convert.ObjectConverterException;
import org.morphix.lang.Nullables;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.GenericType;

/**
 * Test class for {@link JavaNetHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class JavaNetHttpExchangeClientTest {

	private static final String URL = "https://example.com";

	private static final String STRING = "Juju";
	private static final byte[] BYTES = new byte[] { 0x01, 0x02, 0x03 };
	private static final String TEXT_FILE_TXT = "text-file.txt";

	private static final String EXPECTED_CONNECTION_ERROR = "Expected connection error";

	@Mock
	private ApiClient apiClient;

	@Nested
	class ConstructorTests {

		@Test
		void shouldBuildJavaNetHttpExchangeClient() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			assertThat(exchangeClient.getClientProperties(), equalTo(ClientProperties.defaults()));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldSetDefaultVersionIfProvidedVersionIsNull() throws Exception {
			HttpClient.Builder httpClientBuilder = mock(HttpClient.Builder.class);
			HttpClient httpClient = mock(HttpClient.class);
			doReturn(httpClient).when(httpClientBuilder).build();

			ClientProperties properties = new ClientProperties();
			JavaNetHttpProperties javaNetHttpProperties = new JavaNetHttpProperties();
			javaNetHttpProperties.getRequest().setVersion(null);
			properties.setCustomProperties(JavaNetHttpProperties.ROOT, javaNetHttpProperties);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(properties, httpClientBuilder, ClientCustomization.DEFAULT);
			exchangeClient.close();

			// also check the chain
			HttpClient.Version version = Nullables.notNull(javaNetHttpProperties)
					.andNotNull(JavaNetHttpProperties::getRequest)
					.thenNotNull(JavaNetHttpProperties.Request::getHttpVersion)
					.orElse(() -> JavaNetHttpProperties.Request.Default.HTTP_VERSION);

			assertThat(version, equalTo(Version.HTTP_1_1));
			verify(httpClientBuilder).version(Version.HTTP_1_1);
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotSetDefaultVersionIfProvidedVersionIsNullAndClientCustomizationIsNone() throws Exception {
			HttpClient.Builder httpClientBuilder = mock(HttpClient.Builder.class);
			HttpClient httpClient = mock(HttpClient.class);
			doReturn(httpClient).when(httpClientBuilder).build();

			ClientProperties properties = new ClientProperties();
			JavaNetHttpProperties javaNetHttpProperties = new JavaNetHttpProperties();
			javaNetHttpProperties.getRequest().setVersion(null);
			properties.setCustomProperties(JavaNetHttpProperties.ROOT, javaNetHttpProperties);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(properties, httpClientBuilder, ClientCustomization.NONE);
			exchangeClient.close();

			verify(httpClientBuilder, never()).version(Version.HTTP_1_1);
		}

		@Test
		void shouldBuildJavaNetHttpExchangeClientWithProperties() throws Exception {
			ClientProperties properties = new ClientProperties();
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(properties);
			exchangeClient.close();

			assertThat(exchangeClient.getClientProperties(), equalTo(properties));
		}

		@Test
		void shouldBuildJavaNetHttpExchangeClientWithPropertiesWithPrefix() throws Exception {
			ClientProperties properties = new ClientProperties();
			ClientProperties prefixedProperties = new ClientProperties();
			Map<String, Object> propertiesMap = MapConversions.convertToMap(prefixedProperties, String::valueOf, value -> value);
			properties.setClient(Map.of("prefixedClient.clientName", propertiesMap));

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(properties, "prefixedClient", "clientName");
			exchangeClient.close();

			assertThat(exchangeClient.getClientProperties(), equalTo(prefixedProperties));
		}

		@Test
		void shouldCreateSSLContextIfSSLPropertiesAreProvided() throws Exception {
			String sslPropertiesJson = Strings.fromFile("security/ssl/ssl-properties.json");
			SSLProperties sslProperties = JsonBuilder.fromJson(sslPropertiesJson, SSLProperties.class);

			ClientProperties properties = new ClientProperties();
			properties.setCustomProperties(sslProperties);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(properties);
			exchangeClient.close();

			assertThat(exchangeClient.getSslContext(), notNullValue());
		}
	}

	@Nested
	class BuildRequestTests {

		@Test
		void shouldBuildGetRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET);

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("GET"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
		}

		@Test
		void shouldBuildPutRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.PUT)
					.body(STRING);

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("PUT"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
			assertThat(httpRequest.bodyPublisher().isPresent(), equalTo(true));
		}

		@Test
		void shouldBuildPostRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.POST)
					.body(STRING);

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("POST"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
			assertThat(httpRequest.bodyPublisher().isPresent(), equalTo(true));
		}

		@Test
		void shouldBuildDeleteRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.DELETE);

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("DELETE"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
			assertThat(httpRequest.bodyPublisher().isPresent(), equalTo(false));
		}

		@Test
		void shouldBuildHeadRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.HEAD);

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("HEAD"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
			assertThat(httpRequest.bodyPublisher().isPresent(), equalTo(false));
		}

		@Test
		void shouldBuildHeadRequestWithHeaders() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.HEAD)
					.header("X-Custom-Header", "CustomValue");

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("HEAD"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
			assertThat(httpRequest.headers().firstValue("X-Custom-Header").orElse(null), equalTo("CustomValue"));
			assertThat(httpRequest.bodyPublisher().isPresent(), equalTo(false));
		}

		@Test
		void shouldBuildPatchRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.PATCH)
					.body(STRING);

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("PATCH"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
			assertThat(httpRequest.bodyPublisher().isPresent(), equalTo(true));
		}

		@Test
		void shouldBuildOptionsRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.OPTIONS);

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("OPTIONS"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
			assertThat(httpRequest.bodyPublisher().isPresent(), equalTo(true));
			assertThat(httpRequest.bodyPublisher().get().contentLength(), equalTo(0L));
		}

		@Test
		void shouldBuildTraceRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.TRACE);

			HttpRequest httpRequest = exchangeClient.buildRequest(request);

			assertThat(httpRequest.method(), equalTo("TRACE"));
			assertThat(httpRequest.uri().toString(), equalTo(URL));
			assertThat(httpRequest.bodyPublisher().isPresent(), equalTo(true));
			assertThat(httpRequest.bodyPublisher().get().contentLength(), equalTo(0L));
		}
	}

	@Nested
	class BuildResponseTests {

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponse() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			Map<String, List<String>> headers = Map.of(
					"Content-Type", List.of("application/json"));

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(STRING).when(httpResponse).body();
			doReturn(HttpHeaders.of(headers, (v1, v2) -> true)).when(httpResponse).headers();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertThat(apiResponse.getBody(), equalTo(STRING));
			assertThat(apiResponse.getHeaders().size(), equalTo(1));
			assertThat(apiResponse.getHeaders().get("Content-Type"), equalTo(List.of("application/json")));
		}

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponseWhenBodyIsNull() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertNull(apiResponse.getBody());
			assertThat(apiResponse.getHeaders().size(), equalTo(0));
		}

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponseWhenBodyIsEmptyBytesAndExpectedString() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(new byte[0]).when(httpResponse).body();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertThat(apiResponse.getBody(), equalTo(""));
			assertThat(apiResponse.getHeaders().size(), equalTo(0));
		}

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponseWhenBodyIsEmptyBytesAndResponseTypeIsBytes() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(byte[].class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(new byte[0]).when(httpResponse).body();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertNotNull(apiResponse.getBody());
			assertThat(((byte[]) apiResponse.getBody()).length, equalTo(0));
			assertThat(apiResponse.getHeaders().size(), equalTo(0));
		}

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponseWhenBodyIsBytesAndResponseTypeIsString() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn("hello".getBytes(Strings.DEFAULT_CHARSET)).when(httpResponse).body();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertNotNull(apiResponse.getBody());
			assertThat(apiResponse.getBody(), equalTo("hello"));
			assertThat(apiResponse.getHeaders().size(), equalTo(0));
		}

		private record MyDto(String name) {
			// empty record for testing purposes
		}

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponseWithNullBodyWhenBodyIsEmptyBytesAndExpectedHasNoConversion() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(MyDto.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(new byte[0]).when(httpResponse).body();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertNull(apiResponse.getBody());
			assertThat(apiResponse.getHeaders().size(), equalTo(0));
		}

		@Test
		void shouldThrowExceptionOnBuildResponseFromApiRequestAndHttpResponseWhenNoConversionExists() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(MyDto.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(BYTES).when(httpResponse).body();

			UnsupportedOperationException e =
					assertThrows(UnsupportedOperationException.class, () -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(e.getMessage(), equalTo("No content converter found to convert response to: " + MyDto.class.getName()
					+ ", for the response content type: null"));
		}

		@Test
		void shouldThrowExceptionOnBuildResponseFromApiRequestAndHttpResponseWhenResponseTypeIsNotProvided() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(STRING).when(httpResponse).body();

			UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
					() -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(exception.getMessage(), equalTo(
					"No content converter found to convert response to: " + ApiRequest.UNKNOWN_RESPONSE_TYPE
							+ ", for the response content type: null"));
		}

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponseWhenResponseTypeIsNotProvidedAndResponseIsNull() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertNull(apiResponse.getBody());
			assertThat(apiResponse.getHeaders().size(), equalTo(0));
		}

		@Test
		void shouldThrowExceptionOnBuildResponseFromApiRequestAndHttpResponseWhenResponseTypeIsVoid() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(Void.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(STRING).when(httpResponse).body();

			UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
					() -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(exception.getMessage(),
					equalTo("No content converter found to convert response to: " + Void.class.getTypeName()
							+ ", for the response content type: null"));
		}

		@Test
		void shouldThrowExceptionOnBuildResponseFromApiRequestAndHttpResponseWhenResponseTypeIsPrimitive() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(int.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(STRING).when(httpResponse).body();

			UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
					() -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(exception.getMessage(),
					equalTo("No content converter found to convert response to: " + int.class + ", for the response content type: null"));
		}

		@Test
		void shouldReturnDtoOnBuildResponseWhenResponseTypeIsDtoAndContentTypeIsApplicationJson() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(TestDto.class);

			Map<String, List<String>> headers = Map.of(
					"Content-Type", List.of("application/json"));

			TestDto expectedDto = TestDto.of("someId", 10);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(expectedDto.toString()).when(httpResponse).body();
			doReturn(HttpHeaders.of(headers, (v1, v2) -> true)).when(httpResponse).headers();

			ApiResponse<TestDto> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertThat(apiResponse.getBody(), equalTo(expectedDto));
		}

		@Test
		void shouldReturnDtoOnBuildResponseWhenResponseTypeIsGenericClassAndContentTypeIsApplicationJson() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			GenericClass<Map<String, TestDto>> genericResponseType = GenericClass.of(
					GenericType.of(Map.class, GenericType.Arguments.of(String.class, TestDto.class)));
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(genericResponseType);

			Map<String, List<String>> headers = Map.of(
					"Content-Type", List.of("application/json"));

			TestDto expectedInnerDto = TestDto.of("someId", 10);
			Map<String, TestDto> expectedDto = Map.of("key", expectedInnerDto);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(JsonBuilder.toJson(expectedDto)).when(httpResponse).body();
			doReturn(HttpHeaders.of(headers, (v1, v2) -> true)).when(httpResponse).headers();

			ApiResponse<Map<String, TestDto>> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertThat(apiResponse.getBody(), equalTo(expectedDto));
		}

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponseWhenResponseHasStatusNoContent() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.NO_CONTENT.value()).when(httpResponse).statusCode();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertNull(apiResponse.getBody());
			assertThat(apiResponse.getHeaders().size(), equalTo(0));
		}

		@Test
		void shouldBuildResponseFromApiRequestAndHttpResponseWhenResponseHasStatusNotModified() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.NOT_MODIFIED.value()).when(httpResponse).statusCode();

			ApiResponse<?> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertNull(apiResponse.getBody());
			assertThat(apiResponse.getHeaders().size(), equalTo(0));
		}

		@Test
		void shouldReturnResponseWithExceptionOnBuildResponseFromApiRequestAndHttpResponse500() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			Map<String, List<String>> headers = Map.of(
					"Content-Type", List.of(ContentType.Value.APPLICATION_JSON));

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.INTERNAL_SERVER_ERROR.value()).when(httpResponse).statusCode();
			doReturn(STRING).when(httpResponse).body();
			doReturn(HttpHeaders.of(headers, (v1, v2) -> true)).when(httpResponse).headers();

			ApiResponse<String> apiResponse = exchangeClient.buildResponse(request, httpResponse);

			assertThat(apiResponse.getException().getMessage(), equalTo(HttpException.message(HttpStatus.INTERNAL_SERVER_ERROR, STRING)));
			assertThat(apiResponse.getRequest(), equalTo(request));
			assertThat(apiResponse.getBody(), equalTo(STRING));
			assertThat(apiResponse.getHeaders().size(), equalTo(1));
			assertThat(apiResponse.getHeaders().get("Content-Type"), equalTo(List.of(ContentType.Value.APPLICATION_JSON)));
		}

		@Test
		void shouldThrowExceptionOnBuildResponseWhenResponseTypeIsDtoAndContentTypeIsApplicationJsonButResponseIsNotJson() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(TestDto.class);

			Map<String, List<String>> headers = Map.of(
					"Content-Type", List.of("application/json"));

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(STRING).when(httpResponse).body();
			doReturn(HttpHeaders.of(headers, (v1, v2) -> true)).when(httpResponse).headers();

			ObjectConverterException exception = assertThrows(ObjectConverterException.class,
					() -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(exception.getMessage(), equalTo("Error converting JSON response to " + TestDto.class.getName()));
		}

		@Test
		void shouldThrowExceptionOnBuildResponseWhenResponseTypeIsGenericTypeAndContentTypeIsApplicationJsonButResponseIsNotJson() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			GenericClass<Map<String, TestDto>> genericResponseType = GenericClass.of(
					GenericType.of(Map.class, GenericType.Arguments.of(String.class, TestDto.class)));
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(genericResponseType);

			Map<String, List<String>> headers = Map.of(
					"Content-Type", List.of("application/json"));

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(STRING).when(httpResponse).body();
			doReturn(HttpHeaders.of(headers, (v1, v2) -> true)).when(httpResponse).headers();

			ObjectConverterException exception = assertThrows(ObjectConverterException.class,
					() -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(exception.getMessage(), equalTo("Error converting JSON response to " + genericResponseType.getType().getTypeName()));
		}

		@Test
		void shouldThrowPayloadTooLargeWhenRawResponseExceedsConfiguredLimit() throws Exception {
			ClientProperties properties = new ClientProperties();
			properties.getResponse().setMaxBodySize(3);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(properties);
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(new ByteArrayInputStream(STRING.getBytes(StandardCharsets.UTF_8))).when(httpResponse).body();
			doReturn(HttpHeaders.of(Map.of(), (v1, v2) -> true)).when(httpResponse).headers();

			HttpException exception = assertThrows(HttpException.class,
					() -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(exception.getStatus(), equalTo(HttpStatus.PAYLOAD_TOO_LARGE));
			assertThat(exception.getMessage(), startsWith("[413 Payload Too Large]"));
		}

		@Test
		void shouldThrowPayloadTooLargeWhenContentLengthExceedsConfiguredLimitBeforeReadingBody() throws Exception {
			ClientProperties properties = new ClientProperties();
			properties.getResponse().setMaxBodySize(3);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(properties);
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(HttpHeaders.of(Map.of(HttpHeader.CONTENT_LENGTH.value(), List.of("5")), (v1, v2) -> true)).when(httpResponse)
					.headers();

			HttpException exception = assertThrows(HttpException.class,
					() -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(exception.getStatus(), equalTo(HttpStatus.PAYLOAD_TOO_LARGE));
			assertThat(exception.getMessage(), startsWith("[413 Payload Too Large] Response body exceeds configured max size"));
		}

		@Test
		void shouldThrowPayloadTooLargeWhenDecodedResponseExceedsConfiguredLimit() throws Exception {
			ClientProperties properties = new ClientProperties();
			properties.getResponse().setMaxBodySize(1024);
			properties.getResponse().setMaxDecodedBodySize(3);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(properties);
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			Map<String, List<String>> headers = Map.of(
					HttpHeader.CONTENT_ENCODING.value(), List.of(ContentEncoding.GZIP.value()));

			HttpResponse<?> httpResponse = mock(HttpResponse.class);
			doReturn(HttpStatus.OK.value()).when(httpResponse).statusCode();
			doReturn(GZip.compress(STRING.getBytes(StandardCharsets.UTF_8))).when(httpResponse).body();
			doReturn(HttpHeaders.of(headers, (v1, v2) -> true)).when(httpResponse).headers();

			IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> exchangeClient.buildResponse(request, httpResponse));

			assertThat(exception.getMessage(), equalTo("Failed to decode content with encoding: gzip"));
		}
	}

	@Nested
	class SendRequestTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldSendRequestAndReturnHttpResponse() throws Exception {
			HttpClient httpClient = mock(HttpClient.class);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(ClientProperties.defaults(), httpClient);
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> mockedHttpResponse = mock(HttpResponse.class);
			doReturn(200).when(mockedHttpResponse).statusCode();
			doReturn("OK").when(mockedHttpResponse).body();

			doReturn(mockedHttpResponse).when(httpClient).send(any(HttpRequest.class), any(BodyHandler.class));

			HttpResponse<String> httpResponse = exchangeClient.sendRequest(request, exchangeClient.buildRequest(request));

			assertThat(httpResponse.statusCode(), equalTo(200));
			assertThat(httpResponse.body(), equalTo("OK"));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldThrowExceptionWhenHttpClientThrowsExceptionOnSendRequest() throws Exception {
			HttpClient httpClient = mock(HttpClient.class);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(ClientProperties.defaults(), httpClient);
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			RuntimeException exceptionToThrow = new RuntimeException(EXPECTED_CONNECTION_ERROR);
			doThrow(exceptionToThrow).when(httpClient).send(any(HttpRequest.class), any(BodyHandler.class));

			HttpException exception = assertThrows(HttpException.class, // NOSONAR this is exactly what we want to test
					() -> exchangeClient.sendRequest(request, exchangeClient.buildRequest(request)));

			assertThat(exception.getStatus(), equalTo(null));
			assertThat(exception.getMessage(), equalTo(HttpException.message(null, EXPECTED_CONNECTION_ERROR)));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldExchangeApiRequestAndReturnApiResponse() throws Exception {
			HttpClient httpClient = mock(HttpClient.class);

			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient(ClientProperties.defaults(), httpClient);
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.GET)
					.responseType(String.class);

			HttpResponse<?> mockedHttpResponse = mock(HttpResponse.class);
			doReturn(200).when(mockedHttpResponse).statusCode();
			doReturn("OK").when(mockedHttpResponse).body();

			doReturn(mockedHttpResponse).when(httpClient).send(any(HttpRequest.class), any(BodyHandler.class));

			ApiResponse<?> apiResponse = exchangeClient.exchange(request);

			assertThat(apiResponse.getRequest(), equalTo(request));
			assertThat(apiResponse.getBody(), equalTo("OK"));
		}
	}

	@Nested
	class GetResponseBodyHandlerTests {

		@Test
		void shouldReturnInputStreamBodyHandlerWhenNoStreamIsProvided() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.stream(false)
					.responseType(String.class);

			BodyHandler<?> bodyHandler = JavaNetHttpExchangeClient.getResponseBodyHandler(request);

			Subscriber<?> subscriber = bodyHandler.apply(null);

			assertInstanceOf(HttpResponse.BodySubscribers.ofInputStream().getClass(), subscriber);
		}

		@Test
		void shouldReturnInputStreamBodyHandlerWhenStreamIsProvided() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.stream()
					.responseType(String.class);

			BodyHandler<?> bodyHandler = JavaNetHttpExchangeClient.getResponseBodyHandler(request);

			Subscriber<?> subscriber = bodyHandler.apply(null);

			assertInstanceOf(HttpResponse.BodySubscribers.ofInputStream().getClass(), subscriber);
		}
	}

	@Nested
	class ToBodyPublisherTests {

		@Test
		void shouldConvertNullContentTypeToStringBodyPublisherWhenStringIsProvidedAsBody() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.header(HttpHeader.CONTENT_TYPE, (String) null)
					.body(STRING);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			assertThat(bodyPublisher.contentLength(), equalTo((long) STRING.length()));

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(STRING.getBytes(StandardCharsets.UTF_8), equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertNullBodyToNoBodyPublisher() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body(null);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			assertThat(bodyPublisher.contentLength(), equalTo(0L));
		}

		@Test
		void shouldConvertEmptyBodyToNoBodyPublisher() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			assertThat(bodyPublisher.contentLength(), equalTo(0L));
		}

		@Test
		void shouldConvertNullSupplierBodyToNoBodyPublisher() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body((Supplier<?>) null);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			assertThat(bodyPublisher.contentLength(), equalTo(0L));
		}

		@Test
		void shouldConvertByteArrayToByteArrayBodyPublisher() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body(BYTES);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			assertThat(bodyPublisher.contentLength(), equalTo((long) BYTES.length));

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertByteArraySupplierToByteArrayBodyPublisher() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body(() -> BYTES);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			assertThat(bodyPublisher.contentLength(), equalTo((long) BYTES.length));

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertByteArrayPayloadSupplierToByteArrayBodyPublisher() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.payload(() -> BYTES);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			assertThat(bodyPublisher.contentLength(), equalTo((long) BYTES.length));

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertInputStreamToInputStreamBodyPublisher() {
			ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body(bis);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertInputStreamSetWithReflectionToInputStreamBodyPublisher() {
			ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body(bis);
			Fields.set(request, "body", bis);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertInputStreamSupplierToInputStreamBodyPublisher() {
			ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);
			InputStreamSupplier supplier = () -> bis;

			ApiRequest<?> request = ApiClientFluentAdapter.of(apiClient)
					.body(supplier);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertRawInputStreamSupplierToInputStreamBodyPublisher() {
			ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);
			Supplier<InputStream> supplier = () -> bis;

			ApiRequest<?> request = ApiClientFluentAdapter.of(apiClient)
					.body(supplier);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertStringToStringBodyPublisher() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body(STRING);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertThat(bodyPublisher.contentLength(), equalTo((long) STRING.length()));
			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(STRING.getBytes(StandardCharsets.UTF_8), equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertObjectToJsonStringBodyPublisherWhenContentTypeIsApplicationJson() {
			TestDto expectedDto = TestDto.of("someId1", 5);

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.header(HttpHeader.CONTENT_TYPE, "application/json")
					.body(expectedDto);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			String expectedJson = JsonBuilder.toJson(expectedDto);

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertThat(bodyPublisher.contentLength(), equalTo((long) expectedJson.length()));
			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(expectedJson.getBytes(StandardCharsets.UTF_8), equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertObjectToStringBodyPublisherWhenContentTypeIsNotApplicationJson() {
			TestDto expectedDto = TestDto.of("someId2", 10);

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.header(HttpHeader.CONTENT_TYPE, "text/plain")
					.body(expectedDto);

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			String expectedString = expectedDto.toString();

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertThat(bodyPublisher.contentLength(), equalTo((long) expectedString.length()));
			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(expectedString.getBytes(StandardCharsets.UTF_8), equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldConvertFileToFileBodyPublisher() {
			String fileContent = Strings.fromFile(TEXT_FILE_TXT);

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body(Path.of("src/test/resources/" + TEXT_FILE_TXT));

			BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			bodyPublisher.subscribe(subscriber);
			subscriber.awaitCompletion();

			assertThat(bodyPublisher.contentLength(), equalTo((long) fileContent.getBytes(StandardCharsets.UTF_8).length));
			assertTrue(subscriber.isCompleted());
			assertNull(subscriber.getError());
			assertThat(fileContent.getBytes(StandardCharsets.UTF_8), equalTo(subscriber.getReceivedBytes()));
		}

		@Test
		void shouldThrowExceptionIfFileNotFoundWhenTryingToBuildFileBodyPublisher() {
			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.body(Path.of(TEXT_FILE_TXT));

			HttpException exception = assertThrows(HttpException.class, () -> JavaNetHttpExchangeClient.toBodyPublisher(request));

			assertThat(exception.getStatus(), equalTo(HttpStatus.BAD_REQUEST));
			assertThat(exception.getMessage(), equalTo(
					HttpException.message(HttpStatus.BAD_REQUEST, TEXT_FILE_TXT + " not found")));
		}
	}

	@Nested
	class AddHeadersTests {

		@Test
		void shouldNotFailWhenHeadersAreNullOnAddHeaders() {
			HttpRequest.Builder requestBuilder = mock(HttpRequest.Builder.class);

			JavaNetHttpExchangeClient.addHeaders(requestBuilder, null);

			verifyNoInteractions(requestBuilder);
		}

		@Test
		void shouldNotFailWhenHeadersContainNullValuesListOnAddHeaders() {
			HttpRequest.Builder requestBuilder = mock(HttpRequest.Builder.class);
			Map<String, List<String>> headers = Headers.of(
					Header.of("X-Header-1", null),
					Header.of("X-Header-2", List.of("Value1", "Value2")));

			JavaNetHttpExchangeClient.addHeaders(requestBuilder, headers);

			verify(requestBuilder).header("X-Header-2", "Value1");
			verify(requestBuilder).header("X-Header-2", "Value2");
		}
	}

	@Nested
	class ExceptionTests {

		@Test
		void shouldThrowExceptionOnBuildConnectRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(HttpMethod.CONNECT);

			HttpException e = assertThrows(HttpException.class, () -> exchangeClient.exchange(request));

			assertThat(e.getStatus(), equalTo(null));
			assertThat(e.getMessage(),
					equalTo(HttpException.message(null, "HTTP method " + HttpMethod.CONNECT + " is not supported!")));
		}

		@Test
		void shouldThrowExceptionBuildingCustomMethodRequest() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(ApiMethod.UNDEFINED)
					.body(STRING);

			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.exchange(request));

			assertThat(exception.getStatus(), equalTo(null));
			assertThat(exception.getMessage(), equalTo(
					HttpException.message(null, "HTTP method " + ApiMethod.UNDEFINED + " is not supported!")));
		}

		@Test
		void shouldThrowExceptionWhenBuildingRequestWithUnsupportedHttpMethod() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method(ApiMethod.UNDEFINED);

			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.exchange(request));

			assertThat(exception.getStatus(), equalTo(null));
			assertThat(exception.getMessage(),
					equalTo(HttpException.message(null, "HTTP method " + ApiMethod.UNDEFINED + " is not supported!")));
		}

		@Test
		void shouldThrowExceptionWhenBuildingRequestWithNullHttpMethod() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url(URL)
					.method((HttpMethod) null);

			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.exchange(request));

			assertThat(exception.getStatus(), equalTo(null));
			assertThat(exception.getStatusCode(), equalTo(Status.UNKNOWN));
			assertThat(exception.getMessage(),
					equalTo(HttpException.message(null, "HTTP method null is not supported!")));
		}

		@Test
		void shouldThrowExceptionWhenBuildingRequestWithNullUrl() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.method(HttpMethod.GET);

			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.exchange(request));

			assertThat(exception.getStatus(), equalTo(null));
			assertThat(exception.getStatusCode(), equalTo(Status.UNKNOWN));
			assertThat(exception.getMessage(),
					equalTo(HttpException.message(null, "uri must be non-null")));
		}

		@Test
		void shouldThrowExceptionWhenBuildingRequestWithInvalidUrl() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();

			ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
					.url("http://[invalid-url]")
					.method(HttpMethod.GET);

			HttpException exception = assertThrows(HttpException.class, () -> exchangeClient.exchange(request));

			assertThat(exception.getStatus(), equalTo(null));
			assertThat(exception.getMessage(),
					startsWith(HttpException.message(null, "Malformed")));
		}

		@Test
		void shouldExtractNullStatusFromThrowableWithoutStatus() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();
			RuntimeException exceptionToThrow = new RuntimeException(EXPECTED_CONNECTION_ERROR);

			HttpStatus status = exchangeClient.extractHttpStatus(exceptionToThrow);

			assertThat(status, equalTo(null));
		}

		@Test
		void shouldExtractInternalServerErrorStatusFromThrowableWithStatus() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();
			HttpException exceptionToThrow = new HttpException(HttpStatus.BAD_GATEWAY, EXPECTED_CONNECTION_ERROR);

			HttpStatus status = exchangeClient.extractHttpStatus(exceptionToThrow);

			assertThat(status, equalTo(HttpStatus.BAD_GATEWAY));
		}

		@Test
		void shouldExtractNullResponseBodyFromThrowableWithoutBody() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();
			RuntimeException exceptionToThrow = new RuntimeException(EXPECTED_CONNECTION_ERROR);

			Object body = exchangeClient.extractResponseBody(exceptionToThrow);

			assertNull(body);
		}

		@Test
		void shouldExtractNullResponseBodyFromHttpExceptionWithoutBody() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();
			HttpException exceptionToThrow = new HttpException(HttpStatus.BAD_GATEWAY, EXPECTED_CONNECTION_ERROR);

			Object body = exchangeClient.extractResponseBody(exceptionToThrow);

			assertNull(body);
		}

		@Test
		void shouldExtractResponseBodyFromThrowableWithBody() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();
			HttpException exceptionToThrow = new HttpException(HttpStatus.BAD_GATEWAY, EXPECTED_CONNECTION_ERROR, STRING);

			Object body = exchangeClient.extractResponseBody(exceptionToThrow);

			assertThat(body, equalTo(STRING));
		}

		@Test
		void shouldCustomizeHttpExceptionBuilderWithStatusAndBody() throws Exception {
			JavaNetHttpExchangeClient exchangeClient = new JavaNetHttpExchangeClient();
			exchangeClient.close();
			HttpException exceptionToThrow = new HttpException(HttpStatus.BAD_GATEWAY, EXPECTED_CONNECTION_ERROR, STRING);

			HttpException.Builder builder = HttpException.builder();
			exchangeClient.customizeHttpExceptionBuilder(builder, exceptionToThrow);

			HttpException exception = builder.build();

			assertThat(exception.getStatus(), equalTo(HttpStatus.BAD_GATEWAY));
			assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.BAD_GATEWAY, STRING)));
			assertThat(exception.getBody(), equalTo(STRING));
		}
	}
}
