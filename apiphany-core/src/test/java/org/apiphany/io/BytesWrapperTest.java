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
	void shouldReturnEmptyInstanceWhenFromIsCalledWithZeroSize() throws Exception {
		@SuppressWarnings("resource")
		InputStream is = mock(InputStream.class);

		BytesWrapper bytesWrapper = BytesWrapper.from(is, 0);

		assertThat(bytesWrapper, equalTo(BytesWrapper.empty()));
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
	void shouldBeEqualWhenWrappedBytesAreTheSame() {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		BytesWrapper bytesWrapper1 = new BytesWrapper(bytes);
		BytesWrapper bytesWrapper2 = new BytesWrapper(bytes);

		boolean equal = bytesWrapper1.equals(bytesWrapper2);

		assertTrue(equal);
		assertThat(bytesWrapper1.hashCode(), equalTo(bytesWrapper2.hashCode()));
	}

	@Test
	void shouldNotBeEqualToAnotherObject() {
		BytesWrapper bytesWrapper = new BytesWrapper(SIZE);

		boolean equal = bytesWrapper.equals(new Object());

		assertFalse(equal);
	}

	@Test
	void shouldNotBeEqualToNull() {
		BytesWrapper bytesWrapper = new BytesWrapper(SIZE);

		boolean equal = bytesWrapper.equals(null);

		assertFalse(equal);
	}

	@Test
	void shouldReturnEmptyInstanceWhenRequested() {
		BytesWrapper empty = BytesWrapper.empty();

		assertThat(empty.sizeOf(), equalTo(0));
	}

	@Test
	void shouldReturnCorrectSizeOfWrappedBytes() {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		BytesWrapper bytesWrapper = new BytesWrapper(bytes);

		int size = bytesWrapper.sizeOf();

		assertThat(size, equalTo(bytes.length));
	}

	@Test
	void shouldIdentifyAsEmptyWhenNoBytesWrapped() {
		BytesWrapper emptyBytesWrapper = BytesWrapper.empty();

		assertTrue(emptyBytesWrapper.isEmpty());
	}

	@Test
	void shouldIdentifyAsNonEmptyWhenBytesAreWrapped() {
		byte[] bytes = new byte[] { 1, 2, 3 };
		BytesWrapper bytesWrapper = new BytesWrapper(bytes);

		assertFalse(bytesWrapper.isEmpty());
	}

	@Test
	void shouldReturnHexStringRepresentation() {
		byte[] bytes = new byte[] { (byte) 0xAB, (byte) 0xCD, (byte) 0xEF };
		BytesWrapper bytesWrapper = new BytesWrapper(bytes);

		String hexString = bytesWrapper.toString();

		assertThat(hexString, equalTo("ABCDEF"));
	}

	@Test
	void shouldReturnCopyOfWrappedBytes() {
		byte[] originalBytes = new byte[] { 1, 2, 3, 4, 5 };
		BytesWrapper bytesWrapper = new BytesWrapper(originalBytes);

		byte[] copiedBytes = bytesWrapper.toByteArray();

		assertThat(copiedBytes.length, equalTo(originalBytes.length));
		for (int i = 0; i < originalBytes.length; ++i) {
			assertThat(copiedBytes[i], equalTo(originalBytes[i]));
		}

		// modify the copied array and ensure the original is unaffected
		copiedBytes[0] = 99;
		assertThat(originalBytes[0], equalTo((byte) 1));
	}

	@Test
	void shouldReturnEmptyArrayWhenToByteArrayCalledOnEmptyInstance() {
		BytesWrapper emptyBytesWrapper = BytesWrapper.empty();

		byte[] bytes = emptyBytesWrapper.toByteArray();

		assertThat(bytes.length, equalTo(0));
	}

	@Test
	void shouldCreateBytesWrapperWithOffsetAndSize() {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		int offset = 1;
		int size = 3;
		BytesWrapper bytesWrapper = new BytesWrapper(bytes, offset, size);

		byte[] wrappedBytes = bytesWrapper.toByteArray();

		for (int i = offset; i < size + offset; ++i) {
			assertThat(wrappedBytes[i - offset], equalTo(bytes[i]));
		}
	}

	@Test
	void shouldCreateBytesWrapperWithOffsetAndSizeGreaterThanLength() {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		int offset = 1;
		int size = bytes.length * 2;
		BytesWrapper bytesWrapper = new BytesWrapper(bytes, offset, size);

		byte[] wrappedBytes = bytesWrapper.toByteArray();

		for (int i = offset; i < size + offset; ++i) {
			if (i >= bytes.length) {
				assertThat(wrappedBytes[i - offset], equalTo((byte) 0));
			} else {
				assertThat(wrappedBytes[i - offset], equalTo(bytes[i]));
			}
		}
	}

	@Test
	void shouldCreateSliceBytesWrapperWithOffsetAndSize() {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		BytesWrapper originalWrapper = new BytesWrapper(bytes);
		int offset = 1;
		int size = 3;
		BytesWrapper bytesWrapper = originalWrapper.slice(offset, size);

		byte[] wrappedBytes = bytesWrapper.toByteArray();

		for (int i = offset; i < size + offset; ++i) {
			assertThat(wrappedBytes[i - offset], equalTo(bytes[i]));
		}
	}

	@Test
	void shouldCreateSliceBytesWrapperWithOffsetAndSizeGreaterThanLength() {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		BytesWrapper originalWrapper = new BytesWrapper(bytes);
		int offset = 1;
		int size = bytes.length * 2;
		BytesWrapper bytesWrapper = originalWrapper.slice(offset, size);

		byte[] wrappedBytes = bytesWrapper.toByteArray();

		for (int i = offset; i < size + offset; ++i) {
			if (i >= bytes.length) {
				assertThat(wrappedBytes[i - offset], equalTo((byte) 0));
			} else {
				assertThat(wrappedBytes[i - offset], equalTo(bytes[i]));
			}
		}
	}
}
