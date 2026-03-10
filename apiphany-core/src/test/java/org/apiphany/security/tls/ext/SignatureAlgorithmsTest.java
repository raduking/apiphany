package org.apiphany.security.tls.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.apiphany.security.tls.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SignatureAlgorithms}.
 *
 * @author Radu Sebastian LAZIN
 */
class SignatureAlgorithmsTest {

	@Test
	void shouldReadFromInputStream() throws Exception {
		SignatureAlgorithms input = new SignatureAlgorithms();
		byte[] inputData = input.toByteArray();

		SignatureAlgorithms sas = SignatureAlgorithms.from(new ByteArrayInputStream(inputData));

		assertEquals(ExtensionType.SIGNATURE_ALGORITHMS, sas.getType());
		assertEquals(UInt16.of((short) 14), sas.getLength());
		assertEquals(UInt16.of((short) 12), sas.getAlgorithmsSize());
		assertEquals(6, sas.getAlgorithms().size());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		SignatureAlgorithms sas1 = new SignatureAlgorithms();
		SignatureAlgorithms sas2 = new SignatureAlgorithms();

		// same reference
		assertEquals(sas1, sas1);

		// different instance, same values
		assertEquals(sas1, sas2);
		assertEquals(sas2, sas1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(sas1.hashCode(), sas2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		SignatureAlgorithms sas1 = new SignatureAlgorithms(SignatureAlgorithm.ECDSA_SECP256R1_SHA256);
		SignatureAlgorithms sas2 = new SignatureAlgorithms(SignatureAlgorithm.RSA_PKCS1_SHA256);

		// different objects
		assertNotEquals(sas1, sas2);
		assertNotEquals(sas2, sas1);

		// different types
		assertThat(sas1, not(equalTo(null)));
		assertThat(sas1, not(equalTo("not-signature-algorithms")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		SignatureAlgorithms sas = new SignatureAlgorithms();

		int expectedHash = Objects.hash(
				sas.getType(),
				sas.getLength(),
				sas.getAlgorithmsSize(),
				sas.getAlgorithms());

		assertEquals(expectedHash, sas.hashCode());
	}
}
