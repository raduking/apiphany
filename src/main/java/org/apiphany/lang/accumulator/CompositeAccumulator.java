package org.apiphany.lang.accumulator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apiphany.lang.collections.Lists;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;

/**
 * Composite accumulator to aggregate multiple accumulators.
 *
 * @author Radu Sebastian LAZIN
 */
public class CompositeAccumulator extends Accumulator<Object> {

	private final List<Accumulator<?>> accumulators = new ArrayList<>();

	/**
	 * Constructor with multiple accumulators.
	 *
	 * @param accumulators accumulators to add to the composite
	 */
	@SafeVarargs
	private CompositeAccumulator(final Accumulator<?>... accumulators) {
		Nullables.whenNotNull(accumulators)
				.then(accs -> this.accumulators.addAll(List.of(accs)));
	}

	/**
	 * Returns a composite accumulator containing the given accumulators.
	 *
	 * @param accumulators the accumulators
	 * @return a composite accumulator containing the given accumulators
	 */
	@SafeVarargs
	public static CompositeAccumulator of(final Accumulator<?>... accumulators) {
		return new CompositeAccumulator(accumulators);
	}

	/**
	 * Returns an empty composite accumulator.
	 *
	 * @return an empty composite accumulator
	 */
	public static CompositeAccumulator of() {
		return of((Accumulator<? super Object>[]) null);
	}

	/**
	 * @see Accumulator#accumulate(Runnable)
	 */
	@Override
	public void accumulate(final Runnable runnable) {
		if (Lists.isEmpty(accumulators)) {
			return;
		}
		Runnable chainRunnable = runnable;
		for (int i = accumulators.size() - 1; i > 0; --i) {
			Accumulator<?> accumulator = accumulators.get(i);
			Runnable tempRunnable = chainRunnable;
			chainRunnable = () -> accumulator.accumulate(tempRunnable);
		}
		Lists.first(accumulators).accumulate(chainRunnable);
	}

	/**
	 * @see Accumulator#accumulate(Supplier, Object)
	 */
	@Override
	public <U> U accumulate(final Supplier<U> supplier, final U defaultReturn) {
		if (Lists.isEmpty(accumulators)) {
			return defaultReturn;
		}
		Supplier<U> chainSupplier = supplier;
		for (int i = accumulators.size() - 1; i > 0; --i) {
			Accumulator<?> accumulator = accumulators.get(i);
			Supplier<U> tempSupplier = chainSupplier;
			chainSupplier = () -> accumulator.accumulate(tempSupplier, defaultReturn);
		}
		return Lists.first(accumulators).accumulate(chainSupplier);
	}

	/**
	 * @see Accumulator#getInformationList()
	 */
	@Override
	public List<Object> getInformationList() {
		return JavaObjects.cast(accumulators.stream()
				.flatMap(a -> a.getInformationList().stream())
				.toList());
	}

	/**
	 * @see Accumulator#rest()
	 */
	@Override
	public void rest() {
		accumulators.forEach(Accumulator::rest);
	}

	/**
	 * @see Accumulator#clear()
	 */
	@Override
	public void clear() {
		accumulators.forEach(Accumulator::clear);
	}

	/**
	 * Returns the contained accumulators.
	 *
	 * @return the contained accumulators
	 */
	public List<Accumulator<Object>> getAccumulators() {
		return JavaObjects.cast(accumulators);
	}

}
