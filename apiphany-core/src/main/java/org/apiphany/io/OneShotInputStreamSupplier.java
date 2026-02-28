package org.apiphany.io;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * A {@link Supplier} that allows an {@link InputStream} to be obtained only once.
 * <p>
 * This is used to safely model non-repeatable request bodies when retries are enabled. If a retry attempts to obtain
 * the body again, this supplier will fail fast before any network I/O is attempted.
 * <p>
 * This prevents silent retry corruption where a consumed {@link InputStream} would otherwise produce an empty or
 * partial body on subsequent attempts.
 *
 * @author Radu Sebastian LAZIN
 */
public class OneShotInputStreamSupplier implements InputStreamSupplier {

	/**
	 * The InputStream to be supplied. This is not thread-safe and should only be used in a single-threaded context. The
	 * supplier will enforce that it is only accessed once.
	 */
	private final InputStream stream;

	/**
	 * A flag to track whether the InputStream has already been supplied. This ensures that the stream is only consumed
	 * once.
	 */
	private final AtomicBoolean used = new AtomicBoolean();

	/**
	 * Creates a new supplier with the given {@link InputStream}.
	 *
	 * @param stream the input stream to be supplied, must not be null
	 * @throws NullPointerException if the stream is null
	 */
	public OneShotInputStreamSupplier(final InputStream stream) {
		this.stream = Objects.requireNonNull(stream, "stream must not be null");
	}

	/**
	 * Returns the {@link InputStream} if it has not been supplied before. If this method is called more than once, it will
	 * throw an {@link IllegalStateException} to indicate that the stream has already been consumed.
	 *
	 * @return the input stream to be consumed
	 * @throws IllegalStateException if the stream has already been supplied
	 */
	@Override
	public InputStream get() {
		if (!used.compareAndSet(false, true)) {
			throw new IllegalStateException("Stream is not repeatable and has already been consumed.");
		}
		return stream;
	}
}
