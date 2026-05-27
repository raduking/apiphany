package org.apiphany.io;

/**
 * Interface for objects that can be represented as binary data in a chunked format. This is typically used in contexts
 * where large data needs to be transmitted or stored in parts, with a specific boundary separating each chunk.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ChunkedBinary extends BinaryRepresentable {

	/**
	 * Returns the boundary used to separate chunks in the binary representation.
	 *
	 * @return the boundary string used for chunk separation
	 */
	String getBoundary();
}
