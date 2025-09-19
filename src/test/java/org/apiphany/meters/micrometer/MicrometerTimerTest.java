package org.apiphany.meters.micrometer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * Test class for {@link MicrometerTimer}.
 *
 * @author Radu Sebastian LAZIN
 */
class MicrometerTimerTest {

	private static final String TIMER_NAME = "timer.name";

	@Test
	void shouldUnwrapATimerObject() {
		Timer timer = Metrics.globalRegistry.timer(TIMER_NAME);
		MicrometerTimer micrometerTimer = new MicrometerTimer(timer);

		Timer unwraped = micrometerTimer.unwrap(Timer.class);

		assertThat(unwraped, equalTo(micrometerTimer.getTimer()));
	}

	@Test
	void shouldThrowExceptionWhenUnwrappingANonTimerObject() {
		Timer timer = Metrics.globalRegistry.timer(TIMER_NAME);
		MicrometerTimer micrometerTimer = new MicrometerTimer(timer);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> micrometerTimer.unwrap(Integer.class));

		assertThat(e.getMessage(), equalTo("The meter class " + timer.getClass() + " is not of type " + Integer.class));
	}

}
