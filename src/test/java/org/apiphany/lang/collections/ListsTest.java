package org.apiphany.lang.collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Lists}.
 *
 * @author Radu Sebastian LAZIN
 */
class ListsTest {

	private static final int TEST_INT = 10;

	@Test
	void shouldReturnTheSameListOnSafe() {
		List<Integer> list = List.of(1, 2, 3);

		List<Integer> result = Lists.safe(list);

		assertThat(result, equalTo(list));
	}

	@Test
	void shouldReturnEmptyListOnSafeWhenParameterIsNull() {
		List<Integer> result = Lists.safe(null);

		assertThat(result, equalTo(Collections.emptyList()));
	}

	@Test
	void shouldReturnTheFirstElementFromAList() {
		List<Integer> list = List.of(1, 2, 3);

		Integer first = Lists.first(list);

		assertThat(first, equalTo(list.getFirst()));
	}

	@Test
	void shouldReturnNullOnFirstIfListIsNull() {
		Integer first = Lists.first(null);

		assertThat(first, nullValue());
	}

	@Test
	void shouldReturnNullOnFirstIfListIsEmpty() {
		Integer first = Lists.first(List.of());

		assertThat(first, nullValue());
	}

	@Test
	void shouldReturnTheFirstElementFromAListIfListIsNotEmptyWitgDefaultValue() {
		List<Integer> list = List.of(1, 2, 3);

		Integer first = Lists.first(list, TEST_INT);

		assertThat(first, equalTo(list.getFirst()));
	}

	@Test
	void shouldReturnDefaultValueOnFirstIfListIsNull() {
		Integer first = Lists.first(null, TEST_INT);

		assertThat(first, equalTo(TEST_INT));
	}

	@Test
	void shouldReturnDefaultValueOnFirstIfListIsEmpty() {
		Integer first = Lists.first(List.of(), TEST_INT);

		assertThat(first, equalTo(TEST_INT));
	}

	@Test
	void shouldReturnTheLastElementFromAList() {
		List<Integer> list = List.of(1, 2, 3);

		Integer first = Lists.last(list);

		assertThat(first, equalTo(list.getLast()));
	}

	@Test
	void shouldReturnNullOnLastIfListIsNull() {
		Integer first = Lists.last(null);

		assertThat(first, nullValue());
	}

	@Test
	void shouldReturnNullOnLastIfListIsEmpty() {
		Integer first = Lists.last(List.of());

		assertThat(first, nullValue());
	}

	@Test
	void shouldBuildListFromArray() {
		String[] array = new String[] { "mumu", "bubu", "cucu" };

		List<String> list = Lists.asList(array);

		assertThat(list, hasSize(array.length));
		for (int i = 0; i < array.length; ++i) {
			assertThat(list.get(i), equalTo(array[i]));
		}
	}

	@Test
	void shouldBuildEmptyListFromNull() {
		Integer[] array = null;

		List<Integer> list = Lists.asList(array);

		assertThat(list, hasSize(0));
	}

	@Test
	void shouldMerge2SortedLists() {
		List<Integer> l1 = IntStream.rangeClosed(1, 21)
				.filter(n -> n % 2 != 0)
				.boxed()
				.toList();
		List<Integer> l2 = IntStream.rangeClosed(1, 21)
				.filter(n -> n % 2 == 0)
				.boxed()
				.toList();

		List<Integer> expected = IntStream.rangeClosed(1, 21)
				.boxed()
				.toList();

		List<Integer> result = Lists.merge(l1, l2);

		assertThat(result, equalTo(expected));

		result = Lists.merge(l2, l1);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldSkipNullOrEmptyListAndReturnTheOtherOnMerge() {
		List<Integer> l1 = IntStream.rangeClosed(1, 21)
				.filter(n -> n % 2 != 0)
				.boxed()
				.toList();

		List<Integer> result = Lists.merge(l1, null);
		assertThat(result, equalTo(l1));

		result = Lists.merge(null, l1);
		assertThat(result, equalTo(l1));

		result = Lists.merge(null, null);
		assertNull(result);
	}
}
