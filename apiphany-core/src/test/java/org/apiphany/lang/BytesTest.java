package org.apiphany.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Bytes}.
 *
 * @author Radu Sebastian LAZIN
 */
class BytesTest {

	private static final byte BYTE_ZERO = (byte) 0;
	private static final byte BYTE_ONE = (byte) 1;

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Bytes.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldConcatenateByteArrays() {
		int n1 = 10;
		byte[] a1 = generateByteArray(n1);
		int n2 = 7;
		byte[] a2 = generateByteArray(n2);

		byte[] c = Bytes.concatenate(a1, a2);

		for (int i = 0; i < n1; ++i) {
			assertEquals(c[i], (byte) (i + 1));
		}
		for (int i = 0; i < n2; ++i) {
			assertEquals(c[i + n1], (byte) (i + 1));
		}
	}

	@Test
	void shouldNotConcatenateNullArray() {
		int n1 = 10;
		byte[] a1 = generateByteArray(n1);

		byte[] c = Bytes.concatenate(a1, null);

		for (int i = 0; i < n1; ++i) {
			assertEquals(c[i], (byte) (i + 1));
		}
		assertThat(c.length, equalTo(n1));
	}

	@Test
	void shouldThrowExceptionIfFromHexHasAnUnEvenArgument() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHex("AA B"));

		assertThat(e.getMessage(), equalTo("Hex string must have an even number of characters (after whitespace removal)"));
	}

	@Test
	void shouldThrowExceptionOnPadRightIfBlockSizeIsLessThanZero() {
		IllegalArgumentException e =
				assertThrows(IllegalArgumentException.class, () -> Bytes.padRightToBlockSize(Bytes.EMPTY, -66, BYTE_ZERO, false));

		assertThat(e.getMessage(), equalTo("Block size must be greater than zero"));
	}

	@Test
	void shouldThrowExceptionOnPadRightIfBlockSizeIsEqualToZero() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Bytes.padRightToBlockSize(Bytes.EMPTY, 0, BYTE_ZERO, false));

		assertThat(e.getMessage(), equalTo("Block size must be greater than zero"));
	}

	@Test
	void shouldNotPadOnPadRightIfBlockIsAlignedAndExtendIsFalse() {
		int n = 10;
		byte[] a = generateByteArray(n);

		byte[] result = Bytes.padRightToBlockSize(a, n, BYTE_ZERO);

		assertThat(a, equalTo(result));
	}

	@Test
	void shouldPadOnPadRightToBlockSizeMultipleWithGivenByte() {
		int n = 10;
		int blockSize = 4;
		byte[] a = generateByteArray(n);

		byte[] result = Bytes.padRightToBlockSize(a, blockSize, BYTE_ONE);

		assertThat(result.length, equalTo(n + blockSize - (n % blockSize)));
		for (int i = n; i < result.length; ++i) {
			assertThat(result[i], equalTo(BYTE_ONE));
		}
	}

	@Test
	void shouldPadOnPadRightToBlockSizeMultipleWithZero() {
		int n = 10;
		int blockSize = 4;
		byte[] a = generateByteArray(n);

		byte[] result = Bytes.padRightToBlockSize(a, blockSize, BYTE_ZERO);

		assertThat(result.length, equalTo(n + blockSize - (n % blockSize)));
		for (int i = n; i < result.length; ++i) {
			assertThat(result[i], equalTo(BYTE_ZERO));
		}
	}

	public static byte[] generateByteArray(final int n) {
		byte[] result = new byte[n];
		for (int i = 0; i < n; i++) {
			result[i] = (byte) (i + 1);
		}
		return result;
	}
}
