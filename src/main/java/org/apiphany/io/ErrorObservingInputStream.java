package org.apiphany.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.morphix.lang.function.Runnables;
import org.morphix.lang.function.ThrowingRunnable;
import org.morphix.lang.function.ThrowingSupplier;

/**
 * Wrapper around an {@link InputStream} that observes errors.
 * <p>
 * Errors can be observed by injecting an exception {@link Consumer}.
 * <p>
 * <b>Behavioral contract:</b>
 * <ul>
 * <li>The observer is invoked for any {@link Exception} thrown by the underlying stream.</li>
 * <li>Observed exceptions include checked and unchecked exceptions thrown during read or close operations.</li>
 * <li>The original exception is always rethrown unchanged.</li>
 * <li>No recovery, suppression, or transformation is performed.</li>
 * <li>If the observer itself throws an exception, that exception will propagate and may replace the original.</li>
 * </ul>
 *
 * <p>
 * This class is intended for observability concerns such as:
 * <ul>
 * <li>logging</li>
 * <li>metrics collection</li>
 * <li>error exporting or tracing</li>
 * </ul>
 * It is <b>not</b> intended for error handling or recovery.
 *
 * <p>
 * <b>Thread-safety:</b>
 * <p>
 * This class is not thread-safe. It is intended to be configured once and used by a single thread, like most
 * {@link InputStream} implementations.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ErrorObservingInputStream extends InputStream {

	/**
	 * The underlying input stream being wrapped.
	 */
	private final InputStream inputStream;

	/**
	 * The consumer to notify on exceptions.
	 */
	private final Consumer<? super Exception> onError;

	/**
	 * Constructs a new ErrorObservingInputStream that wraps the given InputStream and notifies the given consumer on
	 * exceptions.
	 *
	 * @param inputStream the underlying input stream to wrap
	 * @param onError the consumer to notify on exceptions
	 * @throws NullPointerException if inputStream or onError is null
	 */
	public ErrorObservingInputStream(final InputStream inputStream, final Consumer<? super Exception> onError) {
		this.inputStream = Objects.requireNonNull(inputStream, "inputStream cannot be null");
		this.onError = Objects.requireNonNull(onError, "onError cannot be null");
	}

	/**
	 * Creates a new ErrorObservingInputStream that wraps the given InputStream and notifies the given consumer on
	 * exceptions.
	 *
	 * @param inputStream the underlying input stream to wrap
	 * @param onError the consumer to notify on exceptions
	 * @return a new ErrorObservingInputStream
	 * @throws NullPointerException if inputStream or onError is null
	 */
	public static ErrorObservingInputStream observe(final InputStream inputStream, final Consumer<? super Exception> onError) {
		return new ErrorObservingInputStream(inputStream, onError);
	}

	/**
	 * @see InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		return tryAndRethrow(ThrowingSupplier.unchecked(inputStream::read));
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return tryAndRethrow(ThrowingSupplier.unchecked(() -> inputStream.read(b, off, len)));
	}

	/**
	 * @see InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {
		return tryAndRethrow(ThrowingSupplier.unchecked(() -> inputStream.read(b)));
	}

	/**
	 * @see InputStream#readAllBytes()
	 */
	@Override
	public byte[] readAllBytes() throws IOException {
		return tryAndRethrow(ThrowingSupplier.unchecked(inputStream::readAllBytes));
	}

	/**
	 * @see InputStream#readNBytes(byte[], int, int)
	 */
	@Override
	public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
		return tryAndRethrow(ThrowingSupplier.unchecked(() -> inputStream.readNBytes(b, off, len)));
	}

	/**
	 * @see InputStream#readNBytes(int)
	 */
	@Override
	public byte[] readNBytes(final int len) throws IOException {
		return tryAndRethrow(ThrowingSupplier.unchecked(() -> inputStream.readNBytes(len)));
	}

	/**
	 * @see InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		tryAndRethrow(ThrowingRunnable.unchecked(inputStream::close));
	}

	/**
	 * Helper method to execute a supplier and notify on exceptions.
	 *
	 * @param <T> the supplier return type
	 *
	 * @param supplier the supplier to execute
	 * @return the supplier result
	 */
	protected <T> T tryAndRethrow(final Supplier<T> supplier) {
		return tryAndRethrow(supplier, onError);
	}

	/**
	 * Static helper method to execute a supplier and notify on exceptions.
	 *
	 * @param <T> the supplier return type
	 *
	 * @param supplier the supplier to execute
	 * @param onError the consumer to notify on exceptions
	 * @return the supplier result
	 */
	protected static <T> T tryAndRethrow(final Supplier<T> supplier, final Consumer<? super Exception> onError) {
		try {
			return supplier.get();
		} catch (Exception e) {
			onError.accept(e);
			throw e;
		}
	}

	/**
	 * Helper method to execute a runnable and notify on exceptions.
	 *
	 * @param runnable the runnable to execute
	 */
	protected void tryAndRethrow(final Runnable runnable) {
		tryAndRethrow(Runnables.toSupplier(runnable), onError);
	}
}
