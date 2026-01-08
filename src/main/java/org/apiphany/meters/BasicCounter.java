package org.apiphany.meters;

/**
 * A basic counter implementation that does not send values to any metrics service. This is useful when metrics need to
 * be disabled or not available.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicCounter extends BasicMeter implements MeterCounter {

	/**
	 * The counter value.
	 */
	private double value;

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
		this.value += amount;
	}

	/**
	 * @see MeterCounter#count()
	 */
	@Override
	public double count() {
		return value;
	}
}
