package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.EOFException;

import org.apiphany.lang.Bytes;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link UInt16}.
 *
 * @author Radu Sebastian LAZIN
 */
class UInt16Test {

	private static final short SHORT_666 = 666;
	private static final short SHORT_42 = 666;

	@Test
	void shouldReturnAsStringTheInternalValue() {
		UInt16 uInt16 = UInt16.of(SHORT_666);

		String expected = String.valueOf(SHORT_666);
		String result = uInt16.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnAsStringTheInternalValueAsUnsigned() {
		short value = -1;
		UInt16 uInt16 = UInt16.of(value);

		String expected = String.valueOf(65535);
		String result = uInt16.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldBeEqualWhenSameInstance() {
		UInt16 value = UInt16.of(SHORT_666);

		assertThat(value.equals(value), is(true));
		assertThat(value.hashCode(), equalTo(value.hashCode()));
	}

	@Test
	void shouldBeEqualWhenUnsignedValuesMatch() {
		// 65535 unsigned
		UInt16 a = UInt16.of((short) -1);
		// narrowing to short also -1
		UInt16 b = UInt16.of((short) 65535);

		assertThat(a.equals(b), is(true));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	void shouldNotBeEqualWhenUnsignedValuesDiffer() {
		UInt16 a = UInt16.of((short) 0);
		UInt16 b = UInt16.of((short) 1);

		assertThat(a.equals(b), is(false));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	void shouldNotBeEqualToNull() {
		UInt16 value = UInt16.of(SHORT_42);

		assertThat(value.equals(null), is(false));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	void shouldNotBeEqualToDifferentType() {
		UInt16 value = UInt16.of(SHORT_42);

		assertThat(value.equals("42"), is(false));
	}

	@Test
	void shouldHaveConsistentHashCodeForEqualValues() {
		UInt16 a = UInt16.of(SHORT_666);
		UInt16 b = UInt16.of(SHORT_666);

		// They wrap to the same unsigned value
		assertThat(a.equals(b), is(true));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	void shouldHandleBoundaryValuesCorrectly() {
		// 0 unsigned
		UInt16 min = UInt16.of((short) 0);
		// 65535 unsigned
		UInt16 max = UInt16.of((short) -1);

		assertThat(min.toUnsignedInt(), equalTo(0));
		assertThat(max.toUnsignedInt(), equalTo(65535));

		assertThat(min.equals(max), is(false));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamIsEmpty() {
		ByteArrayInputStream bis = new ByteArrayInputStream(Bytes.EMPTY);

		EOFException e = assertThrows(EOFException.class, () -> UInt16.from(bis));
		assertThat(e.getMessage(), equalTo("Error reading " + UInt16.BYTES + " bytes"));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasLessElements() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12 });

		EOFException e = assertThrows(EOFException.class, () -> UInt16.from(bis));
		assertThat(e.getMessage(), equalTo("Error reading " + UInt16.BYTES + " bytes"));
	}
}
