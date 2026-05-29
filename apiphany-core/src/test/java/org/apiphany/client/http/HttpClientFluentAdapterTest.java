package org.apiphany.client.http;

import static org.apiphany.ParameterFunction.parameter;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.ApiClientFluentAdapter;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.RequestParameters;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.io.ContentType;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link HttpClientFluentAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class HttpClientFluentAdapterTest {

	@Test
	void shouldReturnAllRequestMethods() throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		ApiClient apiClient = ApiClient.of(ApiClient.EMPTY_BASE_URL, client);
		apiClient.close();

		ApiClientFluentAdapter api = ApiClientFluentAdapter.of(apiClient).authenticationType(AuthenticationType.NONE);
		HttpClientFluentAdapter httpApi = HttpClientFluentAdapter.of(api);

		api = httpApi.get();
		assertThat(api.getMethod(), equalTo(HttpMethod.GET));

		api = httpApi.put();
		assertThat(api.getMethod(), equalTo(HttpMethod.PUT));

		api = httpApi.post();
		assertThat(api.getMethod(), equalTo(HttpMethod.POST));

		api = httpApi.delete();
		assertThat(api.getMethod(), equalTo(HttpMethod.DELETE));

		api = httpApi.patch();
		assertThat(api.getMethod(), equalTo(HttpMethod.PATCH));

		api = httpApi.head();
		assertThat(api.getMethod(), equalTo(HttpMethod.HEAD));

		api = httpApi.options();
		assertThat(api.getMethod(), equalTo(HttpMethod.OPTIONS));

		api = httpApi.trace();
		assertThat(api.getMethod(), equalTo(HttpMethod.TRACE));

		api = httpApi.connect();
		assertThat(api.getMethod(), equalTo(HttpMethod.CONNECT));

	}

	@ParameterizedTest
	@EnumSource(HttpMethod.class)
	void shouldReturnAllRequestMethods(final HttpMethod method) throws Exception {
		HttpExchangeClient client = new DummyHttpExchangeClient();
		client.close();

		ApiClient apiClient = ApiClient.of(ApiClient.EMPTY_BASE_URL, client);
		apiClient.close();

		ApiClientFluentAdapter api = ApiClientFluentAdapter.of(apiClient).authenticationType(AuthenticationType.NONE);
		HttpClientFluentAdapter httpApi = HttpClientFluentAdapter.of(api);

		api = httpApi.method(method);
		assertThat(api.getMethod(), equalTo(method));
	}

	@Nested
	class FormTests {

		@Mock
		private ApiClient apiClient;

		@Mock
		private HttpExchangeClient httpExchangeClient;

		private ApiClientFluentAdapter request;

		@BeforeEach
		@SuppressWarnings("resource")
		void setUp() {
			doReturn(httpExchangeClient).when(apiClient).getExchangeClient(any(AuthenticationType.class));
			request = ApiClientFluentAdapter.of(apiClient)
					.authenticationType(AuthenticationType.NONE);
		}

		@Test
		void shouldSetFormBodyAndContentTypeHeader() {
			Map<String, List<String>> params = Map.of("key1", List.of("value1"), "key2", List.of("value2"));

			HttpClientFluentAdapter.of(request)
					.form(params);

			String expectedBody = RequestParameters.asString(RequestParameters.encode(params));
			assertThat(request.getBody(), equalTo(expectedBody));
			assertThat(Headers.get(HttpHeader.CONTENT_TYPE, request.getHeaders()).getFirst(),
					equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED));
		}

		@Test
		void shouldSetFormBodyWithParameterFunctions() {
			HttpClientFluentAdapter.of(request)
					.form(
							parameter("name", "value"),
							parameter("key", "val"));

			Map<String, List<String>> params = RequestParameters.of(
					parameter("name", "value"),
					parameter("key", "val"));
			String expectedBody = RequestParameters.asString(RequestParameters.encode(params));
			assertThat(request.getBody(), equalTo(expectedBody));
			assertThat(Headers.get(HttpHeader.CONTENT_TYPE, request.getHeaders()).getFirst(),
					equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED));
		}

		@Test
		void shouldSetFormBodyWithUrlEncoding() {
			Map<String, List<String>> params = new LinkedHashMap<>();
			params.put("name", List.of("hello world"));
			params.put("email", List.of("a@b.com"));

			HttpClientFluentAdapter.of(request)
					.form(params);

			String expectedBody = RequestParameters.asString(RequestParameters.encode(params));
			assertThat(request.getBody(), equalTo(expectedBody));
			assertThat(request.getBody(), equalTo("name=hello+world&email=a%40b.com"));
		}
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
