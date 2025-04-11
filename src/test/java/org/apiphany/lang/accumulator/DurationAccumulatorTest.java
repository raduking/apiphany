package org.apiphany.lang.accumulator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.lang.Temporals;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link DurationAccumulator}.
 *
 * @author Radu Sebastian LAZIN
 */
class DurationAccumulatorTest {

	@Test
	void shouldGeneratePercentile90() {
		DurationAccumulator durationAccumulator = DurationAccumulator.of();

		durationAccumulator.accumulate(() -> false);

		durationAccumulator.buildStatistics();
		var statistics = durationAccumulator.getStatistics();

		assertThat(statistics.getP90RequestTime(), equalTo(Temporals.toDouble(durationAccumulator.getInformationList().get(0))));
	}

}
