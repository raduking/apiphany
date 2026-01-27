package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt16;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link RSAEncryptedPreMaster}.
 *
 * @author Radu Sebastian LAZIN
 */
class RSAEncryptedPreMasterTest {

	private static final byte[] EPS_BYTES = new byte[] {
			// length: 2 bytes - 4 bytes
			0x00, 0x04,
			// encrypted pre-master secret (4 bytes)
			0x01, 0x02, 0x03, 0x04
	};

	private static final byte[] BYTES = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldCreateRSAEncryptedPreMasterWithTLSObjectsConstructor() {
		RSAEncryptedPreMaster eps = new RSAEncryptedPreMaster(
				UInt16.of((short) 4),
				new BytesWrapper(BYTES));

		assertArrayEquals(EPS_BYTES, eps.toByteArray());
	}

	@Test
	void shouldCreateRSAEncryptedPreMasterWithPrimitiveValuesConstructor() {
		RSAEncryptedPreMaster eps = new RSAEncryptedPreMaster(BYTES);

		assertArrayEquals(EPS_BYTES, eps.toByteArray());
	}

	@Test
	void shouldCreateRSAEncryptedPreMasterFromInputStream() throws Exception {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(EPS_BYTES);

		RSAEncryptedPreMaster eps = RSAEncryptedPreMaster.from(byteArrayInputStream);

		assertArrayEquals(EPS_BYTES, eps.toByteArray());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		RSAEncryptedPreMaster eps1 = new RSAEncryptedPreMaster(BYTES);
		RSAEncryptedPreMaster eps2 = new RSAEncryptedPreMaster(BYTES);

		// same reference
		assertEquals(eps1, eps1);

		// different instance, same values
		assertEquals(eps1, eps2);
		assertEquals(eps2, eps1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(eps1.hashCode(), eps2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		RSAEncryptedPreMaster eps1 = new RSAEncryptedPreMaster(BYTES);
		RSAEncryptedPreMaster eps2 = new RSAEncryptedPreMaster(new byte[] {
				0x01, 0x02, 0x03, 0x04, 0x05
		});

		// different objects
		assertNotEquals(eps1, eps2);
		assertNotEquals(eps1, null);
		assertNotEquals(eps2, "not-an-eps");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		RSAEncryptedPreMaster eps = new RSAEncryptedPreMaster(BYTES);

		int expectedHash = Objects.hash(
				eps.getLength(),
				eps.getBytes());

		assertEquals(expectedHash, eps.hashCode());
	}
}
