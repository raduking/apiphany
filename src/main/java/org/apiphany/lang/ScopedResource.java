package org.apiphany.lang;

import java.util.Map;
import java.util.Objects;

/**
 * A wrapper for managing the life-cycle of AutoCloseable resources with scope control.
 * <p>
 * This class provides controlled access to resources while ensuring proper cleanup based on the managed flag. It
 * supports both managed (automatically closed) and unmanaged (manually closed) resource patterns.
 *
 * @param <T> the type of the AutoCloseable resource being wrapped
 *
 * @author Radu Sebastian LAZIN
 */
public class ScopedResource<T extends AutoCloseable> {

	/**
	 * The wrapped AutoCloseable resource.
	 */
	private final T resource;

	/**
	 * Flag indicating whether this wrapper manages the resource's life-cycle.
	 */
	private final boolean managed;

	/**
	 * Constructs a new ScopedResource instance.
	 *
	 * @param resource the resource to wrap (must not be null)
	 * @param managed whether this wrapper should manage the resource's life-cycle
	 * @throws NullPointerException if resource is null
	 */
	public ScopedResource(final T resource, final boolean managed) {
		this.resource = Objects.requireNonNull(resource, "resource cannot be null");
		this.managed = managed;
	}

	/**
	 * Returns the underlying resource.
	 *
	 * @return the wrapped resource
	 */
	public T unwrap() {
		return resource;
	}

	/**
	 * Checks if this wrapper manages the resource's lifecycle.
	 *
	 * @return true if the resource is managed by this wrapper
	 */
	public boolean isManaged() {
		return managed;
	}

	/**
	 * Closes the resource if it is managed by this wrapper.
	 *
	 * @throws Exception if an error occurs while closing the resource
	 */
	public void closeIfManaged() throws Exception {
		if (managed) {
			resource.close();
		}
	}

	/**
	 * Transforms this scoped resource into a {@link Map}.
	 *
	 * @return a map with one (key, value) with (resource, managed)
	 */
	public Map<T, Boolean> toMap() {
		return Map.of(resource, managed);
	}

	/**
	 * Creates a new ScopedResource instance.
	 *
	 * @param <T> the type of the resource
	 *
	 * @param resource the resource to wrap
	 * @param managed whether the wrapper should manage the resource
	 * @return a new ScopedResource instance
	 */
	public static <T extends AutoCloseable> ScopedResource<T> of(final T resource, final boolean managed) {
		return new ScopedResource<>(resource, managed);
	}

	/**
	 * Creates a new managed ScopedResource instance.
	 *
	 * @param <T> the type of the resource
	 *
	 * @param resource the resource to wrap
	 * @return a new managed ScopedResource instance
	 */
	public static <T extends AutoCloseable> ScopedResource<T> managed(final T resource) {
		return of(resource, true);
	}

	/**
	 * Creates a new unmanaged ScopedResource instance.
	 *
	 * @param <T> the type of the resource
	 *
	 * @param resource the resource to wrap
	 * @return a new unmanaged ScopedResource instance
	 */
	public static <T extends AutoCloseable> ScopedResource<T> unmanaged(final T resource) {
		return of(resource, false);
	}
}
