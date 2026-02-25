package org.apiphany.http;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
}
