package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt64;
import org.apiphany.security.ssl.SSLProtocol;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AdditionalAuthenticatedData}.
 *
 * @author Radu Sebastian LAZIN
 */
class AdditionalAuthenticatedDataTest {

	private static final byte[] AAD = new byte[] {
			// Sequence Number (8 bytes) - 1
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
			// Content Type (1 byte) - Application Data
			0x17,
			// Protocol Version (2 bytes) - TLS 1.2
			0x03, 0x03,
			// Length (2 bytes) - 16 bytes
			0x00, 0x10
	};

	@Test
	void shouldCreateAADWithTLSObjectsConstructor() {
		AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData(
				UInt64.of(1L),
				RecordContentType.APPLICATION_DATA,
				Version.of(SSLProtocol.TLS_1_2),
				UInt16.of((short) 16));

		assertArrayEquals(AAD, aad.toByteArray());
	}

	@Test
	void shouldCreateAADWithPrimitiveValuesConstructor() {
		AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData(
				1L,
				RecordContentType.APPLICATION_DATA,
				SSLProtocol.TLS_1_2,
				(short) 16);

		assertArrayEquals(AAD, aad.toByteArray());
	}

	@Test
	void shouldCreateAADFromInputStream() throws Exception {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AAD);

		AdditionalAuthenticatedData aad = AdditionalAuthenticatedData.from(byteArrayInputStream);

		assertArrayEquals(AAD, aad.toByteArray());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		AdditionalAuthenticatedData aad1 =
				new AdditionalAuthenticatedData(
						1L,
						RecordContentType.APPLICATION_DATA,
						SSLProtocol.TLS_1_2,
						(short) 42);

		AdditionalAuthenticatedData aad2 =
				new AdditionalAuthenticatedData(
						1L,
						RecordContentType.APPLICATION_DATA,
						SSLProtocol.TLS_1_2,
						(short) 42);

		// same reference
		assertEquals(aad1, aad1);

		// different instance, same values
		assertEquals(aad1, aad2);
		assertEquals(aad2, aad1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(aad1.hashCode(), aad2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		AdditionalAuthenticatedData aad1 =
				new AdditionalAuthenticatedData(
						1L,
						RecordContentType.APPLICATION_DATA,
						SSLProtocol.TLS_1_2,
						(short) 42);

		AdditionalAuthenticatedData aad2 =
				new AdditionalAuthenticatedData(
						2L,
						RecordContentType.APPLICATION_DATA,
						SSLProtocol.TLS_1_2,
						(short) 42);

		// different objects
		assertNotEquals(aad1, aad2);
		assertNotEquals(aad1, null);
		assertNotEquals(aad2, "not-an-aad");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		AdditionalAuthenticatedData aad =
				new AdditionalAuthenticatedData(
						1L,
						RecordContentType.APPLICATION_DATA,
						SSLProtocol.TLS_1_2,
						(short) 42);

		int expectedHash = Objects.hash(
				aad.getSequenceNumber(),
				aad.getType(),
				aad.getProtocolVersion(),
				aad.getLength());

		assertEquals(expectedHash, aad.hashCode());
	}
}
