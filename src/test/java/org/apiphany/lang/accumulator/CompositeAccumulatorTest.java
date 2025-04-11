package org.apiphany.lang.accumulator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;
import org.morphix.lang.thread.Threads;

/**
 * Test class for {@link CompositeAccumulator}.
 *
 * @author Radu Sebastian LAZIN
 */
class CompositeAccumulatorTest {

	@Test
	void shouldInstantiateWithMoreAccumulators() {
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		DurationAccumulator durationAccumulator = DurationAccumulator.of();

		CompositeAccumulator victim = CompositeAccumulator.of(durationAccumulator, exceptionsAccumulator);

		assertThat(victim.getAccumulators(), hasSize(2));
	}

	@Test
	void shouldAccumulateWithEachAccumulatorInComposite() {
		DurationAccumulator da1 = DurationAccumulator.of();
		DurationAccumulator da2 = DurationAccumulator.of();

		CompositeAccumulator victim = CompositeAccumulator.of(da1, da2);
		victim.accumulate(Threads.doNothing());

		assertThat(victim.getInformationList(), hasSize(2));
	}

	@Test
	void shouldAccumulateWithEachAccumulatorButExceptionsAccumulatorInComposite() {
		DurationAccumulator da1 = DurationAccumulator.of();
		ExceptionsAccumulator ea1 = ExceptionsAccumulator.of();

		CompositeAccumulator victim = CompositeAccumulator.of(da1, ea1);
		victim.accumulate(Threads.doNothing());

		assertThat(victim.getInformationList(), hasSize(1));
	}

	@Test
	void shouldAccumulateWithEachAccumulatorInCompositeIncludingExceptions() throws Exception {
		DurationAccumulator da1 = DurationAccumulator.of();
		ExceptionsAccumulator ea1 = ExceptionsAccumulator.of(false, false);

		CompositeAccumulator victim = CompositeAccumulator.of(ea1, da1);
		victim.accumulate(() -> {
			throw new RuntimeException();
		});

		assertThat(victim.getInformationList(), hasSize(2));
	}
}
