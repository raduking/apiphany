package org.apiphany.meters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.morphix.lang.thread.Threads;

/**
 * Tests for {@link BasicTimer}.
 *
 * @author Radu Sebastian LAZIN
 */
class BasicTimerTest {

	private static final String TIMER_NAME = "test-timer";

	@Test
	void shouldCreateTimerWithName() {
		BasicTimer timer = BasicTimer.of(TIMER_NAME);

		assertThat(timer.getName(), equalTo(TIMER_NAME));
	}

	@Test
	void shouldRecordDuration() {
		BasicTimer timer = BasicTimer.of(TIMER_NAME);
		Duration expected = Duration.ofMillis(42);

		timer.record(expected);

		assertThat(timer.getDuration(), equalTo(expected));
	}

	@Test
	void shouldOverwritePreviousDuration() {
		BasicTimer timer = BasicTimer.of(TIMER_NAME);

		timer.record(Duration.ofMillis(10));
		timer.record(Duration.ofMillis(20));

		assertThat(timer.getDuration(), equalTo(Duration.ofMillis(20)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldVisibilityAcrossThreads() throws InterruptedException {
		BasicTimer timer = BasicTimer.of(TIMER_NAME);
		Duration expected = Duration.ofMillis(99);
		CountDownLatch writeLatch = new CountDownLatch(1);
		CountDownLatch readLatch = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		executor.submit(() -> {
			Threads.safeWait(writeLatch, Duration.ofSeconds(5));
			timer.record(expected);
			readLatch.countDown();
		});

		final Duration[] observed = new Duration[1];
		executor.submit(() -> {
			Threads.safeWait(readLatch, Duration.ofSeconds(5));
			observed[0] = timer.getDuration();
		});

		writeLatch.countDown();
		executor.shutdown();
		boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);

		assertThat(finished, equalTo(true));

		// duration must be visible across threads
		assertThat(observed[0], notNullValue());
		assertThat(observed[0], equalTo(expected));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldVolatileEnsureVisibilityWithoutSynchronizationBarriers() throws InterruptedException {
		BasicTimer timer = BasicTimer.of(TIMER_NAME);
		Duration expected = Duration.ofMillis(42);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		// writer thread records the duration — no latch between writer and reader
		executor.submit(() -> {
			Threads.safeSleep(Duration.ofMillis(10));
			timer.record(expected);
		});

		// reader thread spins checking getDuration() without any synchronization barrier
		// without volatile the JIT may hoist the read or the CPU cache may stall,
		// causing the reader to spin indefinitely on a stale null
		final Duration[] observed = new Duration[1];
		executor.submit(() -> {
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
			while (System.nanoTime() < deadline) {
				Duration d = timer.getDuration();
				if (d != null) {
					observed[0] = d;
					return;
				}
				Thread.onSpinWait();
			}
		});

		executor.shutdown();
		boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

		assertThat(finished, equalTo(true));

		// duration must be visible without synchronization barriers
		assertThat(observed[0], notNullValue());
		assertThat(observed[0], equalTo(expected));
	}
}
