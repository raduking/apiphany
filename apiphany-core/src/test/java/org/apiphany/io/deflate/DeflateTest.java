package org.apiphany.io.deflate;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Deflate}.
 *
 * @author Radu Sebastian LAZIN
 */
class DeflateTest {

	private static final String TEXT = "This is a text to compress with DEFLATE";

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Deflate.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldCompressAndDecompressWithDeflate() throws Exception {
		byte[] body = TEXT.getBytes();

		byte[] compressedBody = Deflate.compress(body);

		byte[] resultBody = Deflate.decompressToBytes(compressedBody);

		assertThat(resultBody, equalTo(body));
	}

	@Test
	void shouldCompressAndDecompressWithDeflateFromCompressedStream() throws Exception {
		String body = TEXT;

		byte[] compressedBody = Deflate.compress(body.getBytes());
		InputStream resultBody = Deflate.decompress(new ByteArrayInputStream(compressedBody));

		String resultString = new String(resultBody.readAllBytes());

		assertThat(resultString, equalTo(body));
	}

	@Test
	void shouldCompressAndDecompressWithDeflateFromCompressedStreamWithOffset() throws Exception {
		String body = TEXT;

		byte[] compressedBody = Deflate.compress(body.getBytes());
		byte[] compressedBodyWithOffset = new byte[compressedBody.length + 10];
		System.arraycopy(compressedBody, 0, compressedBodyWithOffset, 10, compressedBody.length);

		InputStream resultBody = Deflate.decompress(
				new ByteArrayInputStream(compressedBodyWithOffset, 10, compressedBody.length));

		String resultString = new String(resultBody.readAllBytes());

		assertThat(resultString, equalTo(body));
	}

	@Test
	void shouldThrowExceptionOnDecompressingUnsupportedType() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Deflate.decompress("unsupported type"));

		assertThat(e.getMessage(),
				equalTo("Cannot decompress object of type: class java.lang.String, input must be byte[] or class java.io.InputStream"));
	}
}
