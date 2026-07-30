package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apiphany.Status;
import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link HttpException}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpExceptionTest {

	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String CAUSE_ERROR_MESSAGE = "causeErrorMessage";
	private static final String RESPONSE_BODY = "responseBody";

	@Test
	void shouldBuildExceptionWithIntStatusCodeMessageAndThrowable() {
		RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

		int statusCode = HttpStatus.BAD_REQUEST.getCode();
		HttpException exception = new HttpException(statusCode, ERROR_MESSAGE, cause);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.BAD_REQUEST, ERROR_MESSAGE)));
		assertThat(exception.getCause().getMessage(), equalTo(CAUSE_ERROR_MESSAGE));
		assertThat(exception.getResponseBody(), nullValue());
	}

	@Test
	void shouldBuildExceptionWithIntStatusMessageAndResponseBody() {
		int statusCode = HttpStatus.BAD_REQUEST.getCode();
		HttpException exception = new HttpException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE, RESPONSE_BODY);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.BAD_REQUEST, ERROR_MESSAGE)));
		assertThat(exception.getCause(), nullValue());
		assertThat(exception.getResponseBody(), equalTo(RESPONSE_BODY));
	}

	@Test
	void shouldBuildExceptionWithIntStatusMessageAndCause() {
		RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

		int statusCode = HttpStatus.BAD_REQUEST.getCode();
		HttpException exception = new HttpException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE, cause);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.BAD_REQUEST, ERROR_MESSAGE)));
		assertThat(exception.getCause().getMessage(), equalTo(CAUSE_ERROR_MESSAGE));
		assertThat(exception.getResponseBody(), nullValue());
	}

	@Test
	void shouldBuildExceptionWithIntStatusCodeAndMessage() {
		int statusCode = HttpStatus.FORBIDDEN.getCode();
		HttpException exception = new HttpException(statusCode, ERROR_MESSAGE);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.FORBIDDEN, ERROR_MESSAGE)));
		assertThat(exception.getResponseBody(), nullValue());
		assertThat(exception.getCause(), nullValue());
	}

	@Test
	void shouldBuildExceptionWithStatusCodeAndMessage() {
		int statusCode = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.getCode();
		HttpException exception = new HttpException(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, ERROR_MESSAGE);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, ERROR_MESSAGE)));
		assertThat(exception.getResponseBody(), nullValue());
		assertThat(exception.getCause(), nullValue());
	}

	@Test
	void shouldBuildExceptionWithIntStatusCodeAndNullMessage() {
		int statusCode = HttpStatus.TOO_MANY_REQUESTS.getCode();
		HttpException exception = new HttpException(statusCode, null);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.TOO_MANY_REQUESTS)));
		assertThat(exception.getResponseBody(), nullValue());
		assertThat(exception.getCause(), nullValue());
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

		assertThat(exception.getMessage(), equalTo(HttpException.message(null, CAUSE_ERROR_MESSAGE)));
		assertThat(exception.getStatusCode(), equalTo(Status.UNKNOWN));
	}

	@Test
	void shouldNotWrapHttpExceptionThrownBySupplier() {
		HttpException cause = new HttpException(HttpStatus.BAD_REQUEST, CAUSE_ERROR_MESSAGE);

		HttpException exception = assertThrows(HttpException.class, () -> HttpException.ifThrows(() -> {
			throw cause;
		}));

		assertThat(exception, equalTo(cause));
		assertThat(exception.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST.getCode()));
	}

	@Test
	void shouldDetectCircularRedirectAsRedirectLoopFailure() {
		RuntimeException exception = new RuntimeException("Circular redirect to '/loop'");

		assertThat(HttpMessages.isRedirectLoopFailure(exception), equalTo(true));
	}

	@Test
	void shouldDetectTooManyRedirectsAsRedirectLoopFailure() {
		RuntimeException exception = new RuntimeException("Too many redirects");

		assertThat(HttpMessages.isRedirectLoopFailure(exception), equalTo(true));
	}

	@Test
	void shouldDetectRedirectLoopFailureInCauseChain() {
		RuntimeException rootCause = new RuntimeException("Circular redirect to '/loop'");
		RuntimeException exception = new RuntimeException("Transport error", rootCause);

		assertThat(HttpMessages.isRedirectLoopFailure(exception), equalTo(true));
	}

	@Test
	void shouldNotDetectRedirectLoopFailureForOtherErrors() {
		RuntimeException exception = new RuntimeException("Connection reset");

		assertThat(HttpMessages.isRedirectLoopFailure(exception), equalTo(false));
	}

	@Test
	void shouldNotLoopForeverOnCircularCauseChainWithoutRedirectMessage() {
		RuntimeException exception = new RuntimeException("Transport error");
		RuntimeException cause = new RuntimeException("Network error");
		exception.initCause(cause);
		cause.initCause(exception);

		assertThat(HttpMessages.isRedirectLoopFailure(exception), equalTo(false));
	}

	@Test
	void shouldDetectRedirectLoopOnCircularCauseChain() {
		RuntimeException exception = new RuntimeException("Transport error");
		RuntimeException cause = new RuntimeException("Circular redirect to '/loop'");
		exception.initCause(cause);
		cause.initCause(exception);

		assertThat(HttpMessages.isRedirectLoopFailure(exception), equalTo(true));
	}

	@Test
	void shouldThrowWhenRedirectLoopFailurePredicateIsNull() {
		RuntimeException e = new RuntimeException("x");
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> HttpMessages.isRedirectLoopFailure(e, null));

		assertThat(exception.getMessage(), equalTo("predicate cannot be null"));
	}

	@Test
	void shouldUseCustomRedirectLoopFailurePredicate() {
		RuntimeException exception = new RuntimeException("ignored");
		RuntimeException cause = new RuntimeException("ignored");
		exception.initCause(cause);
		cause.initCause(exception);

		Predicate<Throwable> predicate = t -> t == cause;
		assertThat(HttpMessages.isRedirectLoopFailure(exception, predicate), equalTo(true));
	}

	@Test
	void shouldBuildExceptionWithStatusCodeMessageThrowableAndResponseBody() {
		RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

		int statusCode = HttpStatus.UNAUTHORIZED.getCode();
		HttpException exception = new HttpException(HttpStatus.UNAUTHORIZED, ERROR_MESSAGE, RESPONSE_BODY, cause);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getStatus(), equalTo(HttpStatus.UNAUTHORIZED));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.UNAUTHORIZED, ERROR_MESSAGE)));
		assertThat(exception.getCause().getMessage(), equalTo(CAUSE_ERROR_MESSAGE));
		assertThat(exception.getResponseBody(), equalTo(RESPONSE_BODY));
	}

	@Test
	void shouldBuildExceptionWithIntStatusCodeMessageThrowableAndNullResponseBody() {
		RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

		int statusCode = HttpStatus.BAD_REQUEST.getCode();
		HttpException exception = new HttpException(statusCode, ERROR_MESSAGE, cause);

		assertThat(exception.getStatusCode(), equalTo(statusCode));
		assertThat(exception.getStatus(), equalTo(HttpStatus.BAD_REQUEST));
		assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.BAD_REQUEST, ERROR_MESSAGE)));
		assertThat(exception.getCause().getMessage(), equalTo(CAUSE_ERROR_MESSAGE));
		assertThat(exception.getResponseBody(), equalTo(null));
	}

	@Test
	void shouldCreateRedirectLoopException() {
		HttpException exception = HttpException.redirectLoop();

		assertThat(exception.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.getCode()));
		assertThat(exception.getMessage(), containsString("Redirect loop detected"));
		assertThat(exception.getCause(), nullValue());
	}

	@Test
	void shouldCreateRedirectLoopExceptionWithCause() {
		RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

		HttpException exception = HttpException.redirectLoop(cause);

		assertThat(exception.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.getCode()));
		assertThat(exception.getMessage(), containsString("Redirect loop detected"));
		assertThat(exception.getCause(), equalTo(cause));
	}

	@Nested
	class BuilderTest {

		@Test
		void shouldBuildExceptionWithStatusMessageThrowableAndResponseBody() {
			RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

			int statusCode = HttpStatus.UNAUTHORIZED.getCode();
			HttpException exception = HttpException.builder()
					.status(HttpStatus.UNAUTHORIZED)
					.message(ERROR_MESSAGE)
					.responseBody(RESPONSE_BODY)
					.cause(cause)
					.build();

			assertThat(exception.getStatusCode(), equalTo(statusCode));
			assertThat(exception.getStatus(), equalTo(HttpStatus.UNAUTHORIZED));
			assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.UNAUTHORIZED, ERROR_MESSAGE)));
			assertThat(exception.getCause().getMessage(), equalTo(CAUSE_ERROR_MESSAGE));
			assertThat(exception.getResponseBody(), equalTo(RESPONSE_BODY));
		}

		@Test
		void shouldBuildExceptionWithStatusCodeMessageThrowableAndResponseBody() {
			RuntimeException cause = new RuntimeException(CAUSE_ERROR_MESSAGE);

			int statusCode = HttpStatus.UNAUTHORIZED.getCode();
			HttpException exception = HttpException.builder()
					.status(statusCode)
					.message(ERROR_MESSAGE)
					.responseBody(RESPONSE_BODY)
					.cause(cause)
					.build();

			assertThat(exception.getStatusCode(), equalTo(statusCode));
			assertThat(exception.getStatus(), equalTo(HttpStatus.UNAUTHORIZED));
			assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.UNAUTHORIZED, ERROR_MESSAGE)));
			assertThat(exception.getCause().getMessage(), equalTo(CAUSE_ERROR_MESSAGE));
			assertThat(exception.getResponseBody(), equalTo(RESPONSE_BODY));
			assertThat(exception.getBody(), equalTo(RESPONSE_BODY));
		}

		@Test
		void shouldBuildExceptionWithStatusAndResponseBodyAndUseResponseBodyAsMessage() {
			int statusCode = HttpStatus.UNAUTHORIZED.getCode();
			HttpException exception = HttpException.builder()
					.status(HttpStatus.UNAUTHORIZED)
					.responseBody(RESPONSE_BODY)
					.build();

			assertThat(exception.getStatusCode(), equalTo(statusCode));
			assertThat(exception.getStatus(), equalTo(HttpStatus.UNAUTHORIZED));
			assertThat(exception.getMessage(), equalTo(HttpException.message(HttpStatus.UNAUTHORIZED, RESPONSE_BODY)));
			assertThat(exception.getCause(), nullValue());
			assertThat(exception.getResponseBody(), equalTo(RESPONSE_BODY));
			assertThat(exception.getBody(), equalTo(RESPONSE_BODY));
		}

		@Test
		void shouldBuildExceptionWithResponseHeaders() {
			Map<String, List<String>> headers = Map.of("X-Test", List.of("test-value"));

			HttpException exception = HttpException.builder()
					.status(HttpStatus.BAD_REQUEST)
					.responseBody(RESPONSE_BODY)
					.responseHeaders(headers)
					.build();

			assertThat(exception.getResponseHeaders(), hasKey("X-Test"));
			assertThat(exception.getResponseHeaders(), hasEntry("X-Test", List.of("test-value")));
		}
	}

	@Nested
	class MessageTest {

		@Test
		void shouldHaveCorrectMessages() {
			assertThat(HttpException.Message.REDIRECT_LOOP, equalTo("Redirect loop detected"));
			assertThat(HttpException.Message.RESPONSE_TOO_LARGE, equalTo("Response body exceeds configured max size"));
		}

		@Test
		void shouldThrowExceptionOnInstantiatingValue() {
			UnsupportedOperationException exception = Assertions.assertDefaultConstructorThrows(HttpException.Message.class);

			assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}
	}
}
