package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
	void shouldSkipAheadByteBufferOnSkip() {
		try (ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES)) {
			long result = bis.skip(1);

			ByteBuffer buffer = bis.getByteBuffer();

			int remaining = buffer.remaining();

			assertThat(remaining, equalTo(BYTES.length - 1));
			assertThat(bis.available(), equalTo(remaining));
			assertThat(result, equalTo(1L));
		}
	}

	@Test
	void shouldReturnNegativeOneWhenStreamReachesTheEnd() {
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES);

		byte[] bytes = new byte[BYTES.length];
		bis.read(bytes, 0, BYTES.length);

		int result = bis.read();

		assertThat(result, equalTo(-1));
	}

	@Test
	void shouldReadAllBytesIntoByteArray() {
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES);

		byte[] bytes = new byte[BYTES.length];
		int result = bis.read(bytes);

		assertThat(result, equalTo(BYTES.length));
		for (int i = 0; i < BYTES.length; i++) {
			assertThat(bytes[i], equalTo(BYTES[i]));
		}
	}

	@Test
	void shouldReadAllAvailableBytesIntoArray() {
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES);

		byte[] bytes = new byte[BYTES.length * 2];
		byte fillByte = (byte) 0xFF;
		Arrays.fill(bytes, fillByte);

		int result = bis.read(bytes);

		assertThat(result, equalTo(BYTES.length));
		for (int i = 0; i < BYTES.length; i++) {
			assertThat(bytes[i], equalTo(BYTES[i]));
		}
		for (int i = BYTES.length; i < bytes.length; i++) {
			assertThat(bytes[i], equalTo(fillByte));
		}
	}

	@Test
	void shouldReturnZeroOnSkipIfInputIsZero() {
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES);

		long result = bis.skip(0);

		assertThat(result, equalTo(0L));
	}

	@Test
	void shouldReturnZeroOnSkipIfInputIsNegative() {
		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(BYTES);

		long result = bis.skip(-42);

		assertThat(result, equalTo(0L));
	}
}
