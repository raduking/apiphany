package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link HttpException}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpExceptionTest {

	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String CAUSE_ERROR_MESSAGE = "causeErrorMessage";

	@Test
	void shouldBuildExceptionWithIntStatusCodeMessageAndThrowable() {
		RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

		int statusCode = HttpStatus.BAD_REQUEST.getCode();
		HttpException exception = new HttpException(statusCode, ERROR_MESSAGE, cause);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.BAD_REQUEST, ERROR_MESSAGE)));
		assertThat(exception.getCause().getMessage(), equalTo(CAUSE_ERROR_MESSAGE));
	}

	@Test
	void shouldBuildExceptionWithIntStatusCodeAndMessage() {
		int statusCode = HttpStatus.FORBIDDEN.getCode();
		HttpException exception = new HttpException(statusCode, ERROR_MESSAGE);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.FORBIDDEN, ERROR_MESSAGE)));
	}

	@Test
	void shouldBuildExceptionWithStatusCodeAndMessage() {
		int statusCode = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.getCode();
		HttpException exception = new HttpException(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, ERROR_MESSAGE);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, ERROR_MESSAGE)));
	}

	@Test
	void shouldBuildExceptionWithIntStatusCodeAndNullMessage() {
		int statusCode = HttpStatus.TOO_MANY_REQUESTS.getCode();
		HttpException exception = new HttpException(statusCode, null);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.TOO_MANY_REQUESTS)));
	}

	@Test
	void shouldNotThrowExceptionIfSupplierDoesNotThrow() {
		String result = HttpException.ifThrows(() -> ERROR_MESSAGE);

		assertThat(result, equalTo(ERROR_MESSAGE));
	}

	@Test
	void shouldThrowExceptionIfSupplierThrows() {
		RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

		HttpException exception = assertThrows(HttpException.class, () -> HttpException.ifThrows(() -> {
			throw cause;
		}));

		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.INTERNAL_SERVER_ERROR, CAUSE_ERROR_MESSAGE)));
	}

	@Test
	void shouldNotWrapHttpExceptionThrownBySupplier() {
		HttpException cause = new HttpException(HttpStatus.BAD_REQUEST, CAUSE_ERROR_MESSAGE);

		HttpException exception = assertThrows(HttpException.class, () -> HttpException.ifThrows(() -> {
			throw cause;
		}));

		assertThat(exception, equalTo(cause));
	}
}
