package org.apiphany.security.tls.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link StatusRequest}.
 *
 * @author Radu Sebastian LAZIN
 */
class StatusRequestTest {

	@Test
	void shouldReadFromInputStream() throws Exception {
		byte[] inputData = new byte[] {
				0x00, 0x05, // status_request type
				0x00, 0x05, // responder_id_list length (2 bytes)
				0x01, // certificate_status_type (1 byte)
				0x00, 0x00, // responder_id_info_size (2 bytes)
				0x00, 0x01, // request_extension_info_size (2 bytes)
		};

		StatusRequest sr = StatusRequest.from(new ByteArrayInputStream(inputData));

		assertEquals(ExtensionType.STATUS_REQUEST, sr.getType());
		assertEquals(UInt16.of((short) 0x0005), sr.getLength());
		assertEquals(UInt8.of((byte) 0x01), sr.getCertificateStatusType());
		assertEquals(UInt16.ZERO, sr.getResponderIDInfoSize());
		assertEquals(UInt16.of((short) 0x0001), sr.getRequestExtensionInfoSize());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		StatusRequest sr1 = new StatusRequest();
		StatusRequest sr2 = new StatusRequest();

		// same reference
		assertEquals(sr1, sr1);

		// different instance, same values
		assertEquals(sr1, sr2);
		assertEquals(sr2, sr1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(sr1.hashCode(), sr2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		StatusRequest sr1 = new StatusRequest();
		StatusRequest sr2 = new StatusRequest(ExtensionType.STATUS_REQUEST, UInt16.of((short) 0x0005), UInt8.of((byte) 0x01), UInt16.ZERO,
				UInt16.of((short) 0x0001));

		// different objects
		assertNotEquals(sr1, sr2);
		assertNotEquals(sr2, sr1);

		// different types
		assertNotEquals(sr1, null);
		assertNotEquals(sr2, "not-a-status-request");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		StatusRequest sr = new StatusRequest();

		int expectedHash = Objects.hash(
				sr.getType(),
				sr.getLength(),
				sr.getCertificateStatusType(),
				sr.getResponderIDInfoSize(),
				sr.getRequestExtensionInfoSize());

		assertEquals(expectedHash, sr.hashCode());
	}
}
