package org.apiphany.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * An {@link InputStream} implementation that reads from a byte array using a {@link ByteBuffer} without synchronization
 * overhead. This class provides an unsynchronized alternative to {@link ByteArrayInputStream} for use in
 * performance-critical scenarios where synchronization is not required.
 * <p>
 * <b>Warning:</b>
 * <ul>
 * <li>This class is not thread safe!</li>
 * </ul>
 *
 * @see java.io.InputStream
 * @see java.nio.ByteBuffer
 *
 * @author Radu Sebastian LAZIN
 */
public class ByteBufferInputStream extends InputStream {

	/**
	 * The backing ByteBuffer for this input stream
	 */
	private final ByteBuffer buf;

	/**
	 * Creates a new ByteBufferInputStream that reads from the specified byte buffer. The stream will read from the beginning
	 * of the buffer.
	 *
	 * @param buffer the byte buffer to read from
	 * @throws NullPointerException if the input byte buffer is null
	 */
	public ByteBufferInputStream(final ByteBuffer buffer) {
		this.buf = Objects.requireNonNull(buffer, "Input byte buffer cannot be null");
	}

	/**
	 * Creates a new ByteBufferInputStream that reads from the specified byte array. The stream will read from the beginning
	 * of the array.
	 *
	 * @param bytes the byte array to read from
	 * @throws NullPointerException if the input byte array is null
	 */
	public ByteBufferInputStream(final byte[] bytes) {
		this(ByteBuffer.wrap(Objects.requireNonNull(bytes, "Input byte array cannot be null")));
	}

	/**
	 * Creates a new ByteBufferInputStream that reads from the specified byte array. The stream will read from the beginning
	 * of the array.
	 *
	 * @param bytes the byte array to read from
	 * @return a new byte buffer input stream
	 * @throws NullPointerException if the input byte array is null
	 */
	public static ByteBufferInputStream of(final byte[] bytes) {
		return new ByteBufferInputStream(bytes);
	}

	/**
	 * Creates a new ByteBufferInputStream that reads from the specified byte buffer. The stream will read from the beginning
	 * of the buffer.
	 *
	 * @param buffer the byte buffer to read from
	 * @return a new byte buffer input stream
	 * @throws NullPointerException if the input byte buffer is null
	 */
	public static ByteBufferInputStream of(final ByteBuffer buffer) {
		return new ByteBufferInputStream(buffer);
	}

	/**
	 * @see #read()
	 */
	@Override
	public int read() {
		if (!buf.hasRemaining()) {
			return -1;
		}
		return buf.get() & 0xFF; // Convert to unsigned byte
	}

	/**
	 * @see #read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] bytes, final int off, final int len) {
		if (off < 0 || len < 0 || len > Objects.requireNonNull(bytes, "Byte array cannot be null").length - off) {
			throw new IndexOutOfBoundsException();
		}
		if (!buf.hasRemaining()) {
			return -1;
		}
		int l = Math.min(len, buf.remaining());
		buf.get(bytes, off, l);
		return l;
	}

	/**
	 * @see #available()
	 */
	@Override
	public int available() {
		return buf.remaining();
	}

	/**
	 * @see #skip(long)
	 */
	@Override
	public long skip(final long n) {
		if (n <= 0) {
			return 0;
		}
		int skip = Math.toIntExact(Math.min(n, buf.remaining()));
		buf.position(buf.position() + skip);
		return skip;
	}

	/**
	 * @see #mark(int)
	 */
	@Override
	public void mark(final int readlimit) {
		buf.mark();
	}

	/**
	 * @see #reset()
	 */
	@Override
	public void reset() {
		buf.reset();
	}

	/**
	 * @see #markSupported()
	 */
	@Override
	public boolean markSupported() {
		return true;
	}

	/**
	 * Closing a {@code ByteArrayInputStream} has no effect. The methods in this class can be called after the stream has
	 * been closed without generating an {@code IOException}.
	 */
	@Override
	public void close() throws IOException {
		// empty
	}
}
