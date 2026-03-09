package org.apiphany.security.tls.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.apiphany.security.tls.NamedCurve;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SupportedGroups}.
 *
 * @author Radu Sebastian LAZIN
 */
class SupportedGroupsTest {

	@Test
	void shouldReadFromInputStream() throws Exception {
		SupportedGroups e = new SupportedGroups();
		byte[] inputData = e.toByteArray();

		SupportedGroups sgs = SupportedGroups.from(new ByteArrayInputStream(inputData));

		assertEquals(ExtensionType.SUPPORTED_GROUPS, sgs.getType());
		assertEquals(UInt16.of((short) 14), sgs.getLength());
		assertEquals(UInt16.of((short) 12), sgs.getGroupsSize());
		assertEquals(sgs.getGroups().size(), 6);
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		SupportedGroups sgs1 = new SupportedGroups();
		SupportedGroups sgs2 = new SupportedGroups();

		// same reference
		assertEquals(sgs1, sgs1);

		// different instance, same values
		assertEquals(sgs1, sgs2);
		assertEquals(sgs2, sgs1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(sgs1.hashCode(), sgs2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		SupportedGroups sgs1 = new SupportedGroups(NamedCurve.X25519);
		SupportedGroups sgs2 = new SupportedGroups(NamedCurve.SECP256R1);

		// different objects
		assertNotEquals(sgs1, sgs2);
		assertNotEquals(sgs2, sgs1);

		// different types
		assertNotEquals(sgs1, null);
		assertNotEquals(sgs2, "not-a-supported-groups");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		SupportedGroups sgs = new SupportedGroups();

		int expectedHash = Objects.hash(
				sgs.getType(),
				sgs.getLength(),
				sgs.getGroupsSize(),
				sgs.getGroups());

		assertEquals(expectedHash, sgs.hashCode());
	}
}
