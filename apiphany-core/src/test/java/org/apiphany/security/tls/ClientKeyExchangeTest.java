package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
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
		ClientKeyExchange cke = new ClientKeyExchange(data);

		assertArrayEquals(data.toByteArray(), cke.toByteArray());
		assertArrayEquals(KEY_DATA, cke.toByteArray());
		assertEquals(KEY_DATA.length, cke.sizeOf());
	}

	@Test
	void shouldCreateClientKeyExchangeFromInputStream() throws Exception {
		KeyExchangeData data = new KeyExchangeData(KEY_DATA);
		ClientKeyExchange cke = ClientKeyExchange.from(
				new ByteArrayInputStream(KEY_DATA),
				KEY_DATA.length);

		assertArrayEquals(data.toByteArray(), cke.toByteArray());
		assertArrayEquals(KEY_DATA, cke.toByteArray());
		assertEquals(KEY_DATA.length, cke.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		KeyExchangeData data1 = new KeyExchangeData(KEY_DATA);
		KeyExchangeData data2 = new KeyExchangeData(KEY_DATA);
		ClientKeyExchange cke1 = new ClientKeyExchange(data1);
		ClientKeyExchange cke2 = new ClientKeyExchange(data2);

		// same reference
		assertEquals(cke1, cke1);

		// different instance, same values
		assertEquals(cke1, cke2);
		assertEquals(cke2, cke1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(cke1.hashCode(), cke2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		KeyExchangeData data1 = new KeyExchangeData(KEY_DATA);
		KeyExchangeData data2 = new KeyExchangeData(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		ClientKeyExchange cke1 = new ClientKeyExchange(data1);
		ClientKeyExchange cke2 = new ClientKeyExchange(data2);

		// different values
		assertNotEquals(cke1, cke2);
		assertNotEquals(cke2, cke1);

		// different types
		assertThat(cke1, not(equalTo(null)));
		assertThat(cke1, not(equalTo("not-a-client-key-exchange-object")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		KeyExchangeData data = new KeyExchangeData(KEY_DATA);
		ClientKeyExchange clientKeyExchange = new ClientKeyExchange(data);

		int expectedHashCode = Objects.hash(clientKeyExchange.getKey());

		assertEquals(expectedHashCode, clientKeyExchange.hashCode());
	}

	@Test
	void shouldHaveTheCorrectType() {
		KeyExchangeData data = new KeyExchangeData(KEY_DATA);
		ClientKeyExchange clientKeyExchange = new ClientKeyExchange(data);

		assertEquals(HandshakeType.CLIENT_KEY_EXCHANGE, clientKeyExchange.getType());
	}
}
