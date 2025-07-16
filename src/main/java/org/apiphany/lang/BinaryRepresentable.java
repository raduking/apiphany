package org.apiphany.lang;

/**
 * Represents an object that can be converted to a binary (byte array) representation. This interface is typically
 * implemented by objects that support serialization, network transmission, or persistent storage. The binary format is
 * implementation-specific, but it should be complete enough to reconstruct the object (when combined with a
 * corresponding deserialization mechanism).
 *
 * <p>
 * <b>Contract:</b>
 * </p>
 * <ul>
 * <li>The returned byte array must not be {@code null} (use an empty array for zero-length data).</li>
 * <li>Implementations should ensure that repeated calls to {@code toByteArray()} return equivalent data, unless the
 * object's state has changed between calls.</li>
 * <li>If the object is immutable, subsequent calls should return identical arrays (or copies).</li>
 * </ul>
 * <b>Example:</b>
 *
 * <pre>
 * BinaryRepresentable data = new SerializableData("example");
 * byte[] bytes = data.toByteArray();
 * sendOverNetwork(bytes);
 * </pre>
 *
 * @see ByteSizeable
 * @see java.io.Serializable
 *
 * @author Radu Sebastian LAZIN
 */
public interface BinaryRepresentable {

	/**
	 * Converts this object to its binary representation. The returned byte array should contain all necessary information
	 * to reconstruct the object (when used with a corresponding decoder or deserializer). Modifications to the returned
	 * array should not affect the internal state of the object.
	 * <p>
	 * Implementations are strongly encouraged to return a defensive copy of internal byte data to prevent external
	 * modification. For example:
	 * </p>
	 *
	 * <pre>
	 * public byte[] toByteArray() {
	 *     return Arrays.copyOf(internalByteArray, internalByteArray.length);
	 * }
	 * </pre>
	 *
	 * @return A non-null byte array representing the object's binary form. An empty array is permitted if the object
	 * logically contains no data.
	 * @throws UnsupportedOperationException If the object cannot be converted to bytes (e.g., invalid state or unsupported
	 *     operation).
	 */
	byte[] toByteArray();
}
