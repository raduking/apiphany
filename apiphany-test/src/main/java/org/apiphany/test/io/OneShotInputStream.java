package org.apiphany.test.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * An {@link InputStream} that can be read only once. After the first read, it will return -1 for all subsequent reads.
 * <p>
 * This class is not thread-safe and should only be used in a single-threaded context. It is designed for testing
 * purposes to simulate non-repeatable streams, such as those that might be returned by certain HTTP client libraries
 * when reading request bodies.
 *
 * @author Radu Sebastian LAZIN
 */
public final class OneShotInputStream extends InputStream {

	/**
	 * The data to be read from the stream.
	 */
	private final byte[] data;

	/**
	 * The current index in the data array.
	 */
	private int index = 0;

	/**
	 * Flag to indicate if the stream has been consumed.
	 */
	private boolean consumed = false;

	/**
	 * Constructor that initializes the stream with the given data.
	 *
	 * @param data the data to be read from the stream
	 */
	public OneShotInputStream(final byte[] data) {
		this.data = Objects.requireNonNull(data, "data cannot be null");
	}

	/**
	 * Constructor that initializes the stream with the given string data.
	 *
	 * @param data the string data to be read from the stream
	 */
	public OneShotInputStream(final String data) {
		this(data, Charset.defaultCharset());
	}

	/**
	 * Constructor that initializes the stream with the given string data.
	 *
	 * @param data the string data to be read from the stream
	 */
	public OneShotInputStream(final String data, final Charset charset) {
		this(data.getBytes(charset));
	}

	/**
	 * @see InputStream#read()
	 */
	@Override
	public int read() {
		if (consumed) {
			return -1;
		}
		if (index >= data.length) {
			consumed = true;
			return -1;
		}
		return data[index++] & 0xFF;
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		if (consumed) {
			return -1;
		}
		if (index >= data.length) {
			consumed = true;
			return -1;
		}
		int bytesRead = 0;
		for (int i = off; i < off + len && index < data.length; ++i) {
			b[i] = data[index++];
			++bytesRead;
		}
		return bytesRead;
	}
}
