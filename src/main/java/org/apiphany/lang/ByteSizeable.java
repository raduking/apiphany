package org.apiphany.lang;

/**
 * An object that can report its size in bytes.
 * <p>
 * This is useful for memory-sensitive operations, such as serialization, network transmission, or cache eviction
 * policies. Implementations should ensure that the reported size is consistent with the actual binary representation of
 * the object (if applicable).
 * </p>
 * Example usage:
 *
 * <pre>
 * ByteSizeable data = new SomeDataStructure();
 * int bytes = data.sizeOf();
 * System.out.println("Object size: " + bytes + " bytes");
 * </pre>
 *
 * @see BinaryRepresentable
 *
 * @author Radu Sebastian LAZIN
 */
public interface ByteSizeable {

	/**
	 * Returns the size of the object in bytes.
	 * <p>
	 * The exact definition of "size" depends on the implementation, but it should typically reflect the memory footprint of
	 * the object's internal state. For objects that can be serialized (e.g., {@link BinaryRepresentable}), this should
	 * match the length of their binary representation.
	 * </p>
	 *
	 * @return The size of the object in bytes. Must be non-negative.
	 * @throws IllegalStateException if the size cannot be determined (e.g., the object is in an invalid state for
	 *     measurement).
	 */
	int sizeOf();
}
