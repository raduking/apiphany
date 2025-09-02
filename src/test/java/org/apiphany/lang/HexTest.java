package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apiphany.io.BytesWrapper;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.MemberAccessor;

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

	private static final String HEX = """
			01 02 03 04 01 02 03 04  01 02 03 04 01 02 03 04
			01 02 03 04 01 02 03 04  01 02 03 04 01 02 03 04
			""";

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateClass() throws Exception {
		Throwable targetException = null;
		Constructor<Hex> defaultConstructor = Hex.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<Hex>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

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
	void shouldConvertBytesToHexDumpWith16BytesAlignment() {
		String expected = HEX;
		String result = Hex.dump(BYTES);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldConvertBytesToHexDump() {
		byte[] bytes = Arrays.copyOf(BYTES, 28);

		String expected = """
				01 02 03 04 01 02 03 04  01 02 03 04 01 02 03 04
				01 02 03 04 01 02 03 04  01 02 03 04
				""";
		String result = Hex.dump(bytes, false);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldConvertBytesToHexDumpWithVerbosityWith16ByteAlignment() {
		String expected = """
				0000: 01 02 03 04 01 02 03 04   01 02 03 04 01 02 03 04  ................
				0010: 01 02 03 04 01 02 03 04   01 02 03 04 01 02 03 04  ................
				""";
		String result = Hex.dump(BYTES, true);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldConvertBytesToHexDumpWithVerbosity() {
		byte[] bytes = Arrays.copyOf(BYTES, 28);
		bytes[0] = 0x41;
		bytes[bytes.length - 4] = 0x61;
		bytes[bytes.length - 1] = (byte) 0x80;

		String expected = """
				0000: 41 02 03 04 01 02 03 04   01 02 03 04 01 02 03 04  A...............
				0010: 01 02 03 04 01 02 03 04   61 02 03 80              ........a...
				""";
		String result = Hex.dump(bytes, true);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnNullForEmptyNullBytes() {
		String result = Hex.dump((byte[]) null);

		assertThat(result, equalTo("null"));
	}

	@Test
	void shouldConvertBinaryRepresentableToHexDump() throws IOException {
		String expected = HEX;
		BytesWrapper bytes = BytesWrapper.from(new ByteArrayInputStream(BYTES), BYTES.length);
		String result = Hex.dump(bytes);

		assertThat(result, equalTo(expected));
	}
}
