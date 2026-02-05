package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.ApiClient;
import org.apiphany.ApiClientFluentAdapter;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.http.HttpMethod;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link HttpClientFluentAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
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
