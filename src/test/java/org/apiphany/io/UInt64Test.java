package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link UInt32}.
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

		assertThat(result.getValue(), equalTo(uInt64.getValue()));
	}

}
