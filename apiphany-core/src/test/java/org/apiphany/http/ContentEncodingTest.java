package org.apiphany.http;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apiphany.io.deflate.Deflate;
import org.apiphany.io.gzip.GZip;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link ContentEncoding}.
 *
 * @author Radu Sebastian LAZIN
 */
class ContentEncodingTest {

	private static final String HELLO_WORLD = "Hello World!";
	private static final String HELLO_MULTI_LAYER = "Hello multi-layer compression!";
	private static final String HELLO_COMPRESSED_WORLD = "Hello compressed world!";
	private static final String HELLO_COMPRESSED_STREAM = "Hello compressed stream!";
	private static final String HELLO_DECODING_PIPELINE = "Hello decoding pipeline!";
	private static final String HELLO_LAYERED_COMPRESSION = "Hello layered compression!";

	@ParameterizedTest
	@EnumSource(ContentEncoding.class)
	void shouldBuildWithFromStringWithValidValueEvenIfUppercase(final ContentEncoding contentEncoding) {
		String stringValue = contentEncoding.value().toUpperCase();
		ContentEncoding result = ContentEncoding.fromString(stringValue);
		assertThat(result, equalTo(contentEncoding));
	}

	@ParameterizedTest
	@EnumSource(ContentEncoding.class)
	void shouldMatchValidValueEvenIfUppercase(final ContentEncoding contentEncoding) {
		String stringValue = contentEncoding.value().toUpperCase();
		boolean result = contentEncoding.matches(stringValue);
		assertTrue(result);
	}

	@ParameterizedTest
	@EnumSource(ContentEncoding.class)
	void shouldMatchValidValueEvenIfMixedCase(final ContentEncoding contentEncoding) {
		String stringValue = switch (contentEncoding) {
			case GZIP -> "gZiP";
			case DEFLATE -> "dEfLaTe";
			case BR -> "bR";
			default -> contentEncoding.value();
		};
		boolean result = contentEncoding.matches(stringValue);
		assertTrue(result);
	}

	@ParameterizedTest
	@MethodSource("provideValuesForParsingFromList")
	void shouldParseValueFromAList(final List<String> values, final ContentEncoding expectedEncoding) {
		ContentEncoding result = ContentEncoding.parseFirst(values);

		assertThat(result, equalTo(expectedEncoding));
	}

	@Test
	void shouldReturnNullWhenParsingFromListWithNull() {
		ContentEncoding result = ContentEncoding.parseFirst(null);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenParsingFromListWithEmptyList() {
		ContentEncoding result = ContentEncoding.parseFirst(List.of());

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenParsingFromListWithUnknownValues() {
		ContentEncoding result = ContentEncoding.parseFirst(List.of("unknown1", "unknown2"));

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenParsingFromListWithUnknownAndKnownValues() {
		ContentEncoding result = ContentEncoding.parseFirst(List.of("unknown", "gzip"));

		assertThat(result, equalTo(ContentEncoding.GZIP));
	}

	@Test
	void shouldReturnFirstWhenParsingFromListWithKnownValues() {
		ContentEncoding result = ContentEncoding.parseFirst(List.of("gzip", "deflate"));

		assertThat(result, equalTo(ContentEncoding.GZIP));
	}

	@Test
	void shouldReturnFirstKnownValueWhenParsingFromListWithKnownAndUnknownValues() {
		ContentEncoding result = ContentEncoding.parseFirst(List.of("unknown", "deflate", "gzip"));

		assertThat(result, equalTo(ContentEncoding.DEFLATE));
	}

	@Test
	void shouldReturnFirstKnownValueWhenParsingFromListWithKnownAndUnknownValuesInDifferentOrder() {
		ContentEncoding result = ContentEncoding.parseFirst(List.of("br", "unknown", "gzip"));

		assertThat(result, equalTo(ContentEncoding.BR));
	}

	@Test
	void shouldReturnEmptyListWhenParsingFromListWithNull() {
		List<ContentEncoding> result = ContentEncoding.parseAll(null);

		assertThat(result, equalTo(List.of()));
	}

	@Test
	void shouldReturnEmptyListWhenParsingFromListWithEmptyList() {
		List<ContentEncoding> result = ContentEncoding.parseAll(List.of());

		assertThat(result, equalTo(List.of()));
	}

	@Test
	void shouldReturnEmptyListWhenParsingFromListWithUnknownValues() {
		List<ContentEncoding> result = ContentEncoding.parseAll(List.of("unknown1", "", "unknown2"));

		assertThat(result, equalTo(List.of()));
	}

	@Test
	void shouldReturnListWithKnownValuesWhenParsingFromListWithKnownAndUnknownValues() {
		List<ContentEncoding> result = ContentEncoding.parseAll(List.of("unknown", "gzip", "deflate"));

		assertThat(result, equalTo(List.of(ContentEncoding.GZIP, ContentEncoding.DEFLATE)));
	}

	private static Object[][] provideValuesForParsingFromList() {
		return new Object[][] {
				{ List.of("gzip"), ContentEncoding.GZIP },
				{ List.of("deflate"), ContentEncoding.DEFLATE },
				{ List.of("br"), ContentEncoding.BR },
				{ List.of("gzip", "deflate"), ContentEncoding.GZIP },
				{ List.of("deflate", "br"), ContentEncoding.DEFLATE },
				{ List.of("br", "gzip"), ContentEncoding.BR },
				{ List.of("unknown", "gzip"), ContentEncoding.GZIP },
				{ List.of("unknown1", "unknown2"), null },
				{ List.of(), null },
				{ null, null }
		};
	}

	@Test
	void shouldThrowExceptionOnCallingValueConstructor() {
		UnsupportedOperationException e = assertDefaultConstructorThrows(ContentEncoding.Value.class);
		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldReturnOriginalBodyWhenEncodingsAreEmpty() {
		byte[] body = HELLO_WORLD.getBytes(StandardCharsets.UTF_8);

		byte[] decoded = ContentEncoding.decodeBody(body, List.of());

		assertThat(decoded, equalTo(body));
	}

	@Test
	void shouldReturnOriginalBodyWhenEncodingsIsNull() {
		byte[] body = HELLO_WORLD.getBytes(StandardCharsets.UTF_8);

		byte[] decoded = ContentEncoding.decodeBody(body, null);

		assertThat(decoded, equalTo(body));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDecodeBodyWithMultipleEncodingsInReverseOrder() throws Exception {
		String original = HELLO_MULTI_LAYER;
		byte[] deflated = Deflate.compress(original.getBytes(StandardCharsets.UTF_8));
		byte[] gzipThenDeflate = GZip.compress(deflated);
		InputStream stream = new ByteArrayInputStream(gzipThenDeflate);

		InputStream decoded = ContentEncoding.decodeBody(stream, List.of(ContentEncoding.DEFLATE, ContentEncoding.GZIP));
		byte[] result = decoded.readAllBytes();

		assertThat(new String(result, StandardCharsets.UTF_8), equalTo(original));
	}

	@Test
	void shouldDecodeBodyWithMultipleEncodingsInReverseOrderFromStream() throws Exception {
		String original = HELLO_MULTI_LAYER;
		byte[] deflated = Deflate.compress(original.getBytes(StandardCharsets.UTF_8));
		byte[] gzipThenDeflate = GZip.compress(deflated);

		byte[] decoded = ContentEncoding.decodeBody(gzipThenDeflate, List.of(ContentEncoding.DEFLATE, ContentEncoding.GZIP));

		assertThat(new String(decoded, StandardCharsets.UTF_8), equalTo(original));
	}

	@Test
	void shouldReturnTheSameBodyForUnsupportedTypes() {
		String decoded = ContentEncoding.decodeBody(HELLO_WORLD, List.of(ContentEncoding.BR));

		assertThat(decoded, equalTo(HELLO_WORLD));
	}

	@Nested
	class GZipTests {

		@Test
		void shouldHaveCorrectValue() {
			assertThat(ContentEncoding.GZIP.value(), equalTo("gzip"));
		}

		@Test
		void shouldDecodeGzipEncodedByteArray() throws Exception {
			String original = HELLO_COMPRESSED_WORLD;
			byte[] compressed = gzip(original.getBytes(StandardCharsets.UTF_8));

			byte[] decoded = ContentEncoding.GZIP.decode(compressed);

			assertThat(decoded, notNullValue());
			assertThat(new String(decoded, StandardCharsets.UTF_8), equalTo(original));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldDecodeGzipEncodedInputStream() throws Exception {
			String original = HELLO_COMPRESSED_STREAM;
			byte[] compressed = gzip(original.getBytes(StandardCharsets.UTF_8));
			InputStream stream = new ByteArrayInputStream(compressed);

			InputStream decoded = ContentEncoding.GZIP.decode(stream);
			byte[] result = decoded.readAllBytes();

			assertThat(result, notNullValue());
			assertThat(new String(result, StandardCharsets.UTF_8), equalTo(original));
		}

		@Test
		void shouldDecodeBodyUsingEncodingList() throws Exception {
			String original = HELLO_DECODING_PIPELINE;
			byte[] compressed = gzip(original.getBytes(StandardCharsets.UTF_8));

			byte[] decoded = ContentEncoding.decodeBody(compressed, List.of(ContentEncoding.GZIP));

			assertThat(new String(decoded, StandardCharsets.UTF_8), equalTo(original));
		}

		@Test
		void shouldDecodeBodyInReverseEncodingOrder() throws Exception {
			String original = HELLO_LAYERED_COMPRESSION;
			byte[] gzip1 = gzip(original.getBytes(StandardCharsets.UTF_8));
			byte[] gzip2 = gzip(gzip1);

			byte[] decoded = ContentEncoding.decodeBody(gzip2, List.of(ContentEncoding.GZIP, ContentEncoding.GZIP));

			assertThat(new String(decoded, StandardCharsets.UTF_8), equalTo(original));
		}

		private static byte[] gzip(final byte[] data) throws Exception {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
				gzip.write(data);
			}
			return baos.toByteArray();
		}
	}

	@Nested
	class DeflateTests {

		@Test
		void shouldHaveCorrectValue() {
			assertThat(ContentEncoding.DEFLATE.value(), equalTo("deflate"));
		}

		@Test
		void shouldDecodeDeflateEncodedByteArray() throws Exception {
			String original = HELLO_COMPRESSED_WORLD;
			byte[] compressed = Deflate.compress(original.getBytes(StandardCharsets.UTF_8));

			byte[] decoded = ContentEncoding.DEFLATE.decode(compressed);

			assertThat(decoded, notNullValue());
			assertThat(new String(decoded, StandardCharsets.UTF_8), equalTo(original));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldDecodeDeflateEncodedInputStream() throws Exception {
			String original = HELLO_COMPRESSED_STREAM;
			byte[] compressed = Deflate.compress(original.getBytes(StandardCharsets.UTF_8));
			InputStream stream = new ByteArrayInputStream(compressed);

			InputStream decoded = ContentEncoding.DEFLATE.decode(stream);
			byte[] result = decoded.readAllBytes();

			assertThat(result, notNullValue());
			assertThat(new String(result, StandardCharsets.UTF_8), equalTo(original));
		}
	}
}
