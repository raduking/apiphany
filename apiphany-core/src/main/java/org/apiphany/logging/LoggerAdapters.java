package org.apiphany.logging;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.function.Supplier;

import org.apiphany.logging.slf4j.Slf4jLibrary;
import org.morphix.lang.JavaArrays;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.logging.JulLoggerAdapter;
import org.morphix.reflection.Constructors;
import org.morphix.runtime.OptionalLibrary;

/**
 * Factory for {@link LoggerAdapter} instances with optional library-specific implementations.
 * <p>
 * This class decouples application code from a concrete logging library. When SLF4J is present on the classpath,
 * {@link #of(Class)} returns an SLF4J-backed adapter. Otherwise it falls back to Morphix's {@link JulLoggerAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
public class LoggerAdapters {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private LoggerAdapters() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a {@link LoggerAdapter} for the given class.
	 *
	 * @param clazz the class for which the logger will be created
	 * @return a logger adapter
	 * @throws NullPointerException if the provided class is null
	 */
	public static LoggerAdapter of(final Class<?> clazz) {
		return initializeInstance(clazz, () -> JulLoggerAdapter.of(clazz), Slf4jLibrary.LOGGER_ADAPTER);
	}

	/**
	 * Returns an instance based on the available libraries. The selected implementation class must have a constructor that
	 * takes a {@link Class} argument.
	 *
	 * @param <T> the type of the instance
	 *
	 * @param clazz the class passed to the selected implementation constructor
	 * @param fallbackSupplier the supplier used when no library is present
	 * @param libraryDescriptors the library descriptors
	 * @return an instance of the first available library, or the fallback instance
	 * @throws NullPointerException if {@code clazz} or {@code fallbackSupplier} is null
	 */
	@SafeVarargs
	protected static <T> T initializeInstance(final Class<?> clazz, final Supplier<T> fallbackSupplier,
			final OptionalLibrary<? extends T>... libraryDescriptors) {
		Objects.requireNonNull(clazz, "class must not be null");
		Objects.requireNonNull(fallbackSupplier, "fallbackSupplier must not be null");
		if (JavaArrays.isNotEmpty(libraryDescriptors)) {
			for (OptionalLibrary<? extends T> libraryDescriptor : libraryDescriptors) {
				if (libraryDescriptor.isPresent()) {
					Constructor<? extends T> constructor =
							Constructors.Safe.getDeclared(libraryDescriptor.getFacadeClass(), Class.class);
					return Constructors.IgnoreAccess.newInstance(constructor, clazz);
				}
			}
		}
		return fallbackSupplier.get();
	}
}
