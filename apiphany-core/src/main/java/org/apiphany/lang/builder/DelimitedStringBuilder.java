package org.apiphany.lang.builder;

import org.morphix.lang.JavaObjects;

/**
 * A simple builder for constructing strings delimited by a specified delimiter. This class provides methods to append
 * paths or segments to the string, ensuring proper delimiter placement.
 *
 * @param <T> the type of the builder, used for method chaining
 *
 * @author Radu Sebastian LAZIN
 */
public class DelimitedStringBuilder<T extends DelimitedStringBuilder<T>> {

	/**
	 * The underlying {@link StringBuilder} used to construct the delimited string.
	 */
	private final StringBuilder stringBuilder = new StringBuilder();

	/**
	 * The delimiter used to separate segments in the constructed string.
	 */
	private final String delimiter;

	/**
	 * Indicates whether the delimiter should be treated as a suffix (appended at the end).
	 */
	private boolean isSuffix = false;

	/**
	 * Constructs a new {@link DelimitedStringBuilder} with the specified delimiter.
	 *
	 * @param delimiter the delimiter to use between segments.
	 */
	protected DelimitedStringBuilder(final String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Returns the current instance cast to the generic type T for method chaining.
	 *
	 * @return this instance cast to type T
	 */
	private T self() {
		return JavaObjects.cast(this);
	}

	/**
	 * Creates a new instance of {@link DelimitedStringBuilder} with the specified delimiter.
	 *
	 * @param <T> the type of the builder, used for method chaining
	 *
	 * @param delimiter the delimiter to use between segments.
	 * @return a new {@link DelimitedStringBuilder} instance.
	 */
	public static <T extends DelimitedStringBuilder<T>> DelimitedStringBuilder<T> builder(final String delimiter) {
		return new DelimitedStringBuilder<>(delimiter);
	}

	/**
	 * Creates a new instance of {@link DelimitedStringBuilder} with the specified delimiter and initializes it with the
	 * provided paths. The first path is the delimiter and is used to initialize the builder, and subsequent paths are
	 * appended.
	 *
	 * @param <T> the type of the builder, used for method chaining
	 *
	 * @param paths the initial paths to add to the constructed string.
	 * @return a new {@link DelimitedStringBuilder} instance with the specified paths.
	 * @throws IllegalArgumentException if the provided paths array is null or empty.
	 */
	public static <T extends DelimitedStringBuilder<T>> DelimitedStringBuilder<T> of(final String... paths) {
		if (null == paths || paths.length == 0) {
			throw new IllegalArgumentException("Parameter paths should not be null or empty");
		}
		DelimitedStringBuilder<T> builder = builder(paths[0]);
		for (int i = 1; i < paths.length; ++i) {
			builder.path(paths[i]);
		}
		return builder;
	}

	/**
	 * Builds and returns the constructed delimited string.
	 *
	 * @return the constructed string.
	 */
	public String build() {
		return stringBuilder.toString();
	}

	/**
	 * Appends a path or segment to the constructed string, ensuring proper delimiter placement.
	 *
	 * @param path the path or segment to append.
	 * @return this {@link DelimitedStringBuilder} instance for method chaining.
	 * @throws IllegalArgumentException if the provided path is null.
	 */
	public T path(final String path) {
		if (null == path) {
			throw new IllegalArgumentException("Parameter path should not be null");
		}
		if (shouldAppendDelimiter()) {
			stringBuilder.append(delimiter);
		}
		stringBuilder.append(path);
		return self();
	}

	/**
	 * Appends multiple paths or segments to the constructed string, ensuring proper delimiter placement.
	 *
	 * @param paths the paths or segments to append.
	 * @return this {@link DelimitedStringBuilder} instance for method chaining.
	 * @throws IllegalArgumentException if the provided paths array is null.
	 */
	public T path(final String... paths) {
		if (null == paths) {
			throw new IllegalArgumentException("Parameter paths should not be null");
		}
		for (String path : paths) {
			path(path);
		}
		return self();
	}

	/**
	 * Configures the builder to treat the delimiter as a suffix meaning that it will be built starting with the delimiter
	 * and the purpose is to append the result to an existing string.
	 *
	 * @return this {@link DelimitedStringBuilder} instance for method chaining.
	 */
	public T asSuffix() {
		this.isSuffix = true;
		return self();
	}

	/**
	 * Determines whether the delimiter should be appended based on the current state of the builder.
	 *
	 * @return true if the delimiter should be appended, false otherwise.
	 */
	protected boolean shouldAppendDelimiter() {
		return isSuffix || !stringBuilder.isEmpty();
	}
}
