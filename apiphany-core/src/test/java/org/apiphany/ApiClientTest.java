package org.apiphany;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.retry.Retry;
import org.apiphany.security.AuthenticationType;
import org.apiphany.utils.TestDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.morphix.lang.JavaObjects;

/**
 * Test class for {@link ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientTest {

	private static final String BASE_URL = "https://localhost";
	private static final String DIFFERENT_BASE_URL = "https://different-base-url.com";

	private static final String PATH_TEST = "test";

	private static final String ID1 = "someTestId1";
	private static final String ID2 = "someTestId2";

	private static final int COUNT1 = 666;
	private static final int COUNT2 = 777;

	private static final String SOME_ERROR_MESSAGE = "someErrorMessage";

	private static final String EXCHANGE_CLIENT_NAME_1 = "ThisIsTheName1";
	private static final String EXCHANGE_CLIENT_NAME_2 = "ThisIsTheName2";

	private static final int HTTP_STATUS_OK = 200;
	private static final int HTTP_STATUS_BAD_REQUEST = 400;

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
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

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldReturnDefaultIfCallExchangeClientWithProvidedParametersReturnsNull() {
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

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
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

	@Test
	@SuppressWarnings("resource")
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

		assertInstanceOf(ApiClientFluentAdapter.class, requestCaptor.getValue());
		ApiClientFluentAdapter adapter = JavaObjects.cast(requestCaptor.getValue());

		assertThat(adapter.getMethod(), equalTo(HttpMethod.GET));
		assertThat(adapter.getClassResponseType(), equalTo(TestDto.class));
		assertThat(adapter.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallExchangeClientWithBaseUrlFromExchangeClientPropertiesEvenIfBaseUrlIsSet() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setBaseUrl(DIFFERENT_BASE_URL);
		doReturn(clientProperties).when(exchangeClient).getClientProperties();

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

		assertInstanceOf(ApiClientFluentAdapter.class, requestCaptor.getValue());
		ApiClientFluentAdapter adapter = JavaObjects.cast(requestCaptor.getValue());

		assertThat(adapter.getMethod(), equalTo(HttpMethod.GET));
		assertThat(adapter.getClassResponseType(), equalTo(TestDto.class));
		assertThat(adapter.getUrl(), equalTo(DIFFERENT_BASE_URL + "/" + PATH_TEST));
	}

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldReturnEmptyOnExchangeClientExchangeWithProvidedParametersThrowsException() {
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

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldReturnCorrectApiResponseOnExchangeClientExchangeWithProvidedParametersThrowsException() {
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
		assertThat(result.getExchangeClient(), equalTo(exchangeClient));
	}

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldThrowExceptionOnExchangeClientExchangeWithProvidedParametersThrowsExceptionAndBleedExceptionsIsTrue() {
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

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldThrowExceptionOnCallingClientWihoutParametersWhenMoreExchangeClientsArePresent() {
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

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldThrowExceptionWhenCreatingClientWithExchangeClientsWithDuplicateType() {
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
					+ "[" + ApiClient.class.getName() + "]. "
					+ "Client entry for authentication type: ["
					+ AuthenticationType.OAUTH2
					+ ":"
					+ EXCHANGE_CLIENT_NAME_1
					+ "] already exists when trying to add client: ["
					+ EXCHANGE_CLIENT_NAME_2 + "]"));
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
			assertThat(result.getMessage(), equalTo("At least one: " + ExchangeClient.class.getName()
					+ " must be provided to instantiate: " + ApiClient.class.getName()));
		}

		assertThat(result, notNullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowExceptionWhenTryingToAddExchangeClientWithNoAuthenticationType() throws Exception {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(EXCHANGE_CLIENT_NAME_1).when(exchangeClient).getName();
		Exception result = null;
		try (ApiClient api = ApiClient.of(BASE_URL, List.of(exchangeClient))) {
			api.getExchangeClient(AuthenticationType.OAUTH2);
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("ExchangeClient: [" + EXCHANGE_CLIENT_NAME_1 + "]"
					+ " has no " + AuthenticationType.class.getSimpleName() + " set"));
		}
		assertThat(result, notNullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowExceptionWhenTryingToReturnAuthClientWithNoRequiredAuthClientsSet() throws Exception {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.SSL).when(exchangeClient).getAuthenticationType();
		doReturn(EXCHANGE_CLIENT_NAME_1).when(exchangeClient).getName();
		Exception result = null;
		try (ApiClient api = ApiClient.of(BASE_URL, List.of(exchangeClient))) {
			api.getExchangeClient(AuthenticationType.OAUTH2);
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("No ExchangeClient found for authentication type: " + AuthenticationType.OAUTH2));
		}
		assertThat(result, notNullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetTheRetry() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();
		Retry retry = mock(Retry.class);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setRetry(retry);

		Retry result = api.getRetry();

		assertThat(result, sameInstance(retry));
	}

	@Test
	void shouldBuildApiClientWithUrlUsingDefaultExchangeClient() throws Exception {
		try (ApiClient apiClient = new ApiClient(BASE_URL)) {
			@SuppressWarnings("resource")
			ExchangeClient exchangeClient = apiClient.getExchangeClient(AuthenticationType.NONE);

			assertThat(apiClient.getBaseUrl(), equalTo(BASE_URL));
			assertThat(exchangeClient, notNullValue());
			assertThat(exchangeClient.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(exchangeClient.getClass(), equalTo(JavaNetHttpExchangeClient.class));
			assertThat(exchangeClient.getClientProperties(), equalTo(ClientProperties.defaults()));
		}
	}

	@Test
	void shouldBuildApiClientWithUrlAndClientPropertiesUsingDefaultExchangeClient() throws Exception {
		try (ApiClient apiClient = new ApiClient(BASE_URL, ApiClient.with(new ClientProperties()))) {
			@SuppressWarnings("resource")
			ExchangeClient exchangeClient = apiClient.getExchangeClient(AuthenticationType.NONE);

			assertThat(apiClient.getBaseUrl(), equalTo(BASE_URL));
			assertThat(exchangeClient, notNullValue());
			assertThat(exchangeClient.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(exchangeClient.getClass(), equalTo(JavaNetHttpExchangeClient.class));
			assertThat(exchangeClient.getClientProperties(), notNullValue());
		}
	}

	@Test
	void shouldBuildApiClientWithEmptyBaseUrlUsingDefaultExchangeClientWithFactoryMethod() throws Exception {
		try (ApiClient apiClient = ApiClient.of()) {
			@SuppressWarnings("resource")
			ExchangeClient exchangeClient = apiClient.getExchangeClient(AuthenticationType.NONE);

			assertThat(apiClient.getBaseUrl(), equalTo(ApiClient.EMPTY_BASE_URL));
			assertThat(exchangeClient, notNullValue());
			assertThat(exchangeClient.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(exchangeClient.getClass(), equalTo(JavaNetHttpExchangeClient.class));
			assertThat(exchangeClient.getClientProperties(), equalTo(ClientProperties.defaults()));
		}
	}

	@Test
	void shouldThrowExceptionWhenBuildingApiClientWithEmptyExchangeClientResourceList() throws Exception {
		IllegalStateException result = null;
		try (ApiClient apiClient = new ApiClient(List.of())) {
			// empty
		} catch (IllegalStateException e) {
			result = e;
			assertThat(result.getMessage(), equalTo("At least one: " + ExchangeClient.class.getName()
					+ " must be provided to instantiate: " + ApiClient.class.getName()));
		}

		assertThat(result, notNullValue());
	}
}
