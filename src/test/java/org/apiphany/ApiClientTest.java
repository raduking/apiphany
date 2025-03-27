package org.apiphany;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

import org.apiphany.auth.AuthenticationType;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.retry.Retry;
import org.apiphany.meters.BasicMeters;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;

import io.micrometer.core.instrument.Tags;

/**
 * Test class for {@link ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientTest {

	private static final String BASE_URL = "http://localhost";
	private static final String PATH_TEST = "test";
	private static final String ID1 = "someTestId1";
	private static final String ID2 = "someTestId2";
	private static final int COUNT1 = 666;
	private static final int COUNT2 = 777;
	private static final String SOME_ERROR_MESSAGE = "someErrorMessage";
	private static final String METRICS_PREFIX = "test.metrics";

	private static final int HTTP_STATUS_OK = 200;
	private static final int HTTP_STATUS_BAD_REQUEST = 400;

	@SuppressWarnings("unchecked")
	@Test
	void shouldCallExchangeClientWithProvidedParameters() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.of(expected, HTTP_STATUS_OK, HttpStatus::from, exchangeClient);
		doReturn(response).when(exchangeClient).exchange(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		TestDto result = api.client()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(expected));
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldReturnEmptyIfCallExchangeClientWithProvidedParametersReturnsNull() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();
		ApiResponse<TestDto> response = ApiResponse.of(null, HTTP_STATUS_BAD_REQUEST, HttpStatus::from, exchangeClient);
		doReturn(response).when(exchangeClient).exchange(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		TestDto result = api.client()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(TestDto.EMPTY));
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldCallTheCorrectAuthClientWhenMoreArePresent() {
		ExchangeClient exchangeClient1 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient1).getAuthenticationType();
		TestDto expected1 = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response1 = ApiResponse.of(expected1, HTTP_STATUS_OK, HttpStatus::from, exchangeClient1);
		doReturn(response1).when(exchangeClient1).exchange(any(ApiRequest.class));

		ExchangeClient exchangeClient2 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.SSL_CERTIFICATE).when(exchangeClient2).getAuthenticationType();
		TestDto expected2 = TestDto.of(ID2, COUNT2);
		ApiResponse<TestDto> response2 = ApiResponse.of(expected2, HTTP_STATUS_OK, HttpStatus::from, exchangeClient2);
		doReturn(response2).when(exchangeClient2).exchange(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, List.of(exchangeClient1, exchangeClient2));

		TestDto result1 = api.client(AuthenticationType.OAUTH2_TOKEN)
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result1, equalTo(expected1));

		TestDto result2 = api.client(AuthenticationType.SSL_CERTIFICATE)
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result2, equalTo(expected2));
	}

	@Test
	void shouldCallAuthClientWithTheCorrectParameters() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.of(expected, HttpStatus.OK, exchangeClient);

		ApiClient api = spy(ApiClient.of(BASE_URL, exchangeClient));
		ApiClientFluentAdapter adapter = ApiClientFluentAdapter.of(api).authenticationType(AuthenticationType.OAUTH2_TOKEN);
		doReturn(adapter).when(api).client();
		doReturn(response).when(exchangeClient).exchange(adapter);
		doReturn(HttpMethod.GET).when(exchangeClient).get();

		TestDto result = api.client()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(expected));

		assertThat(adapter.getMethod(), equalTo(HttpMethod.GET));
		assertThat(adapter.getClassResponseType(), equalTo(TestDto.class));
		assertThat(adapter.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST));
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldReturnEmptyIfCallExchangeClientWithProvidedParametersThrowsException() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		var e = new RuntimeException(SOME_ERROR_MESSAGE);
		doThrow(e).when(exchangeClient).exchange(any(ApiRequest.class));

		TestDto result = api.client()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(TestDto.EMPTY));
	}

	@Test
	void shouldSetMetricsToThisMethod() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.get()
				.path(PATH_TEST)
				.metersOnMethod(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".should-set-metrics-to-this-method.";

		assertThat(adapter.getMeters().latency().getId().getName(), equalTo(metricStart + BasicMeters.LATENCY_METRIC));
		assertThat(adapter.getMeters().requests().getId().getName(), equalTo(metricStart + BasicMeters.REQUEST_METRIC));
		assertThat(adapter.getMeters().errors().getId().getName(), equalTo(metricStart + BasicMeters.ERROR_METRIC));
		assertThat(adapter.getMeters().latency().getId().getName(), equalTo(metricStart + BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldSetMetricsToThisMethodWithTags() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.get()
				.path(PATH_TEST)
				.metersOnMethod(METRICS_PREFIX, Tags.empty());

		String metricStart = METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags.";

		assertThat(adapter.getMeters().latency().getId().getName(), equalTo(metricStart + BasicMeters.LATENCY_METRIC));
		assertThat(adapter.getMeters().requests().getId().getName(), equalTo(metricStart + BasicMeters.REQUEST_METRIC));
		assertThat(adapter.getMeters().errors().getId().getName(), equalTo(metricStart + BasicMeters.ERROR_METRIC));
		assertThat(adapter.getMeters().latency().getId().getName(), equalTo(metricStart + BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldSetMetricsWithoutMethod() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.get()
				.path(PATH_TEST)
				.meters(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".";

		assertThat(adapter.getMeters().latency().getId().getName(), equalTo(metricStart + BasicMeters.LATENCY_METRIC));
		assertThat(adapter.getMeters().requests().getId().getName(), equalTo(metricStart + BasicMeters.REQUEST_METRIC));
		assertThat(adapter.getMeters().errors().getId().getName(), equalTo(metricStart + BasicMeters.ERROR_METRIC));
		assertThat(adapter.getMeters().latency().getId().getName(), equalTo(metricStart + BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldUseDefaultMetricsIfNoMetricsAreSetAndMetricsAreDisabled() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(false);

		ApiClientFluentAdapter adapter = api
				.client()
				.get()
				.path(PATH_TEST);

		assertThat(adapter.getMeters(), nullValue());

		assertThat(api.getActiveMeters(adapter).latency().getId().getName(), equalTo(BasicMeters.LATENCY_METRIC));
		assertThat(api.getActiveMeters(adapter).requests().getId().getName(), equalTo(BasicMeters.REQUEST_METRIC));
		assertThat(api.getActiveMeters(adapter).errors().getId().getName(), equalTo(BasicMeters.ERROR_METRIC));
		assertThat(api.getActiveMeters(adapter).latency().getId().getName(), equalTo(BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldUseDefaultMetricsEvenIfNoMetricsAreSetButMetricsAreDisabled() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(false);

		ApiClientFluentAdapter adapter = api
				.client()
				.get()
				.path(PATH_TEST)
				.meters(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".";

		assertThat(adapter.getMeters().latency().getId().getName(), equalTo(metricStart + BasicMeters.LATENCY_METRIC));
		assertThat(adapter.getMeters().requests().getId().getName(), equalTo(metricStart + BasicMeters.REQUEST_METRIC));
		assertThat(adapter.getMeters().errors().getId().getName(), equalTo(metricStart + BasicMeters.ERROR_METRIC));
		assertThat(adapter.getMeters().latency().getId().getName(), equalTo(metricStart + BasicMeters.LATENCY_METRIC));

		assertThat(api.getActiveMeters(adapter).latency().getId().getName(), equalTo(BasicMeters.LATENCY_METRIC));
		assertThat(api.getActiveMeters(adapter).requests().getId().getName(), equalTo(BasicMeters.REQUEST_METRIC));
		assertThat(api.getActiveMeters(adapter).errors().getId().getName(), equalTo(BasicMeters.ERROR_METRIC));
		assertThat(api.getActiveMeters(adapter).latency().getId().getName(), equalTo(BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldSetTheMeters() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();
		BasicMeters basicMeters = mock(BasicMeters.class);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMeters(basicMeters);

		BasicMeters result = api.getMeters();

		assertThat(result, sameInstance(basicMeters));
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldThrowExceptionIfCallExchangeClientWithProvidedParametersThrowsExceptionAndIsBleedExceptionsIsTrue() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		var e = new RuntimeException(SOME_ERROR_MESSAGE);
		doThrow(e).when(exchangeClient).exchange(any(ApiRequest.class));
		api.setBleedExceptions(true);

		Exception result = null;
		try {
			api.client()
				.get()
				.path(PATH_TEST)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);
		} catch (Exception ex) {
			result = ex;
		}

		assertThat(result, notNullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldThrowExceptionWhenMoreAuthClientsArePresent() {
		ExchangeClient exchangeClient1 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient1).getAuthenticationType();
		TestDto expected1 = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response1 = ApiResponse.of(expected1, HttpStatus.OK, exchangeClient1);
		doReturn(response1).when(exchangeClient1).exchange(any(ApiRequest.class));

		ExchangeClient exchangeClient2 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.SSL_CERTIFICATE).when(exchangeClient2).getAuthenticationType();
		TestDto expected2 = TestDto.of(ID2, COUNT2);
		ApiResponse<TestDto> response2 = ApiResponse.of(expected2, HttpStatus.OK, exchangeClient2);
		doReturn(response2).when(exchangeClient2).exchange(any(ApiRequest.class));

		ApiClient api = ApiClient.of(BASE_URL, List.of(exchangeClient1, exchangeClient2));

		Exception result = null;
		try {
			api.client()
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

	@SuppressWarnings("unchecked")
	@Test
	void shouldThrowExceptionWhenCreatingClientWithMoreAuthClientsWithTheSameType() {
		ExchangeClient exchangeClient1 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient1).getAuthenticationType();
		TestDto expected1 = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response1 = ApiResponse.of(expected1, HttpStatus.OK, exchangeClient1);
		doReturn(response1).when(exchangeClient1).exchange(any(ApiRequest.class));

		ExchangeClient exchangeClient2 = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient2).getAuthenticationType();
		TestDto expected2 = TestDto.of(ID2, COUNT2);
		ApiResponse<TestDto> response2 = ApiResponse.of(expected2, HttpStatus.OK, exchangeClient2);
		doReturn(response2).when(exchangeClient2).exchange(any(ApiRequest.class));

		Exception result = null;
		try {
			ApiClient.of(BASE_URL, List.of(exchangeClient1, exchangeClient2));
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("Failed to instantiate "
					+ "[" + ApiClient.class + "]: "
					+ "More than one "
					+ "interface " + ExchangeClient.class.getCanonicalName() + " "
					+ "with type " + AuthenticationType.OAUTH2_TOKEN + " found."));
		}

		assertThat(result, notNullValue());
	}

	@Test
	void shouldThrowExceptionWhenCreatingClientNoAuthClients() {
		Exception result = null;
		try {
			ApiClient.of(BASE_URL, List.of()).client();
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("No ExchangeClient has been set before calling client()"));
		}

		assertThat(result, notNullValue());
	}

	@Test
	void shouldThrowExceptionWhenTryingToReturnAuthClientWithNoAuthClientsSet() {
		Exception result = null;
		ApiClient api = ApiClient.of(BASE_URL, List.of());
		try {
			api.getExchangeClient(AuthenticationType.OAUTH2_TOKEN);
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("No ExchangeClient found for authentication type: " + AuthenticationType.OAUTH2_TOKEN));
		}

		assertThat(result, notNullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldMakeGetCallWithTheCorrectUri() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.of(expected, HttpStatus.OK, exchangeClient);
		doReturn(response).when(exchangeClient).exchange(any(ApiRequest.class));

		DummyApiClient api = spy(new DummyApiClient(BASE_URL, exchangeClient));
		ApiClientFluentAdapter adapter = ApiClientFluentAdapter.of(api).authenticationType(AuthenticationType.OAUTH2_TOKEN);
		doReturn(adapter).when(api).client();

		TestDto result = api.getTest(PATH_TEST, PATH_TEST);

		assertThat(result, equalTo(expected));
		assertThat(adapter.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST + "/" + PATH_TEST));
	}

	@Test
	void shouldInitializeParameterizedTypeReferences() {
		DummyApiClient apiClient = new DummyApiClient(BASE_URL, new DummyExchangeClient());
		assertThat(apiClient.getBaseUrl(), equalTo(BASE_URL));

		Type type1 = DummyApiClient.LIST_TYPE_1.getType();

		assertThat(type1.toString(), equalTo("java.util.List<java.lang.String>"));

		Type type2 = DummyApiClient.LIST_TYPE_2.getType();

		assertThat(type2.toString(), equalTo("java.util.List<java.lang.String>"));

		Type type3 = DummyApiClient.LIST_TYPE_3.getType();

		assertThat(type3.toString(), equalTo("java.util.List<java.util.Map<java.lang.String, java.lang.Object>>"));
	}

	@Disabled
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
	void shouldReturnNonNullTypeFieldInGenericTypeReference() {
		Field field = Fields.getDeclaredFieldInHierarchy(GenericClass.class, "type");

		assertThat(field, notNullValue());
	}

	@Test
	void shouldSetTheRetry() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2_TOKEN).when(exchangeClient).getAuthenticationType();
		Retry retry = mock(Retry.class);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setRetry(retry);

		Retry result = api.getRetry();

		assertThat(result, sameInstance(retry));
	}

}
