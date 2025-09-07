package org.apiphany.meters.micrometer;

import java.util.Collection;
import java.util.Collections;

import org.apiphany.lang.Pair;
import org.apiphany.meters.MeterCounter;
import org.apiphany.meters.MeterFactory;
import org.apiphany.meters.MeterTimer;
import org.morphix.lang.JavaObjects;
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
 * <h2>Tags support</h2> Currently, the {@code timer} and {@code counter} methods require that the {@code tags}
 * parameter be a Micrometer {@link Tags} instance. Passing any other {@link Iterable} type will result in a
 * {@link ClassCastException}.
 * <p>
 * In the future, this factory may be extended to automatically convert generic {@link Iterable} tag representations
 * (e.g., key/value pairs) into Micrometer {@link Tags}.
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
	 * A {@link Pair} indicating whether the Micrometer library is present on the classpath, along with the
	 * {@link MeterFactory} implementation class to use if it is available.
	 * <p>
	 * The {@code Boolean} value is {@code true} if Micrometer is detected, {@code false} otherwise.
	 */
	public static final Pair<Boolean, Class<? extends MeterFactory>> MICROMETER_LIBRARY_INFO =
			Pair.of(Reflection.isClassPresent(MICROMETER_METER_CLASS_NAME), MicrometerFactory.class);

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
	 * <p>
	 * <b>Note:</b> the {@code tags} parameter must currently be an instance of Micrometer {@link Tags}. Passing any other
	 * type will result in a {@link ClassCastException}. Future versions may support generic {@link Iterable} tag
	 * representations.
	 */
	@Override
	public <T, U extends Iterable<T>> MeterTimer timer(final String name, final U tags) {
		Timer timer = meterRegistry.timer(name, toTags(tags));
		return MicrometerTimer.of(timer);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a Micrometer-backed {@link MeterCounter} by delegating to {@link MeterRegistry#counter(String, String...)}
	 * using the provided name and tags.
	 * <p>
	 * <b>Note:</b> the {@code tags} parameter must currently be an instance of Micrometer {@link Tags}. Passing any other
	 * type will result in a {@link ClassCastException}. Future versions may support generic {@link Iterable} tag
	 * representations.
	 */
	@Override
	public <T, U extends Iterable<T>> MeterCounter counter(final String name, final U tags) {
		Counter counter = meterRegistry.counter(name, toTags(tags));
		return MicrometerCounter.of(counter);
	}

	/**
	 * @see #isEmpty(Iterable)
	 */
	@Override
	public <T, U extends Iterable<T>> boolean isEmpty(final U tags) {
		if (tags instanceof Tags micrometerTags) {
			return isEmpty(micrometerTags);
		}
		return super.isEmpty(tags);
	}

	/**
	 * Returns true if the tags object is empty, false otherwise.
	 *
	 * @param tags the tags to check
	 * @return true if the tags object is empty, false otherwise
	 */
	public static boolean isEmpty(final Tags tags) {
		if (null == tags) {
			return true;
		}
		if (Tags.empty() == tags) {
			return true;
		}
		return !tags.iterator().hasNext();
	}

	/**
	 * Transforms the given tags to micrometer tags if possible.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param tags generic tags
	 * @return micrometer tags object
	 */
	public static <T, U extends Iterable<T>> Tags toTags(final U tags) {
		if (null == tags) {
			return null;
		}
		if (Collections.emptyList() == tags) {
			return Tags.empty();
		}
		if (tags instanceof Tags micrometerTags) {
			return JavaObjects.cast(micrometerTags);
		}
		if (tags instanceof Collection<?> collection && collection.isEmpty()) {
			return Tags.empty();
		}
		throw new UnsupportedOperationException("Tags class " + tags.getClass() + " is not supported");
	}
}
