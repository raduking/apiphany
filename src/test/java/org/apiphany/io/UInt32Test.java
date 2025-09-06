package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apiphany.lang.Bytes;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link UInt32}.
 *
 * @author Radu Sebastian LAZIN
 */
class UInt32Test {

	private static final int TEST_INT = 666;

	@Test
	void shouldWriteAndReadUInt32() throws IOException {
		UInt32 uInt32 = UInt32.of(TEST_INT);

		byte[] bytes = uInt32.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

		UInt32 result = UInt32.from(bis);

		assertThat(result.getValue(), equalTo(uInt32.getValue()));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamIsEmpty() {
		ByteArrayInputStream bis = new ByteArrayInputStream(Bytes.EMPTY);

		EOFException e = assertThrows(EOFException.class, () -> UInt32.from(bis));
		assertThat(e.getMessage(), equalTo("Error reading " + UInt32.BYTES + " bytes"));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasLessElements() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12, 0x13, 0x14 });

		EOFException e = assertThrows(EOFException.class, () -> UInt32.from(bis));
		assertThat(e.getMessage(), equalTo("Error reading " + UInt32.BYTES + " bytes"));
	}

	@Test
	void shouldBeEqualToItself() {
		UInt32 value = UInt32.of(123456789);

		assertThat(value.equals(value), is(true));
		assertThat(value.hashCode(), equalTo(value.hashCode()));
	}

	@Test
	void shouldBeEqualWhenUnsignedValuesAreTheSame() {
		// signed -1
		UInt32 a = UInt32.of(-1);
		// max unsigned 32-bit
		UInt32 b = UInt32.of(0xFFFF_FFFF);

		assertThat(a.equals(b), is(true));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	void shouldNotBeEqualWhenUnsignedValuesDiffer() {
		UInt32 a = UInt32.of(42);
		UInt32 b = UInt32.of(43);

		assertThat(a.equals(b), is(false));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	void shouldNotBeEqualToNull() {
		UInt32 value = UInt32.of(123);

		assertThat(value.equals(null), is(false));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	void shouldNotBeEqualToDifferentType() {
		UInt32 value = UInt32.of(123);

		assertThat(value.equals("123"), is(false));
	}

	@Test
	void shouldConvertZeroToString() {
		UInt32 value = UInt32.of(0);

		assertThat(value.toString(), is("0"));
	}

	@Test
	void shouldConvertSmallPositiveValueToString() {
		UInt32 value = UInt32.of(12345);

		assertThat(value.toString(), is("12345"));
	}

	@Test
	void shouldConvertMaxUnsignedToString() {
		// signed -1 = 0xFFFFFFFF
		UInt32 value = UInt32.of(-1);

		assertThat(value.toString(), is("4294967295"));
	}

	@Test
	void shouldConvertHighBitValueToString() {
		// signed -2147483648
		UInt32 value = UInt32.of(0x8000_0000);

		assertThat(value.toString(), is("2147483648"));
	}

	@Test
	void shouldReturnTheCorrectSize() {
		assertThat(UInt32.BYTES, equalTo(4));
		assertThat(UInt32.ZERO.sizeOf(), equalTo(UInt32.BYTES));
	}
}
