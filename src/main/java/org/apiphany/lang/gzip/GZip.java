package org.apiphany.lang.gzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class for compressing / de-compressing via GZIP.
 *
 * @author Radu Sebastian LAZIN
 */
public class GZip {

	/**
	 * GZIP encoding.
	 */
	public static final String ENCODING = "gzip";

	/**
	 * Default character set used when working with GZIP.
	 */
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

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
		throw new UnsupportedOperationException("This class should not be instantiated.");
	}

	/**
	 * Compress a {@link String} with GZIP.
	 *
	 * @param text text to compress
	 * @return compressed text
	 * @throws IOException on error
	 */
	public static byte[] compress(final String text) throws IOException {
		return compress(text.getBytes(DEFAULT_CHARSET));
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
	 * @param body GZIP byte array to de-compress
	 * @return de-compressed string
	 * @throws IOException on error
	 */
	public static String decompress(final byte[] body) throws IOException {
		try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(body))) {
			return toString(gzipInputStream, DEFAULT_CHARSET);
		}
	}

	/**
	 * De-compress a GZIP-ed byte array to a {@link String}.
	 *
	 * @param inputStream input stream to de-compress
	 * @return de-compressed string
	 * @throws IOException on error
	 */
	public static String decompress(final InputStream inputStream) throws IOException {
		try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
			return toString(gzipInputStream, DEFAULT_CHARSET);
		}
	}

	/**
	 * Returns a {@link GZIPInputStream} if the stream represents a GZip encoded stream. If the stream is not an input
	 * stream it returns the input stream wrapped in a {@link PushbackInputStream}.
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

		if (signature[0] == ID1 && signature[1] == ID2) {
			return new GZIPInputStream(pushbackInputStream);
		}
		return pushbackInputStream;
	}

	/**
	 * Transforms the given input stream into a string.
	 *
	 * @param inputStream input stream
	 * @param charset character set
	 * @return a string
	 * @throws IOException on any stream error
	 */
	public static String toString(final InputStream inputStream, final Charset charset) throws IOException {
		return new String(inputStream.readAllBytes(), charset);
	}
}
