package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ByteBufferInputStream}.
 *
 * @author Radu Sebastian LAZIN
 */
class ByteBufferInputStreamTest {

	private static final byte[] BYTES = { 10, 20, 30 };

	@Test
	void shouldDuplicateTheByteBufferWhenConstructing() {
		ByteBuffer original = ByteBuffer.wrap(BYTES);

		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(original);

		bis.read();
		bis.read();

		assertThat(original.position(), equalTo(0));
	}

	@Test
	void shouldThrowNullPointerExceptionIfReadIsCalledWithNullBuffer() {
		ByteBuffer original = ByteBuffer.wrap(BYTES);
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(original);

		NullPointerException npe = assertThrows(NullPointerException.class, () -> bis.read(null, 0, 0));

		assertThat(npe.getMessage(), equalTo("Byte array cannot be null"));
	}

	@Test
	void shouldThrowExceptionIfReadIsCalledWithWrongParameters() {
		ByteBuffer original = ByteBuffer.wrap(BYTES);
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(original);

		byte[] bytes = new byte[BYTES.length];

		assertThrows(IndexOutOfBoundsException.class, () -> bis.read(bytes, -1, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> bis.read(bytes, 0, -1));
		assertThrows(IndexOutOfBoundsException.class, () -> bis.read(bytes, 0, BYTES.length + 1));
	}

	@Test
	void shouldReturnTrueOnMarkSupported() {
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES);

		boolean result = bis.markSupported();

		assertTrue(result);
	}

	@Test
	void shouldAdvanceByteBufferOnSimpleRead() {
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES);

		bis.read();

		ByteBuffer buffer = bis.getByteBuffer();

		int remaining = buffer.remaining();

		assertThat(remaining, equalTo(BYTES.length - 1));
		assertThat(bis.available(), equalTo(remaining));
	}

	@Test
	void shouldResetByteBufferOnReset() {
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES);

		bis.mark(0);
		bis.read();
		bis.reset();

		ByteBuffer buffer = bis.getByteBuffer();

		assertThat(buffer.remaining(), equalTo(BYTES.length));
	}

	@Test
	void shouldSkipAheadByteBufferOnSkip() throws IOException {
		try (ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES)) {
			bis.skip(1);

			ByteBuffer buffer = bis.getByteBuffer();

			int remaining = buffer.remaining();

			assertThat(remaining, equalTo(BYTES.length - 1));
			assertThat(bis.available(), equalTo(remaining));
		}
	}

}
