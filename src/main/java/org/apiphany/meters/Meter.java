package org.apiphany.meters;

import org.morphix.lang.JavaObjects;

/**
 * The base contract for all metric instruments such as counters and timers.
 * <p>
 * A {@code Meter} represents a named measurement source that can be registered with a metrics registry. Subtypes (e.g.
 * {@link MeterCounter}, {@link MeterTimer}) define the concrete type of measurement they support.
 *
 * <h2>Usage example:</h2>
 *
 * <pre>{@code
 * Meter meter = registry.counter("http.requests");
 *
 * // access the meter's name
 * String name = meter.getName();
 *
 * // aafely unwrap to a concrete type
 * MeterCounter counter = meter.unwrap(MeterCounter.class);
 * counter.increment();
 * }</pre>
 *
 * @author Radu Sebastian LAZIN
 */
public interface Meter {

	/**
	 * Returns the name of this meter.
	 * <p>
	 * The name is typically used to identify the metric in the registry and is often combined with tags or labels to
	 * produce unique time series.
	 *
	 * @return the meter name (never {@code null})
	 */
	String getName();

	/**
	 * Attempts to cast this meter to the given concrete type.
	 *
	 * @param cls the expected meter subtype
	 * @param <T> the type of meter to unwrap to
	 * @return this meter cast to the requested type
	 * @throws IllegalArgumentException if this meter is not assignable to {@code cls}
	 */
	default <T> T unwrap(final Class<T> cls) {
		if (cls.isAssignableFrom(this.getClass())) {
			return JavaObjects.cast(this);
		}
		throw new IllegalArgumentException("The meter class " + this.getClass() + " is not of type " + cls);
	}
}
