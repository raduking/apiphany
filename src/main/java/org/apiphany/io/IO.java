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
public interface IO {

	/**
	 * Default buffer size when working with buffers and streams (4096 bytes).
	 */
	int DEFAULT_BUFFER_SIZE = 2 << 12;

	/**
	 * Reads exactly N bytes from the given input stream. Throws an exception if N bytes could not be read.
	 *
	 * @param is the input stream
	 * @param n the number of bytes to read which will also be the size of the returned array
	 * @return an array of bytes exactly of size N
	 * @throws IOException if any error occurs
	 */
    static byte[] readChunk(final InputStream is, final int n) throws IOException {
    	byte[] a = new byte[n];
    	int bytesRead = is.readNBytes(a, 0, n);
        if (bytesRead != n) {
			throw new EOFException("Stream closed; need " + (n - bytesRead) + " more bytes");
		}
        return a;
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
        int left = bytesToCopy;
        while (left > 0) {
            int bytesToRead = Math.min(buffer.length, left);
            int r = is.read(buffer, 0, bytesToRead);
            if (r < 0) {
				throw new EOFException("Stream closed; need " + left + " more bytes");
			}
            os.write(buffer, 0, r);
            left -= r;
        }
    }
}
