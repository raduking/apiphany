package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SessionId}.
 *
 * @author Radu Sebastian LAZIN
 */
class SessionIdTest {

	private static final String SESSION_ID = "session-id-123";

	@Test
	void shouldEqualSameValuesAndSameReference() {
		SessionId si1 = new SessionId(SESSION_ID);
		SessionId si2 = new SessionId(SESSION_ID);

		// same reference
		assertEquals(si1, si1);

		// different instance, same values
		assertEquals(si1, si2);
		assertEquals(si2, si1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(si1.hashCode(), si2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		SessionId si1 = new SessionId(SESSION_ID);
		SessionId si2 = new SessionId();

		// different objects
		assertNotEquals(si1, si2);
		assertNotEquals(si2, si1);

		// different types
		assertThat(si1, not(equalTo(null)));
		assertThat(si1, not(equalTo("not-a-session-id")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		SessionId si = new SessionId(SESSION_ID);

		int expectedHash = Objects.hash(
				si.getLength(),
				si.getValue());

		assertEquals(expectedHash, si.hashCode());
	}
}
