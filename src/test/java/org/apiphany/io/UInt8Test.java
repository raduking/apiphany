package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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

}
