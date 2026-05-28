package org.apiphany.io.deflate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apiphany.io.IOStreams;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;

/**
 * Utility class for compressing / de-compressing via DEFLATE.
 *
 * @author Radu Sebastian LAZIN
 */
public class Deflate {

	/**
	 * Private constructor.
	 */
	private Deflate() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Compress a byte array with DEFLATE.
	 *
	 * @param body body to compress
	 * @return compressed byte array
	 * @throws IOException on error
	 */
	public static byte[] compress(final byte[] body) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DeflaterOutputStream deflater = new DeflaterOutputStream(baos)) {
			deflater.write(body);
		}
		return baos.toByteArray();
	}

	/**
	 * De-compress a DEFLATE byte array to bytes.
	 *
	 * @param body compressed body
	 * @return decompressed bytes
	 * @throws IOException on error
	 */
	public static byte[] decompressToBytes(final byte[] body) throws IOException {
		return decompressToBytes(body, IOStreams.MAX_BUFFER_SIZE);
	}

	/**
	 * De-compress a DEFLATE byte array to bytes with maximum output size.
	 *
	 * @param body compressed body
	 * @param maxBytes maximum number of bytes allowed in decompressed output
	 * @return decompressed bytes
	 * @throws IOException on error
	 */
	public static byte[] decompressToBytes(final byte[] body, final int maxBytes) throws IOException {
		try (InputStream is = inputStream(new ByteArrayInputStream(body))) {
			return IOStreams.toByteArray(is, maxBytes);
		}
	}

	/**
	 * De-compress a DEFLATE input (byte[] or InputStream) to the same output type.
	 *
	 * @param <T> input type
	 *
	 * @param input input to decompress
	 * @return decompressed result
	 * @throws IOException on error
	 */
	public static <T> T decompress(final T input) throws IOException {
		return decompress(input, IOStreams.MAX_BUFFER_SIZE);
	}

	/**
	 * De-compress a DEFLATE input (byte[] or InputStream) to the same output type while applying a maximum output size for
	 * byte-array decoding.
	 *
	 * @param <T> input type
	 *
	 * @param input input to decompress
	 * @param maxBytes maximum number of bytes allowed in decompressed output when input is byte[]
	 * @return decompressed result
	 * @throws IOException on error
	 */
	public static <T> T decompress(final T input, final int maxBytes) throws IOException {
		Object result = switch (input) {
			case byte[] bytes -> decompressToBytes(bytes, maxBytes);
			case InputStream is -> inputStream(is);
			default -> throw new IllegalArgumentException(
					"Cannot decompress object of type: " + input.getClass()
							+ ", input must be byte[] or " + InputStream.class);
		};
		return JavaObjects.cast(result);
	}

	/**
	 * Returns an {@link InflaterInputStream} wrapping the input stream.
	 *
	 * @param inputStream input stream
	 * @return decompressed input stream
	 */
	public static InputStream inputStream(final InputStream inputStream) {
		return new InflaterInputStream(inputStream);
	}
}
