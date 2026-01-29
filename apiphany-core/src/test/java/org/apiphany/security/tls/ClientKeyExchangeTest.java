package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ClientKeyExchange}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClientKeyExchangeTest {

	private static final byte[] KEY_DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldCreateClientKeyExchangeWithKeyDataPayload() {
		KeyExchangeData data = new KeyExchangeData(KEY_DATA);
		ClientKeyExchange appData = new ClientKeyExchange(data);

		assertArrayEquals(data.toByteArray(), appData.toByteArray());
		assertArrayEquals(KEY_DATA, appData.toByteArray());
		assertEquals(KEY_DATA.length, appData.sizeOf());
	}

	@Test
	void shouldCreateClientKeyExchangeFromInputStream() throws Exception {
		KeyExchangeData data = new KeyExchangeData(KEY_DATA);
		ClientKeyExchange appData = ClientKeyExchange.from(
				new ByteArrayInputStream(KEY_DATA),
				KEY_DATA.length);

		assertArrayEquals(data.toByteArray(), appData.toByteArray());
		assertArrayEquals(KEY_DATA, appData.toByteArray());
		assertEquals(KEY_DATA.length, appData.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		KeyExchangeData data1 = new KeyExchangeData(KEY_DATA);
		KeyExchangeData data2 = new KeyExchangeData(KEY_DATA);
		ClientKeyExchange appData1 = new ClientKeyExchange(data1);
		ClientKeyExchange appData2 = new ClientKeyExchange(data2);

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
		KeyExchangeData data1 = new KeyExchangeData(KEY_DATA);
		KeyExchangeData data2 = new KeyExchangeData(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		ClientKeyExchange appData1 = new ClientKeyExchange(data1);
		ClientKeyExchange appData2 = new ClientKeyExchange(data2);

		// different values
		assertNotEquals(appData1, appData2);
		assertNotEquals(appData2, appData1);

		// different types
		assertNotEquals(appData1, null);
		assertNotEquals(appData2, "some string");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		KeyExchangeData data = new KeyExchangeData(KEY_DATA);
		ClientKeyExchange clientKeyExchange = new ClientKeyExchange(data);

		int expectedHashCode = Objects.hash(clientKeyExchange.getKey());

		assertEquals(expectedHashCode, clientKeyExchange.hashCode());
	}
}
