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
 * Test class for {@link ServerNames}.
 *
 * @author Radu Sebastian LAZIN
 */
class ServerNamesTest {

	private static final String MUMU = "mumu";
	private static final String BUBU = "bubu";
	private static final String CUCU = "cucu";

	@Test
	void shouldReadFromInputStream() throws Exception {
		ServerNames input = new ServerNames(MUMU);
		byte[] inputData = input.toByteArray();

		ServerNames sns = ServerNames.from(new ByteArrayInputStream(inputData));

		ServerName sn = new ServerName(MUMU);

		assertEquals(ExtensionType.SERVER_NAME_INDICATION, sns.getType());
		assertEquals(UInt16.of((short) 9), sns.getLength());
		assertEquals(1, sns.getEntries().size());
		assertEquals(sn, sns.getEntries().getFirst());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		ServerNames sn1 = new ServerNames();
		ServerNames sn2 = new ServerNames();

		// same reference
		assertEquals(sn1, sn1);

		// different instance, same values
		assertEquals(sn1, sn2);
		assertEquals(sn2, sn1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(sn1.hashCode(), sn2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		ServerNames sn1 = new ServerNames(CUCU);
		ServerNames sn2 = new ServerNames(BUBU);

		// different objects
		assertNotEquals(sn1, sn2);
		assertNotEquals(sn2, sn1);

		// different types
		assertThat(sn1, not(equalTo(null)));
		assertThat(sn1, not(equalTo("not-server-names")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		ServerNames sn = new ServerNames();

		int expectedHash = Objects.hash(
				sn.getType(),
				sn.getLength(),
				sn.getEntries());

		assertEquals(expectedHash, sn.hashCode());
	}
}
