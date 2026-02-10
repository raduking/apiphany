package org.apiphany.lang;

import java.util.Objects;

import org.morphix.reflection.Reflection;

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
	private final boolean libraryPresent;

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
	protected LibraryDescriptor(final boolean present, final Class<T> specificClass) {
		this.libraryPresent = present;
		this.specificClass = Objects.requireNonNull(specificClass, "specificClass must not be null");
	}

	/**
	 * Factory method to create a {@link LibraryDescriptor} instance indicating the library is present.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param specificClass the specific class associated with the library
	 * @return a new LibraryDescriptor instance with presence set to true
	 */
	public static <T> LibraryDescriptor<T> present(final Class<T> specificClass) {
		return new LibraryDescriptor<>(true, specificClass);
	}

	/**
	 * Factory method to create a {@link LibraryDescriptor} instance indicating the library is not present.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param specificClass the specific class associated with the library
	 * @return a new LibraryDescriptor instance with presence set to false
	 */
	public static <T> LibraryDescriptor<T> notPresent(final Class<T> specificClass) {
		return new LibraryDescriptor<>(false, specificClass);
	}

	/**
	 * Factory method to create a {@link LibraryDescriptor} instance.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param libraryClassName the fully qualified class name to check for presence
	 * @param specificClass the specific class associated with the library
	 * @return a new LibraryDescriptor instance
	 */
	public static <T> LibraryDescriptor<T> of(final String libraryClassName, final Class<T> specificClass) {
		return Reflection.isClassPresent(libraryClassName)
				? present(specificClass)
				: notPresent(specificClass);
	}

	/**
	 * Checks if the library is present.
	 *
	 * @return true if the library is present, false otherwise
	 */
	public boolean isLibraryPresent() {
		return libraryPresent;
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
