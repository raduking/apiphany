package org.apiphany;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apiphany.client.ExchangeClient;
import org.apiphany.http.HttpMethod;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.meters.BasicMeters;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Test class for {@link ApiClientFluentAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientFluentAdapterTest {

	private static final String URL = "http://localhost";
	private static final Map<String, String> PARAMS = RequestParameters.of(ParameterFunction.parameter("name", "value"));
	private static final Map<String, List<String>> HEADERS = Map.of("headerName", List.of("headerValue"));
	private static final String BODY = "SomeBody";
	private static final BasicMeters METERS = BasicMeters.of("some.meters");
	private static final Retry RETRY = Retry.of(WaitCounter.of(2, Duration.ofMillis(1)));

	@Test
	void shouldPopulateAllApiRequestFieldsWhenBuildingWithAnApiRequest() {
		@SuppressWarnings("resource")
		ApiClient apiClient = mock(ApiClient.class);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.url(URL)
				.method(HttpMethod.OPTIONS)
				.params(PARAMS)
				.headers(HEADERS)
				.body(BODY)
				.charset(StandardCharsets.US_ASCII)
				.urlEncoded()
				.stream()
				.meters(METERS)
				.retry(RETRY);

		ApiClientFluentAdapter result = ApiClientFluentAdapter.of(apiClient)
				.apiRequest(request);

		assertThat(result.getUrl(), equalTo(URL));
		assertThat(result.getMethod(), equalTo(HttpMethod.OPTIONS));
		assertThat(result.getParams(), equalTo(PARAMS));
		assertThat(result.getHeaders(), equalTo(HEADERS));
		assertThat(result.getBody(), equalTo(BODY));
		assertThat(result.getCharset(), equalTo(StandardCharsets.US_ASCII));
		assertTrue(result.isUrlEncoded());
		assertTrue(result.isStream());
		assertThat(result.getMeters(), equalTo(METERS));
		assertThat(result.getRetry(), equalTo(RETRY));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetTheExchangeClientIfMissingWhenSettingAuthenticationType() {
		ApiClient apiClient = mock(ApiClient.class);
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION);

		assertThat(request.getExchangeClient(ExchangeClient.class), notNullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallApiClientExchangeOnRetrieve() {
		ApiClient apiClient = mock(ApiClient.class);
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION);

		request.retrieve();

		verify(apiClient).exchange(request);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldEncodeParamsOnRetrieveWhenEncodingIsEnabled() {
		ApiClient apiClient = mock(ApiClient.class);
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		var params = RequestParameters.of(ParameterFunction.parameter("sum", "1+2+3"));

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION)
				.url(URL)
				.params(params)
				.urlEncoded();

		request.retrieve();

		var expected = RequestParameters.of(ParameterFunction.parameter("sum", "1%2B2%2B3"));

		assertThat(request.getParams(), equalTo(expected));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetTheResponseTypeOnRetrieveWithClass() {
		ApiClient apiClient = mock(ApiClient.class);
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION);

		request.retrieve(String.class);

		assertThat(request.getClassResponseType(), equalTo(String.class));
		assertFalse(request.hasGenericType());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetTheResponseTypeOnRetrieveWithGenericClass() {
		ApiClient apiClient = mock(ApiClient.class);
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION);

		var genericClass = new GenericClass<List<Integer>>() {
			// empty
		};
		request.retrieve(genericClass);

		assertThat(request.getGenericResponseType(), equalTo(genericClass));
		assertTrue(request.hasGenericType());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetTheStreamToTrueTypeOnDownload() {
		ApiClient apiClient = mock(ApiClient.class);
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION);

		assertFalse(request.isStream());

		request.download();

		assertTrue(request.isStream());
	}

}
