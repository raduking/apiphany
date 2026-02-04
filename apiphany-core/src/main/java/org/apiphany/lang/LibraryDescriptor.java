package org.apiphany.lang;

import java.util.Objects;

/**
 * Descriptor for a library indicating its presence and associated specific class.
 *
 * @param <T> the type of the specific class associated with the library
 *
 * @author Radu Sebastian LAZIN
 */
public class LibraryDescriptor<T> {

	/**
	 * Indicates if the library is present and the specific class associated with it.
	 */
	private final boolean present;

	/**
	 * The specific class associated with the library.
	 */
	private final Class<T> specificClass;

	/**
	 * Constructor with presence flag and specific class.
	 *
	 * @param present indicates if the library is present
	 * @param specificClass the specific class associated with the library
	 */
	private LibraryDescriptor(final boolean present, final Class<T> specificClass) {
		this.present = present;
		this.specificClass = Objects.requireNonNull(specificClass, "specificClass must not be null");
	}

	/**
	 * Factory method to create a {@link LibraryDescriptor} instance.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param present indicates if the library is present
	 * @param specificClass the specific class associated with the library
	 * @return a new LibraryDescriptor instance
	 */
	public static <T> LibraryDescriptor<T> of(final boolean present, final Class<T> specificClass) {
		return new LibraryDescriptor<>(present, specificClass);
	}

	/**
	 * Checks if the library is present.
	 *
	 * @return true if the library is present, false otherwise
	 */
	public boolean isPresent() {
		return present;
	}

	/**
	 * Retrieves the specific class associated with the library.
	 *
	 * @return the specific class
	 */
	public Class<T> getSpecificClass() {
		return specificClass;
	}
}
