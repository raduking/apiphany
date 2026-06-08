package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Nested;
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

	@Nested
	class ReadChunkTests {

		@Test
		void shouldSucceedReadingChunk() throws IOException {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(BYTES);

			byte[] result = IOStreams.readChunk(inputStream, SIZE);

			assertThat(result, equalTo(BYTES));
		}

		@Test
		void shouldThrowExceptionIfCannotReadChunk() {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(BYTES);

			EOFException e = assertThrows(EOFException.class, () -> IOStreams.readChunk(inputStream, SIZE + 1));
			assertThat(e.getMessage(), equalTo("Stream closed, need 1 more bytes out of " + (SIZE + 1)));
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
	}

	@Nested
	class ReadFullyTests {

		@Test
		void shouldReadFullyWithOffset() throws IOException {
			byte[] input = "abcdef".getBytes(StandardCharsets.UTF_8);
			byte[] output = new byte[10];

			IOStreams.readFully(new ByteArrayInputStream(input), output, 2, input.length);

			assertArrayEquals(new byte[] { 0, 0, 'a', 'b', 'c', 'd', 'e', 'f', 0, 0 }, output);
		}

		@Test
		void shouldReadFullyWhenInputStreamReturnsPartialReads() throws IOException {
			byte[] input = "abcdef".getBytes(StandardCharsets.UTF_8);
			byte[] output = new byte[input.length];
			try (InputStream partialReadInputStream = new InputStream() {
				private int index;

				@Override
				public int read(final byte[] b, final int off, final int len) {
					if (index >= input.length) {
						return -1;
					}
					b[off] = input[index++];
					return 1;
				}

				@Override
				public int read() {
					throw new UnsupportedOperationException();
				}
			}) {
				IOStreams.readFully(partialReadInputStream, output, 0, input.length);
			}

			assertArrayEquals(input, output);
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
	}

	@Nested
	class CopyTests {

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

		@Test
		void shouldCopyInMultipleIterationsWhenBytesToCopyExceedsDefaultBufferSize() throws IOException {
			int size = IOStreams.DEFAULT_BUFFER_SIZE + 3;
			byte[] input = new byte[size];
			Arrays.fill(input, (byte) 0x55);
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			IOStreams.copy(new ByteArrayInputStream(input), output, size);

			assertArrayEquals(input, output.toByteArray());
		}
	}

	@Nested
	class EofExceptionBytesNeededTests {

		@Test
		void shouldBuildExpectedEOFExceptionMessage() {
			EOFException exception = IOStreams.eofExceptionBytesNeeded(7, 10);

			assertThat(exception.getMessage(), equalTo("Stream closed, need 7 more bytes out of 10"));
		}
	}

	@Nested
	class ToByteArrayInputStreamTests {

		@Test
		void shouldReadAllBytesWithDefaultMaxBufferSize() throws IOException {
			byte[] input = "test".getBytes(StandardCharsets.UTF_8);

			byte[] result = IOStreams.toByteArray(new ByteArrayInputStream(input));

			assertArrayEquals(input, result);
		}

		@Test
		void shouldReadAllBytesWhenWithinConfiguredMax() throws IOException {
			byte[] input = "abc".getBytes(StandardCharsets.UTF_8);

			byte[] result = IOStreams.toByteArray(new ByteArrayInputStream(input), 3);

			assertArrayEquals(input, result);
		}

		@Test
		void shouldThrowExceptionWhenInputExceedsConfiguredMax() {
			byte[] input = "abcd".getBytes(StandardCharsets.UTF_8);

			EOFException exception = assertThrows(EOFException.class,
					() -> IOStreams.toByteArray(new ByteArrayInputStream(input), 3));

			assertThat(exception.getMessage(), equalTo("Input stream exceeds max allowed bytes: 3"));
		}

		@Test
		void shouldReturnEmptyArrayForEmptyInputStream() throws IOException {
			byte[] result = IOStreams.toByteArray(new ByteArrayInputStream(new byte[] { }));

			assertArrayEquals(new byte[] { }, result);
		}
	}

	@Nested
	class ToByteArrayWriterTests {

		@Test
		void shouldWriteBytesFromWriterToByteArray() throws IOException {
			byte[] expected = "writer-body".getBytes(StandardCharsets.UTF_8);

			byte[] result = IOStreams.toByteArray(output -> output.write(expected));

			assertArrayEquals(expected, result);
		}

		@Test
		void shouldPropagateIOExceptionFromWriter() {
			IOException exception = assertThrows(IOException.class,
					() -> IOStreams.toByteArray(output -> {
						throw new IOException("boom");
					}));

			assertThat(exception.getMessage(), equalTo("boom"));
		}
	}
}
