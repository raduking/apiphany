package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link UInt16}.
 *
 * @author Radu Sebastian LAZIN
 */
class UInt16Test {

	@Test
	void shouldReturnAsStringTheInternalValue() {
		short value = 666;
		UInt16 uInt16 = UInt16.of(value);

		String expected = String.valueOf(value);
		String result = uInt16.toString();

		assertThat(result, equalTo(expected));
	}
}
