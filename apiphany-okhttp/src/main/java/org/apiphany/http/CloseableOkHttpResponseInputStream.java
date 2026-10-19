package org.apiphany.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.morphix.lang.function.ThrowingSupplier;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Input stream that holds an OkHttp {@link Response} that will get closed when the input stream is closed.
 *
 * @author Radu Sebastian LAZIN
 */
public class CloseableOkHttpResponseInputStream extends InputStream {

	/**
	 * The {@link Closeable} response which contains the actual input stream.
	 */
	private final Response response;

	/**
	 * The input stream delegate from the response.
	 */
	private final InputStream inputStream;

	/**
	 * Hidden constructor, use {@link #of(Response)}.
	 *
	 * @param response closable OkHttp response
	 */
	@SuppressWarnings("resource")
	protected CloseableOkHttpResponseInputStream(final Response response) {
		this.response = Objects.requireNonNull(response, "response cannot be null");
		ResponseBody responseBody = Objects.requireNonNull(response.body(), "response body cannot be null");
		this.inputStream = Objects.requireNonNull(ThrowingSupplier.unchecked(responseBody::byteStream).get(), "body content cannot be null");
	}

	/**
	 * Create a closable response input stream from a closable OkHttp response.
	 *
	 * @param response response
	 * @return input stream
	 */
	public static CloseableOkHttpResponseInputStream of(final Response response) {
		return new CloseableOkHttpResponseInputStream(response);
	}

	/**
	 * @see InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return inputStream.read(b, off, len);
	}

	/**
	 * @see InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {
		return inputStream.read(b);
	}

	/**
	 * @see InputStream#readAllBytes()
	 */
	@Override
	public byte[] readAllBytes() throws IOException {
		return inputStream.readAllBytes();
	}

	/**
	 * @see InputStream#readNBytes(byte[], int, int)
	 */
	@Override
	public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
		return inputStream.readNBytes(b, off, len);
	}

	/**
	 * @see InputStream#readNBytes(int)
	 */
	@Override
	public byte[] readNBytes(final int len) throws IOException {
		return inputStream.readNBytes(len);
	}

	/**
	 * @see InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		// this call also closes the response body and the input stream
		response.close();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(response, inputStream);
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof CloseableOkHttpResponseInputStream that) {
			return Objects.equals(this.response, that.response)
					&& Objects.equals(this.inputStream, that.inputStream);
		}
		return false;
	}
}
