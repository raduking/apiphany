package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link HttpStatus}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpStatusTest {

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldReturnValueAndNameOnToString(final HttpStatus status) {
		String expected = String.format("%d %s", status.getCode(), status.name());

		String result = status.toString();

		assertThat(result, equalTo(expected));
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldBuildStatusFromCode(final HttpStatus status) {
		HttpStatus result = HttpStatus.fromCode(status.getCode());

		assertThat(result, equalTo(status));
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldBuildStatusFromMessage(final HttpStatus status) {
		HttpStatus result = HttpStatus.fromMessage(status.message());

		assertThat(result, equalTo(status));
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldBuildStatusFromName(final HttpStatus status) {
		HttpStatus result = HttpStatus.fromString(status.name());

		assertThat(result, equalTo(status));
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldHaveTheCorrectType(final HttpStatus status) {
		HttpStatus.Type expectedType;
		int code = status.getCode();
		if (code >= 100 && code < 200) {
			expectedType = HttpStatus.Type.INFORMATIONAL;
		} else if (code >= 200 && code < 300) {
			expectedType = HttpStatus.Type.SUCCESSFUL;
		} else if (code >= 300 && code < 400) {
			expectedType = HttpStatus.Type.REDIRECTION;
		} else if (code >= 400 && code < 500) {
			expectedType = HttpStatus.Type.CLIENT_ERROR;
		} else {
			expectedType = HttpStatus.Type.SERVER_ERROR;
		}

		assertThat(status.type(), equalTo(expectedType));
		assertTrue(status.isType(expectedType));
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldReturnFalseForIncorrectType(final HttpStatus status) {
		int code = status.getCode();
		HttpStatus.Type incorrectType;
		if (code >= 100 && code < 200) {
			incorrectType = HttpStatus.Type.SUCCESSFUL;
		} else if (code >= 200 && code < 300) {
			incorrectType = HttpStatus.Type.REDIRECTION;
		} else if (code >= 300 && code < 400) {
			incorrectType = HttpStatus.Type.CLIENT_ERROR;
		} else if (code >= 400 && code < 500) {
			incorrectType = HttpStatus.Type.SERVER_ERROR;
		} else {
			incorrectType = HttpStatus.Type.INFORMATIONAL;
		}

		assertThat(status.type(), not(equalTo(incorrectType)));
		assertFalse(status.isType(incorrectType));
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldReturnTrueForTheCorrectType(final HttpStatus status) {
		boolean correctType = switch (status.type()) {
			case INFORMATIONAL -> status.is1xxInformational();
			case SUCCESSFUL -> status.is2xxSuccessful();
			case REDIRECTION -> status.is3xxRedirection();
			case CLIENT_ERROR -> status.is4xxClientError();
			case SERVER_ERROR -> status.is5xxServerError();
		};

		assertThat(correctType, equalTo(true));
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldReturnSuccessfulOnlyFor2xxSuccessful(final HttpStatus status) {
		switch (status.type()) {
			case SUCCESSFUL -> assertThat(status.isSuccess(), equalTo(true));
			default -> assertThat(status.isSuccess(), equalTo(false));
		}
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldReturnErrorFor4xxAnd5xx(final HttpStatus status) {
		switch (status.type()) {
			case CLIENT_ERROR, SERVER_ERROR -> assertThat(status.isError(), equalTo(true));
			default -> assertThat(status.isError(), equalTo(false));
		}
	}

	@ParameterizedTest
	@EnumSource(HttpStatus.class)
	void shouldBuildTypeFromCode(final HttpStatus status) {
		HttpStatus.Type result = HttpStatus.Type.fromCode(status.getCode());

		assertThat(result, equalTo(status.type()));
	}
}
