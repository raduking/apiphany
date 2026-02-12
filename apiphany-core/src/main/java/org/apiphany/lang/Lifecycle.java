package org.apiphany.lang;

/**
 * Enum for life-cycle management of objects.
 *
 * @author Radu Sebastian LAZIN
 */
public enum Lifecycle {

	/**
	 * Managed life-cycle, meaning that the object is managed by a container or framework, and its life-cycle is controlled
	 * by that container or framework.
	 */
	MANAGED,

	/**
	 * Unmanaged life-cycle, meaning that the object is not managed by any container or framework, and its life-cycle is
	 * controlled by the application code.
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
