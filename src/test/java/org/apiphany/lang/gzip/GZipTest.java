package org.apiphany.lang.gzip;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link GZip}.
 *
 * @author Radu Sebastian LAZIN
 */
class GZipTest {

	@Test
	void shouldCompressAndDecompressWithGzip() throws IOException {
		String text = Strings.fromFile("/text-file.txt");
		byte[] compressedText = GZip.compress(text);

		String resultText = GZip.decompress(compressedText);

		assertThat(resultText, equalTo(text));
	}

	@Test
	void shouldCompressAndDecompressWithGzipFromCompressedStream() throws IOException {
		String text = Strings.fromFile("/text-file.txt");
		byte[] compressedText = GZip.compress(text);

		InputStream is = new ByteArrayInputStream(compressedText);
		String resultText = GZip.decompress(is);
		is.close();

		assertThat(resultText, equalTo(text));
	}

	@Test
	void shouldCompressAndDecompressWithGzipFromStream() throws IOException {
		String text = Strings.fromFile("/text-file.txt");
		byte[] compressedText = GZip.compress(text);

		InputStream is = GZip.inputStream(new ByteArrayInputStream(compressedText));
		String resultText = Strings.toString(is, Strings.DEFAULT_CHARSET, 10);
		is.close();

		assertThat(resultText, equalTo(text));
	}

	@Test
	void shouldReturnAReadableStreamFromUncompressedStream() throws IOException {
		String text = Strings.fromFile("/text-file.txt");

		InputStream is = GZip.inputStream(new ByteArrayInputStream(text.getBytes()));
		String resultText = Strings.toString(is, Strings.DEFAULT_CHARSET, 10);
		is.close();

		assertThat(resultText, equalTo(text));
	}
}
