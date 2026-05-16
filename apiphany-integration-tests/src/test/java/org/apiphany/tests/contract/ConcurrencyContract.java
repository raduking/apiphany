package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.apiphany.ApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for concurrency handling in the API client. These tests ensure that the client can handle multiple
 * concurrent requests without issues.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConcurrencyContract extends ApiphanyContract {

	@DisplayName("Concurrency: The client should be able to handle multiple concurrent requests without issues")
	@Test
	default void shouldHandleConcurrentRequests() throws Exception {
		wiremock().stubFor(get(urlMatching("/concurrent/.*"))
				.willReturn(ok()));

		int threadCount = 50;
		ExecutorService exe = Executors.newVirtualThreadPerTaskExecutor();

		ApiClient api = apiClient();
		try (exe; api) {
			List<CompletableFuture<String>> futures = IntStream.range(0, threadCount)
					.mapToObj(i -> CompletableFuture.supplyAsync(() -> api.client()
							.http()
							.get()
							.path("/concurrent/" + i)
							.retrieve(String.class)
							.orNull(),
							exe))
					.toList();

			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
		}

		wiremock().verify(50, getRequestedFor(urlMatching("/concurrent/.*")));
	}
}
