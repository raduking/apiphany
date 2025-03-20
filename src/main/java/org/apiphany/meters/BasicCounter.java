package org.apiphany.meters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;

/**
 * A basic counter implementation that does not send values to any metrics service. This is useful when metrics need to
 * be disabled or not available.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicCounter implements Counter {

	/**
	 * The counter ID.
	 */
	private Id id;

	/**
	 * The counter value.
	 */
	private double value;

	/**
	 * Hidden constructor.
	 */
	private BasicCounter() {
		// empty
	}

	/**
	 * Returns a new basic counter with the given name.
	 *
	 * @param name counter name
	 * @return a new basic counter with the given name
	 */
	public static BasicCounter of(final String name) {
		BasicCounter counter = new BasicCounter();
		counter.id = new Id(name, Tags.empty(), null, null, Type.COUNTER);
		return counter;
	}

	/**
	 * @see Counter#getId()
	 */
	@Override
	public Id getId() {
		return id;
	}

	/**
	 * @see Counter#increment()
	 */
	@Override
	public void increment(final double amount) {
		this.value += amount;
	}

	/**
	 * @see Counter#count()
	 */
	@Override
	public double count() {
		return value;
	}

}
