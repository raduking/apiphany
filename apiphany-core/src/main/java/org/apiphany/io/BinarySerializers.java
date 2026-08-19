package org.apiphany.io;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Utility interface for binary serialization operations on {@link BinaryRepresentable} and/or {@link ByteSizeable}
 * objects.
 *
 * @author Radu Sebastian LAZIN
 */
public interface BinarySerializers {

	/**
	 * Converts a list of {@link BinaryRepresentable} and {@link ByteSizeable} objects to a single byte array by
	 * concatenating their binary representations. The resulting byte array will have a length equal to the sum of the sizes
	 * of the individual objects.
	 * <p>
	 * The input list must not be {@code null}, but it may be empty, in which case an empty byte array is returned. The
	 * elements in the list must also not be {@code null}, and their {@code toByteArray()} methods must not return
	 * {@code null}.
	 *
	 * @param <T> the type of objects in the list, which must implement both {@link BinaryRepresentable} and
	 *     {@link ByteSizeable}
	 *
	 * @param list the list of objects to convert; must not be {@code null}
	 * @return a byte array containing the concatenated binary representations of all objects in the list
	 */
	static <T extends BinaryRepresentable & ByteSizeable> byte[] toByteArray(final List<T> list) {
		ByteBuffer buffer = ByteBuffer.allocate(ByteSizeable.sizeOf(list));
		for (BinaryRepresentable element : list) {
			buffer.put(element.toByteArray());
		}
		return buffer.array();
	}
}
