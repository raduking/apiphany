package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.apiphany.io.InputStreamSupplier;
import org.apiphany.test.io.OneShotInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.morphix.lang.retry.Retry;
import org.morphix.lang.retry.WaitCounter;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

/**
 * Contract tests for request retries. These tests verify that the client correctly handles retries of failed requests
 * when configured to do so, including handling of non-repeatable request bodies.
 *
 * @author Radu Sebastian LAZIN
 */
public interface RetryContract extends ApiphanyContract {

	@DisplayName("Retries: By default, the client should not retry failed requests")
	@Test
	default void shouldNotRetryByDefault() throws Exception {
		// Apache's HttpClient will automatically retry failed requests by default, but Java's HttpClient does not, so we want
		// to ensure that the ApiClient doesn't add retries either.
		wiremock().stubFor(get("/flaky")
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("flaky error response")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<String> response = api.client()
					.http()
					.get()
					.path("flaky")
					.retrieve(String.class);

			assertNull(response.orNull());
			assertEquals(500, response.getStatus().getCode());
			assertEquals("flaky error response", response.getBody());
		}

		wiremock().verify(1, getRequestedFor(urlEqualTo("/flaky")));
	}

	@DisplayName("Retries: The client should retry failed requests when configured to do so")
	@Test
	default void shouldRetryAndEventuallySucceed() throws Exception {
		wiremock().stubFor(get("/retry-success")
				.inScenario("retry")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("first error response"))
				.willSetStateTo("second"));

		wiremock().stubFor(get("/retry-success")
				.inScenario("retry")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("retry-success")
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve(String.class)
					.orNull();

			assertEquals("OK", result);
		}

		// 2 attempts total
		wiremock().verify(2, getRequestedFor(urlEqualTo("/retry-success")));
	}

	@DisplayName("Retries: The client should retry failed requests when configured to do so")
	@Test
	default void shouldNotFailRetryBeforeSendWhenBodyIsInputStreamSupplierRepeatable() throws Exception {
		wiremock().stubFor(post("/retry-stream-1")
				.inScenario("retry-stream-1")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock().stubFor(post("/retry-stream-1")
				.inScenario("retry-stream-1")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("retry-stream-1")
					.body(InputStreamSupplier.from(() -> new OneShotInputStream("hello")))
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve(String.class)
					.orRethrow();

			assertEquals("hi", result);
		}

		// this will break most clients since the InputStream can only be read once, but we want to ensure that the ApiClient
		// properly retries even in this case by re-creating the stream for the retry attempt
		wiremock().verify(2, postRequestedFor(urlEqualTo("/retry-stream-1"))
				.withRequestBody(equalTo("hello")));
	}

	@DisplayName("Retries: The client should fail retries when the body is a non-repeatable InputStream that cannot be re-created")
	@Test
	@SuppressWarnings("resource")
	default void shouldFailRetryBeforeSendWhenBodyIsNonRepeatable() throws Exception {
		wiremock().stubFor(post("/retry-stream")
				.inScenario("retry-stream")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock().stubFor(post("/retry-stream")
				.inScenario("retry-stream")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			assertThrows(Exception.class, () -> api.client()
					.http()
					.post()
					.path("retry-stream")
					.body(new OneShotInputStream("hello"))
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve()
					.orRethrow());
		}

		// this will break most clients since the InputStream can only be read once, but we want to ensure that the ApiClient
		// properly retries even in this case by re-creating the stream for the retry attempt
		wiremock().verify(1, postRequestedFor(urlEqualTo("/retry-stream"))
				.withRequestBody(equalTo("hello")));
	}

	@DisplayName("Retries: The client should retry failed requests with a non-repeatable InputStream body by re-creating the stream for the retry attempt")
	@Test
	default void shouldNotFailRetryBeforeSendWhenBodyIsRepeatableByRecreatingTheInputStream() throws Exception {
		wiremock().stubFor(post("/retry-stream-2")
				.inScenario("retry-stream-2")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock().stubFor(post("/retry-stream-2")
				.inScenario("retry-stream-2")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("retry-stream-2")
					.body(() -> new OneShotInputStream("hello"))
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve(String.class)
					.orRethrow();

			assertEquals("hi", result);
		}

		// this will break most clients since the InputStream can only be read once, but we want to ensure that the ApiClient
		// properly retries even in this case by re-creating the stream for the retry attempt
		wiremock().verify(2, postRequestedFor(urlEqualTo("/retry-stream-2"))
				.withRequestBody(equalTo("hello")));
	}

	@DisplayName("Retries: The client should fail retries when the body is a non-repeatable InputStream and the supplier returns the same already consumed stream")
	@Test
	default void shouldFailRetryWhenSupplierReturnsSameConsumedInputStream() throws Exception {
		final String expectedBody = "hello world";

		// first request fails
		wiremock().stubFor(post("/retry-stream-3")
				.inScenario("retry-stream-3")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		// retry succeeds
		wiremock().stubFor(post("/retry-stream-3")
				.inScenario("retry-stream-3")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		InputStream alreadyOpened =
				new ByteArrayInputStream(expectedBody.getBytes(StandardCharsets.UTF_8));

		Supplier<InputStream> brokenSupplier = () -> alreadyOpened;

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("retry-stream-3")
					.body(brokenSupplier)
					.charset(StandardCharsets.UTF_8)
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve(String.class)
					.orRethrow();

			assertEquals("hi", result);
		}

		// first attempt
		wiremock().verify(1, postRequestedFor(urlEqualTo("/retry-stream-3"))
				.withRequestBody(equalTo(expectedBody)));

		// retry attempt MUST be broken (stream already consumed)
		wiremock().verify(1, postRequestedFor(urlEqualTo("/retry-stream-3"))
				.withRequestBody(WireMock.notMatching(expectedBody)));

		List<ServeEvent> events =
				wiremock().getAllServeEvents().stream()
						.filter(e -> e.getRequest().getUrl().equals("/retry-stream-3"))
						.toList();

		// events are in reverse order (newest first), so the first attempt is events.get(1) and the retry is events.get(0)
		assertEquals(2, events.size());
		assertEquals("", events.get(0).getRequest().getBodyAsString());
		assertEquals(expectedBody, events.get(1).getRequest().getBodyAsString());
	}
}
