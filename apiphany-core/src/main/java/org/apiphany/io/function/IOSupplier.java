package org.apiphany.io.function;

import java.io.IOException;

/**
 * Represents a supplier of results that can throw an {@link IOException}. This is a functional interface whose
 * functional method is {@link #get()}.
 *
 * @param <T> the type of results supplied by this supplier
 *
 * @author Radu Sebastian LAZIN
 */
public interface IOSupplier<T> {

	/**
	 * Gets a result.
	 *
	 * @return a result
	 * @throws IOException if unable to produce a result
	 */
	T get() throws IOException;
}
