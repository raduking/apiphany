package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link RawHandshakeBody}.
 *
 * @author Radu Sebastian LAZIN
 */
class RawHandshakeBodyTest {

	private static final int SIZE = 13;
	private static final byte[] DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
			0x09, 0x0A, 0x0B, 0x0C, 0x0D
	};

	@Test
	void shouldTransformToByteArrayAndReadItBack() throws IOException {
		RawHandshakeBody body = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(DATA));
		byte[] bodyBytes1 = body.toByteArray();

		body = RawHandshakeBody.from(new ByteArrayInputStream(bodyBytes1), HandshakeType.CERTIFICATE, SIZE);
		assertThat(body.getType(), equalTo(HandshakeType.CERTIFICATE));
		assertThat(body.getBytes().toByteArray(), equalTo(bodyBytes1));

		byte[] bodyBytes2 = body.toByteArray();

		assertThat(bodyBytes1, equalTo(bodyBytes2));
	}

	@Test
	void shouldThrowExceptionWhenReadingFromInputStreamWithNegativeSize() {
		RawHandshakeBody body = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(DATA));
		byte[] bodyBytes = body.toByteArray();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			RawHandshakeBody.from(new ByteArrayInputStream(bodyBytes), HandshakeType.CERTIFICATE, -1);
		});

		assertThat(exception.getMessage(), equalTo("Size cannot be negative"));
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		RawHandshakeBody body1 = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(DATA));
		RawHandshakeBody body2 = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(DATA));

		// same reference
		assertEquals(body1, body1);

		// different instance, same values
		assertEquals(body1, body2);
		assertEquals(body2, body1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(body1.hashCode(), body2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		RawHandshakeBody body1 = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(DATA));
		RawHandshakeBody body2 = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(new byte[] { 0x05, 0x06, 0x07 }));

		// different objects
		assertNotEquals(body1, body2);
		assertNotEquals(body2, body1);

		// different types
		assertNotEquals(body1, null);
		assertNotEquals(body2, "not-an-aad");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		RawHandshakeBody body = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(DATA));

		int expectedHash = Objects.hash(
				body.getType(),
				body.getBytes());

		assertEquals(expectedHash, body.hashCode());
	}

	@Test
	void shouldSerializeToString() {
		RawHandshakeBody body = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(DATA));

		String result = body.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
