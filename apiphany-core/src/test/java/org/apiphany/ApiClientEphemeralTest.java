package org.apiphany;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.client.ClientLifecycle;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.HttpClientFluentAdapter;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.ContentType;
import org.apiphany.lang.ScopedResource;
import org.apiphany.security.AuthenticationType;
import org.apiphany.utils.TestDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.morphix.lang.JavaObjects;

/**
 * Test class for {@link ApiClient} meters.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientEphemeralTest {

	private static final String APIPHANY = "Apiphany";

	private static final String BASE_URL = "http://localhost";
	private static final String PATH_TEST = "test";

	private static final String ID1 = "someTestId1";
	private static final String ID2 = "someTestId2";
	private static final int COUNT1 = 666;
	private static final int COUNT2 = 777;
	private static final String PARAM_ID = "id";

	private static final int HTTP_STATUS_OK = 200;

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldCallExchangeClientWithProvidedParametersOnPost() throws Exception {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.NONE).when(exchangeClient).getAuthenticationType();
		ScopedResource<HttpExchangeClient> scopedClient = ScopedResource.managed(exchangeClient);
		ExchangeClientBuilder builder = mock(ExchangeClientBuilder.class);
		doReturn(scopedClient).when(builder).build();

		TestDto expected = TestDto.of(ID1, COUNT1);
		ApiResponse<TestDto> response = ApiResponse.create(expected)
				.status(HTTP_STATUS_OK, HttpStatus::fromCode)
				.exchangeClient(exchangeClient)
				.build();
		TestDto payload = TestDto.of(ID2, COUNT2);

		Map<String, List<String>> headers = new HashMap<>();
		Headers.addTo(headers, HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON);
		Headers.addTo(headers, HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN);
		Headers.addTo(headers, HttpHeader.USER_AGENT, APIPHANY);

		Map<String, List<String>> params = RequestParameters.of(
				ParameterFunction.parameter(PARAM_ID, ID1));

		doReturn(response).when(exchangeClient).exchange(any(ApiRequest.class));
		doReturn(HttpMethod.POST).when(exchangeClient).post();

		TestDto result = Api
				.http(builder)
				.post()
				.url(BASE_URL)
				.path(PATH_TEST)
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON)
				.header(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN)
				.header(HttpHeader.USER_AGENT, APIPHANY)
				.urlEncoded()
				.param(PARAM_ID, ID1)
				.body(payload)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);

		assertThat(result, equalTo(expected));

		ArgumentCaptor<?> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
		verify(exchangeClient).exchange(JavaObjects.cast(requestCaptor.capture()));
		verify(exchangeClient).close();

		ApiClientFluentAdapter request = JavaObjects.cast(requestCaptor.getValue());

		assertThat(request.getUrl(), equalTo(BASE_URL + "/" + PATH_TEST));
		assertThat(request.getUri(), equalTo(URI.create(request.url + RequestParameters.asUrlSuffix(params))));
		assertThat(request.getMethod(), equalTo(HttpMethod.POST));
		assertThat(request.getAuthenticationType(), equalTo(AuthenticationType.NONE));
		assertThat(request.getClassResponseType(), equalTo(TestDto.class));
		assertThat(request.getGenericResponseType(), nullValue());
		assertThat(request.getCharset(), equalTo(StandardCharsets.UTF_8));
		assertThat(request.getParams(), equalTo(params));
		assertThat(request.getHeaders(), equalTo(headers));
		assertThat(request.getBody(), equalTo(payload));
		assertThat(request.getResponseType(), equalTo(TestDto.class));
		assertThat(request.getClassResponseType(), equalTo(TestDto.class));
		assertTrue(request.isUrlEncoded());
		assertFalse(request.hasGenericType());
		assertFalse(request.isStream());

		ApiClient apiClient = request.getApiClient();
		assertThat(apiClient.getLifecycle(), equalTo(ClientLifecycle.EPHEMERAL));
	}

	@Test
	void shouldUseDefaultExchangeClientWhenNoBuildersProvided() throws Exception {
		HttpClientFluentAdapter client = Api.http();

		try (ApiClient apiClient = client.get().getApiClient()) {
			assertThat(apiClient.getLifecycle(), equalTo(ClientLifecycle.EPHEMERAL));
			assertThat(apiClient.client(), notNullValue());
		}
	}
}
