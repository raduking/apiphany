package org.apiphany;

/**
 * Represents an API-specific method that implements the {@link RequestMethod} interface.
 * <p>
 * This record provides a simple way to define custom API methods beyond standard HTTP methods. It includes a predefined
 * {@link #UNDEFINED} constant for representing undefined or unknown methods.
 * <p>
 * The record implements {@link RequestMethod} by providing both {@code toString()} and {@code value()} methods that
 * return the method name.
 *
 * @author Radu Sebastian LAZIN
 */
public record ApiMethod(String name) implements RequestMethod {

	/**
	 * Constant representing an undefined or unknown API method. This instance is created with a {@code null} name.
	 */
	public static final ApiMethod UNDEFINED = ApiMethod.of(null);

	/**
	 * Factory method for creating new {@code ApiMethod} instances.
	 * <p>
	 * This provides a more readable alternative to the record constructor and maintains consistency with common Java
	 * factory patterns.
	 *
	 * @param name the name of the API method (may be {@code null})
	 * @return a new {@code ApiMethod} instance with the given name
	 */
	public static ApiMethod of(String name) {
		return new ApiMethod(name);
	}

	/**
	 * Returns the string representation of this API method.
	 * <p>
	 * This is equivalent to calling {@link #name()} and provides compatibility with string-based method representations.
	 *
	 * @return the name of this API method
	 */
	@Override
	public final String toString() {
		return name();
	}

	/**
	 * Returns the value of this API method as required by {@link RequestMethod}.
	 * <p>
	 * This is equivalent to calling {@link #name()} and provides the method name in a format suitable for request
	 * processing.
	 *
	 * @return the value of this API method
	 */
	@Override
	public String value() {
		return name();
	}
}
