package org.apiphany.security.tls.ext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SignedCertificateTimestamp}.
 *
 * @author Radu Sebastian LAZIN
 */
class SignedCertificateTimestampTest {

	private static final byte[] DATA = new byte[] {
			// Extension Type: signed_certificate_timestamp (18)
			0x00, 0x12,
			// Extension Length: 0x0004
			0x00, 0x04,
			// SCT Data (4 bytes)
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldReadSignedCertificateTimestampExtensionFromInputStream() throws IOException {
		SignedCertificateTimestamp sctExtension = SignedCertificateTimestamp.from(new ByteArrayInputStream(DATA));

		assertEquals(ExtensionType.SIGNED_CERTIFICATE_TIMESTAMP, sctExtension.getType());
		assertEquals(UInt16.of((short) 4), sctExtension.getLength());
		assertArrayEquals(DATA, sctExtension.toByteArray());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		SignedCertificateTimestamp ri1 = new SignedCertificateTimestamp();
		SignedCertificateTimestamp ri2 = new SignedCertificateTimestamp();

		// same reference
		assertEquals(ri1, ri1);

		// different instance, same values
		assertEquals(ri1, ri2);
		assertEquals(ri2, ri1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(ri1.hashCode(), ri2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		SignedCertificateTimestamp ri1 = new SignedCertificateTimestamp();
		SignedCertificateTimestamp ri2 =
				new SignedCertificateTimestamp(ExtensionType.SIGNED_CERTIFICATE_TIMESTAMP, (short) 0x0005,
						new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 });

		// different objects
		assertNotEquals(ri1, ri2);
		assertNotEquals(ri2, ri1);

		// different types
		assertNotEquals(ri1, null);
		assertNotEquals(ri2, "not-a-signed-certificate-timestamp");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		SignedCertificateTimestamp ri = new SignedCertificateTimestamp();

		int expectedHash = Objects.hash(
				ri.getType(),
				ri.getLength(),
				ri.getData());

		assertEquals(expectedHash, ri.hashCode());
	}
}
