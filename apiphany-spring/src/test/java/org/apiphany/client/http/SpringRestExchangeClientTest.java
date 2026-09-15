package org.apiphany.client.http;

import static org.apiphany.http.HttpMethod.GET;
import static org.apiphany.http.HttpMethod.POST;
import static org.apiphany.http.HttpMethod.PUT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import org.apiphany.ApiRequest;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.SpringHttpSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.reflection.Fields;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ExchangeFunction;

/**
 * Tests for {@link SpringRestExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class SpringRestExchangeClientTest {

	@Nested
	class ConstructorTests {

		@Test
		@SuppressWarnings("resource")
		void shouldBuildWithDefaultConstructor() throws Exception {
			try (SpringRestExchangeClient client = new SpringRestExchangeClient()) {
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getClientProperties(), notNullValue());
				assertThat(client.getRequestFactory(), notNullValue());
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldBuildWithClientProperties() throws Exception {
			ClientProperties properties = ClientProperties.defaults();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties)) {
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getClientProperties(), notNullValue());
				assertThat(client.getRequestFactory(), notNullValue());
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldKeepConfiguredRequestFactoryOnRestClientBuilder() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			ClientHttpRequestFactory requestFactory = mock(ClientHttpRequestFactory.class);
			RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties, builder)) {
				assertThat(SpringHttpSupport.getRequestFactory(builder), equalTo(requestFactory));
				assertThat(Fields.IgnoreAccess.get(client.getRequestFactory(), "delegate"), equalTo(requestFactory));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldSetDetectedRequestFactoryWhenRestClientBuilderHasNone() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			RestClient.Builder builder = RestClient.builder();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties, builder)) {
				assertThat(SpringHttpSupport.getRequestFactory(builder), equalTo(client.getRequestFactory()));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldBuildWithCustomRestClient() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			RestClient customClient = RestClient.builder().build();

			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties, customClient)) {
				assertThat(client.getClientProperties(), notNullValue());
				assertThat(client.getMessageConverters(), notNullValue());
				assertThat(client.getRequestFactory(), notNullValue());
			}
		}
	}

	@Nested
	class MessageConvertersTests {

		@Test
		void shouldReturnDefaultMessageConverters() throws Exception {
			ClientProperties properties = ClientProperties.defaults();
			try (SpringRestExchangeClient client = new SpringRestExchangeClient(properties)) {
				List<?> converters = client.getMessageConverters();

				assertThat(converters.size(), equalTo(3));
				assertThat(converters.get(0), instanceOf(ByteArrayHttpMessageConverter.class));
				assertThat(converters.get(1), instanceOf(StringHttpMessageConverter.class));
				assertThat(converters.get(2), instanceOf(ResourceHttpMessageConverter.class));
			}
		}
	}

	@Nested
	class SendRequestTests {

		private static final String TEST_URL = "https://api.example.com";
		private static final String REQUEST_BODY = "request body";
		private static final String RESPONSE_BODY = "response body";

		private SpringRestExchangeClient client;
		private RestClient restClient;
		private RequestBodyUriSpec requestBodyUriSpec;
		private RequestBodySpec requestBodySpec;
		private HttpRequest request;
		private ConvertibleClientHttpResponse response;

		@BeforeEach
		void setUp() {
			client = new SpringRestExchangeClient(ClientProperties.defaults());
			restClient = mock(RestClient.class);
			Fields.IgnoreAccess.set(client, "restClient", restClient);
			requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
			requestBodySpec = mock(RestClient.RequestBodySpec.class);
			request = mock(HttpRequest.class);
			response = mock(ConvertibleClientHttpResponse.class);
		}

		@AfterEach
		void tearDown() throws Exception {
			client.close();
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldSendRequestWithBody() throws Exception {
			doReturn(HttpStatus.OK).when(response).getStatusCode();
			doReturn(new HttpHeaders()).when(response).getHeaders();
			doReturn(new ByteArrayInputStream(RESPONSE_BODY.getBytes())).when(response).getBody();
			stubRequestChain(HttpMethod.POST, TEST_URL);
			stubBody();
			HttpEntity<String> httpEntity = new HttpEntity<>(REQUEST_BODY);
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(POST).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			doReturn(false).when(apiRequest).isStream();
			stubExchange();

			ResponseEntity<byte[]> result = client.sendRequest(apiRequest, httpEntity);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			assertThat(new String(result.getBody()), equalTo(RESPONSE_BODY));
			verify(requestBodySpec).body(REQUEST_BODY);
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldSendRequestWithoutBody() throws Exception {
			doReturn(HttpStatus.OK).when(response).getStatusCode();
			doReturn(new HttpHeaders()).when(response).getHeaders();
			doReturn(new ByteArrayInputStream(new byte[0])).when(response).getBody();
			stubRequestChain(HttpMethod.GET, TEST_URL);
			HttpEntity<String> httpEntity = new HttpEntity<>(null);
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(GET).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			doReturn(false).when(apiRequest).isStream();
			stubExchange();

			ResponseEntity<byte[]> result = client.sendRequest(apiRequest, httpEntity);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			verify(requestBodySpec, never()).body(any());
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldSendRequestWithHeaders() {
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set("X-Custom", "value");
			HttpEntity<String> httpEntity = new HttpEntity<>(REQUEST_BODY, requestHeaders);
			stubRequestChain(HttpMethod.PUT, TEST_URL);
			stubBody();
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(PUT).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);
			doReturn(ResponseEntity.ok().build()).when(requestBodySpec).exchange(any(ExchangeFunction.class));

			ResponseEntity<byte[]> result = client.sendRequest(apiRequest, httpEntity);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			verify(requestBodySpec).headers(headersCaptor.capture());
			Consumer<HttpHeaders> headersConsumer = headersCaptor.getValue();
			HttpHeaders capturedHeaders = new HttpHeaders();
			headersConsumer.accept(capturedHeaders);
			assertThat(capturedHeaders.get("X-Custom"), equalTo(List.of("value")));
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldSendRequestWithInputStreamResponse() throws Exception {
			doReturn(HttpStatus.OK).when(response).getStatusCode();
			doReturn(new HttpHeaders()).when(response).getHeaders();
			doReturn(new ByteArrayInputStream(RESPONSE_BODY.getBytes())).when(response).getBody();
			stubRequestChain(HttpMethod.GET, TEST_URL);
			HttpEntity<String> httpEntity = new HttpEntity<>(null);
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(GET).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			doReturn(true).when(apiRequest).isStream();
			stubExchange();

			ResponseEntity<InputStream> result = client.sendRequest(apiRequest, httpEntity);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			assertThat(result.getBody(), instanceOf(InputStream.class));
		}

		@Test
		@SuppressWarnings({ "unchecked" })
		void shouldThrowWhenExchangeFails() {
			Error error = new OutOfMemoryError("exchange failed");
			stubRequestChain(HttpMethod.GET, TEST_URL);
			doThrow(error).when(requestBodySpec).exchange(any(ExchangeFunction.class));
			HttpEntity<String> httpEntity = new HttpEntity<>(null);
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(GET).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();

			Error thrown = assertThrows(Error.class, () -> client.sendRequest(apiRequest, httpEntity));

			assertThat(thrown, sameInstance(error));
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldReturnEmptyBodyForEmptyResponse() throws Exception {
			doReturn(HttpStatus.OK).when(response).getStatusCode();
			doReturn(new HttpHeaders()).when(response).getHeaders();
			doReturn(new ByteArrayInputStream(new byte[0])).when(response).getBody();
			stubRequestChain(HttpMethod.GET, TEST_URL);
			HttpEntity<String> httpEntity = new HttpEntity<>(null);
			ApiRequest<String> apiRequest = mock(ApiRequest.class);
			doReturn(GET).when(apiRequest).getMethod();
			doReturn(URI.create(TEST_URL)).when(apiRequest).getUri();
			doReturn(false).when(apiRequest).isStream();
			stubExchange();

			ResponseEntity<byte[]> result = client.sendRequest(apiRequest, httpEntity);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			assertThat(result.getBody().length, equalTo(0));
		}

		private void stubRequestChain(final HttpMethod httpMethod, final String uri) {
			doReturn(requestBodyUriSpec).when(restClient).method(httpMethod);
			doReturn(requestBodySpec).when(requestBodyUriSpec).uri(URI.create(uri));
			doReturn(requestBodySpec).when(requestBodySpec).headers(any());
		}

		private void stubBody() {
			doReturn(requestBodySpec).when(requestBodySpec).body(any(String.class));
		}

		@SuppressWarnings({ "unchecked" })
		private void stubExchange() {
			doAnswer(invocation -> {
				ExchangeFunction<ResponseEntity<byte[]>> exchangeFunction = invocation.getArgument(0);
				return exchangeFunction.exchange(request, response);
			}).when(requestBodySpec).exchange(any(ExchangeFunction.class));
		}
	}
}
