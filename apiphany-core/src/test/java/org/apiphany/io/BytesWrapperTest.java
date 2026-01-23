package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.nio.ByteBuffer;

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

	@Test
	void shouldThrowExceptionOnFromIfBytesToReadIsNegative() {
		@SuppressWarnings("resource")
		InputStream is = mock(InputStream.class);
		int size = -1;

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> BytesWrapper.from(is, size));

		assertThat(e.getMessage(), equalTo("Size cannot be negative: " + size));
	}

	@Test
	void shouldConvertToReadOnlyByteBuffer() {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		BytesWrapper bytesWrapper = new BytesWrapper(bytes);

		ByteBuffer byteBuffer = bytesWrapper.toByteBuffer();

		assertTrue(byteBuffer.isReadOnly());
		for (int i = 0; i < bytes.length; ++i) {
			byte b = byteBuffer.get();
			assertThat(b, equalTo(bytes[i]));
		}
	}

	@Test
	void shouldBeEqualToSelf() {
		BytesWrapper bytesWrapper = new BytesWrapper(SIZE);

		boolean equal = bytesWrapper.equals(bytesWrapper);

		assertTrue(equal);
	}

	@Test
	void shouldNotBeEqualToAnother() {
		byte[] bytes1 = new byte[] { 1, 2, 3, 4, 5 };
		BytesWrapper bytesWrapper1 = new BytesWrapper(bytes1);
		byte[] bytes2 = new byte[] { 3, 2, 1 };
		BytesWrapper bytesWrapper2 = new BytesWrapper(bytes2);

		boolean equal = bytesWrapper1.equals(bytesWrapper2);

		assertFalse(equal);
	}

	@Test
	void shouldNotBeEqualToAnotherObject() {
		BytesWrapper bytesWrapper = new BytesWrapper(SIZE);

		boolean equal = bytesWrapper.equals(new Object());

		assertFalse(equal);
	}
}
