package org.apiphany.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AuthenticationException}.
 *
 * @author Radu Sebastian LAZIN
 */
class AuthenticationExceptionTest {

	private static final String SOME_MESSAGE = "some message";

	@Test
	void shouldSetMessage() {
		SecurityException e = new AuthenticationException(SOME_MESSAGE);

		assertThat(e.getMessage(), equalTo(SOME_MESSAGE));
	}

	@Test
	void shouldSetCause() {
		RuntimeException cause = new RuntimeException();
		SecurityException e = new AuthenticationException(cause);

		assertThat(e.getCause(), equalTo(cause));
	}

	@Test
	void shouldSetMessageAndCause() {
		RuntimeException cause = new RuntimeException();
		SecurityException e = new AuthenticationException(SOME_MESSAGE, cause);

		assertThat(e.getMessage(), equalTo(SOME_MESSAGE));
		assertThat(e.getCause(), equalTo(cause));
	}

}
