package org.apiphany.meters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apiphany.lang.Pair;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link MeterFactory}.
 *
 * @author Radu Sebastian LAZIN
 */
class MeterFactoryTest {

	private static final String PREFIX = "bubu";
	private static final String NAME = "mumu";

	@Test
	void shouldReturnTrueOnEmptyTags() {
		MeterFactory factory = new MeterFactory();
		boolean empty = factory.isEmpty(Collections.emptyList());

		assertTrue(empty);
	}

	@Test
	void shouldReturnTrueOnNullTags() {
		MeterFactory factory = new MeterFactory();
		boolean empty = factory.isEmpty(null);

		assertTrue(empty);
	}

	@Test
	void shouldReturnFalseOnNonEmptyTags() {
		MeterFactory factory = new MeterFactory();
		boolean empty = factory.isEmpty(List.of("some.tag.name", "value"));

		assertFalse(empty);
	}

	@Test
	void shouldReturnTrueOnEmptyTagsWithHasNext() {
		MeterFactory factory = new MeterFactory();
		Collection<?> tags = mock(Collection.class);
		Iterator<?> iterator = mock(Iterator.class);
		doReturn(iterator).when(tags).iterator();
		doReturn(false).when(iterator).hasNext();

		boolean empty = factory.isEmpty(tags);

		assertTrue(empty);
	}

	@Test
	void shouldCreateTimerWithPrefix() {
		MeterFactory factory = new MeterFactory();

		MeterTimer timer = factory.timer(PREFIX, NAME, List.of("tagName", "tagValue"));
		BasicTimer basicTimer = timer.unwrap(BasicTimer.class);

		assertThat(basicTimer.getName(), equalTo(PREFIX + "." + NAME));
		assertThat(basicTimer.getDuration(), equalTo(null));
	}

	@Test
	void shouldCreateTimerWithTagsArray() {
		MeterFactory factory = new MeterFactory();

		MeterTimer timer = factory.timer(NAME, "tagName", "tagValue");
		BasicTimer basicTimer = timer.unwrap(BasicTimer.class);

		assertThat(basicTimer.getName(), equalTo(NAME));
	}

	@Test
	void shouldCreateCounterWithPrefix() {
		MeterFactory factory = new MeterFactory();

		MeterCounter counter = factory.counter(PREFIX, NAME, List.of("tagName", "tagValue"));
		BasicCounter basicCounter = counter.unwrap(BasicCounter.class);

		assertThat(basicCounter.getName(), equalTo(PREFIX + "." + NAME));
		assertThat(basicCounter.count(), equalTo(0.0));
	}

	@Test
	void shouldCreateCounterWithTagsArray() {
		MeterFactory factory = new MeterFactory();

		MeterCounter counter = factory.counter(NAME, "tagName", "tagValue");
		BasicCounter basicCounter = counter.unwrap(BasicCounter.class);

		assertThat(basicCounter.getName(), equalTo(NAME));
	}

	@Test
	void shouldReturnNewMeterFactoryInstanceOnInitialize() {
		MeterFactory meterFactory = MeterFactory.initializeInstance();

		assertThat(meterFactory.getClass(), equalTo(MeterFactory.class));
	}

	@Test
	void shouldReturnNewMeterFactoryInstanceOnInitializeWithNull() {
		MeterFactory meterFactory = MeterFactory.initializeInstance((Pair<Boolean, Class<? extends MeterFactory>>[]) null);

		assertThat(meterFactory.getClass(), equalTo(MeterFactory.class));
	}

	@Test
	void shouldReturnNewMeterFactoryInstanceOnInitializeWhenLibraryIsNotAvailable() {
		MeterFactory meterFactory = MeterFactory.initializeInstance(Pair.of(Boolean.FALSE, DummyMeterFactory.class));

		assertThat(meterFactory.getClass(), equalTo(MeterFactory.class));
	}

	static class DummyMeterFactory extends MeterFactory {
		// empty
	}
}
