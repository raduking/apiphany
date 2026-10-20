package org.apiphany.http;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * {@link AbstractClientHttpRequest} extension that supports {@link StreamingHttpOutputMessage#setBody(Body)} in
 * addition to buffering via {@link #getBody()}.
 * <p>
 * Adapted from Spring Framework's {@code org.springframework.http.client.AbstractStreamingClientHttpRequest}. Spring
 * Framework 7 removes the OkHttp-based {@link ClientHttpRequestFactory} types from {@code spring-web}; Apiphany keeps
 * this base class so {@link OkHttp3ClientHttpRequest} can stream or buffer request bodies the same way as before.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractStreamingClientHttpRequest extends AbstractClientHttpRequest implements StreamingHttpOutputMessage {

	/**
	 * Default initial capacity of the internal buffer when {@link #getBody()} is used.
	 */
	private static final int DEFAULT_BLOCK_SIZE = 1024;

	/**
	 * Streaming body supplied via {@link #setBody(Body)}, if any.
	 */
	private Body body;

	/**
	 * Buffer used when the caller writes to {@link #getBody()} instead of {@link #setBody(Body)}.
	 */
	private FastByteArrayOutputStream bodyStream;

	/**
	 * @see AbstractClientHttpRequest#getBodyInternal(HttpHeaders)
	 */
	@Override
	protected final OutputStream getBodyInternal(final HttpHeaders headers) {
		Assert.state(this.body == null, "Invoke either getBody or setBody; not both");
		if (this.bodyStream == null) {
			this.bodyStream = new FastByteArrayOutputStream(DEFAULT_BLOCK_SIZE);
		}
		return this.bodyStream;
	}

	/**
	 * @see StreamingHttpOutputMessage#setBody(Body)
	 */
	@Override
	public final void setBody(final Body body) {
		Assert.notNull(body, "Body must not be null");
		assertNotExecuted();
		Assert.state(this.bodyStream == null, "Invoke either getBody or setBody; not both");
		this.body = body;
	}

	/**
	 * @see AbstractClientHttpRequest#executeInternal(HttpHeaders)
	 */
	@Override
	protected final ClientHttpResponse executeInternal(final HttpHeaders headers) throws IOException {
		if (null == body && null != bodyStream) {
			this.body = outputStream -> bodyStream.writeTo(outputStream);
		}
		return executeInternal(headers, body);
	}

	/**
	 * Executes the request with the given headers and optional streaming body.
	 *
	 * @param headers request headers
	 * @param body request body, or {@code null} if none
	 * @return the HTTP response
	 * @throws IOException if the exchange fails
	 */
	protected abstract ClientHttpResponse executeInternal(HttpHeaders headers, Body body) throws IOException;
}
