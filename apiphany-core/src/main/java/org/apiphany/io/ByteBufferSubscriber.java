package org.apiphany.io;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apiphany.lang.Bytes;
import org.morphix.lang.thread.Threads;

/**
 * A Flow.Subscriber implementation that collects ByteBuffer items and allows retrieval of the combined byte array.
 *
 * @author Radu Sebastian LAZIN
 */
public class ByteBufferSubscriber implements Subscriber<ByteBuffer> {

	/**
	 * List to store received ByteBuffer items.
	 */
	private final List<ByteBuffer> receivedBuffers = new ArrayList<>();

	/**
	 * Read/write lock for thread-safe access to the buffers list.
	 */
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Atomic reference for Subscription reference to manage the flow of data and ensure thread safety.
	 */
	private final AtomicReference<Subscription> subscriptionReference = new AtomicReference<>();

	/**
	 * Atomic reference for error to ensure thread-safe updates.
	 */
	private final AtomicReference<Throwable> errorReference = new AtomicReference<>();

	/**
	 * Atomic boolean for completion flag to ensure thread-safe updates.
	 */
	private final AtomicBoolean completed = new AtomicBoolean(false);

	/**
	 * Flag to track if we've exceeded the maximum buffer limit.
	 */
	private final AtomicBoolean bufferTooLarge = new AtomicBoolean(false);

	/**
	 * Atomic long for total bytes received, used for monitoring and debugging.
	 */
	private final AtomicLong receivedBytes = new AtomicLong(0L);

	/**
	 * Maximum number of buffers to store before canceling the subscription to prevent memory issues.
	 */
	private final long maxBytes;

	/**
	 * Default constructor.
	 */
	public ByteBufferSubscriber() {
		this(IOStreams.MAX_BUFFER_SIZE);
	}

	/**
	 * Constructor with configurable max bytes limit.
	 *
	 * @param maxBytes maximum number of bytes to store
	 * @throws IllegalArgumentException if maxBytes is negative
	 */
	public ByteBufferSubscriber(final long maxBytes) {
		if (maxBytes < 0) {
			throw new IllegalArgumentException("maxBytes cannot be negative");
		}
		this.maxBytes = maxBytes;
	}

	/**
	 * @see Subscriber#onSubscribe(Subscription)
	 */
	@Override
	public void onSubscribe(final Subscription subscription) {
		if (subscription == null) {
			throw new NullPointerException("subscription cannot be null");
		}
		if (!subscriptionReference.compareAndSet(null, subscription)) {
			// if subscription already exists, cancel the new one
			subscription.cancel();
			return;
		}
		subscription.request(Long.MAX_VALUE);
	}

	/**
	 * @see Subscriber#onNext(Object)
	 */
	@Override
	public void onNext(final ByteBuffer item) {
		Objects.requireNonNull(item, "received item cannot be null");
		if (hasBufferLimitExceeded() || hasError()) {
			cancel();
			return;
		}
		ByteBuffer copy = ByteBuffer.allocate(item.remaining())
				.put(item.duplicate())
				.flip();

		int newBytes = copy.remaining();
		long currentTotal = receivedBytes.get();

		if (currentTotal + newBytes > maxBytes) {
			bufferTooLarge.set(true);
			cancel();
			return;
		}
		lock.writeLock().lock();
		try {
			if (hasBufferLimitExceeded() || hasError()) {
				return;
			}
			receivedBuffers.add(copy);
			receivedBytes.addAndGet(newBytes);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * @see Subscriber#onError(Throwable)
	 */
	@Override
	public void onError(final Throwable throwable) {
		Objects.requireNonNull(throwable, "throwable cannot be null");
		errorReference.set(throwable);
		completed.set(true);
		cancel();
	}

	/**
	 * @see Subscriber#onComplete()
	 */
	@Override
	public void onComplete() {
		completed.set(true);
	}

	/**
	 * Retrieves the combined byte array from all received ByteBuffer items.
	 *
	 * @return combined byte array
	 */
	public byte[] getReceivedBytes() {
		Throwable error = errorReference.get();
		if (error != null) {
			throw new IllegalStateException("Error occurred during subscription", error);
		}

		lock.readLock().lock();
		try {
			if (receivedBuffers.isEmpty()) {
				return Bytes.EMPTY;
			}
			int totalSize = 0;
			for (ByteBuffer buffer : receivedBuffers) {
				totalSize += buffer.remaining();
			}

			byte[] result = new byte[totalSize];
			int offset = 0;

			for (ByteBuffer buffer : receivedBuffers) {
				// create a duplicate to avoid modifying the original buffer position
				ByteBuffer readableBuffer = buffer.duplicate();
				int length = readableBuffer.remaining();
				readableBuffer.get(result, offset, length);
				offset += length;
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks if the subscription has completed.
	 *
	 * @return true if completed, false otherwise
	 */
	public boolean isCompleted() {
		return completed.get();
	}

	/**
	 * Retrieves any error encountered during the subscription.
	 *
	 * @return encountered error, or null if none
	 */
	public Throwable getError() {
		return errorReference.get();
	}

	/**
	 * Retrieves the subscription reference.
	 *
	 * @return subscription reference
	 */
	public Subscription getSubscription() {
		return subscriptionReference.get();
	}

	/**
	 * Awaits the completion of the subscription, checking periodically. This method is thread-safe but should be used with
	 * caution as it blocks.
	 *
	 * @param timeout maximum time to wait for completion
	 * @param pollInterval interval between completion checks
	 * @return true if completed within timeout, false otherwise
	 */
	public boolean awaitCompletion(final Duration timeout, final Duration pollInterval) {
		long startTime = System.currentTimeMillis();
		long timeoutMillis = timeout.toMillis();

		while (!completed.get() && null == errorReference.get()) {
			if (System.currentTimeMillis() - startTime > timeoutMillis) {
				return false;
			}
			Threads.safeSleep(pollInterval);
		}
		return true;
	}

	/**
	 * Awaits the completion of the subscription, checking periodically.
	 *
	 * @return true if completed within default timeout, false otherwise
	 */
	public boolean awaitCompletion() {
		return awaitCompletion(Duration.ofSeconds(1), Duration.ofMillis(10));
	}

	/**
	 * Returns the number of received buffers.
	 *
	 * @return number of buffers
	 */
	public int getBufferCount() {
		lock.readLock().lock();
		try {
			return receivedBuffers.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks if maximum buffer limit was exceeded.
	 *
	 * @return true if maximum buffer limit was exceeded
	 */
	public boolean hasBufferLimitExceeded() {
		return bufferTooLarge.get();
	}

	/**
	 * Checks if an error was encountered during the subscription.
	 *
	 * @return true if an error was encountered, false otherwise
	 */
	public boolean hasError() {
		return null != errorReference.get();
	}

	/**
	 * Returns the maximum number of bytes that can be received before the subscription is canceled to prevent memory
	 * issues.
	 *
	 * @return the maximum number of bytes that can be received
	 */
	public long getMaxBytes() {
		return maxBytes;
	}

	/**
	 * Cancels the subscription if active.
	 */
	public void cancel() {
		Subscription subscription = subscriptionReference.get();
		if (subscription != null) {
			subscription.cancel();
		}
		// clear all received buffers to free up memory
		lock.writeLock().lock();
		try {
			receivedBuffers.clear();
			receivedBytes.set(0L);
		} finally {
			lock.writeLock().unlock();
		}
	}
}
