package org.apiphany.lang.builder;

/**
 * A simple builder for constructing canonical class name strings using a delimiter (default is "."). This class extends
 * {@link DelimitedStringBuilder} to provide a specialized builder for canonical class names.
 *
 * @author Radu Sebastian LAZIN
 */
public class CanonicalClassNameBuilder extends DelimitedStringBuilder {

	/**
	 * The default delimiter used to separate class name segments (e.g., package and class names).
	 */
	public static final String DELIMITER = ".";

	/**
	 * Constructs a new {@link CanonicalClassNameBuilder} with the default delimiter.
	 */
	protected CanonicalClassNameBuilder() {
		super(DELIMITER);
	}

	/**
	 * Creates a new instance of {@link CanonicalClassNameBuilder}.
	 *
	 * @return a new {@link CanonicalClassNameBuilder} instance.
	 */
	public static CanonicalClassNameBuilder builder() {
		return new CanonicalClassNameBuilder();
	}

	/**
	 * Overrides the {@link DelimitedStringBuilder#asSuffix()} method to ensure that canonical class names are not built as
	 * suffixes. This method does nothing and returns the current instance.
	 *
	 * @return this {@link CanonicalClassNameBuilder} instance.
	 */
	@Override
	public CanonicalClassNameBuilder asSuffix() {
		// Class names should not be built as suffixes.
		return this;
	}
}
