package org.apiphany.meters;

/**
 * A skeletal base implementation of a {@link Meter}.
 * <p>
 * {@code BasicMeter} provides a common implementation of the {@link #getName()} method and a constructor for setting
 * the meter's name. Subclasses should extend this class to create concrete meter types such as counters and timers.
 * <p>
 * This class is not itself a {@link Meter} implementation, but is intended to reduce duplication across concrete meter
 * classes.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class BasicMeter {

	/**
	 * The meter name.
	 */
	private final String name;

	/**
	 * Creates a new meter with the given {@code name}.
	 *
	 * @param name the name of the meter (must not be {@code null})
	 */
	protected BasicMeter(final String name) {
		this.name = name;
	}

	/**
	 * Returns the name of this meter.
	 *
	 * @return the meter name
	 */
	public String getName() {
		return name;
	}
}
