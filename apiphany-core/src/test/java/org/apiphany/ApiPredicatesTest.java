package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link ApiPredicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiPredicatesTest {

	private static final String SOME_VALUE = "someValue";

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(ApiPredicates.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldThrowExceptionOnCallingExpectedConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(ApiPredicates.Expected.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldValidateNonEmptyResponse() {
		List<String> list = List.of(SOME_VALUE);

		boolean result = ApiPredicates.responseListIsNotEmpty().test(list);

		assertTrue(result);
	}

	@Test
	void shouldNotValidateEmptyResponse() {
		boolean result = ApiPredicates.responseListIsNotEmpty().test(Collections.emptyList());

		assertFalse(result);
	}

	@Test
	void shouldValidateResponseSize() {
		List<String> list = List.of(SOME_VALUE);

		boolean result = ApiPredicates.responseListHasSize(1).test(list);

		assertTrue(result);
	}

	@Test
	void shouldNotValidateResponseSize() {
		boolean result = ApiPredicates.responseListHasSize(1).test(Collections.emptyList());

		assertFalse(result);
	}

	static class DummyApiPage implements ApiPage<String> {

		private final List<String> content;

		public DummyApiPage(final List<String> content) {
			this.content = content;
		}

		@Override
		public List<String> getContent() {
			return content;
		}

	}

	@Test
	void shouldValidateNonEmptyResponsePage() {
		DummyApiPage page = ApiPage.of(DummyApiPage.class, List.of(SOME_VALUE));

		boolean result = ApiPredicates.responsePageIsNotEmpty().test(page);

		assertTrue(result);
	}

	@Test
	void shouldNotValidateEmptyResponsePage() {
		DummyApiPage page = ApiPage.of(DummyApiPage.class, List.of());

		boolean result = ApiPredicates.responsePageIsNotEmpty().test(page);

		assertFalse(result);
	}

	@Test
	void shouldValidateResponsePageSizeGreaterThan() {
		DummyApiPage page = ApiPage.of(DummyApiPage.class, List.of(SOME_VALUE, SOME_VALUE));

		boolean result = ApiPredicates.responsePageHasSizeGreaterThan(1).test(page);

		assertTrue(result);
	}

	@Test
	void shouldNotValidateResponsePageGreaterThan() {
		DummyApiPage page = ApiPage.of(DummyApiPage.class, List.of());

		boolean result = ApiPredicates.responsePageHasSizeGreaterThan(1).test(page);

		assertFalse(result);
	}

	@Test
	void shouldValidateNonNullResponse() {
		boolean result = ApiPredicates.nonNullResponse().test(SOME_VALUE);

		assertTrue(result);
	}

	@Test
	void shouldNotValidateNullResponse() {
		boolean result = ApiPredicates.nonNullResponse().test(null);

		assertFalse(result);
	}

	@Test
	void shouldValidateResponse() {
		boolean result = ApiPredicates.hasResponse().test(SOME_VALUE);

		assertTrue(result);
	}

	@Test
	void shouldNotValidateResponseForNullResponse() {
		boolean result = ApiPredicates.hasResponse().test(null);

		assertFalse(result);
	}
}
