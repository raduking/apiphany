package org.apiphany.security.tls.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SessionTicket}.
 *
 * @author Radu Sebastian LAZIN
 */
class SessionTicketTest {

	@Test
	void shouldReadFromInputStream() throws Exception {
		byte[] inputData = new byte[] {
				0x00, 0x23, // session_ticket type
				0x00, 0x00, // extension length (0 for this extension)
		};

		SessionTicket st = SessionTicket.from(new ByteArrayInputStream(inputData));

		assertEquals(ExtensionType.SESSION_TICKET, st.getType());
		assertEquals(UInt16.ZERO, st.getLength());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		SessionTicket st1 = new SessionTicket();
		SessionTicket st2 = new SessionTicket();

		// same reference
		assertEquals(st1, st1);

		// different instance, same values
		assertEquals(st1, st2);
		assertEquals(st2, st1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(st1.hashCode(), st2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		SessionTicket st1 = new SessionTicket();
		SessionTicket st2 = new SessionTicket(ExtensionType.SESSION_TICKET, UInt16.of((short) 0x0005));

		// different objects
		assertNotEquals(st1, st2);
		assertNotEquals(st2, st1);

		// different types
		assertThat(st1, not(equalTo(null)));
		assertThat(st1, not(equalTo("not-a-session-ticket")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		SessionTicket st = new SessionTicket();

		int expectedHash = Objects.hash(
				st.getType(),
				st.getLength());

		assertEquals(expectedHash, st.hashCode());
	}
}
