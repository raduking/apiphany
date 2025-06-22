package org.apiphany.lang.accumulator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.apiphany.lang.collections.Lists;
import org.apiphany.lang.retry.Retry;

/**
 * Information accumulator abstract class.<br/>
 * It accumulates information based on a given action.
 * <p>
 * Actions can be given via:<br/>
 * {@link Supplier}, {@link Runnable} classes.
 * <p>
 * Currently used to accumulate exceptions during retries, see {@link Retry}.
 *
 * @param <T> accumulated information type
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class Accumulator<T> {

	/**
	 * Holds the accumulated information
	 */
	private final List<T> list = new CopyOnWriteArrayList<>();

	/**
	 * Hidden constructor.
	 */
	protected Accumulator() {
		// empty
	}

	/**
	 * Information accumulator method.
	 *
	 * @param runnable action
	 */
	public abstract void accumulate(final Runnable runnable);

	/**
	 * Information accumulator method.
	 *
	 * @param <U> supplier type
	 * @param supplier information supplier
	 * @param defaultReturn default return
	 * @return the supplier result or a default return
	 */
	public abstract <U> U accumulate(final Supplier<U> supplier, final U defaultReturn);

	/**
	 * Information accumulator method.
	 *
	 * @param <U> supplier type
	 * @param supplier information supplier
	 * @return the supplier result
	 */
	public <U> U accumulate(final Supplier<U> supplier) {
		return accumulate(supplier, null);
	}

	/**
	 * This method should be called to signal that the accumulator finished accumulating the wanted information.
	 * <p>
	 * Can be implemented in derived classes for any purpose. By default, it clears the accumulated information.
	 */
	public void rest() {
		clear();
	}

	/**
	 * Clears all the accumulated information.
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * Returns all the accumulated information.
	 *
	 * @return all the accumulated information
	 */
	public List<T> getInformationList() {
		return list;
	}

	/**
	 * Returns the first accumulated information, null if no information was accumulated.
	 *
	 * @return the last accumulated information.
	 */
	public T firstInformation() {
		return Lists.first(list);
	}

	/**
	 * Returns the last accumulated information, null if no information was accumulated.
	 *
	 * @return the last accumulated information.
	 */
	public T lastInformation() {
		return Lists.last(list);
	}

	/**
	 * Returns true if the information accumulated is empty.
	 *
	 * @return true if the information accumulated is empty
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Returns true if the information accumulated is not empty. Equivalent to {@link #hasInformation()}.
	 *
	 * @return true if the information accumulated is not empty
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}

	/**
	 * Returns true if the information accumulated is not empty. Equivalent to {@link #isNotEmpty()}.
	 *
	 * @return true if the information accumulated is not empty
	 */
	public boolean hasInformation() {
		return isNotEmpty();
	}

	/**
	 * Returns the size of this accumulator meaning how much information it accumulated.
	 *
	 * @return the size of this accumulator meaning how much information it accumulated
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Returns an empty accumulator. Equivalent to {@link #emptyAccumulator()}
	 *
	 * @param <T> accumulator information type
	 * @return an empty accumulator
	 */
	@SuppressWarnings("unchecked")
	public static <T> Accumulator<T> noAccumulator() {
		return (Accumulator<T>) EmptyAccumulator.EMPTY_ACCUMULATOR;
	}

	/**
	 * Returns an empty accumulator. Equivalent to {@link #noAccumulator()}.
	 *
	 * @param <T> accumulator information type
	 * @return an empty accumulator
	 */
	public static <T> Accumulator<T> emptyAccumulator() {
		return noAccumulator();
	}

	/**
	 * Basic implementation which accumulates nothing.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class EmptyAccumulator extends Accumulator<Object> { // NOSONAR only we need one instance

		private static final EmptyAccumulator EMPTY_ACCUMULATOR = new EmptyAccumulator();

		/**
		 * @see Accumulator#accumulate(Runnable)
		 */
		@Override
		public void accumulate(final Runnable runnable) {
			runnable.run();
		}

		/**
		 * @see Accumulator#accumulate(Supplier, Object)
		 */
		@Override
		public <U> U accumulate(final Supplier<U> supplier, final U defaultReturn) {
			return supplier.get();
		}

	}
}
