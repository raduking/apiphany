package org.apiphany.lang.retry;

import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link WaitCounter}.
 *
 * @author Radu Sebastian LAZIN
 */
class WaitCounterTest {

	private static final long MILLIS = 1;
	private static final Duration INTERVAL = Duration.ofMillis(MILLIS);
	private static final int MAX_COUNT = 3;

	@Test
	void shouldReturnTrueOnTwoEqualObjects() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, INTERVAL);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnTheSameObject() {
		WaitCounter waitCounter = WaitCounter.of(MAX_COUNT, INTERVAL);

		boolean result = waitCounter.equals(waitCounter);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnTwoEqualObjectsWhenCountersAdvance() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, INTERVAL);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		waitCounter1.start();
		waitCounter2.start();

		waitCounter1.keepWaiting();
		waitCounter2.keepWaiting();

		boolean result = waitCounter1.equals(waitCounter2);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWhenOneCounterAdvances() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, INTERVAL);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		waitCounter1.start();
		waitCounter1.keepWaiting();

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentMaxCounts() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, INTERVAL);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT + 1, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentInterval() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS + 1, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentTimeUnits() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.HOURS);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnACopy() {
		WaitCounter waitCounter = WaitCounter.of(MAX_COUNT, INTERVAL);

		WaitCounter waitCounterCopy = waitCounter.copy();

		assertNotSame(waitCounter, waitCounterCopy);
		assertEquals(waitCounter, waitCounterCopy);
	}
}
