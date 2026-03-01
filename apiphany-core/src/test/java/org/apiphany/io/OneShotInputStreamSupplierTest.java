package org.apiphany.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OneShotInputStreamSupplier}.
 *
 * @author Radu Sebastian LAZIN
 */
class OneShotInputStreamSupplierTest {

	private static final byte[] TEST_DATA = "Test data".getBytes();

	@Test
	@SuppressWarnings("resource")
	void shouldReturnTheGivenInputStream() {
		ByteArrayInputStream stream = new ByteArrayInputStream(TEST_DATA);
		OneShotInputStreamSupplier supplier = new OneShotInputStreamSupplier(stream);

		InputStream result = supplier.get();

		assertThat(result, is(sameInstance(stream)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowExceptionWhenStreamIsConsumedMoreThanOnce() {
		ByteArrayInputStream stream = new ByteArrayInputStream(TEST_DATA);
		OneShotInputStreamSupplier supplier = new OneShotInputStreamSupplier(stream);
		supplier.get();

		IllegalStateException e = assertThrows(IllegalStateException.class, supplier::get);

		assertThat(e.getMessage(), is("Stream is not repeatable and has already been consumed."));
	}
}
