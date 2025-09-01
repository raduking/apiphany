package org.apiphany.meters;

import java.util.List;

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
	 * Default constructor.
	 */
	public MeterFactory() {
		// empty
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
	 * @param tags the tags to associate with the meter (may be ignored)
	 * @return a new timer instance
	 */
	public <T, U extends Iterable<T>> MeterTimer timer(final String name, final U tags) {
		return BasicTimer.of(name);
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
	 * @param tags the tags to associate with the meter (may be ignored)
	 * @return a new counter instance
	 */
	public <T, U extends Iterable<T>> MeterCounter counter(final String name, final U tags) {
		return BasicCounter.of(name);
	}

	/**
	 * Creates a new {@link MeterTimer} with the given {@code name} and {@code tags}.
	 * <p>
	 * Tags are accepted as varargs for convenience, and internally converted to a {@link List}.
	 *
	 * @param name the meter name (must not be {@code null})
	 * @param tags the tags to associate with the meter (may be ignored)
	 * @param <T> the tag element type
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
	 * @param name the meter name (must not be {@code null})
	 * @param tags the tags to associate with the meter (may be ignored)
	 * @param <T> the tag element type
	 * @return a new counter instance
	 */
	@SuppressWarnings("unchecked")
	public <T> MeterCounter counter(final String name, final T... tags) {
		return counter(name, List.of(tags));
	}
}
