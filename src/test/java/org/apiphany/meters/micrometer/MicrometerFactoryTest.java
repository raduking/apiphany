package org.apiphany.meters.micrometer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

/**
 * Test class for {@link MicrometerFactory}.
 *
 * @author Radu Sebastian LAZIN
 */
class MicrometerFactoryTest {

	@Test
	void shouldReturnTrueOnEmptyMicrometerTags() {
		boolean empty = MicrometerFactory.isEmpty(Tags.empty());

		assertTrue(empty);
	}

	@Test
	void shouldReturnTrueOnNullMicrometerTags() {
		boolean empty = MicrometerFactory.isEmpty(null);

		assertTrue(empty);
	}

	@Test
	void shouldReturnFalseOnNonEmptyMicrometerTags() {
		boolean empty = MicrometerFactory.isEmpty(Tags.of("some.tag.name", "value"));

		assertFalse(empty);
	}

	@Test
	void shouldReturnTrueOnEmptyMicrometerTagsWithHasNext() {
		Tags tags = mock(Tags.class);
		@SuppressWarnings("unchecked")
		Iterator<Tag> iterator = mock(Iterator.class);
		doReturn(iterator).when(tags).iterator();
		doReturn(false).when(iterator).hasNext();

		boolean empty = MicrometerFactory.isEmpty(tags);

		assertTrue(empty);
	}

	@Test
	void shouldReturnNullOnToTagsIfInputIsNull() {
		Tags tags = MicrometerFactory.toTags(null);

		assertNull(tags);
	}

	@Test
	void shouldThrowExceptionOnToTagsIfInputCollectionCannotBeConvertedToTagsBecauseOfSize() {
		List<Object> list = List.of(new Object());
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> MicrometerFactory.toTags(list));

		assertThat(e.getMessage(), equalTo("size must be even, it is a set of key=value pairs"));
	}

	@Test
	void shouldThrowExceptionOnToTagsIfInputIterableCannotBeConvertedToTags() {
		Iterable<Object> iterable = new Iterable<>() {
			@Override
			public Iterator<Object> iterator() {
				return null;
			}
		};
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> MicrometerFactory.toTags(iterable));

		assertThat(e.getMessage(), equalTo("Tags class " + iterable.getClass() + " is not supported"));
	}

	@Test
	void shouldReturnEmptyOnToTagsWIthEmptyCollection() {
		Set<Object> set = Set.of();

		Tags tags = MicrometerFactory.toTags(set);

		assertThat(tags, equalTo(Tags.empty()));
	}

	@Test
	void shouldConvertEvenObjectCollectionToTags() {
		List<Integer> list = List.of(1, 2);

		Tags tags = MicrometerFactory.toTags(list);

		Iterator<Tag> tagsIterator = tags.iterator();
		Tag tag = tagsIterator.next();

		assertThat(tag.getKey(), equalTo(list.get(0).toString()));
		assertThat(tag.getValue(), equalTo(list.get(1).toString()));
		assertFalse(tagsIterator.hasNext());
	}
}
