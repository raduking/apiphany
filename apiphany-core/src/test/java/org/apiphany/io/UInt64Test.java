package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;

import org.apiphany.lang.Bytes;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link UInt64}.
 *
 * @author Radu Sebastian LAZIN
 */
class UInt64Test {

	private static final long TEST_LONG = 666;

	@Test
	void shouldWriteAndReadUInt64() throws IOException {
		UInt64 uInt64 = UInt64.of(TEST_LONG);

		byte[] bytes = uInt64.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

		UInt64 result = UInt64.from(bis);

		assertThat(result.getSignedValue(), equalTo(uInt64.getSignedValue()));
	}

	@Test
	void shouldReturnAsStringTheInternalValue() {
		long value = 666_666_666;
		UInt64 uInt64 = UInt64.of(value);

		String expected = String.valueOf(value);
		String result = uInt64.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnUnsignedValueInToString() {
		UInt64 uint = UInt64.of(-1);

		String result = uint.toString();

		assertThat(result, equalTo("18446744073709551615"));
	}

	@Test
	void shouldConvertZeroToString() {
		UInt64 u = UInt64.of(0L);

		assertThat(u.toString(), is("0"));
	}

	@Test
	void shouldConvertPositiveValueToString() {
		UInt64 u = UInt64.of(42L);

		assertThat(u.toString(), is("42"));
	}

	@Test
	void shouldConvertHighBitValueToUnsignedString() {
		UInt64 u = UInt64.of(Long.MIN_VALUE);

		assertThat(u.toString(), is("9223372036854775808"));
	}

	@Test
	void shouldBeEqualToItself() {
		UInt64 value = UInt64.of(123456789);

		assertThat(value.equals(value), is(true));
		assertThat(value.hashCode(), equalTo(value.hashCode()));
	}

	@Test
	void shouldBeEqualWhenUnsignedValuesAreTheSame() {
		UInt64 a = UInt64.of(-1);
		UInt64 b = UInt64.of(-1);

		assertThat(a.equals(b), is(true));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	void shouldNotBeEqualWhenValuesDiffer() {
		UInt64 a = UInt64.of(42);
		UInt64 b = UInt64.of(43);

		assertThat(a.equals(b), is(false));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	void shouldNotBeEqualToNull() {
		UInt64 value = UInt64.of(123);

		assertThat(value.equals(null), is(false));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	void shouldNotBeEqualToDifferentType() {
		UInt64 value = UInt64.of(123);

		assertThat(value.equals("123"), is(false));
	}

	@Test
	void shouldHaveDifferentHashCodeForDifferentValues() {
		UInt64 a = UInt64.of(123L);
		UInt64 b = UInt64.of(456L);

		assertThat(a.hashCode(), not(b.hashCode()));
	}

	@Test
	void shouldHaveSameHashCodeForEqualValues() {
		UInt64 a = UInt64.of(123L);
		UInt64 b = UInt64.of(123L);

		assertThat(a.hashCode(), is(b.hashCode()));
	}

	@Test
	void shouldReturnZeroAsBigInteger() {
		UInt64 u = UInt64.of(0L);

		assertThat(u.toUnsignedBigInteger(), is(BigInteger.ZERO));
	}

	@Test
	void shouldReturnMaxUnsignedAsBigInteger() {
		// all bits set
		UInt64 u = UInt64.of(-1L);

		assertThat(u.toUnsignedBigInteger(), is(new BigInteger("18446744073709551615")));
	}

	@Test
	void shouldReturnPositiveValueAsBigInteger() {
		UInt64 u = UInt64.of(42L);

		assertThat(u.toUnsignedBigInteger(), is(BigInteger.valueOf(42)));
	}

	@Test
	void shouldReturnHighBitValueAsBigInteger() {
		// 0x8000000000000000
		UInt64 u = UInt64.of(Long.MIN_VALUE);

		assertThat(u.toUnsignedBigInteger(), is(new BigInteger("9223372036854775808")));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamIsEmpty() {
		ByteArrayInputStream bis = new ByteArrayInputStream(Bytes.EMPTY);

		EOFException e = assertThrows(EOFException.class, () -> UInt64.from(bis));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + UInt64.BYTES + " more bytes out of " + UInt64.BYTES));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasLessElements() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18 });

		EOFException e = assertThrows(EOFException.class, () -> UInt64.from(bis));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + (UInt64.BYTES - 7) + " more bytes out of " + UInt64.BYTES));
	}

	@Test
	void shouldReturnTheCorrectSize() {
		assertThat(UInt64.BYTES, equalTo(8));
		assertThat(UInt64.ZERO.sizeOf(), equalTo(UInt64.BYTES));
	}
}
