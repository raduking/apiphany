package org.apiphany.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.morphix.lang.function.ThrowingConsumer;
import org.morphix.lang.resource.ScopedResource;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * A {@link RequestBody} that wraps a non-repeatable {@link InputStream}. This request body is designed to be consumed
 * only once, and will throw an exception if an attempt is made to write the content multiple times.
 * <p>
 * The original stream is managed using a {@link ScopedResource} to ensure proper cleanup when the request body is
 * written.
 *
 * @author Radu Sebastian LAZIN
 */
public final class OneShotRequestBody extends RequestBody {

	/**
	 * The original {@link InputStream} wrapped in a {@link ScopedResource} for proper lifecycle management. This stream is
	 * expected to be non-repeatable and will be consumed only once.
	 */
	private final ScopedResource<InputStream> original;

	/**
	 * The content type of this request body, may be {@code null}.
	 */
	private final MediaType mediaType;

	/**
	 * A flag to track whether the content has already been consumed. This ensures that the request body can only be written
	 * once.
	 */
	private final AtomicBoolean consumed = new AtomicBoolean();

	/**
	 * Constructs a new {@link OneShotRequestBody} with the given original stream and content type. The original stream is
	 * wrapped in a {@link ScopedResource} to ensure it is properly closed once written.
	 *
	 * @param original the original InputStream to be wrapped, must not be null
	 * @param mediaType the content type of the request body, may be null
	 * @throws NullPointerException if the original stream is null
	 */
	public OneShotRequestBody(final InputStream original, final MediaType mediaType) {
		this.original = ScopedResource.managed(original);
		this.mediaType = mediaType;
	}

	/**
	 * @see RequestBody#contentType()
	 */
	@Override
	public MediaType contentType() {
		return mediaType;
	}

	/**
	 * @see RequestBody#contentLength()
	 */
	@Override
	public long contentLength() {
		return -1;
	}

	/**
	 * Returns {@code true} to indicate that this request body can only be written once, since it wraps a non-repeatable
	 * {@link InputStream}.
	 *
	 * @return true, indicating that this request body is one-shot
	 */
	@Override
	public boolean isOneShot() {
		return true;
	}

	/**
	 * Returns the content, which can only be requested once.
	 *
	 * @return the wrapped input stream
	 */
	private InputStream getContent() {
		if (consumed.getAndSet(true)) {
			throw new IllegalStateException("Stream is not repeatable and has already been consumed.");
		}
		return original.unwrap();
	}

	/**
	 * @see RequestBody#writeTo(BufferedSink)
	 */
	@Override
	public void writeTo(final BufferedSink sink) throws IOException {
		try (Source source = Okio.source(getContent())) {
			sink.writeAll(source);
		} finally {
			close();
		}
	}

	/**
	 * Closes the original stream if it is still managed.
	 *
	 */
	private void close() {
		original.closeIfManaged(ThrowingConsumer.unchecked(e -> {
			throw new IOException("Failed to close original stream", e);
		}));
	}
}
