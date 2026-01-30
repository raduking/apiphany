package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.http.HttpStatus;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;
import org.springframework.context.annotation.Description;

/**
 * Test class for {@link ApiClient} generic types.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientGenericTypesTest {

	private static final String BASE_URL = "http://localhost";
	private static final String PATH_TEST = "test";

	private static final String ID1 = "someTestId1";
	private static final String ID2 = "someTestId2";

	@Test
	@Description("Verify that the 'type' field in GenericClass still exists even if library is upgraded")
	void shouldReturnNonNullTypeFieldInGenericClass() {
		Field field = Fields.getOneDeclaredInHierarchy(GenericClass.class, "type");

		assertThat(field, notNullValue());
	}

	static class ApiClientWithGenericTypes extends ApiClient {

		public static final GenericClass<List<String>> LIST_TYPE_1 = ApiClient.typeObject();

		public static final GenericClass<List<String>> LIST_TYPE_2 = new GenericClass<>() {
			// empty
		};

		public static final GenericClass<List<Map<String, Object>>> LIST_TYPE_3 = ApiClient.typeObject();

		public ApiClientWithGenericTypes(final String baseUrl, final ExchangeClient apiAuthClient) {
			super(baseUrl, apiAuthClient);
		}

		public List<String> getList(final String... paths) {
			return client()
					.http()
					.get()
					.path(paths)
					.retrieve(LIST_TYPE_1)
					.orDefault(Collections::emptyList);
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldMakeGetCallWithTheCorrectUri() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		List<String> expected = List.of(ID1, ID2);
		ApiResponse<List<String>> response = ApiResponse.create(expected)
				.status(HttpStatus.OK)
				.exchangeClient(exchangeClient)
				.build();

		ArgumentCaptor<ApiRequest<?>> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
		doReturn(response).when(exchangeClient).exchange(requestCaptor.capture());

		ApiClientWithGenericTypes api = new ApiClientWithGenericTypes(BASE_URL, exchangeClient);

		List<String> result = api.getList(PATH_TEST, PATH_TEST);

		assertInstanceOf(ApiClientFluentAdapter.class, requestCaptor.getValue());
		ApiClientFluentAdapter adapter = JavaObjects.cast(requestCaptor.getValue());

		assertThat(result, equalTo(expected));
		assertThat(adapter.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST + "/" + PATH_TEST));
	}

	@Test
	void shouldInitializeParameterizedTypeReferences() throws Exception {
		try (@SuppressWarnings("resource")
		ApiClientWithGenericTypes apiClient = new ApiClientWithGenericTypes(BASE_URL, new DummyExchangeClient())) {
			assertThat(apiClient.getBaseUrl(), equalTo(BASE_URL));
		}

		Type type1 = ApiClientWithGenericTypes.LIST_TYPE_1.getType();
		Type type2 = ApiClientWithGenericTypes.LIST_TYPE_2.getType();
		Type type3 = ApiClientWithGenericTypes.LIST_TYPE_3.getType();

		assertThat(type1.toString(), equalTo("java.util.List<java.lang.String>"));
		assertThat(type2.toString(), equalTo("java.util.List<java.lang.String>"));
		assertThat(type3.toString(), equalTo("java.util.List<java.util.Map<java.lang.String, java.lang.Object>>"));
	}

	static class BadApiClient extends ApiClient {

		public static final GenericClass<String> WRONG_TYPE = ApiClient.typeObject();

		public BadApiClient(final String baseUrl, final ExchangeClient exchangeClient) {
			super(baseUrl, exchangeClient);
		}

	}

	@Test
	void shouldThrowExceptionIfGenericClassIsNotParameterized() {
		DummyExchangeClient exchangeClient = assertDoesNotThrow(DummyExchangeClient::new);

		Executable executable = () -> new BadApiClient(BASE_URL, exchangeClient);
		IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, executable);

		exchangeClient.close();

		Field typeObjectField = Fields.getOneDeclared(BadApiClient.class, "WRONG_TYPE");

		assertThat(iae.getMessage(), equalTo("The typeObject method should only be used for generic types, current type: "
				+ String.class.getTypeName() + " is not a generic type for static field: "
				+ typeObjectField.getName()));
	}

	static class DummyExchangeClient implements ExchangeClient {

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		@Override
		public void close() {
			// Do nothing
		}
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
}
