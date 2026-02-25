package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apiphany.lang.Bytes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Test class for {@link ByteBufferSubscriber}.
 *
 * @author Radu Sebastian LAZIN
 */
class ByteBufferSubscriberTest {

	private static final int INT_BUFFER_SIZE = 10;
	private static final long BUFFER_SIZE = INT_BUFFER_SIZE;

	@Nested
	class ConstructorTests {

		@Test
		void shouldCreateSubscriberWithDefaultBufferSize() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();

			assertThat(subscriber.getMaxBytes(), equalTo(Long.valueOf(Integer.MAX_VALUE)));
			assertNotNull(subscriber);
		}

		@Test
		void shouldCreateSubscriberForGivenBufferSize() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);

			assertThat(subscriber.getMaxBytes(), equalTo(BUFFER_SIZE));
			assertNotNull(subscriber);
		}

		@Test
		void shouldThrowIllegalArgumentExceptionForNegativeBufferSize() {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new ByteBufferSubscriber(-1));

			assertThat(e.getMessage(), equalTo("maxBytes cannot be negative"));
		}
	}

	@Nested
	class OnSubscribeTests {

		@Test
		void shouldAcceptValidSubscription() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);

			assertThat(subscription.isRequested(), equalTo(true));
		}

		@Test
		void shouldThrowNullPointerExceptionForNullSubscription() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();

			NullPointerException e = assertThrows(NullPointerException.class, () -> subscriber.onSubscribe(null));

			assertThat(e.getMessage(), equalTo("subscription cannot be null"));
		}

		@Test
		void shouldCancelDuplicateSubscription() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription firstSubscription = new TestSubscription();

			subscriber.onSubscribe(firstSubscription);

			assertThat(firstSubscription.isRequested(), equalTo(true));

			TestSubscription secondSubscription = new TestSubscription();

			subscriber.onSubscribe(secondSubscription);

			assertThat(secondSubscription.isCancelled(), equalTo(true));
		}

		@Test
		void shuldSetSubscriptionReferenceOnlyOnce() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription firstSubscription = new TestSubscription();

			subscriber.onSubscribe(firstSubscription);

			assertThat(firstSubscription.isRequested(), equalTo(true));
			assertThat(subscriber.getSubscription(), equalTo(firstSubscription));

			TestSubscription secondSubscription = new TestSubscription();

			subscriber.onSubscribe(secondSubscription);

			assertThat(secondSubscription.isCancelled(), equalTo(true));
			assertThat(subscriber.getSubscription(), equalTo(firstSubscription));
		}
	}

	@Nested
	class OnNextTests {

		@Test
		void shouldThrowNullPointerExceptionForNullItem() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();

			NullPointerException e = assertThrows(NullPointerException.class, () -> subscriber.onNext(null));

			assertThat(e.getMessage(), equalTo("received item cannot be null"));
		}

		@Test
		void shouldCancelSubscriptionWhenBufferLimitExceeded() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.allocate(INT_BUFFER_SIZE + 1));

			assertThat(subscription.isCancelled(), equalTo(true));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(true));
		}

		@Test
		void shouldAddBufferWhenWithinLimit() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.allocate(INT_BUFFER_SIZE - 1));

			assertThat(subscription.isCancelled(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
		}

		@Test
		void shouldAddBufferWhenExactlyLimit() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.allocate(INT_BUFFER_SIZE));

			assertThat(subscription.isCancelled(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
		}

		@Test
		void shouldCancelSubscriptionWhenBufferLimitExceededAfterMultipleOnNext() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.allocate(INT_BUFFER_SIZE - 1));
			subscriber.onNext(ByteBuffer.allocate(2));

			assertThat(subscription.isCancelled(), equalTo(true));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(true));
		}

		@Test
		void shouldNotAddBufferAfterSubscriptionCancelled() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.allocate(INT_BUFFER_SIZE + 1));
			subscriber.onNext(ByteBuffer.allocate(1));

			assertThat(subscription.isCancelled(), equalTo(true));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(true));
		}

		@Test
		void shouldNotAddBufferAfterError() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onError(new RuntimeException("Test error"));
			subscriber.onNext(ByteBuffer.allocate(1));

			assertThat(subscription.isCancelled(), equalTo(true));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
		}

		@Test
		void shouldNotAddBufferAfterBufferLimitExceeded() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.allocate(INT_BUFFER_SIZE + 1));
			subscriber.onNext(ByteBuffer.allocate(1));

			assertThat(subscription.isCancelled(), equalTo(true));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(true));
		}

		@Test
		void shouldHandleMultipleOnNextWithinLimit() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			for (int i = 0; i < BUFFER_SIZE; ++i) {
				subscriber.onNext(ByteBuffer.allocate(1));
			}

			assertThat(subscription.isCancelled(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
		}
	}

	@Nested
	class ConcurrentOnNextTests {

		private static final int THREADS = 10;
		private static final int BUFFERS_PER_THREAD = 100;

		private static final int MAX_BYTES = 10;

		@Test
		void shouldHandleConcurrentOnNextCalls() throws InterruptedException {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);

			Thread thread1 = Thread.ofVirtual().unstarted(() -> subscriber.onNext(ByteBuffer.allocate(1)));
			Thread thread2 = Thread.ofVirtual().unstarted(() -> subscriber.onNext(ByteBuffer.allocate(1)));

			thread1.start();
			thread2.start();

			thread1.join();
			thread2.join();

			assertThat(subscription.isCancelled(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
			assertThat(subscriber.getBufferCount(), equalTo(2));
		}

		@Test
		@Timeout(5)
		void shouldHandleConcurrentOnNextCallsWithMultipleThreads() throws InterruptedException {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();

			AtomicInteger exceptions = new AtomicInteger(0);
			AtomicInteger successes = new AtomicInteger(0);
			ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(THREADS);

			for (int i = 0; i < THREADS; ++i) {
				final int threadId = i;
				executor.submit(() -> {
					try {
						startLatch.await();
						for (int j = 0; j < BUFFERS_PER_THREAD; ++j) {
							try {
								ByteBuffer buffer = ByteBuffer.wrap(("Thread" + threadId + "-Data" + j).getBytes());
								subscriber.onNext(buffer);
								successes.incrementAndGet();
							} catch (Exception e) {
								exceptions.incrementAndGet();
							}
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} finally {
						endLatch.countDown();
					}
				});
			}
			startLatch.countDown();

			endLatch.await(10, TimeUnit.SECONDS);
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.SECONDS);

			assertThat(exceptions.get(), equalTo(0));
			assertThat(subscriber.getBufferCount(), equalTo(THREADS * BUFFERS_PER_THREAD));
			assertThat(successes.get(), equalTo(THREADS * BUFFERS_PER_THREAD));
			assertFalse(subscriber.hasError());
			assertFalse(subscriber.hasBufferLimitExceeded());
		}

		@Test
		@Timeout(5)
		void shouldHandleConcurrentOnNextWhenLimitExceeded() throws InterruptedException {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(MAX_BYTES);

			AtomicInteger receivedCount = new AtomicInteger(0);
			AtomicInteger rejectedCount = new AtomicInteger(0);
			ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(THREADS);

			int bufferSize = MAX_BYTES / 2;
			int buffersPerThread = 3;
			byte[] data = new byte[bufferSize];
			ByteBuffer buffer = ByteBuffer.wrap(data);

			for (int i = 0; i < THREADS; ++i) {
				executor.submit(() -> {
					try {
						startLatch.await();
						for (int j = 0; j < buffersPerThread; ++j) {
							subscriber.onNext(buffer.duplicate());
							if (subscriber.hasBufferLimitExceeded()) {
								rejectedCount.incrementAndGet();
							} else {
								receivedCount.incrementAndGet();
							}
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} finally {
						endLatch.countDown();
					}
				});
			}
			startLatch.countDown();

			endLatch.await(10, TimeUnit.SECONDS);
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.SECONDS);

			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(true));
			assertThat(receivedCount.get() + rejectedCount.get(), equalTo(THREADS * buffersPerThread));
			assertThat(subscriber.isCompleted(), equalTo(false));
		}
	}

	@Nested
	class OnErrorTests {

		@Test
		void shouldCancelSubscriptionOnError() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onError(new RuntimeException("Test error"));

			assertThat(subscription.isCancelled(), equalTo(true));
			assertThat(subscriber.isCompleted(), equalTo(true));
			assertThat(subscriber.hasError(), equalTo(true));
			assertThat(subscriber.getError().getMessage(), equalTo("Test error"));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
		}
	}

	@Nested
	class OnCompleteTests {

		@Test
		void shouldCompleteSubscriptionOnComplete() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onComplete();

			assertThat(subscription.isCancelled(), equalTo(false));
			assertThat(subscriber.isCompleted(), equalTo(true));
			assertThat(subscriber.hasError(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
		}
	}

	@Nested
	class CancelTests {

		@Test
		void shouldCancelSubscription() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.cancel();

			assertThat(subscription.isCancelled(), equalTo(true));
			assertThat(subscriber.isCompleted(), equalTo(false));
			assertThat(subscriber.hasError(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
		}

		@Test
		void shouldBeAbleToCancelSubscriptionMultipleTimes() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.cancel();
			subscriber.cancel();

			assertThat(subscription.isCancelled(), equalTo(true));
		}
	}

	@Nested
	class GetReceivedBytesTests {

		@Test
		void shouldReturnCombinedBytesFromReceivedBuffers() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.wrap(new byte[] { 1, 2, 3 }));
			subscriber.onNext(ByteBuffer.wrap(new byte[] { 4, 5 }));

			byte[] receivedBytes = subscriber.getReceivedBytes();

			assertThat(receivedBytes, equalTo(new byte[] { 1, 2, 3, 4, 5 }));
		}

		@Test
		void shouldThrowIllegalStateExceptionWhenErrorOccurred() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onError(new RuntimeException("Test error"));

			IllegalStateException e = assertThrows(IllegalStateException.class, () -> subscriber.getReceivedBytes());

			assertThat(e.getMessage(), equalTo("Error occurred during subscription"));
			assertThat(e.getCause().getMessage(), equalTo("Test error"));
		}

		@Test
		void shouldReturnEmptyArrayWhenNoBuffersReceived() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);

			byte[] receivedBytes = subscriber.getReceivedBytes();

			assertThat(receivedBytes, equalTo(new byte[0]));
			assertThat(receivedBytes, equalTo(Bytes.EMPTY));
			assertThat(subscriber.getBufferCount(), equalTo(0));
			assertThat(subscriber.isCompleted(), equalTo(false));
			assertThat(subscriber.hasError(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
			assertThat(subscription.isCancelled(), equalTo(false));
			assertThat(subscription.isRequested(), equalTo(true));
		}
	}

	@Nested
	class AwaitCompletionTests {

		@Test
		void shouldCompleteNormallyWhenSubscriptionCompletes() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onComplete();

			subscriber.awaitCompletion();

			assertThat(subscriber.isCompleted(), equalTo(true));
		}

		@Test
		void shouldCompleteNormallyWhenSubscriptionErrors() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onError(new RuntimeException("Test error"));

			subscriber.awaitCompletion();

			assertThat(subscriber.isCompleted(), equalTo(true));
			assertThat(subscriber.hasError(), equalTo(true));
			assertThat(subscriber.getError().getMessage(), equalTo("Test error"));
		}

		@Test
		void shouldCompleteNormallyWhenSubscriptionCancelled() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.cancel();

			subscriber.awaitCompletion(Duration.ofMillis(100), Duration.ofMillis(10));

			assertThat(subscriber.isCompleted(), equalTo(false));
			assertThat(subscriber.hasError(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(false));
		}

		@Test
		void shouldCompleteNormallyWhenSubscriptionCancelledAfterBufferLimitExceeded() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber(BUFFER_SIZE);
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.allocate(INT_BUFFER_SIZE + 1));

			subscriber.awaitCompletion(Duration.ofMillis(100), Duration.ofMillis(10));

			assertThat(subscriber.isCompleted(), equalTo(false));
			assertThat(subscriber.hasError(), equalTo(false));
			assertThat(subscriber.hasBufferLimitExceeded(), equalTo(true));
		}
	}

	@Nested
	class GetBufferCountTests {

		@Test
		void shouldReturnCorrectBufferCount() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);
			subscriber.onNext(ByteBuffer.allocate(1));
			subscriber.onNext(ByteBuffer.allocate(2));

			assertThat(subscriber.getBufferCount(), equalTo(2));
		}

		@Test
		void shouldReturnZeroWhenNoBuffersReceived() {
			ByteBufferSubscriber subscriber = new ByteBufferSubscriber();
			TestSubscription subscription = new TestSubscription();

			subscriber.onSubscribe(subscription);

			assertThat(subscriber.getBufferCount(), equalTo(0));
		}
	}

	static class TestSubscription implements Subscription {

		private boolean requested = false;

		private boolean cancelled = false;

		@Override
		public void request(final long n) {
			this.requested = true;
		}

		@Override
		public void cancel() {
			this.cancelled = true;
		}

		public boolean isRequested() {
			return requested;
		}

		public boolean isCancelled() {
			return cancelled;
		}
	}

}
