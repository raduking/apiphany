package org.apiphany.meters;

import java.util.Collections;
import java.util.List;

import org.apiphany.lang.LibraryDescriptor;
import org.apiphany.lang.builder.PropertyNameBuilder;
import org.apiphany.meters.micrometer.MicrometerLibrary;
import org.morphix.reflection.Constructors;

/**
 * Factory for creating {@link MeterCounter} and {@link MeterTimer} instances with optional tags.
 * <p>
 * This abstraction decouples application code from the underlying meter implementations (e.g. {@code BasicCounter},
 * {@code BasicTimer}) and provides a convenient way to attach tags to meters.
 * <p>
 * Tags are provided in a generic form ({@code Iterable<T>} or {@code T...}), allowing flexibility in the types used to
 * represent them. The default implementation ignores tags, but they are accepted for API compatibility and future
 * extensions.
 *
 * @author Radu Sebastian LAZIN
 */
public class MeterFactory {

	/**
	 * The instance holder nested class.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * The meter factory.
		 */
		private static final MeterFactory METER_FACTORY = initializeInstance(MicrometerLibrary.DESCRIPTOR);
	}

	/**
	 * Returns an instance based on the available meter libraries.
	 *
	 * @param libraries the library descriptor list
	 * @return a meter factory
	 */
	@SafeVarargs
	protected static MeterFactory initializeInstance(final LibraryDescriptor<? extends MeterFactory>... libraries) {
		if (null != libraries) {
			for (LibraryDescriptor<? extends MeterFactory> library : libraries) {
				if (library.isPresent()) {
					return Constructors.IgnoreAccess.newInstance(library.getSpecificClass());
				}
			}
		}
		return new MeterFactory();
	}

	/**
	 * Default constructor.
	 */
	protected MeterFactory() {
		// empty
	}

	/**
	 * Returns a singleton instance.
	 *
	 * @return a singleton instance
	 */
	public static MeterFactory instance() {
		return InstanceHolder.METER_FACTORY;
	}

	/**
	 * Creates a new {@link MeterTimer} with the given {@code name} and {@code tags}.
	 * <p>
	 * Tags are accepted as a generic {@link Iterable}, but may be ignored by the underlying implementation.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param name the meter name (must not be {@code null})
	 * @param tags the tags to associate with the meter (can be ignored)
	 * @return a new timer instance
	 */
	public <T, U extends Iterable<T>> MeterTimer timer(final String name, final U tags) {
		return BasicTimer.of(name);
	}

	/**
	 * Creates a new {@link MeterTimer} with the given {@code prefix}, {@code name} and {@code tags}.
	 * <p>
	 * Tags are accepted as a generic {@link Iterable}, but may be ignored by the underlying implementation.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the prefix for the meter name
	 * @param name the meter name (must not be {@code null})
	 * @param tags the tags to associate with the meter (can be ignored)
	 * @return a new timer instance
	 */
	public <T, U extends Iterable<T>> MeterTimer timer(final String prefix, final String name, final U tags) {
		return timer(String.join(PropertyNameBuilder.DELIMITER, prefix, name), tags);
	}

	/**
	 * Creates a new {@link MeterCounter} with the given {@code name} and {@code tags}.
	 * <p>
	 * Tags are accepted as a generic {@link Iterable}, but may be ignored by the underlying implementation.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param name the meter name (must not be {@code null})
	 * @param tags the tags to associate with the meter (can be ignored)
	 * @return a new counter instance
	 */
	public <T, U extends Iterable<T>> MeterCounter counter(final String name, final U tags) {
		return BasicCounter.of(name);
	}

	/**
	 * Creates a new {@link MeterCounter} with the given {@code prefix}, {@code name} and {@code tags}.
	 * <p>
	 * Tags are accepted as a generic {@link Iterable}, but may be ignored by the underlying implementation.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the prefix for the meter name
	 * @param name the meter name (must not be {@code null})
	 * @param tags the tags to associate with the meter (can be ignored)
	 * @return a new counter instance
	 */
	public <T, U extends Iterable<T>> MeterCounter counter(final String prefix, final String name, final U tags) {
		return counter(String.join(PropertyNameBuilder.DELIMITER, prefix, name), tags);
	}

	/**
	 * Creates a new {@link MeterTimer} with the given {@code name} and {@code tags}.
	 * <p>
	 * Tags are accepted as varargs for convenience, and internally converted to a {@link List}.
	 *
	 * @param <T> the tag element type
	 *
	 * @param name the meter name (must not be {@code null})
	 * @param tags the tags to associate with the meter (can be ignored)
	 * @return a new timer instance
	 */
	@SuppressWarnings("unchecked")
	public <T> MeterTimer timer(final String name, final T... tags) {
		return timer(name, List.of(tags));
	}

	/**
	 * Creates a new {@link MeterCounter} with the given {@code name} and {@code tags}.
	 * <p>
	 * Tags are accepted as varargs for convenience, and internally converted to a {@link List}.
	 *
	 * @param <T> the tag element type
	 *
	 * @param name the meter name (must not be {@code null})
	 * @param tags the tags to associate with the meter (can be ignored)
	 * @return a new counter instance
	 */
	@SuppressWarnings("unchecked")
	public <T> MeterCounter counter(final String name, final T... tags) {
		return counter(name, List.of(tags));
	}

	/**
	 * Returns true if the tags object is empty, false otherwise.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param tags the tags to check
	 * @return true if the tags object is empty, false otherwise
	 */
	public <T, U extends Iterable<T>> boolean isEmpty(final U tags) {
		if (tags == null) {
			return true;
		}
		if (tags == Collections.emptyList()) {
			return true;
		}
		return !tags.iterator().hasNext();
	}
}
