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
class UInt32Test {

	private static final int TEST_INT = 666;

	@Test
	void shouldWriteAndReadUInt24() throws IOException {
		UInt32 uInt32 = UInt32.of(TEST_INT);

		byte[] bytes = uInt32.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

		UInt32 result = UInt32.from(bis);

		assertThat(result.getValue(), equalTo(uInt32.getValue()));
	}

}
