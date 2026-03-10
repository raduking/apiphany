package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link HandshakeHeader}.
 *
 * @author Radu Sebastian LAZIN
 */
class HandshakeHeaderTest {

	@Test
	void shouldEqualSameValuesAndSameReference() {
		HandshakeHeader hh1 = new HandshakeHeader(HandshakeType.CERTIFICATE);
		HandshakeHeader hh2 = new HandshakeHeader(HandshakeType.CERTIFICATE);

		// same reference
		assertEquals(hh1, hh1);

		// different instance, same values
		assertEquals(hh1, hh2);
		assertEquals(hh2, hh1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(hh1.hashCode(), hh2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		HandshakeHeader hh1 = new HandshakeHeader(HandshakeType.CERTIFICATE);
		HandshakeHeader hh2 = new HandshakeHeader(HandshakeType.FINISHED);

		// different objects
		assertNotEquals(hh1, hh2);
		assertNotEquals(hh2, hh1);

		// different types
		assertThat(hh1, not(equalTo(null)));
		assertThat(hh1, not(equalTo("not-a-handshake-header")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		HandshakeHeader hh = new HandshakeHeader(HandshakeType.CERTIFICATE);

		int expectedHash = Objects.hash(
				hh.getType(),
				hh.getLength());

		assertEquals(expectedHash, hh.hashCode());
	}
}
