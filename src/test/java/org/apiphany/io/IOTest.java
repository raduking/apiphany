package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
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
}
