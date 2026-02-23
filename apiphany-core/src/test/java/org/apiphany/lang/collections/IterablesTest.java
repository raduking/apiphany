package org.apiphany.lang.collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Iterables}.
 *
 * @author Radu Sebastian LAZIN
 */
class IterablesTest {

	private static final String ELEMENT = "element";

	@Test
	void shouldReturnEmptyIterableIfNull() {
		Iterable<String> iterable = null;

		assertNotNull(Iterables.safe(iterable));
		assertFalse(Iterables.safe(iterable).iterator().hasNext());
	}

	@Test
	void shouldReturnEmptyCollectionIfNull() {
		Collection<String> collection = null;

		assertNotNull(Iterables.safe(collection));
		assertFalse(Iterables.safe(collection).iterator().hasNext());
	}

	@Test
	void shouldReturnSameIterableIfNotNull() {
		Iterable<String> iterable = List.of(ELEMENT);

		assertNotNull(Iterables.safe(iterable));

		Iterator<String> iterator = Iterables.safe(iterable).iterator();
		assertTrue(iterator.hasNext());
		assertThat(iterator.next(), equalTo(ELEMENT));
	}

	@Test
	void shouldReturnSameCollectionIfNotNull() {
		Collection<String> collection = Lists.asList(ELEMENT);

		assertNotNull(Iterables.safe(collection));

		Iterator<String> iterator = Iterables.safe(collection).iterator();
		assertTrue(iterator.hasNext());
		assertThat(iterator.next(), equalTo(ELEMENT));
	}
}
