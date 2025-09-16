package org.apiphany.lang.retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.thread.Threads;

/**
 * Timeout wait implementation.
 *
 * @author Radu Sebastian LAZIN
 */
public class WaitTimeout implements Wait {

	/**
	 * Default timeout: 30 seconds.
	 */
	public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

	/**
	 * Default sleep: 1 second.
	 */
	public static final Duration DEFAULT_SLEEP = Duration.ofSeconds(1);

	/**
	 * Default Wait object.
	 */
	public static final WaitTimeout DEFAULT = WaitTimeout.of(DEFAULT_TIMEOUT, DEFAULT_SLEEP);

	/**
	 * Timeout
	 */
	private final long timeout;

	/**
	 * Time unit for timeout
	 */
	private final TimeUnit timeoutTimeUnit;

	/**
	 * Interval between waits
	 */
	private final long interval;

	/**
	 * Interval time unit
	 */
	private final TimeUnit intervalTimeUnit;

	/**
	 * Start time.
	 */
	private Instant start;

	/**
	 * Private constructor.
	 *
	 * @param timeout timeout
	 * @param timeoutTimeUnit timeout time unit
	 * @param interval interval
	 * @param intervalTimeUnit interval time unit
	 */
	private WaitTimeout(final long timeout, final TimeUnit timeoutTimeUnit, final long interval, final TimeUnit intervalTimeUnit) {
		this.timeout = timeout;
		this.timeoutTimeUnit = timeoutTimeUnit;
		this.interval = interval;
		this.intervalTimeUnit = intervalTimeUnit;
		this.start = Instant.now();
	}

	/**
	 * Wait object builder.
	 *
	 * @param timeout timeout
	 * @param timeoutTimeUnit timeout time unit
	 * @param interval interval
	 * @param intervalTimeUnit interval time unit
	 * @return the wait object
	 */
	public static WaitTimeout of(final long timeout, final TimeUnit timeoutTimeUnit, final long interval, final TimeUnit intervalTimeUnit) {
		return new WaitTimeout(timeout, timeoutTimeUnit, interval, intervalTimeUnit);
	}

	/**
	 * Wait object builder.
	 *
	 * @param timeout timeout
	 * @param interval interval
	 * @return the wait object
	 */
	public static WaitTimeout of(final Duration timeout, final Duration interval) {
		return of(timeout.toMillis(), TimeUnit.MILLISECONDS, interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Waits.
	 */
	@Override
	public void now() {
		if (keepWaiting()) {
			Threads.safeSleep(interval, intervalTimeUnit);
		}
	}

	/**
	 * Resets the start time.
	 */
	@Override
	public void start() {
		start(Instant.now());
	}

	/**
	 * Sets the start time.
	 *
	 * @param start time to set
	 */
	protected void start(final Instant start) {
		this.start = start;
	}

	/**
	 * Returns true if the retry should keep waiting.
	 *
	 * @return true if the retry should keep waiting
	 */
	@Override
	public boolean keepWaiting() {
		return !isOver(start);
	}

	/**
	 * Returns true if the retry is over.
	 *
	 * @param start start time
	 * @return true if the retry is over
	 */
	public boolean isOver(final Instant start) {
		return Instant.now().isAfter(start.plus(timeout, timeoutTimeUnit.toChronoUnit()));
	}

	/**
	 * Returns true if the retry is over.
	 *
	 * @param startTimeEpochMilli start time in epoch milliseconds
	 * @return true if the retry is over
	 */
	public boolean isOver(final long startTimeEpochMilli) {
		return isOver(Instant.ofEpochMilli(startTimeEpochMilli));
	}

	/**
	 * Returns a copy.
	 *
	 * @return a copy
	 */
	@Override
	public WaitTimeout copy() {
		return WaitTimeout.of(timeout, timeoutTimeUnit, interval, intervalTimeUnit);
	}

	/**
	 * Equals method that also verifies that objects are of the same class.
	 *
	 * @param that object to test equality with
	 * @return true if objects are equal, false otherwise
	 */
	@Override
	public boolean equals(final Object that) {
		if (this == that) {
			return true;
		}
		if (null == that || that.getClass() != getClass()) {
			return false;
		}
		WaitTimeout thatWait = (WaitTimeout) that;
		return Objects.equals(timeout, thatWait.timeout)
				&& Objects.equals(timeoutTimeUnit, thatWait.timeoutTimeUnit)
				&& Objects.equals(interval, thatWait.interval)
				&& Objects.equals(intervalTimeUnit, thatWait.intervalTimeUnit)
				&& Objects.equals(start, thatWait.start);
	}

	/**
	 * Hash code implementation.
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(timeout, timeoutTimeUnit, interval, intervalTimeUnit, start);
	}
}
