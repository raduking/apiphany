package org.apiphany.security.tls.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExtendedMasterSecret}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExtendedMasterSecretTest {

	@Test
	void shouldReadFromInputStream() throws Exception {
		byte[] inputData = new byte[] {
				0x00, 0x16, // extended_master_secret type
				0x00, 0x00, // extension length (0 for this extension)
		};

		ExtendedMasterSecret ems = ExtendedMasterSecret.from(new ByteArrayInputStream(inputData));

		assertEquals(ExtensionType.EXTENDED_MASTER_SECRET, ems.getType());
		assertEquals(UInt16.ZERO, ems.getLength());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		ExtendedMasterSecret ems1 = new ExtendedMasterSecret();
		ExtendedMasterSecret ems2 = new ExtendedMasterSecret();

		// same reference
		assertEquals(ems1, ems1);

		// different instance, same values
		assertEquals(ems1, ems2);
		assertEquals(ems2, ems1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(ems1.hashCode(), ems2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		ExtendedMasterSecret ems1 = new ExtendedMasterSecret();
		ExtendedMasterSecret ems2 = new ExtendedMasterSecret(ExtensionType.EXTENDED_MASTER_SECRET, UInt16.of((short) 0x0005));

		// different objects
		assertNotEquals(ems1, ems2);
		assertNotEquals(ems2, ems1);

		// different types
		assertThat(ems1, not(equalTo(null)));
		assertThat(ems1, not(equalTo("not-extended-master-secret")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		ExtendedMasterSecret ems = new ExtendedMasterSecret();

		int expectedHash = Objects.hash(
				ems.getType(),
				ems.getLength());

		assertEquals(expectedHash, ems.hashCode());
	}
}
