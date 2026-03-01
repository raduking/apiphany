package org.apiphany.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link InputStreamSupplier}.
 *
 * @author Radu Sebastian LAZIN
 */
class InputStreamSupplierTest {

	private static final byte[] TEST_DATA = "Test data".getBytes();

	@Test
	@SuppressWarnings("resource")
	void shouldConvertSupplierToInputStreamSupplier() {
		ByteArrayInputStream stream = new ByteArrayInputStream(TEST_DATA);

		InputStreamSupplier supplier = InputStreamSupplier.from(() -> stream);

		assertThat(supplier.get(), is(sameInstance(stream)));
	}
}
