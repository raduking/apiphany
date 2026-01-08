package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

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
		ContentEncoding result = ContentEncoding.parse(values);

		assertThat(result, equalTo(expectedEncoding));
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
}
