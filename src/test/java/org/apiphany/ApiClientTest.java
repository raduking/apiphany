package org.apiphany;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.ContentType;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.meters.BasicMeters;
import org.apiphany.meters.MeterCounter;
import org.apiphany.meters.MeterFactory;
import org.apiphany.meters.MeterTimer;
import org.apiphany.security.AuthenticationType;
import org.apiphany.utils.TestDto;
import org.apiphany.utils.client.DummyApiClient;
import org.apiphany.utils.client.DummyExchangeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;

import io.micrometer.core.instrument.Tags;

/**
 * Test class for {@link ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientTest {

	private static final String APIPHANY = "Apiphany";
	private static final String BASE_URL = "http://localhost";
	private static final String PATH_TEST = "test";
	private static final String PARAM_ID = "id";
	private static final String ID1 = "someTestId1";
	private static final String ID2 = "someTestId2";
	private static final int COUNT1 = 666;
	private static final int COUNT2 = 777;
	private static final String SOME_ERROR_MESSAGE = "someErrorMessage";
	private static final String METRICS_PREFIX = "test.metrics";
	private static final String EXCHANGE_CLIENT_NAME_1 = "ThisIsTheName1";
	private static final String EXCHANGE_CLIENT_NAME_2 = "ThisIsTheName2";

	private static final int HTTP_STATUS_OK = 200;
	private static final int HTTP_STATUS_BAD_REQUEST = 400;

	private static final int RETRY_COUNT = 3;

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldCallExchangeClientOnRetrieve() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.create(expected)
				.status(HTTP_STATUS_OK, HttpStatus::fromCode)
				.exchangeClient(exchangeClient)
				.build();

		doReturn(response).when(exchangeClient).exchange(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		TestDto result = api.client()
				.http()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(expected));
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldCallExchangeClientWithProvidedParametersOnGet() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.create(expected)
				.status(HTTP_STATUS_OK, HttpStatus::fromCode)
				.exchangeClient(exchangeClient)
				.build();

		ArgumentCaptor<?> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);

		doReturn(response).when(exchangeClient).exchange(any(ApiRequest.class));
		doReturn(HttpMethod.GET).when(exchangeClient).get();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		TestDto result = api.client()
				.http()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(expected));

		verify(exchangeClient).exchange(JavaObjects.cast(requestCaptor.capture()));

		ApiClientFluentAdapter request = JavaObjects.cast(requestCaptor.getValue());

		assertThat(request.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST));
		assertThat(request.getUri(), equalTo(URI.create(request.url)));
		assertThat(request.getMethod(), equalTo(HttpMethod.GET));
		assertThat(request.getAuthenticationType(), equalTo(AuthenticationType.OAUTH2));
		assertThat(request.getClassResponseType(), equalTo(TestDto.class));
		assertThat(request.getGenericResponseType(), nullValue());
		assertThat(request.getCharset(), equalTo(StandardCharsets.UTF_8));
		assertThat(request.getParams(), nullValue());
		assertThat(request.getHeaders(), is(anEmptyMap()));
		assertThat(request.getBody(), nullValue());
		assertThat(request.getResponseType(), equalTo(TestDto.class));
		assertFalse(request.isStream());
		assertFalse(request.isUrlEncoded());
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldCallExchangeClientWithProvidedParametersOnGetWithHeadersAndRequestParametersSet() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.create(expected)
				.status(HTTP_STATUS_OK, HttpStatus::fromCode)
				.exchangeClient(exchangeClient)
				.build();

		ArgumentCaptor<?> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);

		Map<String, List<String>> headers = new HashMap<>();
		Headers.addTo(headers, HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON);
		Headers.addTo(headers, HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN);
		Headers.addTo(headers, HttpHeader.USER_AGENT, APIPHANY);

		doReturn(response).when(exchangeClient).exchange(any(ApiRequest.class));
		doReturn(HttpMethod.GET).when(exchangeClient).get();
		doReturn(headers.toString()).when(exchangeClient).getHeadersAsString(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		Map<String, String> params = RequestParameters.of(
				ParameterFunction.parameter(PARAM_ID, ID1));

		TestDto result = api.client()
				.http()
				.get()
				.path(PATH_TEST)
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON)
				.header(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN)
				.header(HttpHeader.USER_AGENT, APIPHANY)
				.params(params)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(expected));

		verify(exchangeClient).exchange(JavaObjects.cast(requestCaptor.capture()));

		ApiClientFluentAdapter request = JavaObjects.cast(requestCaptor.getValue());

		assertThat(request.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST));
		assertThat(request.getUri(), equalTo(URI.create(request.url + RequestParameters.asUrlSuffix(params))));
		assertThat(request.getMethod(), equalTo(HttpMethod.GET));
		assertThat(request.getAuthenticationType(), equalTo(AuthenticationType.OAUTH2));
		assertThat(request.getClassResponseType(), equalTo(TestDto.class));
		assertThat(request.getGenericResponseType(), nullValue());
		assertThat(request.getCharset(), equalTo(StandardCharsets.UTF_8));
		assertThat(request.getBody(), nullValue());
		assertThat(request.getHeaders(), equalTo(headers));
		assertThat(request.getParams(), equalTo(params));
		assertThat(request.getResponseType(), equalTo(TestDto.class));
		assertFalse(request.isStream());
		assertFalse(request.isUrlEncoded());
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldReturnEmptyIfCallExchangeClientWithProvidedParametersReturnsNull() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();
		ApiResponse<TestDto> response = ApiResponse.<TestDto>builder()
				.status(HTTP_STATUS_BAD_REQUEST, HttpStatus::fromCode)
				.exchangeClient(exchangeClient)
				.build();
		doReturn(response).when(exchangeClient).exchange(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		TestDto result = api.client()
				.http()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(TestDto.EMPTY));
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldCallTheCorrectExchangeClientWhenMoreArePresent() {
		HttpExchangeClient exchangeClient1 = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient1).getAuthenticationType();
		TestDto expected1 = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response1 = ApiResponse.create(expected1)
				.status(HTTP_STATUS_OK, HttpStatus::fromCode)
				.exchangeClient(exchangeClient1)
				.build();
		doReturn(response1).when(exchangeClient1).exchange(any(ApiRequest.class));

		HttpExchangeClient exchangeClient2 = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.SSL).when(exchangeClient2).getAuthenticationType();
		TestDto expected2 = TestDto.of(ID2, COUNT2);
		ApiResponse<TestDto> response2 = ApiResponse.create(expected2)
				.status(HTTP_STATUS_OK, HttpStatus::fromCode)
				.exchangeClient(exchangeClient2)
				.build();
		doReturn(response2).when(exchangeClient2).exchange(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, List.of(exchangeClient1, exchangeClient2));

		TestDto result1 = api.client(AuthenticationType.OAUTH2)
				.http()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result1, equalTo(expected1));

		TestDto result2 = api.client(AuthenticationType.SSL)
				.http()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result2, equalTo(expected2));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCallExchangeClientWithTheCorrectParameters() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.create(expected)
				.status(HttpStatus.OK)
				.exchangeClient(exchangeClient)
				.build();

		ArgumentCaptor<ApiRequest<?>> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		doReturn(response).when(exchangeClient).exchange(requestCaptor.capture());
		doReturn(HttpMethod.GET).when(exchangeClient).get();

		TestDto result = api.client()
				.http()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(expected));

		assertTrue(requestCaptor.getValue() instanceof ApiClientFluentAdapter);
		ApiClientFluentAdapter adapter = JavaObjects.cast(requestCaptor.getValue());

		assertThat(adapter.getMethod(), equalTo(HttpMethod.GET));
		assertThat(adapter.getClassResponseType(), equalTo(TestDto.class));
		assertThat(adapter.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST));
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldReturnEmptyIfCallExchangeClientWithProvidedParametersThrowsException() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		var e = new RuntimeException(SOME_ERROR_MESSAGE);
		doThrow(e).when(exchangeClient).exchange(any(ApiRequest.class));

		TestDto result = api.client()
				.http()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(TestDto.EMPTY));
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldReturnCorrectApiResponseIfCallExchangeClientWithProvidedParametersThrowsException() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		var e = new RuntimeException(SOME_ERROR_MESSAGE);
		doThrow(e).when(exchangeClient).exchange(any(ApiRequest.class));

		ApiResponse<TestDto> result = api.client()
				.http()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class);

		assertThat(result.getBody(), nullValue());
		assertThat(result.getException(), equalTo(e));
		assertThat(result.getErrorMessage(), equalTo("Exchange error: " + SOME_ERROR_MESSAGE));
		assertThat(Fields.IgnoreAccess.get(result, "exchangeClient"), equalTo(exchangeClient));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldSetMetricsToThisMethod() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST)
				.metersOnMethod(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".should-set-metrics-to-this-method.";

		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
		assertThat(adapter.getMeters().requests().getName(), equalTo(metricStart + BasicMeters.Name.REQUEST));
		assertThat(adapter.getMeters().errors().getName(), equalTo(metricStart + BasicMeters.Name.ERROR));
		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldSetMetricsToThisMethodWithTags() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST)
				.metersOnMethod(METRICS_PREFIX, Tags.empty());

		String metricStart = METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags.";

		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
		assertThat(adapter.getMeters().requests().getName(), equalTo(metricStart + BasicMeters.Name.REQUEST));
		assertThat(adapter.getMeters().errors().getName(), equalTo(metricStart + BasicMeters.Name.ERROR));
		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldSetMetricsWithoutMethod() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST)
				.meters(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".";

		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
		assertThat(adapter.getMeters().requests().getName(), equalTo(metricStart + BasicMeters.Name.REQUEST));
		assertThat(adapter.getMeters().errors().getName(), equalTo(metricStart + BasicMeters.Name.ERROR));
		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldUseDefaultMetricsIfNoMetricsAreSetAndMetricsAreDisabled() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(false);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST);

		assertThat(adapter.getMeters(), nullValue());

		assertThat(api.getActiveMeters(adapter).latency().getName(), equalTo(BasicMeters.Name.LATENCY));
		assertThat(api.getActiveMeters(adapter).requests().getName(), equalTo(BasicMeters.Name.REQUEST));
		assertThat(api.getActiveMeters(adapter).errors().getName(), equalTo(BasicMeters.Name.ERROR));
		assertThat(api.getActiveMeters(adapter).latency().getName(), equalTo(BasicMeters.Name.LATENCY));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldUseDefaultMetricsEvenIfNoMetricsAreSetButMetricsAreDisabled() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(false);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST)
				.meters(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".";

		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
		assertThat(adapter.getMeters().requests().getName(), equalTo(metricStart + BasicMeters.Name.REQUEST));
		assertThat(adapter.getMeters().errors().getName(), equalTo(metricStart + BasicMeters.Name.ERROR));
		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));

		assertThat(api.getActiveMeters(adapter).latency().getName(), equalTo(BasicMeters.Name.LATENCY));
		assertThat(api.getActiveMeters(adapter).requests().getName(), equalTo(BasicMeters.Name.REQUEST));
		assertThat(api.getActiveMeters(adapter).errors().getName(), equalTo(BasicMeters.Name.ERROR));
		assertThat(api.getActiveMeters(adapter).latency().getName(), equalTo(BasicMeters.Name.LATENCY));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldSetTheMeters() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();
		BasicMeters basicMeters = mock(BasicMeters.class);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMeters(basicMeters);

		BasicMeters result = api.getMeters();

		assertThat(result, sameInstance(basicMeters));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldSetTheMeterRegistry() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();
		MeterFactory meterFactory = mock(MeterFactory.class);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMeterFactory(meterFactory);

		MeterFactory result = api.getMeterFactory();

		assertThat(result, sameInstance(meterFactory));
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldThrowExceptionIfCallExchangeClientWithProvidedParametersThrowsExceptionAndIsBleedExceptionsIsTrue() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		var e = new RuntimeException(SOME_ERROR_MESSAGE);
		doThrow(e).when(exchangeClient).exchange(any(ApiRequest.class));
		api.setBleedExceptions(true);

		Exception result = null;
		try {
			api.client()
					.http()
					.get()
					.path(PATH_TEST)
					.retrieve(TestDto.class)
					.orDefault(TestDto.EMPTY);
		} catch (Exception ex) {
			result = ex;
		}

		assertThat(result, notNullValue());
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldThrowExceptionWhenMoreExchangeClientsArePresent() {
		ExchangeClient exchangeClient1 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient1).getAuthenticationType();
		TestDto expected1 = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response1 = ApiResponse.create(expected1)
				.status(HttpStatus.OK)
				.exchangeClient(exchangeClient1)
				.build();
		doReturn(response1).when(exchangeClient1).exchange(any(ApiRequest.class));

		ExchangeClient exchangeClient2 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.SSL).when(exchangeClient2).getAuthenticationType();
		TestDto expected2 = TestDto.of(ID2, COUNT2);
		ApiResponse<TestDto> response2 = ApiResponse.create(expected2)
				.status(HttpStatus.OK)
				.exchangeClient(exchangeClient2)
				.build();
		doReturn(response2).when(exchangeClient2).exchange(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, List.of(exchangeClient1, exchangeClient2));

		Exception result = null;
		try {
			api.client()
					.http()
					.get()
					.path(PATH_TEST)
					.retrieve(TestDto.class)
					.orDefault(TestDto.EMPTY);
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("Client has multiple ExchangeClient objects please call client(AuthenticationType)"));
		}

		assertThat(result, notNullValue());
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldThrowExceptionWhenCreatingClientWithMoreExchangeClientsWithTheSameType() {
		ExchangeClient exchangeClient1 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient1).getAuthenticationType();
		doReturn(EXCHANGE_CLIENT_NAME_1).when(exchangeClient1).getName();
		TestDto expected1 = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response1 = ApiResponse.create(expected1)
				.status(HttpStatus.OK)
				.exchangeClient(exchangeClient1)
				.build();
		doReturn(response1).when(exchangeClient1).exchange(any(ApiRequest.class));

		ExchangeClient exchangeClient2 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient2).getAuthenticationType();
		doReturn(EXCHANGE_CLIENT_NAME_2).when(exchangeClient2).getName();
		TestDto expected2 = TestDto.of(ID2, COUNT2);
		ApiResponse<TestDto> response2 = ApiResponse.create(expected2)
				.status(HttpStatus.OK)
				.exchangeClient(exchangeClient2)
				.build();
		doReturn(response2).when(exchangeClient2).exchange(any(ApiRequest.class));

		Exception result = null;
		try {
			ApiClient.of(BASE_URL, List.of(exchangeClient1, exchangeClient2));
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("Failed to instantiate "
					+ "[" + ApiClient.class + "]: "
					+ "For authentication type "
					+ AuthenticationType.OAUTH2
					+ ", "
					+ EXCHANGE_CLIENT_NAME_1
					+ " already exists"));
		}

		assertThat(result, notNullValue());
	}

	@Test
	void shouldThrowExceptionWhenCreatingClientNoExchangeClients() throws Exception {
		Exception result = null;
		try (ApiClient apiClient = ApiClient.of(BASE_URL, List.of())) {
			apiClient.client();
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("No ExchangeClient has been set before calling client()"));
		}

		assertThat(result, notNullValue());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldThrowExceptionWhenTryingToReturnAuthClientWithNoAuthClientsSet() throws Exception {
		Exception result = null;
		try (ApiClient api = ApiClient.of(BASE_URL, List.of())) {
			api.getExchangeClient(AuthenticationType.OAUTH2);
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("No ExchangeClient found for authentication type: " + AuthenticationType.OAUTH2));
		}
		assertThat(result, notNullValue());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldMakeGetCallWithTheCorrectUri() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.create(expected)
				.status(HttpStatus.OK)
				.exchangeClient(exchangeClient)
				.build();

		ArgumentCaptor<ApiRequest<?>> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
		doReturn(response).when(exchangeClient).exchange(requestCaptor.capture());

		DummyApiClient api = new DummyApiClient(BASE_URL, exchangeClient);

		TestDto result = api.getTest(PATH_TEST, PATH_TEST);

		assertTrue(requestCaptor.getValue() instanceof ApiClientFluentAdapter);
		ApiClientFluentAdapter adapter = JavaObjects.cast(requestCaptor.getValue());

		assertThat(result, equalTo(expected));
		assertThat(adapter.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST + "/" + PATH_TEST));
	}

	@Test
	void shouldInitializeParameterizedTypeReferences() throws Exception {
		try (@SuppressWarnings("resource")
		DummyApiClient apiClient = new DummyApiClient(BASE_URL, new DummyExchangeClient())) {
			assertThat(apiClient.getBaseUrl(), equalTo(BASE_URL));
		}

		Type type1 = DummyApiClient.LIST_TYPE_1.getType();

		assertThat(type1.toString(), equalTo("java.util.List<java.lang.String>"));

		Type type2 = DummyApiClient.LIST_TYPE_2.getType();

		assertThat(type2.toString(), equalTo("java.util.List<java.lang.String>"));

		Type type3 = DummyApiClient.LIST_TYPE_3.getType();

		assertThat(type3.toString(), equalTo("java.util.List<java.util.Map<java.lang.String, java.lang.Object>>"));
	}

	@Test
	void shouldThrowExceptionIfTypeObjectIsInitializedWithANonGenericType() {
		Supplier<ApiClient> clientInstanceSupplier = () -> new ApiClient(BASE_URL, new DummyExchangeClient()) {
			@SuppressWarnings("unused")
			public static final GenericClass<String> WRONG_TYPE = ApiClient.typeObject();
		};
		IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, clientInstanceSupplier::get);
		assertThat(iae.getMessage(), notNullValue());
	}

	@Test
	void shouldReturnNonNullTypeFieldInGenericClass() {
		Field field = Fields.getOneDeclaredInHierarchy(GenericClass.class, "type");

		assertThat(field, notNullValue());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldSetTheRetry() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();
		Retry retry = mock(Retry.class);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setRetry(retry);

		Retry result = api.getRetry();

		assertThat(result, sameInstance(retry));
	}

	static class SomeExchangeClient implements ExchangeClient {

		final ClientProperties clientProperties;
		boolean closed = false;

		public SomeExchangeClient(final ClientProperties clientProperties) {
			this.clientProperties = clientProperties;
		}

		@Override
		public void close() throws Exception {
			this.closed = true;
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		public boolean isClosed() {
			return closed;
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnManagedExchangeClients() throws Exception {
		ClientProperties clientProperties = new ClientProperties();

		ApiClient api = ApiClient.of(BASE_URL, ExchangeClient.builder()
				.client(SomeExchangeClient.class)
				.properties(clientProperties));

		api.close();

		SomeExchangeClient exchangeClient = JavaObjects.cast(api.getExchangeClient(AuthenticationType.NONE));

		assertTrue(exchangeClient.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnManagedExchangeClientsBuiltWithApiClientExchangeClientMethod() throws Exception {
		ClientProperties clientProperties = new ClientProperties();

		ApiClient api = ApiClient.of(BASE_URL, ApiClient
				.withClient(SomeExchangeClient.class)
				.properties(clientProperties));

		SomeExchangeClient exchangeClient = JavaObjects.cast(api.getExchangeClient(AuthenticationType.NONE));

		assertFalse(exchangeClient.isClosed());

		api.close();

		assertTrue(exchangeClient.isClosed());
	}

	@Test
	void shouldNotCallCloseOnNonManagedExchangeClients() throws Exception {
		ClientProperties clientProperties = new ClientProperties();

		SomeExchangeClient exchangeClient = new SomeExchangeClient(clientProperties);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		api.close();

		try {
			assertFalse(exchangeClient.isClosed());
		} finally {
			exchangeClient.close();
		}
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldSetMetricsOnExchangeWhenThereAreNoExceptions() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		MeterFactory meterFactory = mock(MeterFactory.class);
		MeterTimer latency = mock(MeterTimer.class);
		MeterCounter requests = mock(MeterCounter.class);
		MeterCounter errors = mock(MeterCounter.class);
		MeterCounter retries = mock(MeterCounter.class);
		doReturn(latency).when(meterFactory).timer(eq(METRICS_PREFIX), eq(BasicMeters.Name.LATENCY), any(List.class));
		doReturn(requests).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.REQUEST), any(List.class));
		doReturn(errors).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.ERROR), any(List.class));
		doReturn(retries).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.RETRY), any(List.class));

		BasicMeters meters = BasicMeters.of(meterFactory, METRICS_PREFIX);

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(true);
		api.setMeters(meters);

		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofMillis(10)));
		api.setRetry(retry);

		ApiRequest<?> request = mock(ApiRequest.class);
		doReturn(AuthenticationType.OAUTH2).when(request).getAuthenticationType();

		ApiResponse<?> response = mock(ApiResponse.class);
		doReturn(false).when(response).isSuccessful();
		doReturn(response).when(exchangeClient).exchange(request);

		ApiResponse<?> result = api.exchange(request);

		assertThat(result, sameInstance(response));

		verify(retries, times(RETRY_COUNT)).increment();
		verify(requests, times(RETRY_COUNT)).increment();
		verify(latency, times(RETRY_COUNT)).record(any(Duration.class));
		verifyNoInteractions(errors);
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldSetMetricsOnExchangeWhenThereAreExceptions() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		MeterFactory meterFactory = mock(MeterFactory.class);
		MeterTimer latency = mock(MeterTimer.class);
		MeterCounter requests = mock(MeterCounter.class);
		MeterCounter errors = mock(MeterCounter.class);
		MeterCounter retries = mock(MeterCounter.class);
		doReturn(latency).when(meterFactory).timer(eq(METRICS_PREFIX), eq(BasicMeters.Name.LATENCY), any(List.class));
		doReturn(requests).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.REQUEST), any(List.class));
		doReturn(errors).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.ERROR), any(List.class));
		doReturn(retries).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.RETRY), any(List.class));

		BasicMeters meters = BasicMeters.of(meterFactory, METRICS_PREFIX);

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(true);
		api.setMeters(meters);

		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofMillis(10)));
		api.setRetry(retry);

		ApiRequest<?> request = mock(ApiRequest.class);
		doReturn(AuthenticationType.OAUTH2).when(request).getAuthenticationType();

		RuntimeException exception = new RuntimeException(SOME_ERROR_MESSAGE);
		doThrow(exception).when(exchangeClient).exchange(request);

		ApiResponse<?> result = api.exchange(request);

		assertThat(result.getException(), sameInstance(exception));

		verify(retries, times(RETRY_COUNT)).increment();
		verify(requests, times(RETRY_COUNT)).increment();
		verify(latency, times(RETRY_COUNT)).record(any(Duration.class));
		verify(errors, times(RETRY_COUNT)).increment();
	}

	static class BadApiClient extends ApiClient {

		public static final GenericClass<String> WRONG_TYPE = ApiClient.typeObject();

		public BadApiClient(final String baseUrl, final ExchangeClient exchangeClient) {
			super(baseUrl, exchangeClient);
		}

	}

	@Test
	void shouldThrowExceptionIfGenericClassIsNotParameterized() throws Exception {
		DummyExchangeClient exchangeClient = assertDoesNotThrow(DummyExchangeClient::new);

		Executable executable = () -> new BadApiClient(BASE_URL, exchangeClient);
		IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, executable);

		exchangeClient.close();

		Field typeObjectField = Fields.getOneDeclared(BadApiClient.class, "WRONG_TYPE");

		assertThat(iae.getMessage(), equalTo("The typeObject method should only be used for generic types, current type: "
				+ String.class.getTypeName() + " is not a generic type for static field: "
				+ typeObjectField.getName()));
	}

	@Test
	void shouldBuildApiClientWithUrlWithDefaultExchangeClient() throws Exception {
		try (ApiClient apiClient = new ApiClient(BASE_URL)) {
			@SuppressWarnings("resource")
			ExchangeClient exchangeClient = apiClient.getExchangeClient(AuthenticationType.NONE);

			assertThat(apiClient.getBaseUrl(), equalTo(BASE_URL));
			assertThat(exchangeClient, notNullValue());
			assertThat(exchangeClient.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(exchangeClient.getClass(), equalTo(JavaNetHttpExchangeClient.class));
		}
	}
}
