package org.apiphany.meters.micrometer;

import java.time.Duration;

import org.apiphany.meters.MeterTimer;
import org.morphix.lang.JavaObjects;

import io.micrometer.core.instrument.Timer;

/**
 * A {@link MeterTimer} implementation that delegates to a Micrometer {@link Timer}.
 * <p>
 * This class acts as an adapter between the {@code org.apiphany.meters} API and Micrometer's instrumentation library.
 * It allows code written against the {@link MeterTimer} abstraction to be backed by a real Micrometer {@link Timer}.
 *
 * @author Radu Sebastian LAZIN
 */
public class MicrometerTimer implements MeterTimer {

	/**
	 * The Micrometer timer.
	 */
	private final Timer timer;

	/**
	 * Creates a new {@code MicrometerTimer} that wraps the given Micrometer {@link Timer}.
	 *
	 * @param timer the underlying Micrometer timer (must not be {@code null})
	 */
	private MicrometerTimer(final Timer timer) {
		this.timer = timer;
	}

	/**
	 * Factory method for creating a new {@code MicrometerTimer} that wraps the given Micrometer {@link Timer}.
	 *
	 * @param timer the underlying Micrometer timer (must not be {@code null})
	 * @return a new {@code MicrometerTimer} instance
	 */
	public static MicrometerTimer of(final Timer timer) {
		return new MicrometerTimer(timer);
	}

	/**
	 * @see #record(Duration)
	 */
	@Override
	public void record(final Duration duration) {
		timer.record(duration);
	}

	/**
	 * Returns the Micrometer timer.
	 *
	 * @return the Micrometer timer
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * @see #getName()
	 */
	@Override
	public String getName() {
		return timer.getId().getName();
	}

	/**
	 * @see #unwrap(Class)
	 */
	@Override
	public <T> T unwrap(final Class<T> cls) {
		if (cls.isAssignableFrom(Timer.class)) {
			return JavaObjects.cast(timer);
		}
		throw new IllegalArgumentException("The meter class " + timer.getClass() + " is not of type " + cls);
	}
}
