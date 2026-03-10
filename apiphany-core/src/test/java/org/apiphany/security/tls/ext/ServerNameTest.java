package org.apiphany.security.tls.ext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ServerName}.
 *
 * @author Radu Sebastian LAZIN
 */
class ServerNameTest {

	private static final String MUMU = "mumu";

	@Test
	void shouldReadFromInputStream() throws Exception {
		ServerName input = new ServerName(MUMU);
		byte[] inputData = input.toByteArray();

		ServerName sns = ServerName.from(new ByteArrayInputStream(inputData));

		assertEquals(UInt8.ZERO, sns.getType());
		assertEquals(UInt16.of((short) 4), sns.getLength());
		assertEquals(new BytesWrapper(MUMU.getBytes(StandardCharsets.US_ASCII)), sns.getName());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		ServerName sn1 = new ServerName(MUMU);
		ServerName sn2 = new ServerName(MUMU);

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
		ServerName sn1 = new ServerName(MUMU);
		ServerName sn2 = new ServerName("bubu");

		// different objects
		assertNotEquals(sn1, sn2);
		assertNotEquals(sn2, sn1);

		// different types
		assertThat(sn1, not(equalTo(null)));
		assertThat(sn1, not(equalTo("not-a-server-name")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		ServerName sn = new ServerName(MUMU);

		int expectedHash = Objects.hash(
				sn.getSize(),
				sn.getType(),
				sn.getLength(),
				sn.getName());

		assertEquals(expectedHash, sn.hashCode());
	}
}
