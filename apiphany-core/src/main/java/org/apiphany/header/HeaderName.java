package org.apiphany.header;

/**
 * Interface representing the name of a header. This is used to provide type safety and consistency when defining header
 * names.
 *
 * @author Radu Sebastian LAZIN
 */
public interface HeaderName {

	/**
	 * Returns the name of the header as a string value.
	 *
	 * @return the name of the header as a string value
	 */
	String value();

	/**
	 * Returns true if the given string matches the enum value ignoring the case, false otherwise. The HTTP headers are
	 * case-insensitive.
	 *
	 * @param header header as string to match
	 * @return true if the given string matches the enum value ignoring the case, false otherwise.
	 */
	default boolean matches(final String header) {
		return value().equalsIgnoreCase(header);
	}
}
