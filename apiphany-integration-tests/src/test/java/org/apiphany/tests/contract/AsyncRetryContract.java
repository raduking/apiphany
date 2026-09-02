package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.apiphany.ApiClient;
import org.apiphany.ApiClientFluentAdapter;
import org.apiphany.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.morphix.async.retry.AsyncRetry;
import org.morphix.async.retry.AsyncWaitCounter;

/**
 * Contract tests for asynchronous request retries. These tests verify that the client correctly retries failed requests
 * using non-blocking async retry waits when an {@link AsyncRetry} is configured.
 *
 * @author Radu Sebastian LAZIN
 */
public interface AsyncRetryContract extends ApiphanyContract {

	@DisplayName("AsyncRetry: The client should retry a flaky request asynchronously when async retry is configured")
	@Test
	default void shouldRetryAsyncAndEventuallySucceed() throws Exception {
		wiremock().stubFor(get("/async-retry-success")
				.inScenario("async-retry")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("first error response"))
				.willSetStateTo("second"));

		wiremock().stubFor(get("/async-retry-success")
				.inScenario("async-retry")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ApiClient api = apiClient();
		AsyncRetry asyncRetry = AsyncRetry.of(AsyncWaitCounter.of(2, Duration.ofMillis(100)));
		api.setAsyncRetry(asyncRetry);

		try (api) {
			ApiClientFluentAdapter request = api.client().http().get().path("async-retry-success").responseType(String.class);
			CompletableFuture<ApiResponse<Object>> future = api.asyncExchange(request);

			ApiResponse<Object> response = future.get();

			assertEquals(200, response.getStatus().getCode());
			assertEquals("OK", response.getBody());
		}

		// 2 attempts total
		wiremock().verify(2, getRequestedFor(urlEqualTo("/async-retry-success")));
	}

	@DisplayName("AsyncRetry: The client should not block the calling thread while waiting between retries")
	@Test
	default void shouldNotBlockTheCallingThreadDuringAsyncRetryWaits() throws Exception {
		wiremock().stubFor(get("/async-retry-wait")
				.inScenario("async-retry-wait")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock().stubFor(get("/async-retry-wait")
				.inScenario("async-retry-wait")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ApiClient api = apiClient();
		AsyncRetry asyncRetry = AsyncRetry.of(AsyncWaitCounter.of(2, Duration.ofMillis(1000)));
		api.setAsyncRetry(asyncRetry);

		long start = System.currentTimeMillis();

		try (api) {
			ApiClientFluentAdapter request = api.client().http().get().path("async-retry-wait").responseType(String.class);
			CompletableFuture<ApiResponse<Object>> future = api.asyncExchange(request);

			// the asyncExchange call itself must return (non-blocking), well before the 1s retry wait elapses
			long elapsedToReturn = System.currentTimeMillis() - start;
			assertTrue(elapsedToReturn < 500, "asyncExchange should return immediately, took: " + elapsedToReturn + " ms");

			ApiResponse<Object> response = future.get();
			assertEquals(200, response.getStatus().getCode());
			assertEquals("OK", response.getBody());
		}

		wiremock().verify(2, getRequestedFor(urlEqualTo("/async-retry-wait")));
	}
}
