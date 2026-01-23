package org.apiphany.utils.client.http;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

import org.morphix.lang.thread.Threads;

public class TestBodySubscriber implements Flow.Subscriber<ByteBuffer> {

	private final List<ByteBuffer> receivedBuffers = new ArrayList<>();
	private Flow.Subscription subscription;
	private Throwable error;
	private boolean completed = false;

	@Override
	public void onSubscribe(final Flow.Subscription subscription) {
		this.subscription = subscription;
		subscription.request(Long.MAX_VALUE);
	}

	@Override
	public void onNext(final ByteBuffer item) {
		receivedBuffers.add(item);
	}

	@Override
	public void onError(final Throwable throwable) {
		this.error = throwable;
	}

	@Override
	public void onComplete() {
		this.completed = true;
	}

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

	public boolean isCompleted() {
		return completed;
	}

	public Throwable getError() {
		return error;
	}

	public Flow.Subscription getSubscription() {
		return subscription;
	}

	public void awaitCompletion() {
		int attempts = 0;
		while (!completed && error == null && attempts < 100) {
			Threads.safeSleep(Duration.ofMillis(10));
			attempts++;
		}
	}
}
