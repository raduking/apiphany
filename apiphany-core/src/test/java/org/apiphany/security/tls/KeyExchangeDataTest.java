package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
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
		KeyExchangeData keyExchangeData = new KeyExchangeData(data);

		assertArrayEquals(data.toByteArray(), keyExchangeData.toByteArray());
		assertArrayEquals(DATA, keyExchangeData.toByteArray());
		assertEquals(DATA.length, keyExchangeData.sizeOf());
	}

	@Test
	void shouldCreateKeyExchangeDataFromInputStream() throws Exception {
		BytesWrapper data = new BytesWrapper(DATA);
		KeyExchangeData keyExchangeData = KeyExchangeData.from(
				new ByteArrayInputStream(DATA),
				DATA.length);

		assertArrayEquals(data.toByteArray(), keyExchangeData.toByteArray());
		assertArrayEquals(DATA, keyExchangeData.toByteArray());
		assertEquals(DATA.length, keyExchangeData.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		BytesWrapper data1 = new BytesWrapper(DATA);
		BytesWrapper data2 = new BytesWrapper(DATA);
		KeyExchangeData keyExchangeData1 = new KeyExchangeData(data1);
		KeyExchangeData keyExchangeData2 = new KeyExchangeData(data2);

		// same reference
		assertEquals(keyExchangeData1, keyExchangeData1);

		// different instance, same values
		assertEquals(keyExchangeData1, keyExchangeData2);
		assertEquals(keyExchangeData2, keyExchangeData1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(keyExchangeData1.hashCode(), keyExchangeData2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		BytesWrapper data1 = new BytesWrapper(DATA);
		BytesWrapper data2 = new BytesWrapper(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		KeyExchangeData keyExchangeData1 = new KeyExchangeData(data1);
		KeyExchangeData keyExchangeData2 = new KeyExchangeData(data2);

		// different values
		assertNotEquals(keyExchangeData1, keyExchangeData2);
		assertNotEquals(keyExchangeData2, keyExchangeData1);

		// different types
		assertThat(keyExchangeData1, not(equalTo(null)));
		assertThat(keyExchangeData1, not(equalTo("not-a-key-exchange-data-object")));
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
