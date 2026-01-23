package org.apiphany.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.morphix.lang.function.ThrowingSupplier;

/**
 * Input stream that holds a {@link ClassicHttpResponse} that will get closed when the input stream is closed.
 *
 * @author Radu Sebastian LAZIN
 */
public class CloseableHttpResponseInputStream extends InputStream {

	/**
	 * The {@link Closeable} response which contains the actual input stream.
	 */
	private final ClassicHttpResponse classicHttpResponse;

	/**
	 * The input stream delegate from the response.
	 */
	private final InputStream inputStream;

	/**
	 * Hidden constructor, use {@link #of(ClassicHttpResponse)}.
	 *
	 * @param classicHttpResponse closable HTTP response
	 */
	@SuppressWarnings("resource")
	protected CloseableHttpResponseInputStream(final ClassicHttpResponse classicHttpResponse) {
		this.classicHttpResponse = Objects.requireNonNull(classicHttpResponse, "response cannot be null");
		HttpEntity httpEntity = Objects.requireNonNull(classicHttpResponse.getEntity(), "response entity cannot be null");
		this.inputStream = Objects.requireNonNull(ThrowingSupplier.unchecked(httpEntity::getContent).get());
	}

	/**
	 * Create a closable response input stream from a closable HTTP response.
	 *
	 * @param classicHttpResponse response
	 * @return input stream
	 */
	public static CloseableHttpResponseInputStream of(final ClassicHttpResponse classicHttpResponse) {
		return new CloseableHttpResponseInputStream(classicHttpResponse);
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
		// this call also closes the input stream
		classicHttpResponse.close();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(classicHttpResponse, inputStream);
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CloseableHttpResponseInputStream)) {
			return false;
		}
		CloseableHttpResponseInputStream other = (CloseableHttpResponseInputStream) obj;
		return Objects.equals(classicHttpResponse, other.classicHttpResponse)
				&& Objects.equals(inputStream, other.inputStream);
	}

}
