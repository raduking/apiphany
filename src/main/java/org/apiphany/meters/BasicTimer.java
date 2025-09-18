package org.apiphany.meters;

import java.time.Duration;
import java.util.Objects;

import io.micrometer.core.instrument.Timer;

/**
 * A basic timer implementation that does not send values to any metrics service. This is useful when metrics need to be
 * disabled or not available.
 * <p>
 * This timer only holds the last recorded time.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicTimer extends BasicMeter implements MeterTimer {

	/**
	 * The recorded duration.
	 */
	private Duration duration;

	/**
	 * Private constructor to enforce use of factory method.
	 */
	private BasicTimer(final String name) {
		super(name);
	}

	/**
	 * Creates a new BasicTimer instance with the specified name.
	 *
	 * @param name the name for the timer
	 * @return a new BasicTimer instance
	 * @throws NullPointerException if name is null
	 */
	public static BasicTimer of(final String name) {
		Objects.requireNonNull(name);
		return new BasicTimer(name);
	}

	/**
	 * @see Timer#record(Duration)
	 */
	@Override
	public void record(final Duration duration) { // NOSONAR
		this.duration = duration;
	}

	/**
	 * Returns the last duration recorded.
	 *
	 * @return the last duration recorded
	 */
	public Duration getDuration() {
		return duration;
	}
}
