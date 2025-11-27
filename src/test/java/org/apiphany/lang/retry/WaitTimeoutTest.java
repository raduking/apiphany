package org.apiphany.lang.retry;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.morphix.lang.thread.Threads;

/**
 * Test class for {@link WaitTimeout}.
 *
 * @author Radu Sebastian LAZIN
 */
class WaitTimeoutTest {

	private static final long TIMEOUT_MS = 1;
	private static final long INTERVAL_MS = 1;
	private static final Duration TIMEOUT = Duration.ofMillis(TIMEOUT_MS);
	private static final Duration INTERVAL = Duration.ofMillis(INTERVAL_MS);
	private static final Instant START = Instant.MIN;

	@Test
	void shouldReturnTrueOnTwoEqualObjects() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertTrue(result);
		assertThat(waitTimeout1.hashCode(), equalTo(waitTimeout2.hashCode()));
	}

	@Test
	void shouldReturnTrueOnEqualsForCopied() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		waitTimeout1.start(START);

		WaitTimeout waitTimeout2 = waitTimeout1.copy();
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertTrue(result);
		assertThat(waitTimeout1.hashCode(), equalTo(waitTimeout2.hashCode()));
	}

	@Test
	void shouldReturnTrueOnEqualsOnTheSameObject() {
		WaitTimeout waitTimeout = WaitTimeout.of(TIMEOUT, INTERVAL);

		boolean result = waitTimeout.equals(waitTimeout);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfTimeoutIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS + 1, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfTimeoutUnitIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.DAYS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfIntervalIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS + 1, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfIntervalUnitIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.DAYS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfStartIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START.plus(1, ChronoUnit.HOURS));

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldSetTheStartTimeOnCallingStart() {
		WaitTimeout waitTimeout = WaitTimeout.of(TIMEOUT, INTERVAL);

		waitTimeout.start();
		Threads.safeSleep(Duration.ofMillis(50));

		WaitTimeout copyWithoutStart = waitTimeout.copy();
		boolean result = waitTimeout.equals(copyWithoutStart);

		assertFalse(result);
	}
}
