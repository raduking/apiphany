package org.apiphany.security.tls.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link RenegotiationInfo}.
 *
 * @author Radu Sebastian LAZIN
 */
class RenegotiationInfoTest {

	@Test
	void shouldReadFromInputStream() throws Exception {
		byte[] inputData = new byte[] {
				(byte) 0xFF, 0x01, // renegotiation_info type
				0x00, 0x02, // extension length (2 for this extension)
				0x01
		};

		RenegotiationInfo ri = RenegotiationInfo.from(new ByteArrayInputStream(inputData));

		assertEquals(ExtensionType.RENEGOTIATION_INFO, ri.getType());
		assertEquals(UInt16.of((short) 2), ri.getSize());
		assertEquals(UInt8.of((byte) 0x01), ri.getLength());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		RenegotiationInfo ri1 = new RenegotiationInfo();
		RenegotiationInfo ri2 = new RenegotiationInfo();

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
		RenegotiationInfo ri1 = new RenegotiationInfo();
		RenegotiationInfo ri2 = new RenegotiationInfo(ExtensionType.RENEGOTIATION_INFO, UInt16.of((short) 0x0005), UInt8.of((byte) 0x01));

		// different objects
		assertNotEquals(ri1, ri2);
		assertNotEquals(ri2, ri1);

		// different types
		assertNotEquals(ri1, null);
		assertNotEquals(ri2, "not-a-renegotiation-info");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		RenegotiationInfo ri = new RenegotiationInfo();

		int expectedHash = Objects.hash(
				ri.getType(),
				ri.getSize(),
				ri.getLength());

		assertEquals(expectedHash, ri.hashCode());
	}
}
