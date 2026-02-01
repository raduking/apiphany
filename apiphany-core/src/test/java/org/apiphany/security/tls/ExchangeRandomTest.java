package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.security.SecureRandom;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExchangeRandom}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExchangeRandomTest {

	private static final byte[] FIXED_BYTES = new byte[] {
			(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
			(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
			(byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
			(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
			(byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13,
			(byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17,
			(byte) 0x18, (byte) 0x19, (byte) 0x1A, (byte) 0x1B,
			(byte) 0x1C, (byte) 0x1D, (byte) 0x1E, (byte) 0x1F
	};
	private static final int BYTES = 32;

	@Test
	void shouldBuildExchangeRandomFromByteArray() {
		ExchangeRandom exchangeRandom = new ExchangeRandom(FIXED_BYTES);

		int size = exchangeRandom.sizeOf();

		assertThat(size, equalTo(BYTES));
	}

	@Test
	void shouldThrowExceptionWhenBuildingExchangeRandomFromShortByteArray() {
		byte[] shortBytes = new byte[] {
				(byte) 0x00, (byte) 0x01, (byte) 0x02
		};

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new ExchangeRandom(shortBytes));

		assertThat(exception.getMessage(), equalTo("ExchangeRandom must be exactly 32 bytes long"));
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		ExchangeRandom er1 = new ExchangeRandom(FIXED_BYTES);
		ExchangeRandom er2 = new ExchangeRandom(FIXED_BYTES);

		// same reference
		assertEquals(er1, er1);

		// different instance, same values
		assertEquals(er1, er2);
		assertEquals(er2, er1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(er1.hashCode(), er2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		ExchangeRandom er1 = new ExchangeRandom();
		ExchangeRandom er2 = new ExchangeRandom(new SecureRandom());

		// different values
		assertNotEquals(er1, er2);
		assertNotEquals(er2, er1);

		// different types
		assertNotEquals(er1, null);
		assertNotEquals(er2, "some string");
	}

	@Test
	void shouldReadExchangeRandomFromInputStream() throws Exception {
		ExchangeRandom exchangeRandom = ExchangeRandom.from(new ByteArrayInputStream(FIXED_BYTES));

		assertThat(exchangeRandom.toByteArray(), equalTo(FIXED_BYTES));
		assertThat(exchangeRandom.getRandom(), equalTo(FIXED_BYTES));
	}

	@Test
	void shouldGenerateRandomBytesOfGivenLength() {
		byte[] randomBytes1 = ExchangeRandom.generate(BYTES);
		byte[] randomBytes2 = ExchangeRandom.generate(BYTES);

		assertThat(randomBytes1.length, equalTo(BYTES));
		assertThat(randomBytes2.length, equalTo(BYTES));

		assertFalse(Arrays.equals(randomBytes1, randomBytes2));
	}
}
