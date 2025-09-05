package org.apiphany.lang.retry;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

}
