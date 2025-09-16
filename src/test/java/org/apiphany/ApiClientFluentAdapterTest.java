package org.apiphany;

import static org.apiphany.ParameterFunction.parameter;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apiphany.client.ExchangeClient;
import org.apiphany.http.HttpMethod;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.meters.BasicMeters;
import org.apiphany.meters.MeterCounter;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.reflection.GenericClass;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

/**
 * Test class for {@link ApiClientFluentAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class ApiClientFluentAdapterTest {

	private static final String URL = "http://localhost";
	private static final Map<String, String> PARAMS = RequestParameters.of(parameter("name", "value"));
	private static final Map<String, List<String>> HEADERS = Map.of("headerName", List.of("headerValue"));
	private static final String BODY = "SomeBody";
	private static final String HEADER_NAME = "header-name";
	private static final String HEADER_VALUE = "header-value";
	private static final String SOME_METERS_PREFIX = "some.meters";
	private static final BasicMeters METERS = BasicMeters.of(SOME_METERS_PREFIX);
	private static final Retry RETRY = Retry.of(WaitCounter.of(2, Duration.ofMillis(1)));
	private static final String API = "api";
	private static final String USERS = "users";

	@Mock
	private ApiClient apiClient;

	@Test
	void shouldPopulateAllApiRequestFieldsWhenBuildingWithAnApiRequest() {
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
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION);

		assertThat(request.getExchangeClient(ExchangeClient.class), notNullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallApiClientExchangeOnRetrieve() {
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
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		var params = RequestParameters.of(parameter("sum", "1+2+3"));

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION)
				.url(URL)
				.params(params)
				.urlEncoded();

		request.retrieve();

		var expected = RequestParameters.of(parameter("sum", "1%2B2%2B3"));

		assertThat(request.getParams(), equalTo(expected));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldEncodeParamsOnRetrieveWhenEncodingIsEnabledWithDirectParameterFunction() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION)
				.url(URL)
				.params(parameter("sum", "1+2+3"))
				.urlEncoded();

		request.retrieve();

		var expected = RequestParameters.of(parameter("sum", "1%2B2%2B3"));

		assertThat(request.getParams(), equalTo(expected));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetTheResponseTypeOnRetrieveWithClass() {
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
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(exchangeClient).when(apiClient).getExchangeClient(AuthenticationType.SESSION);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.authenticationType(AuthenticationType.SESSION);

		assertFalse(request.isStream());

		request.download();

		assertTrue(request.isStream());
	}

	@Test
	void shouldPopulateBodyOnWhenSettingThePayload() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.payload(BODY);

		assertThat(request.getBody(), equalTo(BODY));
	}

	@Test
	void shouldPopulateDefaultRetryOnWhenSettingDefaultRetry() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.defaultRetry();

		assertThat(request.getRetry(), equalTo(Retry.defaultRetry()));
	}

	@Test
	void shouldPopulateUrlOnWhenSettingUri() {
		URI uri = URI.create(URL);
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.uri(uri);

		assertThat(request.getUrl(), equalTo(URL));
	}

	@Test
	void shouldPopulateUrlOnWhenSettingUriWithPathSegments() {
		URI uri = URI.create(URL);
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.uri(uri, API, USERS);

		assertThat(request.getUrl(), equalTo(URL + "/" + API + "/" + USERS));
	}

	@Test
	void shouldPopulateUrlOnWhenSettingUriWithPathSegmentsEvenIfEncodingIsTrueButNoEncodingNecessary() {
		URI uri = URI.create(URL);
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.urlEncoded()
				.uri(uri, API, USERS);

		assertThat(request.getUrl(), equalTo(URL + "/" + API + "/" + USERS));
	}

	@Test
	void shouldPopulateUrlOnWhenSettingUriWithPathSegmentsEvenIfEncodingIsTrue() {
		URI uri = URI.create(URL);
		String segmentWithSpace = "hello world";
		String segmentWithSpecial = "name@example.com";

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.urlEncoded()
				.uri(uri, API, segmentWithSpace, segmentWithSpecial);

		String expected = URL + "/" + API + "/hello+world/name%40example.com";

		assertThat(request.getUrl(), equalTo(expected));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldPopulateUrlOnWhenSettingPathSegmentsEvenIfEncodingIsTrue() {
		doReturn(URL).when(apiClient).getBaseUrl();
		String segmentWithSpace = "hello world";
		String segmentWithSpecial = "name@example.com";

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.pathEncoded(API, segmentWithSpace, segmentWithSpecial);

		String expected = URL + "/" + API + "/hello+world/name%40example.com";

		assertThat(request.getUrl(), equalTo(expected));
	}

	@Test
	void shouldPopulateUrlOnWhenSettingUriWithPathSegmentsContainingSlashes() {
		URI uri = URI.create(URL);
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.uri(uri, API + "/", USERS, "/" + API);

		assertThat(request.getUrl(), equalTo(URL + "/" + API + "/" + USERS + "/" + API));
	}

	@Test
	void shouldPopulateUrlOnWhenSettingUrlWithPathSegmentsAndUrlContainingMultipleSlashes() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.url(URL + "//", API + "//", USERS, "//" + API, "//" + USERS + "//");

		assertThat(request.getUrl(), equalTo(URL + "/" + API + "/" + USERS + "/" + API + "/" + USERS));
	}

	@Test
	void shouldPopulateUrlOnWhenSettingUriWithPathSegmentsEvenIfPathSegmentsAreEmpty() {
		URI uri = URI.create(URL);
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.uri(uri, API, "", "//", "/", USERS);

		assertThat(request.getUrl(), equalTo(URL + "/" + API + "/" + USERS));
	}

	@Test
	void shouldThrowExceptionIfUrlIsEmptyWhenUsingPathSegments() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> request.url("", API));

		assertThat(e.getMessage(), equalTo("url cannot be null or empty"));
	}

	@Test
	void shouldAddMetersWithTags() {
		List<String> tagsList = List.of("some.tag.name", "some.tag.value");
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.meters(SOME_METERS_PREFIX, tagsList);

		BasicMeters meters = request.getMeters();

		assertThat(meters.requests().getName(), equalTo(SOME_METERS_PREFIX + "." + BasicMeters.Name.REQUEST));
		assertThat(meters.latency().getName(), equalTo(SOME_METERS_PREFIX + "." + BasicMeters.Name.LATENCY));
		assertThat(meters.errors().getName(), equalTo(SOME_METERS_PREFIX + "." + BasicMeters.Name.ERROR));
		assertThat(meters.retries().getName(), equalTo(SOME_METERS_PREFIX + "." + BasicMeters.Name.RETRY));

		List<Tag> tags = meters.latency().unwrap(Timer.class).getId().getTags();

		Tag tag = tags.getFirst();

		assertThat(tags, hasSize(1));
		assertThat(tag.getKey(), equalTo(tagsList.get(0)));
		assertThat(tag.getValue(), equalTo(tagsList.get(1)));

		List<Supplier<MeterCounter>> counters = List.of(meters::requests, meters::errors, meters::retries);
		for (Supplier<MeterCounter> counterSupplier : counters) {
			tags = counterSupplier.get().unwrap(Counter.class).getId().getTags();
			tag = tags.getFirst();

			assertThat(tags, hasSize(1));
			assertThat(tag.getKey(), equalTo(tagsList.get(0)));
			assertThat(tag.getValue(), equalTo(tagsList.get(1)));
		}
	}

	@Test
	void shouldPopulateHeaderOnHeaderWhenIfConditionIsTrue() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.headerWhen(true, HEADER_NAME, HEADER_VALUE);

		assertThat(request.getHeaders(), equalTo(Map.of(HEADER_NAME, List.of(HEADER_VALUE))));
	}

	@Test
	void shouldNotPopulateHeaderOnHeaderWhenIfConditionIsFalse() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.headerWhen(false, HEADER_NAME, HEADER_VALUE);

		assertThat(request.getHeaders(), equalTo(Collections.emptyMap()));
	}

	@Test
	void shouldPopulateHeadersOnHeadersWhenIfConditionIsTrue() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.headersWhen(true, () -> Map.of(HEADER_NAME, List.of(HEADER_VALUE)));

		assertThat(request.getHeaders(), equalTo(Map.of(HEADER_NAME, List.of(HEADER_VALUE))));
	}

	@Test
	void shouldNotPopulateHeadersOnHeadersWhenIfConditionIsFalse() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.headersWhen(false, () -> Map.of(HEADER_NAME, List.of(HEADER_VALUE)));

		assertThat(request.getHeaders(), equalTo(Collections.emptyMap()));
	}
}
