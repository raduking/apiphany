package org.apiphany;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Objects;

import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiResponse}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiResponseTest {

	private static final String MUMU = "mumu";
	private static final String BUBU = "bubu";
	private static final String PREFIX = "big";

	@Test
	void shouldReturnDefaultOnOrDefaultWithObjectWhenNotSuccessfull() {
		ApiResponse<A> response = ApiResponse.create((A) null)
				.status(HttpStatus.NOT_FOUND)
				.build();
		A a = new A();

		A result = response.orDefault(a);

		assertThat(result, equalTo(a));
	}

	@Test
	void shouldReturnBodyOnOrDefaultWithObjectWhenSuccessfull() {
		A a1 = new A();
		ApiResponse<A> response = ApiResponse.create(a1)
				.status(HttpStatus.OK)
				.build();
		A a2 = new A();

		A result = response.orDefault(a2);

		assertThat(result, equalTo(a1));
	}

	@Test
	void shouldReturnDefaultOnOrDefaultWithClassWhenNotSuccessfull() {
		ApiResponse<A> response = ApiResponse.create((A) null)
				.status(HttpStatus.NOT_FOUND)
				.build();

		A result = response.orDefault(A.class);

		assertNotNull(result);
		assertNull(result.getS());
	}

	@Test
	void shouldReturnDefaultOnOrNullWhenNotSuccessfull() {
		ApiResponse<A> response = ApiResponse.create((A) null)
				.status(HttpStatus.NOT_FOUND)
				.build();

		A result = response.orNull();

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnBodyOnOrNullWithObjectWhenSuccessfull() {
		A a = new A();
		ApiResponse<A> response = ApiResponse.create(a)
				.status(HttpStatus.OK)
				.build();

		A result = response.orNull();

		assertThat(result, equalTo(a));
	}

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

	@Test
	void shouldReturnListOnAsListFromArrayWhenSuccessfull() {
		A a1 = new A(MUMU);
		A a2 = new A(BUBU);
		ApiResponse<A[]> response = ApiResponse.create(new A[] { a1, a2 })
				.status(HttpStatus.OK)
				.build();

		List<A> result = response.asList();

		assertThat(result, equalTo(List.of(a1, a2)));
	}

	@Test
	void shouldReturnEmptyListOnAsListFromArrayWhenNotSuccessfull() {
		ApiResponse<A[]> response = ApiResponse.create((A[]) null)
				.status(HttpStatus.NOT_FOUND)
				.build();

		List<A> result = response.asList();

		assertThat(result, hasSize(0));
	}

	@Test
	void shouldReturnListOnAsListFromOrEmptyWithFunctionIfSuccessful() {
		ApiResponse<A> response = ApiResponse.create(new A(BUBU))
				.status(HttpStatus.OK)
				.build();

		List<String> result = response.asListFromOrEmpty(A::getResults);

		assertThat(result, equalTo(List.of(BUBU)));
	}

	@Test
	void shouldReturnFirstOnFirstFromOrDefaultWithFunctionIfSuccessful() {
		ApiResponse<A> response = ApiResponse.create(new A(BUBU))
				.status(HttpStatus.OK)
				.build();

		String result = response.firstFromOrDefault(A::getResults, MUMU);

		assertThat(result, equalTo(BUBU));
	}

	@Test
	void shouldReturnDefaultOnFirstFromOrDefaultWithFunctionIfNotSuccessful() {
		ApiResponse<A> response = ApiResponse.create((A) null)
				.status(HttpStatus.NOT_FOUND)
				.build();

		String result = response.firstFromOrDefault(A::getResults, MUMU);

		assertThat(result, equalTo(MUMU));
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
