package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Hex}.
 *
 * @author Radu Sebastian LAZIN
 */
class HexTest {

	private static final byte[] BYTES = new byte[] {
			0x01, 0x02, 0x03, 0x04,
			0x01, 0x02, 0x03, 0x04,
			0x01, 0x02, 0x03, 0x04,
			0x01, 0x02, 0x03, 0x04,
			0x01, 0x02, 0x03, 0x04,
			0x01, 0x02, 0x03, 0x04,
			0x01, 0x02, 0x03, 0x04,
			0x01, 0x02, 0x03, 0x04,
	};

	@Test
	void shouldConvertBytesToHexString() {
		byte[] bytes = new byte[] {
				0x01, 0x02, 0x03, 0x04
		};

		String expected = "01 02 03 04";
		String result = Hex.string(bytes);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldConvertBytesToHexDump() {
		String expected = """
				01 02 03 04 01 02 03 04  01 02 03 04 01 02 03 04
				01 02 03 04 01 02 03 04  01 02 03 04 01 02 03 04
				""";
		String result = Hex.dump(BYTES);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldConvertBytesToHexDumpWithoutVerbosity() {
		String expected = """
				0000: 01 02 03 04 01 02 03 04   01 02 03 04 01 02 03 04  ................
				0010: 01 02 03 04 01 02 03 04   01 02 03 04 01 02 03 04  ................
				""";
		String result = Hex.dump(BYTES, true);

		assertThat(result, equalTo(expected));
	}
}
