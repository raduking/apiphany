package org.apiphany.test.http;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.morphix.lang.thread.Threads;

/**
 * A Flow.Subscriber implementation that collects ByteBuffer items and allows retrieval of the combined byte array.
 *
 * @author Radu Sebastian LAZIN
 */
public class ByteBufferBodySubscriber implements Subscriber<ByteBuffer> {

	/**
	 * List to store received ByteBuffer items.
	 */
	private final List<ByteBuffer> receivedBuffers = new ArrayList<>();

	/**
	 * Subscription reference to manage the flow of data.
	 */
	private Subscription subscription;

	/**
	 * Error encountered during the subscription, if any.
	 */
	private Throwable error;

	/**
	 * Flag indicating whether the subscription has completed.
	 */
	private boolean completed = false;

	/**
	 * Default constructor.
	 */
	public ByteBufferBodySubscriber() {
		// empty
	}

	/**
	 * @see Subscriber#onSubscribe(Subscription)
	 */
	@Override
	public void onSubscribe(final Subscription subscription) {
		this.subscription = subscription;
		subscription.request(Long.MAX_VALUE);
	}

	/**
	 * @see Subscriber#onNext(Object)
	 */
	@Override
	public void onNext(final ByteBuffer item) {
		receivedBuffers.add(item);
	}

	/**
	 * @see Subscriber#onError(Throwable)
	 */
	@Override
	public void onError(final Throwable throwable) {
		this.error = throwable;
	}

	/**
	 * @see Subscriber#onComplete()
	 */
	@Override
	public void onComplete() {
		this.completed = true;
	}

	/**
	 * Retrieves the combined byte array from all received ByteBuffer items.
	 *
	 * @return combined byte array
	 */
	public byte[] getReceivedBytes() {
		int totalSize = receivedBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
		byte[] result = new byte[totalSize];
		int offset = 0;

		for (ByteBuffer buffer : receivedBuffers) {
			int length = buffer.remaining();
			buffer.get(result, offset, length);
			offset += length;
		}
		return result;
	}

	/**
	 * Checks if the subscription has completed.
	 *
	 * @return true if completed, false otherwise
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * Retrieves any error encountered during the subscription.
	 *
	 * @return encountered error, or null if none
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * Retrieves the subscription reference.
	 *
	 * @return subscription reference
	 */
	public Subscription getSubscription() {
		return subscription;
	}

	/**
	 * Awaits the completion of the subscription, checking periodically.
	 */
	public void awaitCompletion() {
		int attempts = 0;
		while (!completed && error == null && attempts < 100) {
			Threads.safeSleep(Duration.ofMillis(10));
			attempts++;
		}
	}
}
