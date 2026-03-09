package org.apiphany.security.tls.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ECPointFormats}.
 *
 * @author Radu Sebastian LAZIN
 */
class ECPointFormatsTest {

	@Test
	void shouldReadFromInputStream() throws Exception {
		ECPointFormats input = new ECPointFormats(UInt8.of((byte) 13));
		byte[] inputData = input.toByteArray();

		ECPointFormats ecpf = ECPointFormats.from(new ByteArrayInputStream(inputData));

		UInt8 ec = UInt8.of((byte) 13);

		assertEquals(ExtensionType.EC_POINTS_FORMAT, ecpf.getType());
		assertEquals(UInt16.of((short) 2), ecpf.getLength());
		assertEquals(UInt8.of((byte) 1), ecpf.getFormatsSize());
		assertEquals(ecpf.getFormats().size(), 1);
		assertEquals(ec, ecpf.getFormats().getFirst());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		ECPointFormats ecpf1 = new ECPointFormats();
		ECPointFormats ecpf2 = new ECPointFormats();

		// same reference
		assertEquals(ecpf1, ecpf1);

		// different instance, same values
		assertEquals(ecpf1, ecpf2);
		assertEquals(ecpf2, ecpf1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(ecpf1.hashCode(), ecpf2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		ECPointFormats ecpf1 = new ECPointFormats();
		ECPointFormats ecpf2 = new ECPointFormats(UInt8.of((byte) 13));

		// different objects
		assertNotEquals(ecpf1, ecpf2);
		assertNotEquals(ecpf2, ecpf1);

		// different types
		assertNotEquals(ecpf1, null);
		assertNotEquals(ecpf2, "not-a-server-name-indication");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		ECPointFormats ecpf = new ECPointFormats();

		int expectedHash = Objects.hash(
				ecpf.getType(),
				ecpf.getLength(),
				ecpf.getFormatsSize(),
				ecpf.getFormats());

		assertEquals(expectedHash, ecpf.hashCode());
	}
}
