package org.apiphany.io;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link ContentType}.
 *
 * @author Radu Sebastian LAZIN
 */
class ContentTypeTest {

	@Test
	void shouldThrowExceptionOnCallingValueConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(ContentType.Value.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@ParameterizedTest
	@EnumSource(ContentType.class)
	void shouldCheckInValues(final ContentType contentType) {
		boolean result = contentType.in(contentType.value());

		assertTrue(result);
	}

	@ParameterizedTest
	@EnumSource(ContentType.class)
	void shouldReturnThisOnContentType(final ContentType contentType) {
		boolean result = contentType == contentType.contentType();

		assertTrue(result);
	}

	@ParameterizedTest
	@EnumSource(ContentType.class)
	void shouldMatchItsOwnValue(final ContentType contentType) {
		assertTrue(contentType.matches(contentType.value()));
	}

	@Test
	void shouldMatchMediaTypeIgnoringParametersAndWhitespace() {
		assertTrue(ContentType.APPLICATION_JSON.matches("application/json; charset=UTF-8"));
		assertTrue(ContentType.APPLICATION_JSON.matches("application/json;charset=UTF-8"));
		assertTrue(ContentType.APPLICATION_JSON.matches("  application/json\t; charset=UTF-8  "));
		assertTrue(ContentType.MULTIPART_FORM_DATA.matches("multipart/form-data; boundary=abc"));
	}

	@Test
	void shouldMatchMediaTypeIgnoringCase() {
		assertTrue(ContentType.APPLICATION_JSON.matches("Application/JSON"));
		assertTrue(ContentType.APPLICATION_JSON.matches("APPLICATION/JSON; charset=UTF-8"));
	}

	@Test
	void shouldNotMatchDifferentMediaType() {
		assertFalse(ContentType.APPLICATION_JSON.matches("text/plain"));
		assertFalse(ContentType.APPLICATION_JSON.matches("application/xml; charset=UTF-8"));
		assertFalse(ContentType.APPLICATION_JSON.matches("not-application/json"));
	}

	@Test
	void shouldNotMatchNullOrBlankValue() {
		assertFalse(ContentType.APPLICATION_JSON.matches(null));
		assertFalse(ContentType.APPLICATION_JSON.matches(""));
		assertFalse(ContentType.APPLICATION_JSON.matches("   "));
	}

	@Test
	void shouldMatchAnyValueMatchingThisContentType() {
		assertTrue(ContentType.APPLICATION_JSON.matchesAny(List.of("application/json")));
		assertTrue(ContentType.APPLICATION_JSON.matchesAny(List.of("application/json; charset=UTF-8")));
		assertTrue(ContentType.APPLICATION_JSON.matchesAny(List.of("Application/JSON")));
		assertTrue(ContentType.APPLICATION_JSON.matchesAny(List.of("text/plain", "application/json")));
	}

	@Test
	void shouldNotMatchAnyWhenNoValueMatchesThisContentType() {
		assertFalse(ContentType.APPLICATION_JSON.matchesAny(List.of("text/plain", "application/xml")));
		assertFalse(ContentType.APPLICATION_JSON.matchesAny(List.of("not-application/json")));
	}

	@Test
	void shouldNotMatchAnyWhenValuesAreNullOrEmpty() {
		assertFalse(ContentType.APPLICATION_JSON.matchesAny(null));
		assertFalse(ContentType.APPLICATION_JSON.matchesAny(List.of()));
	}

	@Test
	void shouldNotMatchAnyNullElements() {
		assertFalse(ContentType.APPLICATION_JSON.matchesAny(Arrays.asList(null, null)));
		assertTrue(ContentType.APPLICATION_JSON.matchesAny(Arrays.asList(null, "application/json")));
	}
}
