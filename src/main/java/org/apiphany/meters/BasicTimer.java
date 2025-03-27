package org.apiphany.meters;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.morphix.lang.Nullables;
import org.morphix.lang.function.ThrowingSupplier;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;

/**
 * A basic timer implementation that does not send values to any metrics service. This is useful when metrics need to be
 * disabled or not available.
 * <p>
 * This timer only holds the last recorded time.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicTimer implements Timer {

	/**
	 * The identifier for this timer, containing name and metadata.
	 */
	private Id id;

	/**
	 * The recorded time amount stored in the timer's base time unit (milliseconds).
	 */
	private long amount;

	/**
	 * Private constructor to enforce use of factory method.
	 */
	private BasicTimer() {
		// empty
	}

	/**
	 * Creates a new BasicTimer instance with the specified name.
	 *
	 * @param name the name for the timer
	 * @return a new BasicTimer instance
	 * @throws NullPointerException if name is null
	 */
	public static BasicTimer of(final String name) {
		BasicTimer timer = new BasicTimer();
		timer.id = new Id(name, Tags.empty(), null, null, Type.TIMER);
		return timer;
	}

	/**
	 * @see Timer#getId()
	 */
	@Override
	public Id getId() {
		return id;
	}

	/**
	 * @see Timer#takeSnapshot()
	 */
	@Override
	public HistogramSnapshot takeSnapshot() {
		return HistogramSnapshot.empty(1, amount, amount);
	}

	/**
	 * @see Timer#record(long, TimeUnit)
	 */
	@Override
	public void record(final long amount, final TimeUnit unit) {
		this.amount = unit.convert(amount, baseTimeUnit());
	}

	/**
	 * @see Timer#record(Supplier)
	 */
	@Override
	public <T> T record(final Supplier<T> f) {
		return Nullables.whenNotNull(f, f);
	}

	/**
	 * @see Timer#recordCallable(Callable)
	 */
	@Override
	public <T> T recordCallable(final Callable<T> f) {
		return Nullables.whenNotNull(f, ThrowingSupplier.unchecked(f::call));
	}

	/**
	 * @see Timer#record(Runnable)
	 */
	@Override
	public void record(final Runnable f) {
		Nullables.whenNotNull(f, f);
	}

	/**
	 * @see Timer#count()
	 */
	@Override
	public long count() {
		return 1;
	}

	/**
	 * @see Timer#totalTime(TimeUnit)
	 */
	@Override
	public double totalTime(final TimeUnit unit) {
		return baseTimeUnit().convert(amount, unit);
	}

	/**
	 * @see Timer#max(TimeUnit)
	 */
	@Override
	public double max(final TimeUnit unit) {
		return baseTimeUnit().convert(amount, unit);
	}

	/**
	 * @see Timer#baseTimeUnit()
	 */
	@Override
	public TimeUnit baseTimeUnit() {
		return TimeUnit.MILLISECONDS;
	}

}
