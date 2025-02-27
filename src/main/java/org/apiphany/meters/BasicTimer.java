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

	private Id id;

	private long amount;

	private BasicTimer() {
		// empty
	}

	public static BasicTimer of(final String name) {
		BasicTimer timer = new BasicTimer();
		timer.id = new Id(name, Tags.empty(), null, null, Type.TIMER);
		return timer;
	}

	@Override
	public Id getId() {
		return id;
	}

	@Override
	public HistogramSnapshot takeSnapshot() {
		return HistogramSnapshot.empty(1, amount, amount);
	}

	@Override
	public void record(final long amount, final TimeUnit unit) {
		this.amount = unit.convert(amount, baseTimeUnit());
	}

	@Override
	public <T> T record(final Supplier<T> f) {
		return Nullables.whenNotNull(f, f);
	}

	@Override
	public <T> T recordCallable(final Callable<T> f) {
		return Nullables.whenNotNull(f, ThrowingSupplier.unchecked(f::call));
	}

	@Override
	public void record(final Runnable f) {
		Nullables.whenNotNull(f, f);
	}

	@Override
	public long count() {
		return 1;
	}

	@Override
	public double totalTime(final TimeUnit unit) {
		return baseTimeUnit().convert(amount, unit);
	}

	@Override
	public double max(final TimeUnit unit) {
		return baseTimeUnit().convert(amount, unit);
	}

	@Override
	public TimeUnit baseTimeUnit() {
		return TimeUnit.MILLISECONDS;
	}

}
