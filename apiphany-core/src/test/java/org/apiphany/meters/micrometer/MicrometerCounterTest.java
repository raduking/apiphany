package org.apiphany.meters.micrometer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

/**
 * Test class for {@link MicrometerCounter}.
 *
 * @author Radu Sebastian LAZIN
 */
class MicrometerCounterTest {

	private static final String COUNTER_NAME = "counter.name";

	@Test
	void shouldUnwrapACounterObject() {
		Counter counter = Metrics.globalRegistry.counter(COUNTER_NAME);
		MicrometerCounter micrometerCounter = new MicrometerCounter(counter);

		Counter unwrapped = micrometerCounter.unwrap(Counter.class);

		assertThat(unwrapped, equalTo(micrometerCounter.getCounter()));
	}

	@Test
	void shouldThrowExceptionWhenUnwrappingANonCounterObject() {
		Counter counter = Metrics.globalRegistry.counter(COUNTER_NAME);
		MicrometerCounter micrometerCounter = new MicrometerCounter(counter);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> micrometerCounter.unwrap(Integer.class));

		assertThat(e.getMessage(), equalTo("The meter class " + counter.getClass() + " is not of type " + Integer.class));
	}

	@Test
	void shouldCallMicrometerInternalCountOnCount() {
		Counter counter = mock(Counter.class);
		MicrometerCounter micrometerCounter = new MicrometerCounter(counter);

		micrometerCounter.count();

		verify(counter).count();
	}
}
