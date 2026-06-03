package org.apiphany.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apiphany.io.function.IOConsumer;
import org.apiphany.lang.Require;

/**
 * Helper class for input/output operations.
 *
 * @author Radu Sebastian LAZIN
 */
public interface IOStreams {

	/**
	 * Default buffer size when working with buffers and streams (8192 bytes).
	 */
	int DEFAULT_BUFFER_SIZE = 2 << 12;

	/**
	 * Maximum buffer size to prevent OutOfMemoryError: {@link Integer#MAX_VALUE}.
	 */
	int MAX_BUFFER_SIZE = Integer.MAX_VALUE;

	/**
	 * Reads exactly N bytes from the given input stream (a chunk). Throws an exception if N bytes could not be read.
	 *
	 * @param is the input stream
	 * @param bytesToRead the number of bytes to read which will also be the size of the returned array
	 * @return an array of bytes exactly of size N
	 * @throws IOException if any error occurs
	 */
	static byte[] readChunk(final InputStream is, final int bytesToRead) throws IOException {
		byte[] result = new byte[bytesToRead];
		int bytesRead = is.readNBytes(result, 0, bytesToRead);
		if (bytesRead != bytesToRead) {
			throw eofExceptionBytesNeeded(bytesToRead - bytesRead, bytesToRead);
		}
		return result;
	}

	/**
	 * Fully reads the required number of bytes from the input stream.
	 *
	 * @param is the input stream providing the data
	 * @param buffer the output buffer
	 * @param offset the offset in the buffer to read into
	 * @param bytesToRead the number of bytes to read
	 * @throws IOException if any error occurs
	 */
	static void readFully(final InputStream is, final byte[] buffer, final int offset, final int bytesToRead) throws IOException {
		int currentOffset = offset;
		int remainingBytes = bytesToRead;
		while (remainingBytes > 0) {
			int bytesRead = is.read(buffer, currentOffset, remainingBytes);
			if (bytesRead < 0) {
				throw eofExceptionBytesNeeded(remainingBytes, bytesToRead);
			}
			currentOffset += bytesRead;
			remainingBytes -= bytesRead;
		}
	}

	/**
	 * Copies exactly N bytes from the input stream to the output stream.
	 *
	 * @param is the input stream
	 * @param os the output stream
	 * @param bytesToCopy the number of bytes to copy
	 * @throws IOException if any error occurs
	 */
	static void copy(final InputStream is, final OutputStream os, final int bytesToCopy) throws IOException {
		if (bytesToCopy == 0) {
			return;
		}
		byte[] buffer = new byte[Math.min(DEFAULT_BUFFER_SIZE, bytesToCopy)];
		int remainingBytes = bytesToCopy;
		while (remainingBytes > 0) {
			int bytesToRead = Math.min(buffer.length, remainingBytes);
			int bytesRead = is.read(buffer, 0, bytesToRead);
			if (bytesRead < 0) {
				throw eofExceptionBytesNeeded(remainingBytes, bytesToCopy);
			}
			os.write(buffer, 0, bytesRead);
			remainingBytes -= bytesRead;
		}
		os.flush();
	}

	/**
	 * Returns an {@link EOFException} with the remaining bytes needed out of a total needed bytes.
	 *
	 * @param remaining the remaining unread bytes needed
	 * @param total the total needed bytes
	 * @return a new {@link EOFException}
	 */
	static EOFException eofExceptionBytesNeeded(final int remaining, final int total) {
		return new EOFException("Stream closed, need " + remaining + " more bytes out of " + total);
	}

	/**
	 * Reads all bytes from the given input stream and returns them as a byte array.
	 *
	 * @param is the input stream
	 * @return a byte array containing all bytes read from the input stream
	 * @throws IOException if any error occurs
	 */
	static byte[] toByteArray(final InputStream is) throws IOException {
		return toByteArray(is, MAX_BUFFER_SIZE);
	}

	/**
	 * Reads all bytes from the given input stream and returns them as a byte array. Fails if the number of bytes read
	 * exceeds the configured maximum.
	 *
	 * @param is the input stream
	 * @param maxBytes maximum number of bytes allowed to be read
	 * @return a byte array containing all bytes read from the input stream
	 * @throws IOException if any error occurs
	 * @throws EOFException if the stream content exceeds maxBytes
	 */
	static byte[] toByteArray(final InputStream is, final int maxBytes) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n;
		long totalBytes = 0;
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			while ((n = is.read(buffer)) != -1) {
				totalBytes += n;
				if (totalBytes > maxBytes) {
					throw new EOFException("Input stream exceeds max allowed bytes: " + maxBytes);
				}
				byteArrayOutputStream.write(buffer, 0, n);
			}
			return byteArrayOutputStream.toByteArray();
		}
	}

	/**
	 * Writes bytes to an output stream using the provided writer and returns the written bytes as a byte array.
	 *
	 * @param writer a consumer that writes to an output stream
	 * @return a byte array containing the bytes written by the writer
	 * @throws IOException if any error occurs
	 */
	static byte[] toByteArray(final IOConsumer<OutputStream> writer) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			writer.accept(byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
	}

	/**
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters,
	 * the result will be {@code null}.
	 *
	 * @param inputStream the input stream to read from
	 * @param encoding character encoding to use when reading the input stream
	 * @param maxSize maximum size in characters to read from the input stream
	 * @param bufferSize buffer size in characters to use when reading the input stream
	 * @return the input stream as string
	 * @throws IOException if an I/O error occurs
	 * @throws IllegalArgumentException if maxSize or bufferSize are not strictly positive
	 * @throws NullPointerException if inputStream or encoding is null
	 */
	static String toStringOrThrow(final InputStream inputStream, final Charset encoding, final int maxSize, final int bufferSize) throws IOException {
		Require.that(maxSize > 0, "Maximum size must be strictly positive");
		Require.that(bufferSize > 0, "Buffer size must be strictly positive");
		if (null == inputStream) {
			return null;
		}
		final StringBuilder out = new StringBuilder(Math.min(maxSize, IOStreams.DEFAULT_BUFFER_SIZE));
		try (Reader in = new InputStreamReader(inputStream, encoding)) {
			final char[] buffer = new char[bufferSize];
			long totalRead = 0;
			int s;
			while ((s = in.read(buffer, 0, buffer.length)) >= 0) {
				out.append(buffer, 0, s);
				totalRead += s;
				if (totalRead > maxSize) {
					throw new IOException("Input stream exceeds maximum size of " + maxSize + " characters");
				}
			}
		}
		return out.toString();
	}
}
