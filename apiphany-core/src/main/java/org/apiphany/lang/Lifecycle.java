package org.apiphany.lang;

/**
 * Enum for life-cycle management of objects.
 * <p>
 * This enum defines ownership responsibility, not resource lifetime and is defined at the point of the
 * {@link ScopedResource} creation. It indicates whether the resource is managed (owned) by the current scope or
 * unmanaged (owned by an external container or framework).
 *
 * @author Radu Sebastian LAZIN
 */
public enum Lifecycle {

	/**
	 * Managed life-cycle, meaning that the object is owned by the current scope, and its life-cycle is controlled by
	 * whoever owns the {@link ScopedResource}.
	 */
	MANAGED,

	/**
	 * Unmanaged life-cycle, meaning that the object is managed by a container or framework, and its life-cycle is
	 * controlled by the container or framework not by the owner of the {@link ScopedResource}.
	 */
	UNMANAGED;

	/**
	 * Converts a boolean value to a {@link Lifecycle} enum. If the boolean value is true, it returns {@link #MANAGED},
	 * otherwise it returns {@link #UNMANAGED}.
	 *
	 * @param isManaged boolean value indicating if the life-cycle is managed or not
	 * @return Lifecycle enum corresponding to the boolean value
	 */
	public static Lifecycle from(final boolean isManaged) {
		return isManaged ? MANAGED : UNMANAGED;
	}

	/**
	 * Checks if the life-cycle is managed.
	 *
	 * @return true if the life-cycle is managed, false otherwise
	 */
	public boolean isManaged() {
		return MANAGED == this;
	}

	/**
	 * Checks if the life-cycle is unmanaged.
	 *
	 * @return true if the life-cycle is unmanaged, false otherwise
	 */
	public boolean isUnmanaged() {
		return UNMANAGED == this;
	}
}
