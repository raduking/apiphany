package org.apiphany.lang;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A wrapper for managing the life-cycle of {@link AutoCloseable} resources with scope control.
 * <p>
 * This class provides controlled access to resources while ensuring proper cleanup based on the managed flag. It
 * supports both managed (automatically closed) and unmanaged (manually closed) resource patterns.
 *
 * @param <T> the type of the {@link AutoCloseable} resource being wrapped
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
	 * Constructs a new managed ScopedResource instance.
	 *
	 * @param resource the resource to wrap (must not be null)
	 * @throws NullPointerException if resource is null
	 */
	public ScopedResource(final T resource) {
		this(resource, true);
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
	 * Checks if this wrapper manages the resource's life-cycle.
	 *
	 * @return true if the resource is managed by this wrapper
	 */
	public boolean isManaged() {
		return managed;
	}

	/**
	 * Checks if this wrapper manages the resource's life-cycle.
	 *
	 * @return true if the resource is not managed by this wrapper
	 */
	public boolean isNotManaged() {
		return !isManaged();
	}

	/**
	 * Closes the resource if it is managed by this wrapper. For unmanaged resources, this method does nothing. For handling
	 * exceptions, consider using {@link #closeIfManaged(Consumer)}.
	 *
	 * @throws Exception if an error occurs while closing the resource
	 */
	public void closeIfManaged() throws Exception {
		if (managed) {
			resource.close();
		}
	}

	/**
	 * Closes the resource if it is managed by this wrapper, handling any exceptions using the provided exception handler.
	 *
	 * @param exceptionHandler a consumer to handle exceptions that may occur during closing
	 */
	public void closeIfManaged(final Consumer<? super Exception> exceptionHandler) {
		try {
			closeIfManaged();
		} catch (Exception e) {
			exceptionHandler.accept(e);
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

	/**
	 * Checks the first parameter against the second for the same underlying reference. If the referenced resources are the
	 * same and they are both managed then we return an unmanaged scoped resource. Only one scoped resource should manage
	 * the same resource.
	 *
	 * @param <T> the type of the {@link AutoCloseable} resources being wrapped
	 *
	 * @param checkedResource the resource to check
	 * @param resource the resource to check against
	 * @return a checked scoped resource
	 */
	@SuppressWarnings("resource")
	public static <T extends AutoCloseable> ScopedResource<T> checked(
			final ScopedResource<T> checkedResource,
			final ScopedResource<T> resource) {
		if (checkedResource.isNotManaged() || resource.isNotManaged()) {
			return checkedResource;
		}
		T rawCheckedResource = checkedResource.unwrap();
		return rawCheckedResource == resource.unwrap()
				? ScopedResource.unmanaged(rawCheckedResource)
				: checkedResource;
	}

}
