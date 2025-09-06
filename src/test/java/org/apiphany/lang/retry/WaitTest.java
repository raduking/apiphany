package org.apiphany.lang.retry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Wait}.
 *
 * @author Radu Sebastian LAZIN
 */
class WaitTest {

	@Test
	void shouldHaveFunctionalInterfaceAnnotation() {
		assertTrue(Wait.class.isAnnotationPresent(FunctionalInterface.class));
	}

	static class TestWait implements Wait {

		Object member = mock(Object.class);

		@Override
		public boolean keepWaiting() {
			return false;
		}
	};

	@Test
	void shouldHaveNoSideEffectsOnCallingDefaultMethods() {
		TestWait wait = new TestWait();
		wait.start();
		wait.now();

		assertFalse(wait.keepWaiting());

		verifyNoInteractions(wait.member);
	}

	@Test
	void shouldReturnTheSameInstanceOnCopy() {
		TestWait wait = new TestWait();

		Wait copy = wait.copy();

		assertSame(wait, copy);
	}
}
