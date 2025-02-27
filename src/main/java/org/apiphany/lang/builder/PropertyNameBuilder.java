package org.apiphany.lang.builder;

import org.morphix.lang.JavaObjects;

/**
 * A simple builder for constructing property names using a delimiter (default is "."). This class extends
 * {@link DelimitedStringBuilder} to provide a specialized builder for property names.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyNameBuilder extends DelimitedStringBuilder {

	/**
	 * The default delimiter used to separate property name segments.
	 */
	public static final String DELIMITER = ".";

	/**
	 * Constructs a new {@link PropertyNameBuilder} with the default delimiter.
	 */
	protected PropertyNameBuilder() {
		super(DELIMITER);
	}

	/**
	 * Creates a new instance of {@link PropertyNameBuilder}.
	 *
	 * @return a new {@link PropertyNameBuilder} instance.
	 */
	public static PropertyNameBuilder builder() {
		return new PropertyNameBuilder();
	}

	/**
	 * Creates a new instance of {@link PropertyNameBuilder} and initializes it with the provided paths.
	 *
	 * @param paths the initial paths to add to the property name.
	 * @return a new {@link PropertyNameBuilder} instance with the specified paths.
	 */
	public static PropertyNameBuilder of(final String... paths) {
		return JavaObjects.cast(builder().path(paths));
	}

	/**
	 * Appends a path or segment to the property name, ensuring proper delimiter placement.
	 *
	 * @param path the path or segment to append.
	 * @return this {@link PropertyNameBuilder} instance for method chaining.
	 */
	@Override
	public PropertyNameBuilder path(final String path) {
		return JavaObjects.cast(super.path(path));
	}

	/**
	 * Appends multiple paths or segments to the property name, ensuring proper delimiter placement.
	 *
	 * @param paths the paths or segments to append.
	 * @return this {@link PropertyNameBuilder} instance for method chaining.
	 */
	@Override
	public PropertyNameBuilder path(final String... paths) {
		return JavaObjects.cast(super.path(paths));
	}

	/**
	 * Configures the builder to treat the delimiter as a suffix (appended at the end).
	 *
	 * @return this {@link PropertyNameBuilder} instance for method chaining.
	 */
	@Override
	public PropertyNameBuilder asSuffix() {
		return JavaObjects.cast(super.asSuffix());
	}
}
