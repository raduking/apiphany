package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apiphany.http.HttpMethod;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.meters.BasicMeters;
import org.junit.jupiter.api.Test;

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
		ApiClient apiClient = mock(ApiClient.class);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.url(URL)
				.method(HttpMethod.OPTIONS)
				.params(PARAMS)
				.headers(HEADERS)
				.body(BODY)
				.charset(StandardCharsets.US_ASCII)
				.urlEncode(true)
				.stream(true)
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

}
