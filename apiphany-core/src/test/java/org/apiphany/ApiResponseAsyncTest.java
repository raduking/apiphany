package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.CompletableFuture;

import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiResponseAsync}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiResponseAsyncTest {

	private static final String BODY = "body";
	private static final String DEFAULT_BODY = "default";

	@Test
	void shouldCompleteWithBodyOnOrNullWhenResponseIsSuccessful() {
		ApiResponseAsync<String> asyncResponse = successful();

		String result = asyncResponse.orNull().join();

		assertThat(result, equalTo(BODY));
	}

	@Test
	void shouldCompleteWithNullOnOrNullWhenResponseIsNotSuccessful() {
		ApiResponseAsync<String> asyncResponse = failed();

		String result = asyncResponse.orNull().join();

		assertThat(result, nullValue());
	}

	@Test
	void shouldCompleteWithBodyOnOrDefaultWhenResponseIsSuccessful() {
		ApiResponseAsync<String> asyncResponse = successful();

		String result = asyncResponse.orDefault(DEFAULT_BODY).join();

		assertThat(result, equalTo(BODY));
	}

	@Test
	void shouldCompleteWithDefaultBodyOnOrDefaultWhenResponseIsNotSuccessful() {
		ApiResponseAsync<String> asyncResponse = failed();

		String result = asyncResponse.orDefault(DEFAULT_BODY).join();

		assertThat(result, equalTo(DEFAULT_BODY));
	}

	@Test
	void shouldCompleteWithSuppliedDefaultOnOrDefaultWhenResponseIsNotSuccessful() {
		ApiResponseAsync<String> asyncResponse = failed();

		String result = asyncResponse.orDefault(() -> DEFAULT_BODY).join();

		assertThat(result, equalTo(DEFAULT_BODY));
	}

	@Test
	void shouldCompleteWithNewInstanceOnOrDefaultWhenResponseIsNotSuccessful() {
		ApiResponseAsync<String> asyncResponse = failed();

		String result = asyncResponse.orDefault(String.class).join();

		assertThat(result, equalTo(""));
	}

	@Test
	void shouldPropagateFailureToTheBodyTerminal() {
		ApiResponseAsync<String> asyncResponse = new ApiResponseAsync<>();
		RuntimeException expected = new RuntimeException("boom");
		asyncResponse.completeExceptionally(expected);

		CompletableFuture<String> result = asyncResponse.orNull();

		assertThat(result.isCompletedExceptionally(), equalTo(true));
	}

	@Test
	void shouldBeUsableAsACompletableFutureOfApiResponse() {
		ApiResponseAsync<String> asyncResponse = successful();

		CompletableFuture<ApiResponse<String>> future = asyncResponse;

		assertThat(future.join().getBody(), equalTo(BODY));
		assertThat(asyncResponse.thenApply(ApiResponse::getBody), instanceOf(CompletableFuture.class));
	}

	private static ApiResponseAsync<String> successful() {
		return completedWith(ApiResponse.create(BODY).status(HttpStatus.OK).build());
	}

	private static ApiResponseAsync<String> failed() {
		return completedWith(ApiResponse.create(BODY).status(HttpStatus.INTERNAL_SERVER_ERROR).build());
	}

	private static ApiResponseAsync<String> completedWith(final ApiResponse<String> apiResponse) {
		ApiResponseAsync<String> asyncResponse = new ApiResponseAsync<>();
		asyncResponse.complete(apiResponse);
		return asyncResponse;
	}
}
