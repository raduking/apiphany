package org.apiphany.http;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.http.HttpClient.Version;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link HttpMessages}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpMessagesTest {

	private static final long LONG_42 = 42L;
	private static final long LONG_666 = 666L;

	@ParameterizedTest
	@MethodSource("provideJavaNetHttpVersions")
	void shouldTransformStringToJavaNetHttpVersion(final String httpVersion, final Version version) {
		Version result = HttpMessages.parseJavaNetHttpVersion(httpVersion);

		assertThat(result, equalTo(version));
	}

	private static Stream<Arguments> provideJavaNetHttpVersions() {
		return Stream.of(
				Arguments.of("HTTP/1.1", Version.HTTP_1_1),
				Arguments.of("HTTP/2", Version.HTTP_2));
	}

	@ParameterizedTest
	@MethodSource("provideJavaNetHttpVersions")
	void shouldTransformJavaNetHttpVersionToString(final String httpVersion, final Version version) {
		String result = HttpMessages.toProtocolString(version);

		assertThat(result, equalTo(httpVersion));
	}

	@Test
	void shouldThrowIllegalArgumentExceptionWhenParsingUnknownJavaNetHttpVersion() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> HttpMessages.parseJavaNetHttpVersion("HTTP/3"));

		assertThat(e.getMessage(), equalTo("Unsupported HTTP version: HTTP/3"));
	}

	@Test
	void shouldBuildRangeString() {
		String result = HttpMessages.getRangeString(LONG_42, LONG_666);

		assertThat(result, equalTo("bytes=42-666"));
	}

	@Test
	void shouldAssumeZeroForNullsOnRangeString() {
		String result = HttpMessages.getRangeString(null, null);

		assertThat(result, equalTo("bytes=0-0"));
	}

	@Test
	void shouldThrowExceptionIfFirstParameterIsBiggerThanTheSecondOnRangeString() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> HttpMessages.getRangeString(LONG_666, LONG_42));

		assertThat(e.getMessage(), equalTo("rangeEnd must be greater or equal to rangeStart"));
	}

	@Test
	void shouldThrowExceptionWhenCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(HttpMessages.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldFindRedirectLoopFailureInThrowableChain() {
		Throwable cause = new RuntimeException("circular redirect");
		Throwable throwable = new RuntimeException("outer", cause);

		assertThat(HttpMessages.isRedirectLoopFailure(throwable), equalTo(true));
	}

	@Test
	void shouldReturnFalseWhenNoRedirectLoopInThrowableChain() {
		Throwable throwable = new RuntimeException("something else");

		assertThat(HttpMessages.isRedirectLoopFailure(throwable), equalTo(false));
	}

	@Test
	void shouldReturnFalseForNullThrowable() {
		assertThat(HttpMessages.isRedirectLoopFailure(null), equalTo(false));
	}

	@Test
	void shouldReturnFalseForThrowableWithNullMessage() {
		Throwable throwable = new RuntimeException();

		assertThat(HttpMessages.isRedirectLoopFailure(throwable), equalTo(false));
	}

	@Test
	void shouldReturnFalseWhenPredicateReceivesNullThrowable() {
		Predicate<Throwable> predicate = HttpMessages.defaultRedirectLoopFailurePredicate();

		assertThat(predicate.test(null), equalTo(false));
	}

	@Test
	void shouldReturnTrueForCircularRedirectThroughPredicate() {
		Predicate<Throwable> predicate = HttpMessages.defaultRedirectLoopFailurePredicate();

		assertThat(predicate.test(new RuntimeException("circular redirect")), equalTo(true));
	}

	@Test
	void shouldReturnTrueForTooManyRedirectsThroughPredicate() {
		Predicate<Throwable> predicate = HttpMessages.defaultRedirectLoopFailurePredicate();

		assertThat(predicate.test(new RuntimeException("too many redirects")), equalTo(true));
	}
}
