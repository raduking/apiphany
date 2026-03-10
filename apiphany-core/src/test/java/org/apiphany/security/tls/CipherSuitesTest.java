package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
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
		CipherSuites css1 = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256);
		CipherSuites css2 = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256);

		// same reference
		assertEquals(css1, css1);

		// different instance, same values
		assertEquals(css1, css2);
		assertEquals(css2, css1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(css1.hashCode(), css2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		CipherSuites css1 = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256);
		CipherSuites css2 = new CipherSuites(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384);

		// different objects
		assertNotEquals(css1, css2);
		assertNotEquals(css2, css1);

		// different types
		assertThat(css1, not(equalTo(null)));
		assertThat(css1, not(equalTo("not-cipher-suites")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		CipherSuites css = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256);

		int expectedHash = Objects.hash(
				css.getSize(),
				css.getSuites());

		assertEquals(expectedHash, css.hashCode());
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
		CipherSuites css = CipherSuites.from(new ByteArrayInputStream(bytes));

		assertEquals(bytes.length, css.sizeOf());
		assertArrayEquals(bytes, css.toByteArray());

		assertEquals(2, css.getSuites().size());
		assertEquals(CipherSuite.TLS_AES_128_GCM_SHA256, css.getSuites().get(0));
		assertEquals(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384, css.getSuites().get(1));
	}

	@Test
	void shouldSerializeToString() {
		CipherSuites css = new CipherSuites(CipherSuite.TLS_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384);

		String result = css.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
