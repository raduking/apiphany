package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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

}
