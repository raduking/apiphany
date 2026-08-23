package org.apiphany.test;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TestResources}.
 *
 * @author Radu Sebastian LAZIN
 */
class TestResourcesTest {

	@Test
	void shouldThrowExceptionOnInstantiatingTestResources() {
		UnsupportedOperationException exception = assertDefaultConstructorThrows(TestResources.class);

		assertNotNull(exception);
	}
}
