package org.apiphany.meters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.morphix.lang.thread.Threads;

/**
 * Tests for {@link BasicCounter}.
 *
 * @author Radu Sebastian LAZIN
 */
class BasicCounterTest {

	private static final String COUNTER_NAME = "test-counter";

	@Test
	void shouldCreateCounterWithName() {
		BasicCounter counter = BasicCounter.of(COUNTER_NAME);

		assertThat(counter.getName(), equalTo(COUNTER_NAME));
	}

	@Test
	void shouldReturnZeroCountInitially() {
		BasicCounter counter = BasicCounter.of(COUNTER_NAME);

		assertThat(counter.count(), equalTo(0.0));
	}

	@Test
	void shouldIncrementByOne() {
		BasicCounter counter = BasicCounter.of(COUNTER_NAME);

		counter.increment();

		assertThat(counter.count(), equalTo(1.0));
	}

	@Test
	void shouldIncrementByAmount() {
		BasicCounter counter = BasicCounter.of(COUNTER_NAME);

		counter.increment(5.0);

		assertThat(counter.count(), equalTo(5.0));
	}

	@Test
	void shouldIncrementByZero() {
		BasicCounter counter = BasicCounter.of(COUNTER_NAME);

		counter.increment(0.0);

		assertThat(counter.count(), equalTo(0.0));
	}

	@Test
	void shouldAccumulateMultipleIncrements() {
		BasicCounter counter = BasicCounter.of(COUNTER_NAME);

		counter.increment(1.0);
		counter.increment(2.0);
		counter.increment(3.0);

		assertThat(counter.count(), equalTo(6.0));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCountAccuratelyUnderConcurrency() {
		BasicCounter counter = BasicCounter.of(COUNTER_NAME);
		int threadCount = 8;
		int incrementsPerThread = 100_000;
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threadCount);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		try {
			for (int i = 0; i < threadCount; ++i) {
				executor.submit(() -> {
					Threads.safeWait(startLatch, Duration.ofSeconds(30));
					for (int j = 0; j < incrementsPerThread; ++j) {
						counter.increment();
					}
					doneLatch.countDown();
				});
			}
			startLatch.countDown();
			Threads.safeWait(doneLatch, Duration.ofSeconds(30));
		} finally {
			executor.shutdown();
		}

		double expected = (double) threadCount * incrementsPerThread;

		assertThat(counter.count(), equalTo(expected));
	}
}
