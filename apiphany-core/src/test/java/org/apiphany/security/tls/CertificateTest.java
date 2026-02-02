package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Objects;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Certificate}.
 *
 * @author Radu Sebastian LAZIN
 */
class CertificateTest {

	private static final byte[] DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Certificate cert1 = new Certificate(DATA);
		Certificate cert2 = new Certificate(DATA);

		// same reference
		assertEquals(cert1, cert1);

		// different instance, same values
		assertEquals(cert1, cert2);
		assertEquals(cert2, cert1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(cert1.hashCode(), cert2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Certificate cert1 = new Certificate(DATA);
		Certificate cert2 = new Certificate(new byte[] { 0x05, 0x06, 0x07 });

		// different objects
		assertNotEquals(cert1, cert2);
		assertNotEquals(cert2, cert1);

		// different types
		assertNotEquals(cert1, null);
		assertNotEquals(cert2, "not-an-aad");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		Certificate cert = new Certificate(DATA);

		int expectedHash = Objects.hash(
				cert.getLength(),
				cert.getData());

		assertEquals(expectedHash, cert.hashCode());
	}

	@Test
	void shouldParseX509Certificate() throws Exception {
		// to generate the test certificate files, you can read the docs/security/ssl/key-transformations.md
		byte[] certBytes = Files.readAllBytes(
				Path.of("src/test/resources/security/ssl/rsa_certificate.der"));

		Certificate certificate = new Certificate(certBytes);

		X509Certificate x509 = certificate.toX509Certificate();

		assertNotNull(x509);
		assertEquals("X.509", x509.getType());
		assertNotNull(x509.getPublicKey());
	}

	@Test
	void shouldNotParsePublicKey() throws Exception {
		// to generate the test certificate files, you can read the docs/security/ssl/key-transformations.md
		byte[] certBytes = Files.readAllBytes(
				Path.of("src/test/resources/security/ssl/rsa_public.der"));

		Certificate certificate = new Certificate(certBytes);

		SecurityException exception = assertThrows(SecurityException.class, certificate::toX509Certificate);

		assertEquals("Failed to parse X.509 certificate", exception.getMessage());
	}

	@Test
	void shouldReadCertificateFromInputStream() throws Exception {
		byte[] bytes = new byte[] {
				// length: 3 bytes
				0x00, 0x00, 0x04,
				// data: 4 bytes
				0x01, 0x02, 0x03, 0x04
		};
		Certificate cert = Certificate.from(new ByteArrayInputStream(bytes));

		assertEquals(bytes.length, cert.sizeOf());
		assertArrayEquals(bytes, cert.toByteArray());
		assertEquals(cert.getData().sizeOf(), cert.sizeOf() - cert.getLength().sizeOf());
	}

	@Test
	void shouldSerializeToString() {
		Certificate cert = new Certificate(DATA);

		String result = cert.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
