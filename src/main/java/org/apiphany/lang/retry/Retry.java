package org.apiphany.lang.retry;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiphany.lang.accumulator.Accumulator;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.function.Runnables;

/**
 * A basic configurable retry implementation.
 *
 * @author Radu Sebastian LAZIN
 */
public class Retry {

	/**
	 * Default retry object.
	 */
	public static final Retry DEFAULT = Retry.of(WaitTimeout.DEFAULT);

	/**
	 * No wait object
	 */
	public static final Wait NO_WAIT = () -> false;

	/**
	 * No retry object
	 */
	public static final Retry NO_RETRY = Retry.of(NO_WAIT);

	/**
	 * Wait prototype object which will serve as the source for copies.
	 */
	private final Wait waitPrototype;

	/**
	 * Private constructor.
	 *
	 * @param wait wait object
	 */
	private Retry(final Wait wait) {
		this.waitPrototype = wait;
	}

	/**
	 * Builds a retry object.
	 *
	 * @param wait wait object that configures wait intervals
	 * @return a retry object
	 */
	public static Retry of(final Wait wait) {
		return new Retry(wait);
	}

	/**
	 * Returns the default retry.
	 *
	 * @return the default retry
	 */
	public static Retry defaultRetry() {
		return DEFAULT;
	}

	/**
	 * Retries the {@link Supplier#get()} until the predicate is satisfied or the timeout is reached.
	 *
	 * @param <T> result type
	 *
	 * @param resultSupplier result supplier
	 * @param exitCondition end predicate
	 * @return result from supplier
	 */
	public <T> T when(final Supplier<T> resultSupplier, final Predicate<T> exitCondition) {
		return when(resultSupplier, exitCondition, Runnables.doNothing());
	}

	/**
	 * Retries the {@link Supplier#get()} until the predicate is satisfied or the timeout is reached.
	 *
	 * @param <T> result type
	 *
	 * @param resultSupplier result supplier
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @return result from supplier
	 */
	public <T> T when(final Supplier<T> resultSupplier, final Predicate<T> exitCondition, final Runnable beforeWait) {
		return when(resultSupplier, exitCondition, e -> beforeWait.run());
	}

	/**
	 * Retries the {@link Supplier#get()} until the predicate is satisfied or the timeout is reached.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier result supplier
	 * @param exitCondition end predicate
	 * @param accumulator information accumulator
	 * @return result from supplier
	 */
	public <T, U> T when(final Supplier<T> resultSupplier, final Predicate<T> exitCondition, final Accumulator<U> accumulator) {
		return when(resultSupplier, exitCondition, Consumers.consumeNothing(), accumulator);
	}

	/**
	 * Retries the {@link Supplier#get()} until the predicate is satisfied or the timeout is reached.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier result supplier
	 * @param exitCondition end predicate
	 * @param accumulatorSupplier information accumulator supplier
	 * @return result from supplier
	 */
	public <T, U> T when(final Supplier<T> resultSupplier, final Predicate<T> exitCondition, final Supplier<Accumulator<U>> accumulatorSupplier) {
		return when(resultSupplier, exitCondition, Consumers.noConsumer(), accumulatorSupplier);
	}

	/**
	 * Retries the {@link Supplier#get()} until the predicate is satisfied or the timeout is reached.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier result supplier
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @return result from supplier
	 */
	public <T, U> T when(final Supplier<T> resultSupplier, final Predicate<T> exitCondition, final Consumer<U> beforeWait,
			final Accumulator<U> accumulator) {
		return when(resultSupplier, Consumers.noBiConsumer(), exitCondition, beforeWait, accumulator);
	}

	/**
	 * Retries the {@link Supplier#get()} until the predicate is satisfied or the timeout is reached.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier result supplier
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @return result from supplier
	 */
	public <T, U> T when(final Supplier<T> resultSupplier, final Predicate<T> exitCondition, final Consumer<U> beforeWait) {
		return when(resultSupplier, exitCondition, beforeWait, Accumulator.noAccumulator());
	}

	/**
	 * Retries the {@link Supplier#get()} until the predicate is satisfied or the timeout is reached.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier result supplier
	 * @param afterResult code to run after the result supplier was called and accumulator accumulated the value
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @return result from supplier
	 */
	public <T, U> T when(final Supplier<T> resultSupplier, final BiConsumer<T, U> afterResult, final Predicate<T> exitCondition,
			final Consumer<U> beforeWait, final Accumulator<U> accumulator) {
		if (this == NO_RETRY) {
			return whenNoRetry(resultSupplier, afterResult, accumulator);
		}
		T result;
		boolean successful;

		Wait wait = waitPrototype.copy();
		wait.start();
		do {
			result = accumulator.accumulate(resultSupplier);

			U lastAccumulated = accumulator.lastInformation();
			afterResult.accept(result, lastAccumulated);

			successful = exitCondition.test(result);

			if (!successful) {
				beforeWait.accept(lastAccumulated);
				wait.now();
			}
		} while (!successful && wait.keepWaiting());

		if (!successful && accumulator.isNotEmpty()) {
			accumulator.rest();
		}

		return result;
	}

	/**
	 * Returns the supplied result with accumulated information when no retry is given.
	 *
	 * @param <T> result type
	 * @param <U> accumulated information type
	 *
	 * @param resultSupplier result supplier
	 * @param afterResult code to run after the result supplier was called and accumulator accumulated the value
	 * @param accumulator information accumulator
	 * @return result from supplier
	 */
	private static <T, U> T whenNoRetry(final Supplier<T> resultSupplier, final BiConsumer<T, U> afterResult, final Accumulator<U> accumulator) {
		T result = accumulator.accumulate(resultSupplier);
		afterResult.accept(result, accumulator.lastInformation());
		if (accumulator.isNotEmpty()) {
			accumulator.rest();
		}
		return result;
	}

	/**
	 * Retries the {@link Supplier#get()} until the predicate is satisfied or the timeout is reached.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier result supplier
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @param accumulatorSupplier accumulator supplier
	 * @return result from supplier
	 */
	public <T, U> T when(
			final Supplier<T> resultSupplier,
			final Predicate<T> exitCondition,
			final Consumer<U> beforeWait,
			final Supplier<Accumulator<U>> accumulatorSupplier) {
		return when(resultSupplier, exitCondition, beforeWait, accumulatorSupplier.get());
	}

	/**
	 * Returns a wait that does not wait.
	 *
	 * @return no wait
	 */
	public static Wait noWait() {
		return NO_WAIT;
	}

	/**
	 * Returns a non-null object.
	 * <p>
	 * This method should be used when a retry is needed on a {@link Runnable} and since most retries check for a non
	 * <code>null</code> result this is a handy way to transform the runnable into a supplier that returns non {@code null}
	 * since the runnable doesn't have a return value.
	 * <p>
	 * The method effectively returns an empty {@link Optional}.
	 *
	 * @return a non null object
	 */
	public static Object nonNull() {
		return Optional.empty();
	}

	/**
	 * Equals method that also verifies that objects are of the same class.
	 */
	@Override
	public boolean equals(final Object that) {
		if (this == that) {
			return true;
		}
		if (null == that || that.getClass() != getClass()) {
			return false;
		}
		Retry thatRetry = (Retry) that;
		return Objects.equals(waitPrototype, thatRetry.waitPrototype);
	}

	/**
	 * Hash code implementation.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(waitPrototype);
	}

	/**
	 * Returns a fluent {@link Retry} adapter for fluent style API.
	 *
	 * @param <T> result type
	 * @param <U> accumulated information type
	 * @return fluent retry adapter
	 */
	public <T, U> FluentRetry<T, U> fluent() {
		return new FluentRetry<>(this);
	}

	/**
	 * A fluent adapter for the {@link Retry} class, providing a more expressive way to configure and execute retry logic.
	 * This class allows for chaining methods to define exit conditions, pre-wait actions, and accumulation of results.
	 *
	 * @param <T> the type of the result produced by the retry operation.
	 * @param <U> the type of the accumulated information during retries.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class FluentRetry<T, U> {

		/**
		 * The underlying {@link Retry} instance used to execute the retry logic.
		 */
		private final Retry retry;

		/**
		 * The condition that determines when to stop retrying.
		 */
		private Predicate<T> exitCondition = Objects::nonNull;

		/**
		 * An action to execute before waiting between retries.
		 */
		private Runnable doBeforeWait = Runnables.doNothing();

		/**
		 * A consumer to process accumulated information before waiting between retries.
		 */
		private Consumer<U> consumeBeforeWait = Consumers.consumeNothing();

		/**
		 * The accumulator used to collect information during retries.
		 */
		private Accumulator<U> accumulator = null;

		/**
		 * Constructs a new {@link FluentRetry} instance with the specified {@link Retry} configuration.
		 *
		 * @param retry the {@link Retry} instance to use for retry logic.
		 */
		private FluentRetry(final Retry retry) {
			this.retry = retry;
		}

		/**
		 * Sets the condition that determines when to stop retrying.
		 *
		 * @param exitCondition the predicate that evaluates whether to stop retrying.
		 * @return this {@link FluentRetry} instance for method chaining.
		 */
		public FluentRetry<T, U> stopWhen(final Predicate<T> exitCondition) {
			this.exitCondition = exitCondition;
			return this;
		}

		/**
		 * Sets the action to execute before waiting between retries.
		 *
		 * @param doBeforeWait the action to execute before waiting.
		 * @return this {@link FluentRetry} instance for method chaining.
		 */
		public FluentRetry<T, U> doBeforeWait(final Runnable doBeforeWait) {
			this.doBeforeWait = doBeforeWait;
			return this;
		}

		/**
		 * Sets the consumer to process accumulated information before waiting between retries.
		 *
		 * @param consumeBeforeWait the consumer to process accumulated information.
		 * @return this {@link FluentRetry} instance for method chaining.
		 */
		public FluentRetry<T, U> consumeBeforeWait(final Consumer<U> consumeBeforeWait) {
			this.consumeBeforeWait = consumeBeforeWait;
			return this;
		}

		/**
		 * Sets the accumulator used to collect information during retries.
		 *
		 * @param <A> the accumulator type
		 *
		 * @param accumulator the accumulator to use for collecting information.
		 * @return this {@link FluentRetry} instance for method chaining.
		 */
		public <A extends Accumulator<U>> FluentRetry<T, U> accumulateWith(final A accumulator) {
			this.accumulator = accumulator;
			return this;
		}

		/**
		 * Executes the retry logic using the provided result supplier.
		 *
		 * @param resultSupplier the supplier that produces the result to evaluate.
		 * @return the result of the retry operation.
		 */
		public T on(final Supplier<T> resultSupplier) {
			if (null == accumulator) {
				return retry.when(resultSupplier, exitCondition, doBeforeWait);
			}
			return retry.when(resultSupplier, exitCondition, consumeBeforeWait, accumulator);
		}
	}
}
