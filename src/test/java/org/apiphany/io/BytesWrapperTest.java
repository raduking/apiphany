package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link BytesWrapper}.
 *
 * @author Radu Sebastian LAZIN
 */
class BytesWrapperTest {

	private static final int SIZE = 5;

	@Test
	void shouldInitializeTheBytesWithZeroWhenInstantiatedWithSize() {
		BytesWrapper bytesWrapper = new BytesWrapper(SIZE);

		byte[] bytes = bytesWrapper.toByteArray();

		assertThat(bytes.length, equalTo(SIZE));
		for (byte b : bytes) {
			assertThat(b, equalTo((byte) 0));
		}
	}

	@Test
	void shouldInitializeTheBytesWithZeroWhenInstantiatedWithSizeAndCheckForEquality() {
		BytesWrapper bytesWrapper1 = new BytesWrapper(SIZE);
		BytesWrapper bytesWrapper2 = new BytesWrapper(new byte[SIZE]);

		boolean equal = bytesWrapper1.equals(bytesWrapper2);

		assertTrue(equal);
		assertThat(bytesWrapper1.hashCode(), equalTo(bytesWrapper2.hashCode()));
	}
}
