package org.apiphany.lang;

/**
 * Holds a value of type <code>T</code>. This class is useful for legacy/native APIs to be used as output parameters in
 * methods.
 *
 * @param <T> value type
 *
 * @author Radu Sebastian LAZIN
 */
public final class Holder<T> {

	/**
	 * The value contained in the holder.
	 */
	private T value;

	/**
	 * Creates a new holder with a <code>null</code> value.
	 */
	public Holder() {
		// empty
	}

	/**
	 * Create a new holder with the specified value.
	 *
	 * @param value value to be stored in the holder
	 */
	public Holder(final T value) {
		setValue(value);
	}

	/**
	 * Returns the hold value.
	 *
	 * @return the hold value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the hold value.
	 *
	 * @param value value to hold
	 */
	public void setValue(final T value) {
		this.value = value;
	}

	/**
	 * Creates a new holder with no value.
	 *
	 * @param <U> value type
	 *
	 * @return holder
	 */
	public static <U> Holder<U> noValue() {
		return of(null);
	}

	/**
	 * Creates a new holder with the given value
	 *
	 * @param <U> value type
	 *
	 * @param value value to hold
	 * @return holder
	 */
	public static <U> Holder<U> of(final U value) {
		return new Holder<>(value);
	}
}
