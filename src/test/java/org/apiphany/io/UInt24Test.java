package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
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

	private static final int TEST_INT = 666;

	@Test
	void shouldWriteAndReadUInt24() throws IOException {
		UInt24 uInt24 = UInt24.of(TEST_INT);

		byte[] bytes = uInt24.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

		UInt24 result = UInt24.from(bis);

		assertThat(result.getValue(), equalTo(uInt24.getValue()));
	}

	@Test
	void shouldReturnAsStringTheInternalValue() {
		UInt24 uInt24 = UInt24.of(TEST_INT);

		String expected = String.valueOf(TEST_INT);
		String result = uInt24.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamIsEmpty() {
		ByteArrayInputStream bis = new ByteArrayInputStream(Bytes.EMPTY);

		EOFException e = assertThrows(EOFException.class, () -> UInt24.from(bis));
		assertThat(e.getMessage(), equalTo("Error reading " + UInt24.BYTES + " bytes"));
	}

	@Test
	void shouldThrowExceptionWhenInputStreamHasLessElements() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 0x12, 0x13 });

		EOFException e = assertThrows(EOFException.class, () -> UInt24.from(bis));
		assertThat(e.getMessage(), equalTo("Error reading " + UInt24.BYTES + " bytes"));
	}
}
