package org.apiphany.lang.retry;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

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

		waitTimeout1.setStart(START);
		waitTimeout2.setStart(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertTrue(result);
	}

}
