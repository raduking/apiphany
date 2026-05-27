package org.apiphany.io.function;

import java.io.IOException;

/**
 * Functional interface for functions that can throw an IOException.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface IOFunction<T, R> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 * @throws IOException if an I/O error occurs
	 */
	R apply(T t) throws IOException;

	/**
	 * Returns a function that always returns its input argument.
	 *
	 * @param <T> the type of the input and output of the function
	 * @return a function that always returns its input argument
	 */
	static <T> IOFunction<T, T> identity() {
		return t -> t;
	}
}
