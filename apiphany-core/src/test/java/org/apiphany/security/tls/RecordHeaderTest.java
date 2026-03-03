package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

import org.apiphany.security.ssl.SSLProtocol;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link RecordHeader}.
 *
 * @author Radu Sebastian LAZIN
 */
class RecordHeaderTest {

	@Test
	void shouldEqualSameValuesAndSameReference() {
		RecordHeader rh1 = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) 123);
		RecordHeader rh2 = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) 123);

		// same reference
		assertEquals(rh1, rh1);

		// different instance, same values
		assertEquals(rh1, rh2);
		assertEquals(rh2, rh1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(rh1.hashCode(), rh2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		RecordHeader rh1 = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) 123);
		RecordHeader rh2 = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) 124);

		// different objects
		assertNotEquals(rh1, rh2);
		assertNotEquals(rh2, rh1);

		// different types
		assertNotEquals(rh1, null);
		assertNotEquals(rh2, "not-a-record-header");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		RecordHeader rh = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) 123);

		int expectedHash = Objects.hash(
				rh.getType(),
				rh.getVersion(),
				rh.getLength());

		assertEquals(expectedHash, rh.hashCode());
	}

	@Test
	void shouldThrowExceptionWheReadingTheWrongTypeFromInputStream() {
		RecordHeader rh = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) 123);
		ByteArrayInputStream bais = new ByteArrayInputStream(rh.toByteArray());

		IOException exception = assertThrows(IOException.class, () -> {
			RecordHeader.from(bais, RecordContentType.APPLICATION_DATA);
		});

		assertEquals("Expected APPLICATION_DATA record to continue, got HANDSHAKE", exception.getMessage());
	}
}
