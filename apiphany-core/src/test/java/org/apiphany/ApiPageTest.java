package org.apiphany;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiPage}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiPageTest {

	private static final String SOME_VALUE = "someValue";

	@Test
	void shouldReturnContent() {
		List<String> list = List.of(SOME_VALUE);

		DummyApiPage page = new DummyApiPage(list);

		assertThat(page.getContent(), equalTo(list));
	}

	@Test
	void shouldReturnFirst() {
		List<String> list = List.of(SOME_VALUE);

		DummyApiPage page = new DummyApiPage(list);

		assertThat(ApiPage.first(page), equalTo(SOME_VALUE));
	}

	@Test
	void shouldReturnLast() {
		List<String> list = List.of(SOME_VALUE);

		DummyApiPage page = new DummyApiPage(list);

		assertThat(ApiPage.last(page), equalTo(SOME_VALUE));
	}

	@Test
	void shouldReturnNullOnFirstForNullPage() {
		assertThat(ApiPage.first(null), nullValue());
	}

	@Test
	void shouldReturnNullOnLastForNullPage() {
		assertThat(ApiPage.last(null), nullValue());
	}

	static class DummyApiPage implements ApiPage<String> {

		private final List<String> content;

		DummyApiPage(final List<String> content) {
			this.content = content;
		}

		@Override
		public List<String> getContent() {
			return content;
		}

	}

}
