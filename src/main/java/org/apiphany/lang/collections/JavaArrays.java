package org.apiphany.lang.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.StreamSupport;

/**
 * Utility class for Java arrays. It is named JavaArrays to avoid confusion with {@link Arrays} class.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JavaArrays {

	/**
	 * Returns the provided array, or an empty array if the parameter is {@code null}.
	 *
	 * @param <T> the component type of the array
	 *
	 * @param array the provided array
	 * @param componentType due to type erasure this information is needed to create an empty array
	 * @return the given array if not null, empty array otherwise
	 */
	@SuppressWarnings("unchecked")
	static <T> T[] safe(final T[] array, final Class<T> componentType) {
		return null == array ? (T[]) Array.newInstance(componentType, 0) : array;
	}

	/**
	 * Converts the given value into an Object array. This method is useful for handling various input types uniformly as
	 * arrays in scenarios such as processing method arguments, handling collections, or dealing with variable-length
	 * inputs, it simplifies the handling of different input types by providing a consistent array representation.
	 * <p>
	 * <ul>
	 * <li>If the value is {@code null}, {@code null} is returned.</li>
	 * <li>If the value is already an array, it is converted to Object array</li>
	 * <li>If the value is an instance of {@link Iterable}, it is converted to an Object array using streams.</li>
	 * <li>If the value is neither an array nor an {@link Iterable}, a single-element Object array is returned containing
	 * the value.</li>
	 * </ul>
	 * This method always returns a new Object array, ensuring that the returned array is a separate instance from the input
	 * value.
	 *
	 * @param value the array to convert
	 * @return the converted Object array
	 */
	static Object[] toArray(final Object value) {
		if (null == value) {
			return null;
		}
		Class<?> type = value.getClass();

		if (type.isArray()) {
			if (!type.getComponentType().isPrimitive()) {
				return ((Object[]) value).clone();
			}
			int length = Array.getLength(value);
			Object[] array = new Object[length];
			for (int i = 0; i < length; ++i) {
				array[i] = Array.get(value, i);
			}
			return array;
		}
		if (value instanceof Iterable<?> iterable) {
			return StreamSupport.stream(iterable.spliterator(), false).toArray();
		}
		return new Object[] { value };
	}
}
