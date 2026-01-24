package org.apiphany.io.gzip;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.io.IOStreams;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link GZip}.
 *
 * @author Radu Sebastian LAZIN
 */
class GZipTest {

	private static final String TEXT_FILE_PATH = "text-file.txt";
	private static final String TEXT = Strings.fromFile(TEXT_FILE_PATH);

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(GZip.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldCompressAndDecompressWithGzip() throws IOException {
		byte[] compressedText = GZip.compress(TEXT);

		String resultText = GZip.decompressToString(compressedText);

		assertThat(resultText, equalTo(TEXT));
	}

	@Test
	void shouldCompressAndDecompressWithGzipFromCompressedStream() throws IOException {
		byte[] compressedText = GZip.compress(TEXT);

		InputStream is = new ByteArrayInputStream(compressedText);
		String resultText = GZip.decompressToString(is);
		is.close();

		assertThat(resultText, equalTo(TEXT));
	}

	@Test
	void shouldCompressAndDecompressWithGzipFromStream() throws IOException {
		byte[] compressedText = GZip.compress(TEXT);

		InputStream is = GZip.inputStream(new ByteArrayInputStream(compressedText));
		String resultText = Strings.toString(is, Strings.DEFAULT_CHARSET, 10);
		is.close();

		assertThat(resultText, equalTo(TEXT));
	}

	@Test
	void shouldReturnAReadableStreamFromUncompressedStream() throws IOException {
		InputStream is = GZip.inputStream(new ByteArrayInputStream(TEXT.getBytes()));
		String resultText = Strings.toString(is, Strings.DEFAULT_CHARSET, 10);
		is.close();

		assertThat(resultText, equalTo(TEXT));
	}

	@Test
	void shouldDecompressBytesFromCompressedBytes() throws IOException {
		byte[] compressedText = GZip.compress(TEXT);

		byte[] decompressedBytes = GZip.decompressToBytes(compressedText);
		String resultText = new String(decompressedBytes, Strings.DEFAULT_CHARSET);

		assertThat(resultText, equalTo(TEXT));
	}

	@Test
	void shouldDecompressBytesFromCompressedBytesWithGenericMethod() throws IOException {
		byte[] compressedText = GZip.compress(TEXT);

		byte[] decompressedBytes = GZip.decompress(compressedText);
		String resultText = new String(decompressedBytes, Strings.DEFAULT_CHARSET);

		assertThat(resultText, equalTo(TEXT));
	}

	@Test
	void shouldDecompressInputStreamFromCompressedInputStreamWithGenericMethod() throws IOException {
		byte[] compressedText = GZip.compress(TEXT);

		InputStream decompressedInputStream = GZip.decompress(new ByteArrayInputStream(compressedText));
		String resultText = Strings.toString(decompressedInputStream, Strings.DEFAULT_CHARSET, IOStreams.DEFAULT_BUFFER_SIZE);
		decompressedInputStream.close();

		assertThat(resultText, equalTo(TEXT));
	}

	@Test
	void shouldThrowExceptionWhenDecompressingUnknownType() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> GZip.decompress("This is a string, not a byte array or input stream"));

		assertNotNull(exception);
		assertThat(exception.getMessage(),
				equalTo("Cannot decompress object of type: class java.lang.String, input must be byte[] or class java.io.InputStream"));
	}
}
