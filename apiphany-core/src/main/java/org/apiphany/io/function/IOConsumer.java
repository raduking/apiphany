package org.apiphany.io.function;

import java.io.IOException;

/**
 * Represents a consumer that can throw an {@link IOException}. This is a functional interface, allowing it to be used
 * in lambda expressions or method references where an I/O operation is performed.
 *
 * @param <T> the type of the input to the operation
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface IOConsumer<T> {

	/**
	 * Performs this operation on the given argument, potentially throwing an {@link IOException}.
	 *
	 * @param arg the input argument
	 * @throws IOException if an I/O error occurs
	 */
	void accept(T arg) throws IOException;
}
