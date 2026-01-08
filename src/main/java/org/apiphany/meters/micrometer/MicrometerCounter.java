package org.apiphany.meters.micrometer;

import java.util.Objects;

import org.apiphany.meters.Meter;
import org.apiphany.meters.MeterCounter;
import org.morphix.lang.JavaObjects;

import io.micrometer.core.instrument.Counter;

/**
 * A {@link MeterCounter} implementation that delegates to a Micrometer {@link Counter}.
 * <p>
 * This class acts as an adapter between the {@code org.apiphany.meters} API and Micrometer's instrumentation library.
 * It allows code written against the {@link MeterCounter} abstraction to be backed by a real Micrometer
 * {@link Counter}.
 *
 * @author Radu Sebastian LAZIN
 */
public class MicrometerCounter implements MeterCounter {

	/**
	 * The Micrometer counter.
	 */
	private final Counter counter;

	/**
	 * Creates a new {@code MicrometerCounter} that wraps the given Micrometer {@link Counter}.
	 *
	 * @param counter the underlying Micrometer counter (must not be {@code null})
	 */
	protected MicrometerCounter(final Counter counter) {
		this.counter = Objects.requireNonNull(counter);
	}

	/**
	 * Factory method for creating a new {@code MicrometerCounter} that wraps the given Micrometer {@link Counter}.
	 *
	 * @param counter the underlying Micrometer counter (must not be {@code null})
	 * @return a new {@code MicrometerCounter} instance
	 */
	public static MicrometerCounter of(final Counter counter) {
		return new MicrometerCounter(counter);
	}

	/**
	 * @see MeterCounter#increment(double)
	 */
	@Override
	public void increment(final double amount) {
		counter.increment(amount);
	}

	/**
	 * @see MeterCounter#count()
	 */
	@Override
	public double count() {
		return counter.count();
	}

	/**
	 * Returns the Micrometer counter.
	 *
	 * @return the Micrometer counter
	 */
	public Counter getCounter() {
		return counter;
	}

	/**
	 * @see Meter#getName()
	 */
	@Override
	public String getName() {
		return counter.getId().getName();
	}

	/**
	 * @see Meter#unwrap(Class)
	 */
	@Override
	public <T> T unwrap(final Class<T> cls) {
		if (Counter.class.isAssignableFrom(cls)) {
			return JavaObjects.cast(counter);
		}
		throw new IllegalArgumentException("The meter class " + counter.getClass() + " is not of type " + cls);
	}
}
