package org.apiphany;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;

/**
 * Test class for {@link ApiResponse}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiResponseTest {

	private static final String MUMU = "mumu";
	private static final String BUBU = "bubu";
	private static final String PREFIX = "big";

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#orDefault(Object)}.</li>
	 * <li>{@link ApiResponse#orDefault(Class)}</li>
	 * <li>{@link ApiResponse#orDefault(Supplier)}</li>
	 * </ul>
	 */
	@Nested
	class OrDefaultTests {

		@Test
		void shouldReturnDefaultOnOrDefaultWithObjectWhenNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();
			A a = new A();

			A result = response.orDefault(a);

			assertThat(result, equalTo(a));
		}

		@Test
		void shouldReturnBodyOnOrDefaultWithObjectWhenSuccessful() {
			A a1 = new A();
			ApiResponse<A> response = ApiResponse.create(a1)
					.status(HttpStatus.OK)
					.build();
			A a2 = new A();

			A result = response.orDefault(a2);

			assertThat(result, equalTo(a1));
		}

		@Test
		void shouldReturnDefaultOnOrDefaultWithClassWhenNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			A result = response.orDefault(A.class);

			assertNotNull(result);
			assertNull(result.getS());
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#orNull()}.</li>
	 * </ul>
	 */
	@Nested
	class OrNullTests {

		@Test
		void shouldReturnDefaultOnOrNullWhenNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			A result = response.orNull();

			assertThat(result, nullValue());
		}

		@Test
		void shouldReturnBodyOnOrNullWithObjectWhenSuccessful() {
			A a = new A();
			ApiResponse<A> response = ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build();

			A result = response.orNull();

			assertThat(result, equalTo(a));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#fromOrDefault(Function, Object)}.</li>
	 * <li>{@link ApiResponse#fromOrDefault(BiFunction, Object, Object)}.</li>
	 * </ul>
	 */
	@Nested
	class FromOrDefaultTests {

		@Test
		void shouldReturnDefaultOnFromOrDefaultWithFunctionIfNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			String result = response.fromOrDefault(A::getS, MUMU);

			assertThat(result, equalTo(MUMU));
		}

		@Test
		void shouldReturnValueOnFromOrDefaultWithFunctionIfSuccessful() {
			ApiResponse<A> response = ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build();

			String result = response.fromOrDefault(A::getS, MUMU);

			assertThat(result, equalTo(BUBU));
		}

		@Test
		void shouldReturnDefaultOnFromOrDefaultWithBiFunctionIfNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			String result = response.fromOrDefault(A::getPrefixedS, PREFIX, MUMU);

			assertThat(result, equalTo(MUMU));
		}

		@Test
		void shouldReturnFromBodyOnFromOrDefaultWithBiFunctionIfSuccessful() {
			ApiResponse<A> response = ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build();

			String result = response.fromOrDefault(A::getPrefixedS, PREFIX, MUMU);

			assertThat(result, equalTo(PREFIX + BUBU));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#asList()}.</li>
	 * </ul>
	 */
	@Nested
	class AsListTests {

		@Test
		void shouldReturnListOnAsListFromArrayWhenSuccessful() {
			A a1 = new A(MUMU);
			A a2 = new A(BUBU);
			ApiResponse<A[]> response = ApiResponse.create(new A[] { a1, a2 })
					.status(HttpStatus.OK)
					.build();

			List<A> result = response.asList();

			assertThat(result, equalTo(List.of(a1, a2)));
		}

		@Test
		void shouldReturnEmptyListOnAsListFromArrayWhenNotSuccessful() {
			ApiResponse<A[]> response = ApiResponse.create((A[]) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			List<A> result = response.asList();

			assertThat(result, hasSize(0));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#asListFromOrEmpty(Function)}.</li>
	 * </ul>
	 */
	@Nested
	class AsListFromOrEmptyTests {

		@Test
		void shouldReturnListOnAsListFromOrEmptyWithFunctionIfSuccessful() {
			ApiResponse<A> response = ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build();

			List<String> result = response.asListFromOrEmpty(A::getResults);

			assertThat(result, equalTo(List.of(BUBU)));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#asListFromOrDefault(Function, List)}.</li>
	 * </ul>
	 */
	@Nested
	class AsListFromOrDefaultTests {

		@Test
		void shouldReturnFirstOnFirstFromOrDefaultWithFunctionIfSuccessful() {
			ApiResponse<A> response = ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build();

			String result = response.firstFromOrDefault(A::getResults, MUMU);

			assertThat(result, equalTo(BUBU));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#firstFromOrDefault(Function, Object)}.</li>
	 * </ul>
	 */
	@Nested
	class FirstFromOrDefaultTests {

		@Test
		void shouldReturnDefaultOnFirstFromOrDefaultWithFunctionIfNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			String result = response.firstFromOrDefault(A::getResults, MUMU);

			assertThat(result, equalTo(MUMU));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#orThrow(Throwable)}}.</li>
	 * </ul>
	 */
	@Nested
	class OrThrowTests {

		@Test
		void shouldThrowExceptionOnOrThrowWhenNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			IllegalStateException e = assertThrows(IllegalStateException.class,
					() -> response.orThrow(new IllegalStateException("Not successful")));

			assertThat(e.getMessage(), equalTo("Not successful"));
		}

		@Test
		void shouldReturnBodyOnOrThrowWhenSuccessful() {
			A a = new A(MUMU);
			ApiResponse<A> response = ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build();

			A result = response.orThrow(new IllegalStateException("Not successful"));

			assertThat(result, equalTo(a));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#orRethrow()}}.</li>
	 * <li>{@link ApiResponse#orRethrow(Function)}}.</li>
	 * </ul>
	 */
	@Nested
	class OrReThrowTests {

		private static final String EXCEPTION_MESSAGE = "Boom: Not successful";

		@Test
		void shouldThrowExceptionOnOrRehrowWhenNotSuccessfulAndHasException() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.exception(new IllegalStateException(EXCEPTION_MESSAGE))
					.build();

			IllegalStateException e = assertThrows(IllegalStateException.class, response::orRethrow);

			assertThat(e.getMessage(), equalTo(EXCEPTION_MESSAGE));
		}

		@Test
		void shouldReturnBodyOnOrRethrowWhenSuccessful() {
			A a = new A(MUMU);
			ApiResponse<A> response = ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build();

			A result = response.orRethrow();

			assertThat(result, equalTo(a));
		}

		@Test
		void shouldThrowWrappedExceptionOnOrRehrowWhenNotSuccessfulAndHasException() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.exception(new IllegalStateException(EXCEPTION_MESSAGE))
					.build();

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> response.orRethrow(e -> new IllegalArgumentException("Wrapped exception", e)));

			assertThat(exception.getMessage(), equalTo("Wrapped exception"));
			assertThat(exception.getCause().getMessage(), equalTo(EXCEPTION_MESSAGE));
		}

		@Test
		void shouldReturnBodyOnOrRethrowWrappedWhenSuccessful() {
			A a = new A(MUMU);
			ApiResponse<A> response = ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build();

			A result = response.orRethrow(IllegalArgumentException::new);

			assertThat(result, equalTo(a));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#orHandleError(Function)}.</li>
	 * </ul>
	 */
	@Nested
	class OrHandleErrorTests {

		@Test
		void shouldHandleErrorOnOrHandleErrorWhenNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.exception(new IllegalStateException("Not successful"))
					.build();

			A result = response.orHandleError(exception -> new A(MUMU));

			assertThat(result.getS(), equalTo(MUMU));
		}

		@Test
		void shouldReturnBodyOnOrHandleErrorWhenSuccessful() {
			A a = new A(BUBU);
			ApiResponse<A> response = ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build();

			A result = response.orHandleError(exception -> new A(MUMU));

			assertThat(result, equalTo(a));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#mapResultOrDefault(Function, Object)}.</li>
	 * </ul>
	 */
	@Nested
	class MapResultOrDefaultTests {

		@Test
		void shouldReturnDefaultOnMapResultOrDefaultWhenNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			String result = response.mapResultOrDefault(r -> r.getBody().getS(), MUMU);

			assertThat(result, equalTo(MUMU));
		}

		@Test
		void shouldReturnMappedValueOnMapResultOrDefaultWhenSuccessful() {
			ApiResponse<A> response = ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build();

			String result = response.mapResultOrDefault(r -> r.getBody().getS(), MUMU);

			assertThat(result, equalTo(BUBU));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#mapResultOrNull(Function)}.</li>
	 * </ul>
	 */
	@Nested
	class MapResultOrNullTests {

		@Test
		void shouldReturnDefaultOnMapResultOrDefaultWhenNotSuccessful() {
			ApiResponse<A> response = ApiResponse.create((A) null)
					.status(HttpStatus.NOT_FOUND)
					.build();

			String result = response.mapResultOrNull(r -> r.getBody().getS());

			assertThat(result, nullValue());
		}

		@Test
		void shouldReturnMappedValueOnMapResultOrDefaultWhenSuccessful() {
			ApiResponse<A> response = ApiResponse.create(new A(BUBU))
					.status(HttpStatus.OK)
					.build();

			String result = response.mapResultOrNull(r -> r.getBody().getS());

			assertThat(result, equalTo(BUBU));
		}
	}

	/**
	 * Tests for
	 *
	 * <ul>
	 * <li>{@link ApiResponse#inputStream()}.</li>
	 * <li>{@link ApiResponse#inputStream(Function)}.</li>
	 * </ul>
	 */
	@Nested
	class InputStreamTests {

		@Test
		void shouldReturnBodyAsInputStreamWhenBodyIsInputStream() {
			ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x01 });
			ApiResponse<Object> response = ApiResponse.create((Object) bis)
					.status(HttpStatus.OK)
					.build();

			@SuppressWarnings("resource")
			InputStream is = response.inputStream();

			assertThat(is, equalTo(bis));
		}

		@Test
		void shouldReturnBodyAsInputStreamWhenBodyIsByteArray() {
			byte[] bytes = new byte[] { 0x01, 0x02, 0x03 };
			ApiResponse<Object> response = ApiResponse.create((Object) bytes)
					.status(HttpStatus.OK)
					.build();

			@SuppressWarnings("resource")
			InputStream is = response.inputStream();
			ByteArrayInputStream result = JavaObjects.cast(is);

			assertThat(result.readAllBytes(), equalTo(bytes));
		}

		@Test
		void shouldReturnBodyAsInputStreamWhenBodyIsCharSequence() {
			ApiResponse<String> response = ApiResponse.create(MUMU)
					.status(HttpStatus.OK)
					.build();

			@SuppressWarnings("resource")
			InputStream is = response.inputStream();
			ByteArrayInputStream result = JavaObjects.cast(is);

			assertThat(result.readAllBytes(), equalTo(MUMU.getBytes()));
		}

		@Test
		void shouldReturnBodyToStringAsInputStreamWhenBodyIsObject() {
			A a = new A(MUMU);
			ApiResponse<A> response = ApiResponse.create(a)
					.status(HttpStatus.OK)
					.build();

			@SuppressWarnings("resource")
			InputStream is = response.inputStream();
			ByteArrayInputStream result = JavaObjects.cast(is);

			assertThat(result.readAllBytes(), equalTo(MUMU.getBytes()));
		}

		@Test
		void shouldThrowExceptionWhenBodyCannotBeConvertedToInputStream() {
			ApiResponse<Object> response = ApiResponse.create(null)
					.status(HttpStatus.OK)
					.build();

			IllegalStateException exception = assertThrows(IllegalStateException.class, response::inputStream);
			assertThat(exception.getMessage(), equalTo("Cannot convert null body to InputStream"));
		}
	}

	@Test
	void shouldReturnStatus() {
		ApiResponse<Object> response = ApiResponse.create(null)
				.status(HttpStatus.OK)
				.build();

		HttpStatus status = response.getStatus();

		assertThat(status, equalTo(HttpStatus.OK));
	}

	@Test
	void shouldReturnStatusCode() {
		ApiResponse<Object> response = ApiResponse.create(null)
				.status(HttpStatus.OK)
				.build();

		int statusCode = response.getStatusCode();

		assertThat(statusCode, equalTo(HttpStatus.OK.getCode()));
	}

	@Test
	void shouldReturnUnknownStatusCodeWhenStatusCodeIsMissing() {
		ApiResponse<Object> response = ApiResponse.create(null)
				.build();

		int statusCode = response.getStatusCode();

		assertThat(statusCode, equalTo(Status.UNKNOWN));
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
