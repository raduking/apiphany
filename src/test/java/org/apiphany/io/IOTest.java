package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Test class {@link IO}.
 *
 * @author Radu Sebastian LAZIN
 */
class IOTest {

	private static final int SIZE = 48;
	private static final byte[] BYTES = new byte[SIZE];
	static {
		Arrays.fill(BYTES, (byte) 0x0b);
	}

	@Test
	void shouldReadChunkFromInputStream() throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);

		byte[] halfChunk = IO.readChunk(bis, SIZE / 2);
		assertThat(halfChunk.length, equalTo(SIZE / 2));

		halfChunk = IO.readChunk(bis, SIZE / 2);
		assertThat(halfChunk.length, equalTo(SIZE / 2));

		EOFException e = assertThrows(EOFException.class, () -> IO.readChunk(bis, SIZE));
		assertThat(e.getMessage(), equalTo("Stream closed; need " + SIZE + " more bytes"));
	}

	@Test
	void shouldReadChunkFromByteBufferInputStream() throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(BYTES);

		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(buffer);

		byte[] halfChunk = IO.readChunk(bis, SIZE / 2);
		assertThat(halfChunk.length, equalTo(SIZE / 2));

		halfChunk = IO.readChunk(bis, SIZE / 2);
		assertThat(halfChunk.length, equalTo(SIZE / 2));

		EOFException e = assertThrows(EOFException.class, () -> IO.readChunk(bis, SIZE));
		assertThat(e.getMessage(), equalTo("Stream closed; need " + SIZE + " more bytes"));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasLessElementsOnReadFully() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12 });

		int size = 10;
		byte[] buffer = new byte[size];
		EOFException expected = IO.eofExceptionBytesNeeded(size - 1);

		EOFException e = assertThrows(EOFException.class, () -> IO.readFully(bis, buffer, 0, size));
		assertThat(e.getMessage(), equalTo(expected.getMessage()));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasLessElementsOnCopy() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12 });

		int size = 10;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		EOFException expected = IO.eofExceptionBytesNeeded(size - 1);

		EOFException e = assertThrows(EOFException.class, () -> IO.copy(bis, bos, size));
		assertThat(e.getMessage(), equalTo(expected.getMessage()));
	}

	@Test
	void shouldNotDoAnythingIfSizeIsZeroOnCopy() throws IOException {
		ByteArrayInputStream bis = mock(ByteArrayInputStream.class);
		ByteArrayOutputStream bos = mock(ByteArrayOutputStream.class);

		IO.copy(bis, bos, 0);

		verifyNoInteractions(bis, bos);
	}
}
