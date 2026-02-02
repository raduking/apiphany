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
 * Test class for {@link CipherSuites}.
 *
 * @author Radu Sebastian LAZIN
 */
class CipherSuitesTest {

	@Test
	void shouldEqualSameValuesAndSameReference() {
		CipherSuites certs1 = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256);
		CipherSuites certs2 = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256);

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
		CipherSuites certs1 = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256);
		CipherSuites certs2 = new CipherSuites(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384);

		// different objects
		assertNotEquals(certs1, certs2);
		assertNotEquals(certs2, certs1);

		// different types
		assertNotEquals(certs1, null);
		assertNotEquals(certs2, "not-an-aad");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		CipherSuites certs = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256);

		int expectedHash = Objects.hash(
				certs.getSize(),
				certs.getSuites());

		assertEquals(expectedHash, certs.hashCode());
	}

	@Test
	void shouldReadCertificateFromInputStream() throws Exception {
		byte[] bytes = new byte[] {
				// full length: 2 bytes
				0x00, 0x04,
				// TLS_AES_128_GCM_SHA256
				0x13, 0x01,
				// TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384
				(byte) (0xC0 & 0xFF), 0x24
		};
		CipherSuites certs = CipherSuites.from(new ByteArrayInputStream(bytes));

		assertEquals(bytes.length, certs.sizeOf());
		assertArrayEquals(bytes, certs.toByteArray());

		assertEquals(2, certs.getSuites().size());
		assertEquals(CipherSuite.TLS_AES_128_GCM_SHA256, certs.getSuites().get(0));
		assertEquals(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384, certs.getSuites().get(1));
	}

	@Test
	void shouldSerializeToString() {
		CipherSuites certs = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384);

		String result = certs.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
