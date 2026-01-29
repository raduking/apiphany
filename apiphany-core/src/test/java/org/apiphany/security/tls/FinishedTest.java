package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
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
		Finished finished = new Finished(verifyDataPayload);

		assertArrayEquals(verifyDataPayload.toByteArray(), finished.toByteArray());
		assertArrayEquals(VERIFY_DATA_PAYLOAD, finished.toByteArray());
		assertEquals(VERIFY_DATA_PAYLOAD.length, finished.sizeOf());
	}

	@Test
	void shouldReturnFinishedAsType() {
		Finished finished = new Finished(VERIFY_DATA_PAYLOAD);

		assertThat(finished.getType(), equalTo(HandshakeType.FINISHED));
	}

	@Test
	void shouldCreateFinishedWithVerifyDataBytesPayload() {
		Finished finished = new Finished(VERIFY_DATA_PAYLOAD);

		assertArrayEquals(VERIFY_DATA_PAYLOAD, finished.toByteArray());
		assertEquals(VERIFY_DATA_PAYLOAD.length, finished.sizeOf());
	}

	@Test
	void shouldCreateFinishedFromInputStream() throws Exception {
		BytesWrapper verifyDataPayload = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		Finished finished = Finished.from(
				new ByteArrayInputStream(VERIFY_DATA_PAYLOAD),
				VERIFY_DATA_PAYLOAD.length);

		assertArrayEquals(verifyDataPayload.toByteArray(), finished.toByteArray());
		assertArrayEquals(VERIFY_DATA_PAYLOAD, finished.toByteArray());
		assertEquals(VERIFY_DATA_PAYLOAD.length, finished.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		BytesWrapper verifyDataPayload1 = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		BytesWrapper verifyDataPayload2 = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		Finished finished1 = new Finished(verifyDataPayload1);
		Finished finished2 = new Finished(verifyDataPayload2);

		// same reference
		assertEquals(finished1, finished1);

		// different instance, same values
		assertEquals(finished1, finished2);
		assertEquals(finished2, finished1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(finished1.hashCode(), finished2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		BytesWrapper verifyDataPayload1 = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		BytesWrapper verifyDataPayload2 = new BytesWrapper(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		Finished finished1 = new Finished(verifyDataPayload1);
		Finished finished2 = new Finished(verifyDataPayload2);

		// different values
		assertNotEquals(finished1, finished2);
		assertNotEquals(finished2, finished1);

		// different types
		assertNotEquals(finished1, null);
		assertNotEquals(finished2, "some string");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		BytesWrapper verifyDataPayload = new BytesWrapper(VERIFY_DATA_PAYLOAD);
		Finished finished = new Finished(verifyDataPayload);

		int expectedHashCode = Objects.hash(finished.getVerifyData());

		assertEquals(expectedHashCode, finished.hashCode());
	}
}
