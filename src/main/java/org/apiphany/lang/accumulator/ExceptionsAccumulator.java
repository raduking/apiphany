package org.apiphany.lang.accumulator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.Unchecked;

/**
 * Extends the {@link Accumulator} class for exception accumulation.
 *
 * @author Radu Sebastian LAZIN
 */
public class ExceptionsAccumulator extends Accumulator<Exception> {

	/**
	 * After all exceptions are accumulated the last exception is thrown in the {@link #rest()} method. This flag tells the
	 * accumulator if it should wrap the exception in an {@link AccumulatorException}.
	 */
	private final boolean wrapException;

	/**
	 * Flag that tells the accumulator to throw the last caught exception when {@link #rest()} method is called.
	 */
	private final boolean throwException;

	/**
	 * Specifies the exception types accumulated. If this list is empty, all exceptions are accumulated; otherwise only the
	 * types present in this list are accumulated.
	 * <p>
	 * Linked list is used because:
	 * <ul>
	 * <li>it is more efficient in terms of memory consumption</li>
	 * <li>accessing the first and last has O(1) complexity</li>
	 * <li>no random access is needed</li>
	 * </ul>
	 */
	private final List<Class<? super Exception>> exceptionTypes = new LinkedList<>();

	/**
	 * Private constructor.
	 *
	 * @param wrapException flag for exception wrapping
	 * @param throwException flag to throw the exception on {@link #rest()}
	 * @param exceptionTypes exception types to accumulate
	 */
	private ExceptionsAccumulator(final boolean wrapException, final boolean throwException, final Set<Class<?>> exceptionTypes) {
		this.wrapException = wrapException;
		this.throwException = throwException;
		if (null != exceptionTypes) {
			for (Class<?> exceptionType : exceptionTypes) {
				this.exceptionTypes.add(JavaObjects.cast(exceptionType));
			}
		}
	}

	/**
	 * Private constructor with accumulated exception types. If no type is specified, then all exceptions are accumulated,
	 * otherwise only the types given are accumulated.
	 *
	 * @param exceptionTypes exception types to accumulate
	 */
	private ExceptionsAccumulator(final Set<Class<?>> exceptionTypes) {
		this(false, true, exceptionTypes);
	}

	/**
	 * Returns a new exceptions accumulator. If no exception type is specified, then all exceptions are accumulated,
	 * otherwise only the types given are accumulated.
	 *
	 * @param wrapException flag for exception wrapping
	 * @param throwException flag to throw the exception on {@link #rest()}
	 * @param exceptionTypes exception types to accumulate
	 * @return a new exceptions accumulator
	 */
	public static ExceptionsAccumulator of(final boolean wrapException, final boolean throwException, final Set<Class<?>> exceptionTypes) {
		return new ExceptionsAccumulator(wrapException, throwException, exceptionTypes);
	}

	/**
	 * Returns a new exceptions accumulator.
	 *
	 * @param wrapException flag for exception wrapping
	 * @param throwException flag to throw the exception on {@link #rest()}
	 * @return a new exceptions accumulator
	 */
	public static ExceptionsAccumulator of(final boolean wrapException, final boolean throwException) {
		return new ExceptionsAccumulator(wrapException, throwException, Collections.emptySet());
	}

	/**
	 * Returns a new exceptions accumulator with no exception wrapping, accumulation of all exceptions and automatic
	 * throwing of the last exception when {@link #rest()} method is called.
	 * <p>
	 * If no exception type is specified, then all exceptions are accumulated, otherwise only the types given are
	 * accumulated.
	 *
	 * @param exceptionTypes exceptions to accumulate
	 * @return a new exceptions accumulator
	 */
	public static ExceptionsAccumulator of(final Set<Class<?>> exceptionTypes) {
		return new ExceptionsAccumulator(exceptionTypes);
	}

	/**
	 * Returns a new exceptions accumulator with no exception wrapping, accumulation of all exceptions and automatic
	 * throwing of the last exception when {@link #rest()} method is called.
	 *
	 * @return a new exceptions accumulator
	 */
	public static ExceptionsAccumulator of() {
		return of(Collections.emptySet());
	}

	/**
	 * Alias for {@link #getInformationList()}
	 *
	 * @return the exception list
	 */
	public List<Exception> getExceptions() {
		return getInformationList();
	}

	/**
	 * Returns the last accumulated exception.
	 *
	 * @return the last accumulated exception
	 */
	public Exception lastException() {
		return lastInformation();
	}

	/**
	 * Returns true if the accumulator has at least one exception, false otherwise.
	 *
	 * @return true if the accumulator has at least one exception
	 */
	public boolean hasExceptions() {
		return hasInformation();
	}

	/**
	 * @see #accumulate(Runnable)
	 */
	@Override
	public void accumulate(final Runnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			addExceptions(e);
		}
	}

	/**
	 * @see #accumulate(Supplier, Object)
	 */
	@Override
	public <T> T accumulate(final Supplier<T> supplier, final T defaultReturn) {
		try {
			return supplier.get();
		} catch (Exception e) {
			addExceptions(e);
			return defaultReturn;
		}
	}

	/**
	 * @see #rest()
	 */
	@Override
	public void rest() {
		Exception lastException = lastInformation();
		if (null != lastException) {
			if (wrapException) {
				// the cause is by default considered the last exception accumulated
				throw new AccumulatorException(lastException, this);
			}
			if (throwException) {
				Unchecked.reThrow(lastException);
			}
		}
	}

	/**
	 * Returns the exception types.
	 *
	 * @return the exception types
	 */
	public List<Class<Exception>> getExceptionTypes() {
		return JavaObjects.cast(exceptionTypes);
	}

	/**
	 * Adds an exception.
	 *
	 * @param e exception to add
	 */
	private void addExceptions(final Exception e) {
		if (!exceptionTypes.isEmpty() && !exceptionTypes.contains(e.getClass())) {
			Unchecked.reThrow(e);
		}
		getExceptions().add(e);
	}

	/**
	 * Returns the wrap exception flag.
	 *
	 * @return the wrap exception flag
	 */
	public boolean isWrapException() {
		return wrapException;
	}

	/**
	 * Returns the throw exception flag.
	 *
	 * @return the throw exception flag
	 */
	public boolean isThrowException() {
		return throwException;
	}
}
