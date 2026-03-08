package org.apiphany.security.tls.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link RenegotiationInfoExtended}.
 *
 * @author Radu Sebastian LAZIN
 */
class RenegotiationInfoExtendedTest {

	@Test
	void shouldReadFromInputStream() throws Exception {
		byte[] inputData = new byte[] {
				0x00, 0x17, // renegotiation_info_extended type
				0x00, 0x00, // extension length (0 for this extension)
		};

		RenegotiationInfoExtended ems = RenegotiationInfoExtended.from(new ByteArrayInputStream(inputData));

		assertEquals(ExtensionType.RENEGOTIATION_INFO_EXTENDED, ems.getType());
		assertEquals(UInt16.ZERO, ems.getLength());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		RenegotiationInfoExtended rie1 = new RenegotiationInfoExtended();
		RenegotiationInfoExtended rie2 = new RenegotiationInfoExtended();

		// same reference
		assertEquals(rie1, rie1);

		// different instance, same values
		assertEquals(rie1, rie2);
		assertEquals(rie2, rie1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(rie1.hashCode(), rie2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		RenegotiationInfoExtended rie1 = new RenegotiationInfoExtended();
		RenegotiationInfoExtended rie2 = new RenegotiationInfoExtended(ExtensionType.RENEGOTIATION_INFO_EXTENDED, UInt16.of((short) 0x0005));

		// different objects
		assertNotEquals(rie1, rie2);
		assertNotEquals(rie2, rie1);

		// different types
		assertNotEquals(rie1, null);
		assertNotEquals(rie2, "not-a-status-request");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		RenegotiationInfoExtended sr = new RenegotiationInfoExtended();

		int expectedHash = Objects.hash(
				sr.getType(),
				sr.getLength());

		assertEquals(expectedHash, sr.hashCode());
	}
}
