package org.apiphany.lang.collections;

import java.lang.reflect.Array;

/**
 * Utility class for Java arrays.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Arrays {

	/**
	 * Returns the array given as parameter or an array if the parameter is {@code null}.
	 *
	 * @param array a provided array
	 * @param componentType due to type erasure this information is needed to create an empty array
	 * @param <T> The array's key type
	 * @return the given array if not null, empty array otherwise
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] safe(final T[] array, final Class<T> componentType) {
		return array == null ? (T[]) Array.newInstance(componentType, 0) : array;
	}

}
