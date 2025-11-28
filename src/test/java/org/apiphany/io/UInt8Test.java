package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.EOFException;

import org.apiphany.lang.Bytes;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link UInt8}.
 *
 * @author Radu Sebastian LAZIN
 */
class UInt8Test {

	private static final byte BYTE_66 = 66;
	private static final byte BYTE_42 = 42;

	@Test
	void shouldReturnAsStringTheInternalValue() {
		byte value = 0x13;
		UInt8 uInt8 = UInt8.of(value);

		String expected = String.valueOf(value);
		String result = uInt8.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTrueWhenComparingToZeroStaticObject() {
		UInt8 zero = UInt8.ZERO;

		assertTrue(zero.isZero());
	}

	@Test
	void shouldReturnTrueWhenComparingToNewZeroObject() {
		UInt8 zero = UInt8.of((byte) 0);

		assertTrue(zero.isZero());
	}

	@Test
	void shoudReturnFalseOnNonZeroElements() {
		UInt8 nonZero = UInt8.of((byte) 42);

		assertFalse(nonZero.isZero());
	}

	@Test
	void shouldThrowExceptionWhenInputStreamIsEmpty() {
		ByteArrayInputStream bis = new ByteArrayInputStream(Bytes.EMPTY);

		EOFException e = assertThrows(EOFException.class, () -> UInt8.from(bis));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + UInt8.BYTES + " more bytes out of " + UInt8.BYTES));
	}

	@Test
	void shouldBeEqualWhenSameInstance() {
		UInt8 value = UInt8.of(BYTE_66);

		assertThat(value.equals(value), is(true));
		assertThat(value.hashCode(), equalTo(value.hashCode()));
	}

	@Test
	void shouldBeEqualWhenUnsignedValuesMatch() {
		// 255 unsigned
		UInt8 a = UInt8.of((byte) -1);
		// narrowing to byte also -1
		UInt8 b = UInt8.of((byte) 255);

		assertThat(a.equals(b), is(true));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	void shouldNotBeEqualWhenUnsignedValuesDiffer() {
		UInt8 a = UInt8.of((byte) 0);
		UInt8 b = UInt8.of((byte) 1);

		assertThat(a.equals(b), is(false));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	void shouldNotBeEqualToNull() {
		UInt8 value = UInt8.of(BYTE_42);

		assertThat(value.equals(null), is(false));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	void shouldNotBeEqualToDifferentType() {
		UInt8 value = UInt8.of(BYTE_42);

		assertThat(value.equals("42"), is(false));
	}

	@Test
	void shouldHaveConsistentHashCodeForEqualValues() {
		UInt8 a = UInt8.of(BYTE_66);
		UInt8 b = UInt8.of(BYTE_66);

		// they wrap to the same unsigned value
		assertThat(a.equals(b), is(true));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	void shouldHandleBoundaryValuesCorrectly() {
		// 0 unsigned
		UInt8 min = UInt8.of((byte) 0);
		// 255 unsigned
		UInt8 max = UInt8.of((byte) -1);

		assertThat(min.toUnsignedInt(), equalTo(0));
		assertThat(max.toUnsignedInt(), equalTo(255));

		assertThat(min.equals(max), is(false));
	}
}
