package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Test class {@link IOStreams}.
 *
 * @author Radu Sebastian LAZIN
 */
class IOStreamsTest {

	private static final int SIZE = 48;
	private static final byte[] BYTES = new byte[SIZE];
	static {
		Arrays.fill(BYTES, (byte) 0x0b);
	}

	@Test
	void shouldReadChunkFromInputStream() throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);

		byte[] halfChunk = IOStreams.readChunk(bis, SIZE / 2);
		assertThat(halfChunk.length, equalTo(SIZE / 2));

		halfChunk = IOStreams.readChunk(bis, SIZE / 2);
		assertThat(halfChunk.length, equalTo(SIZE / 2));

		EOFException e = assertThrows(EOFException.class, () -> IOStreams.readChunk(bis, SIZE));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + SIZE + " more bytes out of " + SIZE));
	}

	@Test
	void shouldReadChunkFromByteBufferInputStream() throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(BYTES);

		@SuppressWarnings("resource")
		ByteBufferInputStream bis = ByteBufferInputStream.of(buffer);

		byte[] halfChunk = IOStreams.readChunk(bis, SIZE / 2);
		assertThat(halfChunk.length, equalTo(SIZE / 2));

		halfChunk = IOStreams.readChunk(bis, SIZE / 2);
		assertThat(halfChunk.length, equalTo(SIZE / 2));

		EOFException e = assertThrows(EOFException.class, () -> IOStreams.readChunk(bis, SIZE));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + SIZE + " more bytes out of " + SIZE));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasLessElementsOnReadFully() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12 });

		int size = 10;
		byte[] buffer = new byte[size];
		EOFException expected = IOStreams.eofExceptionBytesNeeded(size - 1, size);

		EOFException e = assertThrows(EOFException.class, () -> IOStreams.readFully(bis, buffer, 0, size));
		assertThat(e.getMessage(), equalTo(expected.getMessage()));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + (size - 1) + " more bytes out of " + size));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasLessElementsOnCopy() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12 });

		int size = 10;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		EOFException expected = IOStreams.eofExceptionBytesNeeded(size - 1, size);

		EOFException e = assertThrows(EOFException.class, () -> IOStreams.copy(bis, bos, size));
		assertThat(e.getMessage(), equalTo(expected.getMessage()));
	}

	@Test
	void shouldNotDoAnythingIfSizeIsZeroOnCopy() throws IOException {
		ByteArrayInputStream bis = mock(ByteArrayInputStream.class);
		ByteArrayOutputStream bos = mock(ByteArrayOutputStream.class);

		IOStreams.copy(bis, bos, 0);

		verifyNoInteractions(bis, bos);
	}

	@Test
	void shouldCopyTheGivenNumberOfBytes() throws IOException {
		ByteArrayInputStream bis = mock(ByteArrayInputStream.class);
		ByteArrayOutputStream bos = mock(ByteArrayOutputStream.class);

		int size = 10;

		doReturn(size).when(bis).read(any(byte[].class), eq(0), eq(size));

		IOStreams.copy(bis, bos, size);

		verify(bos).write(any(byte[].class), eq(0), eq(size));
		verify(bos).flush();
	}
}
