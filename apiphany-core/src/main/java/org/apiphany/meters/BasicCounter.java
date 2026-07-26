package org.apiphany.meters;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A basic counter implementation that does not send values to any metrics service. This is useful when metrics need to
 * be disabled or not available.
 * <p>
 * This counter is thread-safe. The counter value is stored as a {@code long} bit representation of a {@code double}
 * using {@link AtomicLong} for lock-free atomic updates.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicCounter extends BasicMeter implements MeterCounter {

	/**
	 * The counter value stored as bit-packed double in an AtomicLong for thread-safe atomic updates.
	 */
	private final AtomicLong value = new AtomicLong();

	/**
	 * Hidden constructor.
	 */
	private BasicCounter(final String name) {
		super(name);
	}

	/**
	 * Returns a new basic counter with the given name.
	 *
	 * @param name counter name
	 * @return a new basic counter with the given name
	 */
	public static BasicCounter of(final String name) {
		return new BasicCounter(name);
	}

	/**
	 * @see MeterCounter#increment(double)
	 */
	@Override
	public void increment(final double amount) {
		value.updateAndGet(current -> Double.doubleToLongBits(Double.longBitsToDouble(current) + amount));
	}

	/**
	 * @see MeterCounter#count()
	 */
	@Override
	public double count() {
		return Double.longBitsToDouble(value.get());
	}
}
