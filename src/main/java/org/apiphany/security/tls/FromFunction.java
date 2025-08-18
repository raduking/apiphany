package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a function that can deserialize a {@link TLSObject} from an input stream.
 * <p>
 * This is a functional interface used throughout the TLS implementation to parse various protocol objects from their
 * binary representations. The function handles both the reading and conversion of raw bytes into the appropriate Java
 * object.
 *
 * @param <T> the type of TLSObject this function can produce
 *
 * @author Radu Sebastian LAZIN
 */
public interface FromFunction<T extends TLSObject> {

	/**
	 * Reads and parses a {@link TLSObject} of a given size in bytes from the provided input stream.
	 *
	 * @param is the input stream to read from
	 * @param size the number of bytes available for reading (may be ignored by some implementations)
	 * @return the parsed TLSObject
	 * @throws IOException If an I/O error occurs while reading from the stream
	 * @throws IllegalArgumentException If the input data is malformed or invalid
	 */
	T from(InputStream is, int size) throws IOException;

	/**
	 * A variant of {@link FromFunction} that doesn't require a size parameter.
	 * <p>
	 * Used for parsing TLS objects where the size is either known in advance or determined from the input stream itself.
	 *
	 * @param <T> the type of TLSObject this function can produce
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface NoSize<T extends TLSObject> {

		/**
		 * Reads and parses a {@link TLSObject} from the provided input stream.
		 *
		 * @param is The input stream to read from
		 * @return the parsed TLSObject
		 * @throws IOException If an I/O error occurs while reading from the stream
		 * @throws IllegalArgumentException If the input data is malformed or invalid
		 */
		T from(InputStream is) throws IOException;
	}

	/**
	 * Creates a {@link FromFunction} that ignores the size parameter and delegates to a {@link NoSize} function.
	 *
	 * @param <T> the type of {@link TLSObject} the function will produce
	 *
	 * @param noSize the size-agnostic parsing function
	 * @return a new FromFunction that wraps the provided NoSize function
	 */
	static <T extends TLSObject> FromFunction<T> ignoreSize(final NoSize<T> noSize) {
		return (is, size) -> noSize.from(is);
	}
}
