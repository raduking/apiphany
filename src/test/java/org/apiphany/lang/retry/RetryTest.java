package org.apiphany.lang.retry;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apiphany.lang.accumulator.Accumulator;
import org.apiphany.lang.accumulator.DurationAccumulator;
import org.apiphany.lang.accumulator.ExceptionsAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.thread.Threads;
import org.morphix.lang.thread.Threads.ExecutionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link Retry}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class RetryTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetryTest.class);

	private static final int RETRY_COUNT = 3;
	private static final String STRING_RESULT = "Done";
	private static final String NAME = "Foo";

	@Spy
	private Foo inSupplier;

	@Spy
	private Foo inConsumer;

	@Test
	void shouldRetryGivenTimes() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		retry.when(() -> {
			inSupplier.foo();
			return null;
		}, Objects::nonNull);

		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldNotRetryWithNoRetry() {
		Retry retry = Retry.NO_RETRY;

		retry.when(() -> {
			inSupplier.foo();
			return null;
		}, Objects::nonNull);

		verify(inSupplier).foo();
	}

	@Test
	void shouldReturnDefaultRetry() {
		Retry retry = Retry.defaultRetry();

		assertThat(retry, equalTo(Retry.DEFAULT));
	}

	@Test
	void shouldRetryGivenTimesWithEmptyAccumulator() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		retry.when(() -> inSupplier.foo(), Objects::nonNull, Accumulator.noAccumulator());

		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldRetryGivenTimesWithDurationAccumulator() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		DurationAccumulator durationAccumulator = DurationAccumulator.of();
		retry.when(() -> inSupplier.foo(), Objects::nonNull, durationAccumulator);

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(durationAccumulator.getInformationList(), hasSize(RETRY_COUNT));
	}

	@Test
	void shouldRetryGivenTimesWithDurationAccumulatorSupplier() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		retry.when(() -> inSupplier.foo(), Objects::nonNull, DurationAccumulator::of);

		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldRetryGivenTimesAndAccumulateAllExceptions() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		assertThrows(RuntimeException.class,
				() -> retry.when(
						() -> inSupplier.errorFoo(counter.incrementAndGet()),
						Objects::nonNull,
						exceptionsAccumulator));

		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT));
		for (int i = 1; i <= RETRY_COUNT; ++i) {
			verify(inSupplier).errorFoo(i);
			assertThat(exceptionsAccumulator.getExceptions().get(i - 1).getMessage(), equalTo(String.valueOf(i)));
		}
	}

	@Test
	void shouldRetryGivenTimesAndAccumulateExceptions() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		String result = retry.when(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return STRING_RESULT;
		}, Objects::nonNull, exceptionsAccumulator);

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(result, equalTo(STRING_RESULT));
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldFluentRetryGivenTimesAndAccumulateExceptions() {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		var result = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Exception>fluent()
				.stopWhen(STRING_RESULT::equals)
				.accumulateWith(exceptionsAccumulator)
				.on(() -> {
					inSupplier.foo();
					int c = counter.incrementAndGet();
					if (c < RETRY_COUNT) {
						throw expectedExceptions.get(c - 1);
					}
					return STRING_RESULT;
				});

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(result, equalTo(STRING_RESULT));
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldFluentRetryGivenTimesConsumeAndAccumulateExceptions() {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		var retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Exception>fluent()
				.stopWhen(STRING_RESULT::equals)
				.consumeBeforeWait(e -> inConsumer.foo(e))
				.accumulateWith(exceptionsAccumulator);

		var result = retry.on(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return STRING_RESULT;
		});

		verify(inSupplier, times(RETRY_COUNT)).foo();
		for (RuntimeException e : expectedExceptions) {
			verify(inConsumer).foo(e);
		}
		assertThat(result, equalTo(STRING_RESULT));
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldFluentRetryWithRunnableGivenTimesAndAccumulateExceptions() {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		var retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<Object, Exception>fluent()
				.accumulateWith(exceptionsAccumulator);

		retry.on(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return Retry.nonNull();
		});

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldNotRetryWithNoRetryWhenAccumulatingInformation() {
		Retry retry = Retry.NO_RETRY;

		DurationAccumulator durationAccumulator = DurationAccumulator.of();
		String result = retry.when(() -> inSupplier.name(), Objects::nonNull, durationAccumulator);

		verify(inSupplier).name();
		assertThat(durationAccumulator.getInformationList(), hasSize(1));
		assertThat(result, equalTo(NAME));
	}

	@Test
	void shouldRetryFluentGivenTimesWithDurationAccumulator() {
		DurationAccumulator durationAccumulator = DurationAccumulator.of();
		var retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Duration>fluent()
				.accumulateWith(durationAccumulator);

		retry.on(() -> inSupplier.foo());

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(durationAccumulator.getInformationList(), hasSize(RETRY_COUNT));
	}

	@Test
	void shouldReturnNoWait() {
		Wait wait = Retry.noWait();

		assertThat(wait, equalTo(Retry.NO_WAIT));
	}

	@Test
	void shouldRetryWithTheSameRetryInMultipleThreads() {
		int retryCount = 10;
		int threadCount = 10;
		List<Integer> integers = IntStream.range(0, threadCount).boxed().toList();

		Retry retry = Retry.of(WaitCounter.of(retryCount, Duration.ofMillis(10)));

		Threads.executeForEachIn(integers,
				i -> retry.when(() -> inSupplier.foo(), Objects::nonNull), ExecutionType.PARALLEL);

		verify(inSupplier, times(threadCount * retryCount)).foo();
	}

	public static class Foo {

		public <T> T foo() {
			return null;
		}

		public String name() {
			return NAME;
		}

		public String errorFoo(final int i) {
			throw new RuntimeException(String.valueOf(i));
		}

		public void foo(final Exception e) {
			LOGGER.info("Exception: type: {}, message: {}", e.getClass(), e.getMessage());
		}

	}

}
