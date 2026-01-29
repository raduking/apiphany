package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Finished}.
 *
 * @author Radu Sebastian LAZIN
 */
class FinishedTest {

	private static final byte[] VERIFY_DATA_PAYLOAD = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldCreateFinishedWithVerifyDataPayload() {
		BytesWrapper verifyDataPayload = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		Finished appData = new Finished(verifyDataPayload);

		assertArrayEquals(verifyDataPayload.toByteArray(), appData.toByteArray());
		assertArrayEquals(VERIFY_DATA_PAYLOAD, appData.toByteArray());
		assertEquals(VERIFY_DATA_PAYLOAD.length, appData.sizeOf());
	}

	@Test
	void shouldCreateFinishedFromInputStream() throws Exception {
		BytesWrapper verifyDataPayload = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		Finished appData = Finished.from(
				new ByteArrayInputStream(VERIFY_DATA_PAYLOAD),
				VERIFY_DATA_PAYLOAD.length);

		assertArrayEquals(verifyDataPayload.toByteArray(), appData.toByteArray());
		assertArrayEquals(VERIFY_DATA_PAYLOAD, appData.toByteArray());
		assertEquals(VERIFY_DATA_PAYLOAD.length, appData.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		BytesWrapper verifyDataPayload1 = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		BytesWrapper verifyDataPayload2 = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		Finished appData1 = new Finished(verifyDataPayload1);
		Finished appData2 = new Finished(verifyDataPayload2);

		// same reference
		assertEquals(appData1, appData1);

		// different instance, same values
		assertEquals(appData1, appData2);
		assertEquals(appData2, appData1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(appData1.hashCode(), appData2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		BytesWrapper verifyDataPayload1 = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		BytesWrapper verifyDataPayload2 = new BytesWrapper(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		Finished appData1 = new Finished(verifyDataPayload1);
		Finished appData2 = new Finished(verifyDataPayload2);

		// different values
		assertNotEquals(appData1, appData2);
		assertNotEquals(appData2, appData1);

		// different types
		assertNotEquals(appData1, null);
		assertNotEquals(appData2, "some string");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		BytesWrapper verifyDataPayload = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		Finished finished = new Finished(verifyDataPayload);

		int expectedHashCode = Objects.hash(finished.getVerifyData());

		assertEquals(expectedHashCode, finished.hashCode());
	}
}
