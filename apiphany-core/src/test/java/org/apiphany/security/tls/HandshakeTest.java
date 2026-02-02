package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Handshake}.
 *
 * @author Radu Sebastian LAZIN
 */
class HandshakeTest {

	private static final byte[] DATA = new byte[] { 0x01, 0x02, 0x03, 0x04 };

	@Test
	void shouldBuildHandshakeAndSetTheCorrectHandshakeHeader() {
		Finished finished = new Finished(DATA);
		Handshake handshake = new Handshake(finished);

		HandshakeHeader header = handshake.getHeader();

		assertThat(header.getType(), equalTo(HandshakeType.FINISHED));
		assertThat(header.getLength().toUnsignedInt(), equalTo(DATA.length));
	}

	@Test
	void shouldBuildHandshakeAndSetTheCorrectFinishedBody() {
		Finished finished = new Finished(DATA);
		Handshake handshake = new Handshake(finished);

		Finished body = (Finished) handshake.getBody();

		assertThat(body.getVerifyData().toByteArray(), equalTo(DATA));
	}

	@Test
	void shouldBuildHandshakeFromHeaderAndBody() {
		Finished finished = new Finished(DATA);
		Handshake originalHandshake = new Handshake(finished);
		HandshakeHeader header = originalHandshake.getHeader();
		Finished body = (Finished) originalHandshake.getBody();

		Handshake handshake = new Handshake(header, body);

		assertThat(handshake.getHeader().getType(), equalTo(HandshakeType.FINISHED));
		assertThat(handshake.getHeader().getLength().toUnsignedInt(), equalTo(DATA.length));
		assertThat(((Finished) handshake.getBody()).getVerifyData().toByteArray(), equalTo(DATA));
	}

	@Test
	void shouldReadHandshakeFromInputStream() throws Exception {
		Finished finished = new Finished(DATA);
		Handshake originalHandshake = new Handshake(finished);
		byte[] handshakeBytes = originalHandshake.toByteArray();

		Handshake handshake = Handshake.from(new ByteArrayInputStream(handshakeBytes));

		assertThat(handshake.getHeader().getType(), equalTo(HandshakeType.FINISHED));
		assertThat(handshake.getHeader().getLength().toUnsignedInt(), equalTo(DATA.length));
		assertThat(handshake.getBody().toByteArray(), equalTo(DATA));
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Finished finished1 = new Finished(DATA);
		Finished finished2 = new Finished(DATA);
		Handshake handshake1 = new Handshake(finished1);
		Handshake handshake2 = new Handshake(finished2);

		// same reference
		assertEquals(handshake1, handshake1);

		// different instance, same values
		assertEquals(handshake1, handshake2);
		assertEquals(handshake2, handshake1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(handshake1.hashCode(), handshake2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Finished finished1 = new Finished(DATA);
		Finished finished2 = new Finished(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		Handshake handshake1 = new Handshake(finished1);
		Handshake handshake2 = new Handshake(finished2);

		// different values
		assertNotEquals(handshake1, handshake2);
		assertNotEquals(handshake2, handshake1);

		// different types
		assertNotEquals(handshake1, null);
		assertNotEquals(handshake2, "some string");
	}

	@Test
	void shouldValidateHandshakeBody() {
		Finished finished = new Finished(DATA);
		Handshake handshake = new Handshake(finished);

		assertTrue(handshake.is(Finished.class));
		assertFalse(handshake.is(ClientHello.class));
	}

	@Test
	void shouldGetHandshakeBodyAsExpectedType() {
		Finished finished = new Finished(DATA);
		Handshake handshake = new Handshake(finished);

		Finished body = handshake.get(Finished.class);

		assertThat(body.toByteArray(), equalTo(DATA));
	}

	@Test
	void shouldThrowWhenGettingHandshakeBodyAsWrongType() {
		Finished finished = new Finished(DATA);
		Handshake handshake = new Handshake(finished);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> handshake.get(ClientHello.class));

		assertThat(e.getMessage(), equalTo("Cannot cast TLS handshake body from " +
				finished.getClass() + " to " + ClientHello.class));
	}

	@Test
	void shouldSerializeToString() {
		Finished finished = new Finished(DATA);
		Handshake handshake = new Handshake(finished);

		String result = handshake.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
