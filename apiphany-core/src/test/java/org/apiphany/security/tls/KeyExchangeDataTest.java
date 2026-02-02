package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link KeyExchangeData}.
 *
 * @author Radu Sebastian LAZIN
 */
class KeyExchangeDataTest {

	private static final int SIZE = 13;

	private static final byte[] DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldTransformToByteArrayAndReadItBack() throws IOException {
		byte[] bytes = new byte[SIZE];
		Arrays.fill(bytes, (byte) 0x17);

		KeyExchangeData body = new KeyExchangeData(new BytesWrapper(bytes));
		byte[] bodyBytes1 = body.toByteArray();

		body = KeyExchangeData.from(new ByteArrayInputStream(bodyBytes1), SIZE);
		assertThat(body.getBytes().toByteArray(), equalTo(bodyBytes1));

		byte[] bodyBytes2 = body.toByteArray();

		assertThat(bodyBytes1, equalTo(bodyBytes2));
	}

	@Test
	void shouldCreateKeyExchangeDataWithDataPayload() {
		BytesWrapper data = new BytesWrapper(DATA);
		KeyExchangeData appData = new KeyExchangeData(data);

		assertArrayEquals(data.toByteArray(), appData.toByteArray());
		assertArrayEquals(DATA, appData.toByteArray());
		assertEquals(DATA.length, appData.sizeOf());
	}

	@Test
	void shouldCreateKeyExchangeDataFromInputStream() throws Exception {
		BytesWrapper data = new BytesWrapper(DATA);
		KeyExchangeData appData = KeyExchangeData.from(
				new ByteArrayInputStream(DATA),
				DATA.length);

		assertArrayEquals(data.toByteArray(), appData.toByteArray());
		assertArrayEquals(DATA, appData.toByteArray());
		assertEquals(DATA.length, appData.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		BytesWrapper data1 = new BytesWrapper(DATA);
		BytesWrapper data2 = new BytesWrapper(DATA);
		KeyExchangeData appData1 = new KeyExchangeData(data1);
		KeyExchangeData appData2 = new KeyExchangeData(data2);

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
		BytesWrapper data1 = new BytesWrapper(DATA);
		BytesWrapper data2 = new BytesWrapper(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		KeyExchangeData appData1 = new KeyExchangeData(data1);
		KeyExchangeData appData2 = new KeyExchangeData(data2);

		// different values
		assertNotEquals(appData1, appData2);
		assertNotEquals(appData2, appData1);

		// different types
		assertNotEquals(appData1, null);
		assertNotEquals(appData2, "some string");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		BytesWrapper data = new BytesWrapper(DATA);
		KeyExchangeData keyExchangeData = new KeyExchangeData(data);

		int expectedHashCode = Objects.hash(keyExchangeData.getBytes());

		assertEquals(expectedHashCode, keyExchangeData.hashCode());
	}

	@Test
	void shouldSerializeToString() {
		BytesWrapper data = new BytesWrapper(DATA);
		KeyExchangeData keyExchangeData = new KeyExchangeData(data);

		String result = keyExchangeData.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
