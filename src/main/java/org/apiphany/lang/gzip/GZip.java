package org.apiphany.lang.gzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apiphany.io.IOStreams;
import org.apiphany.lang.Strings;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;

/**
 * Utility class for compressing / de-compressing via GZIP.
 *
 * @author Radu Sebastian LAZIN
 */
public class GZip {

	/**
	 * GZIP magic number, fixed values in the beginning to identify the GZIP format
	 * <p>
	 * <a href="http://www.gzip.org/zlib/rfc-gzip.html#file-format">GZIP file format</a>
	 */
	private static final byte ID1 = 0x1f;

	/**
	 * GZIP magic number, fixed values in the beginning to identify the GZIP format
	 * <p>
	 * <a href="http://www.gzip.org/zlib/rfc-gzip.html#file-format">GZIP file format</a>
	 */
	private static final byte ID2 = (byte) 0x8b;

	/**
	 * Private constructor.
	 */
	private GZip() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Compress a {@link String} with GZIP.
	 *
	 * @param text text to compress
	 * @return compressed text
	 * @throws IOException on error
	 */
	public static byte[] compress(final String text) throws IOException {
		return compress(text, Strings.DEFAULT_CHARSET);
	}

	/**
	 * Compress a {@link String} with GZIP.
	 *
	 * @param text text to compress
	 * @param charset character set of the text
	 * @return compressed text
	 * @throws IOException on error
	 */
	public static byte[] compress(final String text, final Charset charset) throws IOException {
		return compress(text.getBytes(charset));
	}

	/**
	 * Compress a byte array with GZIP.
	 *
	 * @param body body to GZIP
	 * @return compressed GZIP byte array
	 * @throws IOException on error
	 */
	public static byte[] compress(final byte[] body) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
			gzipOutputStream.write(body);
		}
		return baos.toByteArray();
	}

	/**
	 * De-compress a GZIP-ed byte array to a {@link String}.
	 *
	 * @param body GZIP byte array to decompress
	 * @return de-compressed string
	 * @throws IOException on error
	 */
	public static String decompressToString(final byte[] body) throws IOException {
		try (InputStream inputStream = inputStream(new ByteArrayInputStream(body))) {
			return Strings.toString(inputStream, Strings.DEFAULT_CHARSET, IOStreams.DEFAULT_BUFFER_SIZE);
		}
	}

	/**
	 * De-compress a GZIP-ed byte array to a {@link String}.
	 *
	 * @param inputStream input stream to de-compress
	 * @return de-compressed string
	 * @throws IOException on error
	 */
	public static String decompressToString(final InputStream inputStream) throws IOException {
		try (InputStream newInputStream = inputStream(inputStream)) {
			return Strings.toString(newInputStream, Strings.DEFAULT_CHARSET, IOStreams.DEFAULT_BUFFER_SIZE);
		}
	}

	/**
	 * De-compress a GZIP-ed byte array to a byte array.
	 *
	 * @param body GZIP byte array to decompress
	 * @return de-compressed byte array
	 * @throws IOException on error
	 */
	public static byte[] decompressToBytes(final byte[] body) throws IOException {
		try (InputStream inputStream = inputStream(new ByteArrayInputStream(body))) {
			return IOStreams.toByteArray(inputStream);
		}
	}

	/**
	 * De-compress a GZIP-ed input (byte array or input stream) to a the same output type as the input.
	 *
	 * @param <T> input type
	 *
	 * @param input input to de-compress
	 * @return de-compressed value in the same type as the input
	 * @throws IOException on error
	 */
	public static <T> T decompress(final T input) throws IOException {
		Object result = switch (input) {
			case byte[] bytes -> decompressToBytes(bytes);
			case InputStream is -> inputStream(is);
			default -> throw new IllegalArgumentException("Cannot decompress object of type: " + input.getClass()
					+ ", input must be byte[] or " + InputStream.class);
		};
		return JavaObjects.cast(result);
	}

	/**
	 * Returns a {@link GZIPInputStream} if the stream represents a GZip encoded stream. If the stream is not an input
	 * stream, it returns the input stream wrapped in a {@link PushbackInputStream}.
	 *
	 * @param inputStream GZIP input stream de-compress
	 * @return GZip input stream if the stream is a GZip input stream
	 * @throws IOException on error
	 */
	public static InputStream inputStream(final InputStream inputStream) throws IOException {
		PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 2);

		byte[] signature = new byte[2];
		if (2 == pushbackInputStream.read(signature)) {
			pushbackInputStream.unread(signature);
		}

		if (ID1 == signature[0] && ID2 == signature[1]) {
			return new GZIPInputStream(pushbackInputStream);
		}
		return pushbackInputStream;
	}

}
