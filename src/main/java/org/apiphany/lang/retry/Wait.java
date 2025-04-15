package org.apiphany.lang.retry;

/**
 * Wait interface.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface Wait {

	/**
	 * Waits, it is not called wait because of java object restriction.
	 */
	default void now() {
		// empty
	}

	/**
	 * Starts the waiting process.
	 */
	default void start() {
		// empty
	}

	/**
	 * Returns true if the retry should keep waiting.
	 *
	 * @return true if the retry should keep waiting
	 */
	boolean keepWaiting();

	/**
	 * Returns a copy of the current object. This is needed for thread safety. By default, it doesn't create a copy, so any
	 * class that doesn't implement it is not thread safe.
	 *
	 * @return a copy of the current object
	 */
	default Wait copy() {
		return this;
	}

}
