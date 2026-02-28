package org.apiphany.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apiphany.lang.ScopedResource;
import org.morphix.lang.function.ThrowingConsumer;

/**
 * An {@link AbstractHttpEntity} that wraps a non-repeatable {@link InputStream}. This entity is designed to be consumed
 * only once, and will throw an exception if an attempt is made to read the content multiple times.
 * <p>
 * The original stream is managed using a {@link ScopedResource} to ensure proper cleanup when the entity is closed.
 *
 * @author Radu Sebastian LAZIN
 */
public final class OneShotHttpEntity extends AbstractHttpEntity {

	/**
	 * The original {@link InputStream} wrapped in a {@link ScopedResource} for proper lifecycle management. This stream is
	 * expected to be non-repeatable and will be consumed only once.
	 */
	private final ScopedResource<InputStream> original;

	/**
	 * A flag to track whether the content has already been consumed. This ensures that the entity can only be read once.
	 */
	private final AtomicBoolean consumed = new AtomicBoolean();

	/**
	 * Constructs a new {@link OneShotHttpEntity} with the given original stream and content type. The original stream is
	 * wrapped in a {@link ScopedResource} to ensure it is properly closed when the entity is closed.
	 *
	 * @param original the original InputStream to be wrapped, must not be null
	 * @param contentType the content type of the entity, may be null
	 * @throws NullPointerException if the original stream is null
	 */
	public OneShotHttpEntity(final InputStream original, final ContentType contentType) {
		super(contentType, null, false);
		this.original = ScopedResource.managed(original);
	}

	/**
	 * @see AbstractHttpEntity#isRepeatable()
	 */
	@Override
	public boolean isRepeatable() {
		return false;
	}

	/**
	 * @see AbstractHttpEntity#getContentLength()
	 */
	@Override
	public long getContentLength() {
		return -1;
	}

	/**
	 * @see AbstractHttpEntity#getContent()
	 */
	@Override
	public InputStream getContent() {
		if (consumed.getAndSet(true)) {
			throw new IllegalStateException(
					"Stream is not repeatable and has already been consumed.");
		}
		return original.unwrap();
	}

	/**
	 * @see AbstractHttpEntity#writeTo(OutputStream)
	 */
	@Override
	public void writeTo(final OutputStream out) throws IOException {
		try (InputStream in = getContent()) {
			in.transferTo(out);
		}
	}

	/**
	 * Returns true to indicate that this entity is streaming, as it wraps a non-repeatable InputStream that can only be
	 * consumed once.
	 *
	 * @return true, indicating that this entity is streaming
	 */
	@Override
	public boolean isStreaming() {
		return true;
	}

	/**
	 * @see Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		original.closeIfManaged(ThrowingConsumer.unchecked(e -> {
			throw new IOException("Failed to close original stream", e);
		}));
	}
}
