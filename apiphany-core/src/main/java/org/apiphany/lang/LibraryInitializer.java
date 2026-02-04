package org.apiphany.lang;

import java.util.function.Supplier;

import org.apiphany.lang.collections.JavaArrays;
import org.morphix.reflection.Constructors;

/**
 * Utility interface for initializing libraries based on their presence in the classpath.
 *
 * @author Radu Sebastian LAZIN
 */
public interface LibraryInitializer {

	/**
	 * Initializes and returns an instance of the first available library from the provided descriptors. If none of the
	 * libraries are present, it uses the fallback supplier to provide a default instance.
	 *
	 * @param <T> the type of the library instance
	 *
	 * @param fallbackSupplier the supplier to provide a default instance if no libraries are present
	 * @param libraryDescriptors the library descriptors to check for presence
	 * @return an instance of the first available library or a default instance from the fallback supplier
	 */
	@SafeVarargs
	static <T> T instance(final Supplier<T> fallbackSupplier, final LibraryDescriptor<? extends T>... libraryDescriptors) {
		if (JavaArrays.isNotEmpty(libraryDescriptors)) {
			for (LibraryDescriptor<? extends T> libraryDescriptor : libraryDescriptors) {
				if (libraryDescriptor.isPresent()) {
					return Constructors.IgnoreAccess.newInstance(libraryDescriptor.getSpecificClass());
				}
			}
		}
		return fallbackSupplier.get();
	}

}
