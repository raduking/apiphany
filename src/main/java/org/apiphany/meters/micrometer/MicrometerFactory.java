package org.apiphany.meters.micrometer;

import org.apiphany.lang.Pair;
import org.apiphany.meters.MeterCounter;
import org.apiphany.meters.MeterFactory;
import org.apiphany.meters.MeterTimer;
import org.morphix.reflection.Reflection;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * A {@link MeterFactory} implementation that creates meters backed by Micrometer.
 * <p>
 * This class adapts the {@code org.apiphany.meters} abstraction to Micrometer's {@link MeterRegistry}, producing
 * {@link MicrometerCounter} and {@link MicrometerTimer} instances that wrap Micrometer {@link Counter} and
 * {@link Timer} meters.
 * <p>
 * By default, the factory uses the global Micrometer registry ({@link Metrics#globalRegistry}), but a custom
 * {@link MeterRegistry} may be provided via the constructor.
 *
 * <h2>Library presence detection</h2> The static field {@link #MICROMETER_LIBRARY_INFO} can be used to detect at
 * runtime whether the Micrometer library is present on the classpath. This allows code to conditionally enable
 * Micrometer-backed metrics without a hard dependency.
 *
 * @author Radu Sebastian LAZIN
 */
public class MicrometerFactory extends MeterFactory {

	/**
	 * The Micrometer meter class name.
	 */
	private static final String MICROMETER_METER_CLASS_NAME = "io.micrometer.core.instrument.Meter";

	/**
	 * Pair that shows if Micrometer library is present in the class path and the {@link MeterFactory} specific class.
	 */
	public static final Pair<Boolean, Class<? extends MeterFactory>> MICROMETER_LIBRARY_INFO =
			Pair.of(null != Reflection.getClass(MICROMETER_METER_CLASS_NAME), MicrometerFactory.class);

	/**
	 * The Micrometer meter registry.
	 */
	private final MeterRegistry meterRegistry;

	/**
	 * Creates a new {@code MicrometerFactory} backed by the given {@link MeterRegistry}.
	 *
	 * @param meterRegistry the Micrometer registry to use (must not be {@code null})
	 */
	public MicrometerFactory(final MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	/**
	 * Creates a new {@code MicrometerFactory} backed by the global Micrometer registry ({@link Metrics#globalRegistry}).
	 */
	public MicrometerFactory() {
		this(Metrics.globalRegistry);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a Micrometer-backed {@link MeterTimer} by delegating to {@link MeterRegistry#timer(String, String...)} using
	 * the provided name and tags.
	 */
	@Override
	public <T, U extends Iterable<T>> MeterTimer timer(final String name, final U tags) {
		Timer timer = meterRegistry.timer(name, (Tags) tags);
		return MicrometerTimer.of(timer);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a Micrometer-backed {@link MeterCounter} by delegating to {@link MeterRegistry#counter(String, String...)}
	 * using the provided name and tags.
	 */
	@Override
	public <T, U extends Iterable<T>> MeterCounter counter(final String name, final U tags) {
		Counter counter = meterRegistry.counter(name, (Tags) tags);
		return MicrometerCounter.of(counter);
	}
}
