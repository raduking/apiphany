package org.apiphany.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.function.ThrowingRunnable;

/**
 * Test class for {@link ErrorObservingInputStream}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class ErrorObservingInputStreamTest {

	private static final int TEST_INT = 42;
	private static final byte[] TEST_BYTE_ARRAY = new byte[] { 1, 2, 3 };
	private static final int OFFSET = 2;
	private static final int LENGTH = 5;

	private static final IOException TEST_IO_EXCEPTION = new IOException("Test IOException");

	@Mock
	private InputStream mockInputStream;

	private List<Exception> observedExceptions = new ArrayList<>();

	private static Executable executable(final ThrowingRunnable runnable) {
		return runnable::run;
	}

	@SuppressWarnings("resource")
	@Test
	void shoudObserveReadWithIOException() throws IOException {
		doReturn(TEST_INT).doThrow(TEST_IO_EXCEPTION).when(mockInputStream).read();

		var errorObservingInputStream = ErrorObservingInputStream.observe(mockInputStream, observedExceptions::add);

		int readValue = errorObservingInputStream.read();
		IOException thrownException = assertThrows(IOException.class, executable(errorObservingInputStream::read));

		assertThat(readValue, equalTo(TEST_INT));
		assertSame(TEST_IO_EXCEPTION, thrownException);
		assertThat(observedExceptions, hasSize(1));
		assertSame(TEST_IO_EXCEPTION, observedExceptions.get(0));
	}

	@Test
	void shoudObserveCloseWithIOException() throws IOException {
		doNothing().doThrow(TEST_IO_EXCEPTION).when(mockInputStream).close();

		var errorObservingInputStream = ErrorObservingInputStream.observe(mockInputStream, observedExceptions::add);

		errorObservingInputStream.close();
		IOException thrownException = assertThrows(IOException.class, executable(errorObservingInputStream::close));

		assertSame(TEST_IO_EXCEPTION, thrownException);
		assertThat(observedExceptions, hasSize(1));
		assertSame(TEST_IO_EXCEPTION, observedExceptions.get(0));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldObserveReadByteArrayWithIOException() throws IOException {
		byte[] buffer = new byte[10];
		doReturn(TEST_INT).doThrow(TEST_IO_EXCEPTION).when(mockInputStream).read(buffer);

		var errorObservingInputStream = ErrorObservingInputStream.observe(mockInputStream, observedExceptions::add);

		int readValue = errorObservingInputStream.read(buffer);
		IOException thrownException = assertThrows(IOException.class, executable(() -> errorObservingInputStream.read(buffer)));

		assertThat(readValue, equalTo(TEST_INT));
		assertSame(TEST_IO_EXCEPTION, thrownException);
		assertThat(observedExceptions, hasSize(1));
		assertSame(TEST_IO_EXCEPTION, observedExceptions.get(0));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldObserveReadByteArrayOffsetLengthWithIOException() throws IOException {
		byte[] buffer = new byte[10];
		doReturn(TEST_INT).doThrow(TEST_IO_EXCEPTION).when(mockInputStream).read(buffer, 2, 5);

		var errorObservingInputStream = ErrorObservingInputStream.observe(mockInputStream, observedExceptions::add);

		int readValue = errorObservingInputStream.read(buffer, 2, 5);
		IOException thrownException = assertThrows(IOException.class, executable(() -> errorObservingInputStream.read(buffer, 2, 5)));

		assertThat(readValue, equalTo(TEST_INT));
		assertSame(TEST_IO_EXCEPTION, thrownException);
		assertThat(observedExceptions, hasSize(1));
		assertSame(TEST_IO_EXCEPTION, observedExceptions.get(0));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldObserveReadNBytesWithIOException() throws IOException {
		doReturn(TEST_BYTE_ARRAY).doThrow(TEST_IO_EXCEPTION).when(mockInputStream).readNBytes(LENGTH);

		var errorObservingInputStream = ErrorObservingInputStream.observe(mockInputStream, observedExceptions::add);

		byte[] readValue = errorObservingInputStream.readNBytes(LENGTH);
		IOException thrownException = assertThrows(IOException.class, executable(() -> errorObservingInputStream.readNBytes(LENGTH)));

		assertThat(readValue, equalTo(TEST_BYTE_ARRAY));
		assertSame(TEST_IO_EXCEPTION, thrownException);
		assertThat(observedExceptions, hasSize(1));
		assertSame(TEST_IO_EXCEPTION, observedExceptions.get(0));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldObserveReadNBytesByteArrayOffsetLengthWithIOException() throws IOException {
		byte[] buffer = new byte[10];
		doReturn(TEST_INT).doThrow(TEST_IO_EXCEPTION).when(mockInputStream).readNBytes(buffer, OFFSET, LENGTH);

		var errorObservingInputStream = ErrorObservingInputStream.observe(mockInputStream, observedExceptions::add);

		int readValue = errorObservingInputStream.readNBytes(buffer, OFFSET, LENGTH);
		IOException thrownException = assertThrows(IOException.class, executable(() -> errorObservingInputStream.readNBytes(buffer, OFFSET, LENGTH)));

		assertThat(readValue, equalTo(TEST_INT));
		assertSame(TEST_IO_EXCEPTION, thrownException);
		assertThat(observedExceptions, hasSize(1));
		assertSame(TEST_IO_EXCEPTION, observedExceptions.get(0));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldObserveReadAllBytesWithIOException() throws IOException {
		doReturn(TEST_BYTE_ARRAY).doThrow(TEST_IO_EXCEPTION).when(mockInputStream).readAllBytes();

		var errorObservingInputStream = ErrorObservingInputStream.observe(mockInputStream, observedExceptions::add);

		byte[] readValue = errorObservingInputStream.readAllBytes();
		IOException thrownException = assertThrows(IOException.class, executable(errorObservingInputStream::readAllBytes));

		assertThat(readValue, equalTo(TEST_BYTE_ARRAY));
		assertSame(TEST_IO_EXCEPTION, thrownException);
		assertThat(observedExceptions, hasSize(1));
		assertSame(TEST_IO_EXCEPTION, observedExceptions.get(0));
	}
}
