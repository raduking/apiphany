package org.apiphany.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link BytesOrder}.
 *
 * @author Radu Sebastian LAZIN
 */
class BytesOrderTest {

	@ParameterizedTest
	@EnumSource(BytesOrder.class)
	void shouldGetBytesOrderValueFromString(BytesOrder bytesOrder) {
		assertEquals(bytesOrder, BytesOrder.fromString(bytesOrder.name()));
	}

}
