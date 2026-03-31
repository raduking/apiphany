package org.apiphany.lang.builder;

/**
 * A simple builder for constructing property names using a delimiter (default is "."). This class extends
 * {@link DelimitedStringBuilder} to provide a specialized builder for property names.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyNameBuilder extends DelimitedStringBuilder<PropertyNameBuilder> {

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
	@SuppressWarnings("unchecked")
	public static PropertyNameBuilder of(final String... paths) {
		return builder().path(paths);
	}
}
