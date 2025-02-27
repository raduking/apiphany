package org.apiphany.lang.builder;

/**
 * A simple builder for constructing strings delimited by a specified delimiter. This class provides methods to append
 * paths or segments to the string, ensuring proper delimiter placement.
 *
 * @author Radu Sebastian LAZIN
 */
public class DelimitedStringBuilder {

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
	 * Creates a new instance of {@link DelimitedStringBuilder} with the specified delimiter.
	 *
	 * @param delimiter the delimiter to use between segments.
	 * @return a new {@link DelimitedStringBuilder} instance.
	 */
	public static DelimitedStringBuilder builder(final String delimiter) {
		return new DelimitedStringBuilder(delimiter);
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
	public DelimitedStringBuilder path(final String path) {
		if (null == path) {
			throw new IllegalArgumentException("Parameter path should not be null");
		}
		if (shouldAppendDelimiter()) {
			stringBuilder.append(delimiter);
		}
		stringBuilder.append(path);
		return this;
	}

	/**
	 * Appends multiple paths or segments to the constructed string, ensuring proper delimiter placement.
	 *
	 * @param paths the paths or segments to append.
	 * @return this {@link DelimitedStringBuilder} instance for method chaining.
	 * @throws IllegalArgumentException if the provided paths array is null.
	 */
	public DelimitedStringBuilder path(final String... paths) {
		if (null == paths) {
			throw new IllegalArgumentException("Parameter paths should not be null");
		}
		for (String path : paths) {
			path(path);
		}
		return this;
	}

	/**
	 * Configures the builder to treat the delimiter as a suffix (appended at the end).
	 *
	 * @return this {@link DelimitedStringBuilder} instance for method chaining.
	 */
	public DelimitedStringBuilder asSuffix() {
		this.isSuffix = true;
		return this;
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
