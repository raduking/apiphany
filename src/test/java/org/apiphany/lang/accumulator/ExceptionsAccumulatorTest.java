package org.apiphany.lang.accumulator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.apiphany.lang.accumulator.ExceptionsAccumulator.ThrowMode;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExceptionsAccumulator}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExceptionsAccumulatorTest {

	private static final int COUNT = 3;

	@Test
	void shouldBuildExceptionsAccumulatorFromSet() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Set.of(RuntimeException.class));

		assertThat(ea.getExceptionTypes(), hasSize(1));
		assertThat(ea.getExceptionTypes().getFirst(), equalTo(RuntimeException.class));
		assertFalse(ea.isWrapException());
		assertTrue(ea.isThrowException());
	}

	@Test
	void shouldBuildExceptionsAccumulatorFromNullThrowMode() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of((ThrowMode) null);

		assertThat(ea.getExceptionTypes(), empty());
	}

	@Test
	void shouldBuildExceptionsAccumulatorFromNullExceptions() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of((Set<Class<?>>) null);

		assertThat(ea.getExceptionTypes(), empty());
	}

	@Test
	void shouldBuildExceptionsAccumulator() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();

		assertThat(ea.getExceptionTypes(), empty());
		assertFalse(ea.isWrapException());
		assertTrue(ea.isThrowException());
	}

	@Test
	void shouldBuildExceptionsAccumulatorWithAllParams() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(ThrowMode.THROW_NONE, Set.of(RuntimeException.class));

		assertThat(ea.getExceptionTypes(), hasSize(1));
		assertThat(ea.getExceptionTypes().getFirst(), equalTo(RuntimeException.class));
		assertFalse(ea.isWrapException());
		assertFalse(ea.isThrowException());
	}

	@Test
	void shouldBuildExceptionsAccumulatorWithThrowMode() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(ThrowMode.THROW_NONE);

		assertThat(ea.getExceptionTypes(), hasSize(0));
		assertFalse(ea.isWrapException());
		assertFalse(ea.isThrowException());
	}

	@Test
	void shouldAccumulateAllExceptions() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate(() -> {
				throw new RuntimeException();
			});
		}

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertTrue(ea.hasExceptions());
	}

	@Test
	void shouldNotAccumulateAnything() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate(() -> COUNT);
		}

		assertThat(ea.getExceptions(), hasSize(0));
		assertFalse(ea.hasExceptions());
	}

	@Test
	void shouldAccumulateAllExceptionsWithRunnable() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException();
			});
		}

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertTrue(ea.hasExceptions());
	}

	@Test
	void shouldNotAccumulateAnythingWithRunnable() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate(() -> {
				@SuppressWarnings("unused")
				int x = 0;
			});
		}

		assertThat(ea.getExceptions(), hasSize(0));
		assertFalse(ea.hasExceptions());
	}

	@Test
	void shouldAccumulateOnlyRequiredExceptions() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Set.of(IllegalArgumentException.class));
		for (int i = 0; i < COUNT; ++i) {
			try {
				ea.accumulate(() -> {
					throw new RuntimeException();
				});
			} catch (Exception e) {
				// swallow
			}
			int n = i;
			try {
				ea.accumulate(() -> {
					throw new IllegalArgumentException(String.valueOf(n));
				});
			} catch (Exception e) {
				// swallow
			}
		}

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertThat(ea.lastException().getMessage(), equalTo(String.valueOf(COUNT - 1)));
	}

	@Test
	void shouldThrowLastExceptionOnRest() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		RuntimeException result = assertThrows(RuntimeException.class, ea::rest);

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertThat(result.getMessage(), equalTo(String.valueOf(COUNT - 1)));
	}

	@Test
	void shouldThrowLastExceptionOnRestWhenThrowRawIsConfigured() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(ThrowMode.THROW_RAW);
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		RuntimeException result = assertThrows(RuntimeException.class, ea::rest);

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertThat(result.getMessage(), equalTo(String.valueOf(COUNT - 1)));
	}

	@Test
	void shouldThrowWrappedLastExceptionOnRest() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(ThrowMode.THROW_WRAPPED);
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		AccumulatorException result = assertThrows(AccumulatorException.class, ea::rest);

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertThat(result.getCause().getMessage(), equalTo(String.valueOf(COUNT - 1)));
		assertTrue(ea.isWrapException());
	}

	@Test
	void shouldNotDoAnythingWhenNoWrappingOrThrowingIsConfiguredOnRest() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(ThrowMode.THROW_NONE);
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		ea.rest();

		assertThat(ea.getExceptions(), hasSize(COUNT));
	}

	@Test
	void shouldNotDoAnythingWhenNoThrowingIsConfiguredOnRest() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(ThrowMode.THROW_NONE);
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		ea.rest();

		assertThat(ea.getExceptions(), hasSize(COUNT));
	}

	@Test
	void shouldNotDoAnythingOnRestWhenNothingIsAccumulated() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate(() -> COUNT);
		}
		ea.rest();

		assertFalse(ea.hasExceptions());
	}
}
