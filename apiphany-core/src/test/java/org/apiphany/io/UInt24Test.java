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
 * Test class for {@link UInt24}.
 *
 * @author Radu Sebastian LAZIN
 */
class UInt24Test {

	private static final int INT_42 = 42;
	private static final int INT_666 = 666;

	@Test
	void shouldWriteAndReadUInt24() throws IOException {
		UInt24 uInt24 = UInt24.of(INT_666);

		byte[] bytes = uInt24.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

		UInt24 result = UInt24.from(bis);

		assertThat(result.getValue(), equalTo(uInt24.getValue()));
	}

	@Test
	void shouldReturnAsStringTheInternalValue() {
		UInt24 uInt24 = UInt24.of(INT_666);

		String expected = String.valueOf(INT_666);
		String result = uInt24.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamIsEmpty() {
		ByteArrayInputStream bis = new ByteArrayInputStream(Bytes.EMPTY);

		EOFException e = assertThrows(EOFException.class, () -> UInt24.from(bis));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + UInt24.BYTES + " more bytes out of " + UInt24.BYTES));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasTwoLessElements() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12 });

		EOFException e = assertThrows(EOFException.class, () -> UInt24.from(bis));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + (UInt24.BYTES - 1) + " more bytes out of " + UInt24.BYTES));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasOneLessElement() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12, 0x13 });

		EOFException e = assertThrows(EOFException.class, () -> UInt24.from(bis));
		assertThat(e.getMessage(), equalTo("Stream closed, need " + (UInt24.BYTES - 2) + " more bytes out of " + UInt24.BYTES));
	}

	@Test
	void shouldBeEqualWhenSameInstance() {
		UInt24 value = UInt24.of(123456);

		assertThat(value.equals(value), is(true));
		assertThat(value.hashCode(), equalTo(value.hashCode()));
	}

	@Test
	void shouldBeEqualWhenValuesMatch() {
		UInt24 a = UInt24.of(0x00FF_FF); // 65535
		UInt24 b = UInt24.of(65535);

		assertThat(a.equals(b), is(true));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	void shouldNotBeEqualWhenValuesDiffer() {
		UInt24 a = UInt24.of(0);
		UInt24 b = UInt24.of(1);

		assertThat(a.equals(b), is(false));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	void shouldNotBeEqualToNull() {
		UInt24 value = UInt24.of(INT_42);

		assertThat(value.equals(null), is(false));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	void shouldNotBeEqualToDifferentType() {
		UInt24 value = UInt24.of(INT_42);

		assertThat(value.equals("42"), is(false));
	}

	@Test
	void shouldHaveConsistentHashCodeForEqualValues() {
		UInt24 a = UInt24.of(INT_666);
		UInt24 b = UInt24.of(INT_666);

		assertThat(a.equals(b), is(true));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	void shouldHandleBoundaryValuesCorrectly() {
		UInt24 min = UInt24.of(0);
		UInt24 max = UInt24.of(UInt24.MAX_VALUE);

		assertThat(min.toUnsignedInt(), equalTo(0));
		assertThat(max.toUnsignedInt(), equalTo(16_777_215));

		assertThat(min.equals(max), is(false));
	}
}
