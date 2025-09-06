package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		assertThat(e.getMessage(), equalTo("Error reading " + UInt8.BYTES + " bytes"));
	}
}
