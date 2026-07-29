package org.apiphany.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.springframework.http.client.ClientHttpResponse;

/**
 * Input stream wrapper that keeps the underlying {@link ClientHttpResponse} open until the stream is closed.
 *
 * @author Radu Sebastian LAZIN
 */
public class CloseableClientHttpResponseInputStream extends InputStream {

	/**
	 * The HTTP response owning the body stream.
	 */
	private final ClientHttpResponse response;

	/**
	 * The response body input stream.
	 */
	private final InputStream inputStream;

	/**
	 * Constructor.
	 *
	 * @param response the HTTP response
	 * @throws IOException if reading the response body stream fails
	 */
	protected CloseableClientHttpResponseInputStream(final ClientHttpResponse response) throws IOException {
		this.response = Objects.requireNonNull(response, "response cannot be null");
		this.inputStream = Objects.requireNonNull(response.getBody(), "response body cannot be null");
	}

	/**
	 * Creates a new closeable input stream for the given response.
	 *
	 * @param response the HTTP response
	 * @return closeable response input stream
	 * @throws IOException if reading the response body stream fails
	 */
	public static CloseableClientHttpResponseInputStream of(final ClientHttpResponse response) throws IOException {
		return new CloseableClientHttpResponseInputStream(response);
	}

	/**
	 * @see InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

	/**
	 * @see InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {
		return inputStream.read(b);
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return inputStream.read(b, off, len);
	}

	/**
	 * @see InputStream#readAllBytes()
	 */
	@Override
	public byte[] readAllBytes() throws IOException {
		return inputStream.readAllBytes();
	}

	/**
	 * @see InputStream#readNBytes(int)
	 */
	@Override
	public byte[] readNBytes(final int len) throws IOException {
		return inputStream.readNBytes(len);
	}

	/**
	 * @see InputStream#readNBytes(byte[], int, int)
	 */
	@Override
	public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
		return inputStream.readNBytes(b, off, len);
	}

	/**
	 * @see InputStream#skip(long)
	 */
	@Override
	public long skip(final long n) throws IOException {
		return inputStream.skip(n);
	}

	/**
	 * @see InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return inputStream.available();
	}

	/**
	 * @see InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		response.close();
	}
}
