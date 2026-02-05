package org.apiphany.security.tls.ext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
		assertArrayEquals(DATA, sctExtension.toByteArray());
	}
}
