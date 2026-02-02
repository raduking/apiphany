package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Certificates}.
 *
 * @author Radu Sebastian LAZIN
 */
class CertificatesTest {

	private static final byte[] DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Certificate cert1 = new Certificate(DATA);
		Certificate cert2 = new Certificate(DATA);
		Certificates certs1 = new Certificates(cert1);
		Certificates certs2 = new Certificates(cert2);

		// same reference
		assertEquals(certs1, certs1);

		// different instance, same values
		assertEquals(certs1, certs2);
		assertEquals(certs2, certs1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(certs1.hashCode(), certs2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Certificate cert1 = new Certificate(DATA);
		Certificate cert2 = new Certificate(new byte[] { 0x05, 0x06, 0x07 });
		Certificates certs1 = new Certificates(cert1);
		Certificates certs2 = new Certificates(cert2);

		// different objects
		assertNotEquals(certs1, certs2);
		assertNotEquals(certs2, certs1);

		// different types
		assertNotEquals(certs1, null);
		assertNotEquals(certs2, "not-an-aad");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		Certificate cert = new Certificate(DATA);
		Certificates certs = new Certificates(cert);

		int expectedHash = Objects.hash(
				certs.getLength(),
				certs.getList());

		assertEquals(expectedHash, certs.hashCode());
	}

	@Test
	void shouldReadCertificateFromInputStream() throws Exception {
		byte[] bytes = new byte[] {
				// full length: 3 bytes
				0x00, 0x00, 0x07,
				// length: 3 bytes
				0x00, 0x00, 0x04,
				// data: 4 bytes
				0x01, 0x02, 0x03, 0x04
		};
		Certificates certs = Certificates.from(new ByteArrayInputStream(bytes));

		assertEquals(bytes.length, certs.sizeOf());
		assertArrayEquals(bytes, certs.toByteArray());
	}

	@Test
	void shouldSerializeToString() {
		Certificate cert = new Certificate(DATA);
		Certificates certs = new Certificates(cert);

		String result = certs.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}

	@Test
	void shouldReturnFirst() {
		Certificate cert = new Certificate(DATA);
		Certificates certs = new Certificates(cert);

		Certificate result = certs.first();

		assertEquals(cert, result);
	}

	@Test
	void shouldReturnByIndex() {
		Certificate cert1 = new Certificate(new byte[] { 0x01 });
		Certificate cert2 = new Certificate(new byte[] { 0x02 });
		Certificate cert3 = new Certificate(new byte[] { 0x03 });
		Certificates certs = new Certificates(cert1, cert2, cert3);

		Certificate result = certs.get(2);

		assertEquals(cert3, result);
	}
}
