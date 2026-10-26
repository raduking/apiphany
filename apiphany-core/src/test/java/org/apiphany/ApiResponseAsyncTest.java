package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;

/**
 * Test class for {@link ApiResponseAsync}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiResponseAsyncTest {

	private static final String MUMU = "mumu";

	private static final String BUBU = "bubu";
	private static final String PREFIX = "big";

	private static final String BODY = "body";
	private static final String DEFAULT_BODY = "default";

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#orDefault(Object)}.</li>
	 * <li>{@link ApiResponseAsync#orDefault(Class)}.</li>
	 * </ul>
	 */
	@Nested
	class OrDefaultTests {

		@Test
		void shouldReturnDefaultOnOrDefaultWithObjectWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());
			A a = new A();

			A result = asyncResponse.orDefault(a).join();

			assertThat(result, equalTo(a));
		}

		@Test
		void shouldReturnBodyOnOrDefaultWithObjectWhenSuccessful() {
			A a1 = new A();
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(a1)
					.status(HttpStatus.OK)
					.build());
			A a2 = new A();

			A result = asyncResponse.orDefault(a2).join();

			assertThat(result, equalTo(a1));
		}

		@Test
		void shouldReturnDefaultOnOrDefaultWithClassWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			A result = asyncResponse.orDefault(A.class).join();

			assertNotNull(result);
			assertNull(result.getS());
		}

		@Test
		void shouldCompleteWithDefaultBodyOnOrDefaultWhenResponseIsNotSuccessful() {
			ApiResponseAsync<String> asyncResponse = failed();

			String result = asyncResponse.orDefault(DEFAULT_BODY).join();

			assertThat(result, equalTo(DEFAULT_BODY));
		}

		@Test
		void shouldCompleteWithNewInstanceOnOrDefaultWhenResponseIsNotSuccessful() {
			ApiResponseAsync<String> asyncResponse = failed();

			String result = asyncResponse.orDefault(String.class).join();

			assertThat(result, equalTo(""));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#orGet(java.util.function.Supplier)}.</li>
	 * </ul>
	 */
	@Nested
	class OrGetTests {

		@Test
		void shouldReturnDefaultOnOrGetWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			A result = asyncResponse.orGet(() -> new A(BUBU)).join();

			assertNotNull(result);
			assertThat(result.getS(), equalTo(BUBU));
		}

		@Test
		void shouldCompleteWithSuppliedDefaultOnOrGetWhenResponseIsNotSuccessful() {
			ApiResponseAsync<String> asyncResponse = failed();

			String result = asyncResponse.orGet(() -> DEFAULT_BODY).join();

			assertThat(result, equalTo(DEFAULT_BODY));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#orNull()}.</li>
	 * </ul>
	 */
	@Nested
	class OrNullTests {

		@Test
		void shouldReturnDefaultOnOrNullWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			A result = asyncResponse.orNull().join();

			assertThat(result, nullValue());
		}

		@Test
		void shouldReturnBodyOnOrNullWithObjectWhenSuccessful() {
			A a = new A();
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build());

			A result = asyncResponse.orNull().join();

			assertThat(result, equalTo(a));
		}

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
		void shouldPropagateFailureToTheBodyTerminal() {
			ApiResponseAsync<String> asyncResponse = new ApiResponseAsync<>();
			RuntimeException expected = new RuntimeException("boom");
			asyncResponse.completeExceptionally(expected);

			CompletableFuture<String> result = asyncResponse.orNull();

			assertThat(result.isCompletedExceptionally(), equalTo(true));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#fromOrDefault(java.util.function.Function, Object)}.</li>
	 * <li>{@link ApiResponseAsync#fromOrDefault(java.util.function.BiFunction, Object, Object)}.</li>
	 * </ul>
	 */
	@Nested
	class FromOrDefaultTests {

		@Test
		void shouldReturnDefaultOnFromOrDefaultWithFunctionIfNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			String result = asyncResponse.fromOrDefault(A::getS, MUMU).join();

			assertThat(result, equalTo(MUMU));
		}

		@Test
		void shouldReturnValueOnFromOrDefaultWithFunctionIfSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());

			String result = asyncResponse.fromOrDefault(A::getS, MUMU).join();

			assertThat(result, equalTo(BUBU));
		}

		@Test
		void shouldReturnDefaultOnFromOrDefaultWithBiFunctionIfNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			String result = asyncResponse.fromOrDefault(A::getPrefixedS, PREFIX, MUMU).join();

			assertThat(result, equalTo(MUMU));
		}

		@Test
		void shouldReturnFromBodyOnFromOrDefaultWithBiFunctionIfSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());

			String result = asyncResponse.fromOrDefault(A::getPrefixedS, PREFIX, MUMU).join();

			assertThat(result, equalTo(PREFIX + BUBU));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#asListOrDefault(List)}.</li>
	 * </ul>
	 */
	@Nested
	class AsListOrDefaultTests {

		@Test
		void shouldReturnDefaultListOnAsListOrDefaultWhenNotSuccessful() {
			A a1 = new A(MUMU);
			List<A> defaultList = List.of(a1);
			ApiResponseAsync<A[]> asyncResponse = completedWith(ApiResponse.create((A[]) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			List<A> result = asyncResponse.asListOrDefault(defaultList).join();

			assertThat(result, equalTo(defaultList));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#asList()}.</li>
	 * </ul>
	 */
	@Nested
	class AsListTests {

		@Test
		void shouldReturnListOnAsListFromArrayWhenSuccessful() {
			A a1 = new A(MUMU);
			A a2 = new A(BUBU);
			ApiResponseAsync<A[]> asyncResponse = completedWith(ApiResponse.create(new A[] { a1, a2 })
					.status(HttpStatus.OK)
					.build());

			List<A> result = JavaObjects.cast(asyncResponse.asList().join());

			assertThat(result, equalTo(List.of(a1, a2)));
		}

		@Test
		void shouldReturnEmptyListOnAsListFromArrayWhenNotSuccessful() {
			ApiResponseAsync<A[]> asyncResponse = completedWith(ApiResponse.create((A[]) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			List<A> result = JavaObjects.cast(asyncResponse.asList().join());

			assertThat(result, hasSize(0));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#asListFromOrEmpty(java.util.function.Function)}.</li>
	 * </ul>
	 */
	@Nested
	class AsListFromOrEmptyTests {

		@Test
		void shouldReturnListOnAsListFromOrEmptyWithFunctionIfSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());

			List<String> result = asyncResponse.asListFromOrEmpty(A::getResults).join();

			assertThat(result, equalTo(List.of(BUBU)));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#asListFromOrDefault(java.util.function.Function, List)}.</li>
	 * </ul>
	 */
	@Nested
	class AsListFromOrDefaultTests {

		@Test
		void shouldReturnListOnAsListFromOrDefaultWithFunctionIfSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());

			List<String> result = asyncResponse.asListFromOrDefault(A::getResults, List.of(MUMU)).join();

			assertThat(result, equalTo(List.of(BUBU)));
		}

		@Test
		void shouldReturnDefaultListOnAsListFromOrDefaultWithFunctionIfNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			List<String> result = asyncResponse.asListFromOrDefault(A::getResults, List.of(MUMU)).join();

			assertThat(result, equalTo(List.of(MUMU)));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#firstFromOrDefault(java.util.function.Function, Object)}.</li>
	 * </ul>
	 */
	@Nested
	class FirstFromOrDefaultTests {

		@Test
		void shouldReturnDefaultOnFirstFromOrDefaultWithFunctionIfNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			String result = asyncResponse.firstFromOrDefault(A::getResults, MUMU).join();

			assertThat(result, equalTo(MUMU));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#orThrow(Throwable)}.</li>
	 * </ul>
	 */
	@Nested
	class OrThrowTests {

		private static final String NOT_SUCCESSFUL = "Not successful";

		@Test
		void shouldThrowExceptionOnOrThrowWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			IllegalStateException ise = new IllegalStateException(NOT_SUCCESSFUL);
			CompletionException completionException = assertThrows(CompletionException.class,
					() -> asyncResponse.orThrow(ise).join());

			assertThat(completionException.getCause(), equalTo(ise));
			assertThat(completionException.getCause().getMessage(), equalTo(NOT_SUCCESSFUL));
		}

		@Test
		void shouldReturnBodyOnOrThrowWhenSuccessful() {
			A a = new A(MUMU);
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build());

			A result = asyncResponse.orThrow(new IllegalStateException(NOT_SUCCESSFUL)).join();

			assertThat(result, equalTo(a));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#orRethrow()}.</li>
	 * <li>{@link ApiResponseAsync#orRethrow(java.util.function.Function)}.</li>
	 * </ul>
	 */
	@Nested
	class OrReThrowTests {

		private static final String EXCEPTION_MESSAGE = "Boom: Not successful";

		@Test
		void shouldThrowExceptionOnOrRehrowWhenNotSuccessfulAndHasException() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.exception(new IllegalStateException(EXCEPTION_MESSAGE))
					.build());

			CompletionException completionException = assertThrows(CompletionException.class,
					() -> asyncResponse.orRethrow().join());

			assertThat(completionException.getCause(), instanceOf(IllegalStateException.class));
			assertThat(completionException.getCause().getMessage(), equalTo(EXCEPTION_MESSAGE));
		}

		@Test
		void shouldReturnBodyOnOrRethrowWhenSuccessful() {
			A a = new A(MUMU);
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build());

			A result = asyncResponse.orRethrow().join();

			assertThat(result, equalTo(a));
		}

		@Test
		void shouldThrowWrappedExceptionOnOrRehrowWhenNotSuccessfulAndHasException() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.exception(new IllegalStateException(EXCEPTION_MESSAGE))
					.build());

			CompletionException completionException = assertThrows(CompletionException.class,
					() -> asyncResponse.orRethrow(e -> new IllegalArgumentException("Wrapped exception", e)).join());

			assertThat(completionException.getCause(), instanceOf(IllegalArgumentException.class));
			assertThat(completionException.getCause().getMessage(), equalTo("Wrapped exception"));
			assertThat(completionException.getCause().getCause().getMessage(), equalTo(EXCEPTION_MESSAGE));
		}

		@Test
		void shouldReturnBodyOnOrRethrowWrappedWhenSuccessful() {
			A a = new A(MUMU);
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build());

			A result = asyncResponse.orRethrow(IllegalArgumentException::new).join();

			assertThat(result, equalTo(a));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#orHandleError(java.util.function.Function)}.</li>
	 * </ul>
	 */
	@Nested
	class OrHandleErrorTests {

		@Test
		void shouldHandleErrorOnOrHandleErrorWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.exception(new IllegalStateException("Not successful"))
					.build());

			A result = asyncResponse.orHandleError(exception -> new A(MUMU)).join();

			assertThat(result.getS(), equalTo(MUMU));
		}

		@Test
		void shouldReturnBodyOnOrHandleErrorWhenSuccessful() {
			A a = new A(BUBU);
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build());

			A result = asyncResponse.orHandleError(exception -> new A(MUMU)).join();

			assertThat(result, equalTo(a));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#mapResultOrDefault(java.util.function.Function, Object)}.</li>
	 * <li>{@link ApiResponseAsync#mapResultOrGet(java.util.function.Function, java.util.function.Supplier)}.</li>
	 * </ul>
	 */
	@Nested
	class MapResultOrDefaultTests {

		@Test
		void shouldReturnDefaultOnMapResultOrDefaultWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			String result = asyncResponse.mapResultOrDefault(r -> r.getBody().getS(), MUMU).join();

			assertThat(result, equalTo(MUMU));
		}

		@Test
		void shouldReturnMappedValueOnMapResultOrDefaultWhenSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());

			String result = asyncResponse.mapResultOrDefault(r -> r.getBody().getS(), MUMU).join();

			assertThat(result, equalTo(BUBU));
		}

		@Test
		void shouldReturnDefaultOnMapResultOrDefaultWithSupplierWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			String result = asyncResponse.mapResultOrGet(r -> r.getBody().getS(), () -> MUMU).join();

			assertThat(result, equalTo(MUMU));
		}

		@Test
		void shouldReturnMappedValueOnMapResultOrDefaultWithSupplierWhenSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());

			String result = asyncResponse.mapResultOrGet(r -> r.getBody().getS(), () -> MUMU).join();

			assertThat(result, equalTo(BUBU));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#mapResultOrNull(java.util.function.Function)}.</li>
	 * </ul>
	 */
	@Nested
	class MapResultOrNullTests {

		@Test
		void shouldReturnDefaultOnMapResultOrDefaultWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());

			String result = asyncResponse.mapResultOrNull(r -> r.getBody().getS()).join();

			assertThat(result, nullValue());
		}

		@Test
		void shouldReturnMappedValueOnMapResultOrDefaultWhenSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());

			String result = asyncResponse.mapResultOrNull(r -> r.getBody().getS()).join();

			assertThat(result, equalTo(BUBU));
		}

		@Test
		void shouldCompleteWithMappedValueOnMapResultOrNullWhenResponseIsSuccessful() {
			ApiResponseAsync<String> asyncResponse = successful();

			String result = asyncResponse.mapResultOrNull(ApiResponse::getBody).join();

			assertThat(result, equalTo(BODY));
		}

		@Test
		void shouldCompleteWithNullOnMapResultOrNullWhenResponseIsNotSuccessful() {
			ApiResponseAsync<String> asyncResponse = failed();

			String result = asyncResponse.mapResultOrNull(ApiResponse::getBody).join();

			assertThat(result, nullValue());
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#onSuccess(Consumer)}.</li>
	 * </ul>
	 */
	@Nested
	class OnSuccessTests {

		@Test
		@SuppressWarnings("unchecked")
		void shouldRunActionOnOnSuccessWhenSuccessful() {
			A a = new A(BUBU);
			ApiResponse<A> response = ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build();
			ApiResponseAsync<A> asyncResponse = completedWith(response);
			Consumer<ApiResponse<A>> action = mock(Consumer.class);

			ApiResponse<A> result = asyncResponse.onSuccess(action).join();

			assertThat(result, equalTo(response));
			verify(action).accept(same(response));
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldNotRunActionOnOnSuccessWhenNotSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build());
			Consumer<ApiResponse<A>> action = mock(Consumer.class);

			asyncResponse.onSuccess(action).join();

			verify(action, never()).accept(any());
		}

		@Test
		void shouldChainOnSuccessBeforeBodyTerminal() {
			ApiResponseAsync<String> asyncResponse = successful();
			AtomicReference<String> observed = new AtomicReference<>();

			String result = asyncResponse
					.onSuccess(response -> observed.set(response.getBody()))
					.thenApply(ApiResponse::orNull)
					.join();

			assertThat(result, equalTo(BODY));
			assertThat(observed.get(), equalTo(BODY));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#onFailure(Consumer)}.</li>
	 * </ul>
	 */
	@Nested
	class OnFailureTests {

		@Test
		@SuppressWarnings("unchecked")
		void shouldRunActionOnOnFailureWhenFailed() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();
			ApiResponseAsync<A> asyncResponse = completedWith(response);
			Consumer<ApiResponse<A>> action = mock(Consumer.class);

			ApiResponse<A> result = asyncResponse.onFailure(action).join();

			assertThat(result, equalTo(response));
			verify(action).accept(same(response));
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldNotRunActionOnOnFailureWhenSuccessful() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());
			Consumer<ApiResponse<A>> action = mock(Consumer.class);

			asyncResponse.onFailure(action).join();

			verify(action, never()).accept(any());
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldNotRunActionOnOnFailureWhenStatusIsNotError() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.MOVED_PERMANENTLY)
					.build());
			Consumer<ApiResponse<A>> action = mock(Consumer.class);

			asyncResponse.onFailure(action).join();

			verify(action, never()).accept(any());
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldRunActionOnOnFailureWhenResponseIsFailed() {
			ApiResponseAsync<String> asyncResponse = failed();
			Consumer<ApiResponse<String>> action = mock(Consumer.class);

			ApiResponse<String> response = asyncResponse.onFailure(action).join();

			assertThat(response.getBody(), equalTo(BODY));
			verify(action).accept(same(response));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#onException(Consumer)}.</li>
	 * </ul>
	 */
	@Nested
	class OnExceptionTests {

		private static final String EXCEPTION_MESSAGE = "Not successful";

		@Test
		@SuppressWarnings("unchecked")
		void shouldRunActionOnOnExceptionWhenHasException() {
			IllegalStateException exception = new IllegalStateException(EXCEPTION_MESSAGE);
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.exception(exception)
					.build();
			ApiResponseAsync<A> asyncResponse = completedWith(response);
			Consumer<Exception> action = mock(Consumer.class);

			ApiResponse<A> result = asyncResponse.onException(action).join();

			assertThat(result, equalTo(response));
			verify(action).accept(same(exception));
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldNotRunActionOnOnExceptionWhenHasNoException() {
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build());
			Consumer<Exception> action = mock(Consumer.class);

			asyncResponse.onException(action).join();

			verify(action, never()).accept(any());
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponseAsync#inputStream()}.</li>
	 * <li>{@link ApiResponseAsync#inputStream(java.util.function.Function)}.</li>
	 * </ul>
	 */
	@Nested
	class InputStreamTests {

		@Test
		void shouldReturnBodyAsInputStreamWhenBodyIsInputStream() {
			ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x01 });
			ApiResponseAsync<Object> asyncResponse = completedWith(ApiResponse.create((Object) bis)
					.status(HttpStatus.OK)
					.build());

			@SuppressWarnings("resource")
			InputStream is = asyncResponse.inputStream().join();

			assertThat(is, equalTo(bis));
		}

		@Test
		void shouldReturnBodyAsInputStreamWhenBodyIsByteArray() throws Exception {
			byte[] bytes = new byte[] { 0x01, 0x02, 0x03 };
			ApiResponseAsync<Object> asyncResponse = completedWith(ApiResponse.create((Object) bytes)
					.status(HttpStatus.OK)
					.build());

			@SuppressWarnings("resource")
			InputStream is = asyncResponse.inputStream().join();
			ByteArrayInputStream result = JavaObjects.cast(is);

			assertThat(result.readAllBytes(), equalTo(bytes));
		}

		@Test
		void shouldReturnBodyAsInputStreamWhenBodyIsCharSequence() throws Exception {
			ApiResponseAsync<String> asyncResponse = completedWith(ApiResponse.create(MUMU)
					.status(HttpStatus.OK)
					.build());

			@SuppressWarnings("resource")
			InputStream is = asyncResponse.inputStream().join();
			ByteArrayInputStream result = JavaObjects.cast(is);

			assertThat(result.readAllBytes(), equalTo(MUMU.getBytes()));
		}

		@Test
		void shouldReturnBodyToStringAsInputStreamWhenBodyIsObject() throws Exception {
			A a = new A(MUMU);
			ApiResponseAsync<A> asyncResponse = completedWith(ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build());

			@SuppressWarnings("resource")
			InputStream is = asyncResponse.inputStream().join();
			ByteArrayInputStream result = JavaObjects.cast(is);

			assertThat(result.readAllBytes(), equalTo(MUMU.getBytes()));
		}

		@Test
		void shouldReturnBodyAsInputStreamWhenUsingCustomConverter() throws Exception {
			ApiResponseAsync<String> asyncResponse = successful();

			@SuppressWarnings("resource")
			InputStream is = asyncResponse.inputStream(body -> new ByteArrayInputStream(body.getBytes())).join();

			assertThat(is.readAllBytes(), equalTo(BODY.getBytes()));
		}

		@Test
		void shouldThrowExceptionWhenBodyCannotBeConvertedToInputStream() {
			ApiResponseAsync<Object> asyncResponse = completedWith(ApiResponse.create(null)
					.status(HttpStatus.OK)
					.build());

			CompletionException completionException = assertThrows(CompletionException.class,
					asyncResponse.inputStream()::join);

			assertThat(completionException.getCause(), instanceOf(IllegalStateException.class));
			assertThat(completionException.getCause().getMessage(), equalTo("Cannot convert null body to InputStream"));
		}
	}

	/**
	 * Tests for {@link ApiResponseAsync} as a {@link CompletableFuture} of {@link ApiResponse}.
	 */
	@Nested
	class CompletableFutureTests {

		@Test
		void shouldBeUsableAsACompletableFutureOfApiResponse() {
			ApiResponseAsync<String> asyncResponse = successful();

			CompletableFuture<ApiResponse<String>> future = asyncResponse;

			assertThat(future.join().getBody(), equalTo(BODY));
			assertThat(asyncResponse.thenApply(ApiResponse::getBody), instanceOf(CompletableFuture.class));
		}
	}

	private static ApiResponseAsync<String> successful() {
		return completedWith(ApiResponse.create(BODY).status(HttpStatus.OK).build());
	}

	private static ApiResponseAsync<String> failed() {
		return completedWith(ApiResponse.create(BODY).status(HttpStatus.INTERNAL_SERVER_ERROR).build());
	}

	private static <T> ApiResponseAsync<T> completedWith(final ApiResponse<T> apiResponse) {
		ApiResponseAsync<T> asyncResponse = new ApiResponseAsync<>();
		asyncResponse.complete(apiResponse);
		return asyncResponse;
	}

	static class A {

		private String s;

		private A() {
			// empty
		}

		public A(final String s) {
			this.s = s;
		}

		public String getS() {
			return s;
		}

		public void setS(final String s) {
			this.s = s;
		}

		public String getPrefixedS(final String prefix) {
			return prefix + s;
		}

		public List<String> getResults() {
			return List.of(s);
		}

		@Override
		public String toString() {
			return getS();
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof A that) {
				return Objects.equals(this.s, that.s);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(s);
		}
	}
}
