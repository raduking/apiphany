package org.apiphany.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
			throw eofExceptionBytesNeeded(bytesToRead - bytesRead);
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
				throw eofExceptionBytesNeeded(remainingBytes);
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
				throw eofExceptionBytesNeeded(remainingBytes);
			}
			os.write(buffer, 0, bytesRead);
			remainingBytes -= bytesRead;
		}
	}

	/**
	 * Returns an {@link EOFException} with the remaining bytes needed.
	 *
	 * @param remainingBytes the needed bytes.
	 * @return a new {@link EOFException}
	 */
	static EOFException eofExceptionBytesNeeded(final int remainingBytes) {
		return new EOFException("Stream closed; need " + remainingBytes + " more bytes");
	}
}
