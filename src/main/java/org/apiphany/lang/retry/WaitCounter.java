package org.apiphany.lang.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.thread.Threads;

/**
 * Counter wait implementation. After each iteration, the counter waits for the given interval. If the interval is 0,
 * the counter doesn't wait.
 *
 * @author Radu Sebastian LAZIN
 */
public class WaitCounter implements Wait {

	/**
	 * Default wait counter: 3 times one second apart.
	 */
	public static final WaitCounter DEFAULT = WaitCounter.of(3, Duration.ofSeconds(1));

	/**
	 * Interval between waits
	 */
	private final long interval;

	/**
	 * Interval time unit
	 */
	private final TimeUnit intervalTimeUnit;

	/**
	 * Maximum times to wait.
	 */
	private final int maxCount;

	/**
	 * Counter.
	 */
	private int count;

	/**
	 * Private constructor.
	 *
	 * @param interval interval
	 * @param intervalTimeUnit interval time unit
	 */
	private WaitCounter(final int maxCount, final long interval, final TimeUnit intervalTimeUnit) {
		this.maxCount = maxCount;
		this.interval = interval;
		this.intervalTimeUnit = intervalTimeUnit;
		this.count = 0;
	}

	/**
	 * Wait object builder.
	 *
	 * @param maxCount maximum number of retries
	 * @param interval interval
	 * @param intervalTimeUnit interval time unit
	 * @return wait object
	 */
	public static WaitCounter of(final int maxCount, final long interval, final TimeUnit intervalTimeUnit) {
		return new WaitCounter(maxCount, interval, intervalTimeUnit);
	}

	/**
	 * Wait object builder.
	 *
	 * @param maxCount maximum number of retries
	 * @param interval interval
	 * @return wait object
	 */
	public static WaitCounter of(final int maxCount, final Duration interval) {
		return of(maxCount, interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Waits.
	 */
	@Override
	public void now() {
		Threads.safeSleep(interval, intervalTimeUnit);
	}

	/**
	 * Resets the start time.
	 */
	@Override
	public void start() {
		this.count = 0;
	}

	/**
	 * Returns true if the wait should keep waiting.
	 *
	 * @return true if the wait should keep waiting
	 */
	@Override
	public boolean keepWaiting() {
		return !isOver(++count);
	}

	/**
	 * Returns true if the wait is over.
	 *
	 * @param count current count
	 * @return true, if the wait is over
	 */
	public boolean isOver(final int count) {
		return count >= maxCount;
	}

	/**
	 * Returns a copy.
	 *
	 * @return a copy
	 */
	@Override
	public WaitCounter copy() {
		return WaitCounter.of(maxCount, interval, intervalTimeUnit);
	}

	/**
	 * Equals method that also verifies that objects are of the same class.
	 */
	@Override
	public boolean equals(final Object that) {
		if (this == that) {
			return true;
		}
		if (null == that || that.getClass() != getClass()) {
			return false;
		}
		WaitCounter thatWait = (WaitCounter) that;
		return Objects.equals(interval, thatWait.interval)
				&& Objects.equals(intervalTimeUnit, thatWait.intervalTimeUnit)
				&& Objects.equals(maxCount, thatWait.maxCount)
				&& Objects.equals(count, thatWait.count);
	}

	/**
	 * Hash code implementation.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(interval, intervalTimeUnit, maxCount, count);
	}

}
