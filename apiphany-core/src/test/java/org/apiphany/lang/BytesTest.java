package org.apiphany.lang;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
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
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Bytes.class);
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

	@Test
	void shouldPadOnPadRightIfBlockIsAlignedAndExtendIsTrue() {
		int n = 10;
		byte[] a = generateByteArray(n);

		byte[] result = Bytes.padRightToBlockSize(a, n, BYTE_ZERO, true);

		assertThat(result.length, equalTo(n * 2));
		for (int i = n; i < result.length; ++i) {
			assertThat(result[i], equalTo(BYTE_ZERO));
		}
	}

	@Test
	void shouldReturnTrueOnIsEmptyForNullArray() {
		assertThat(Bytes.isEmpty(null), equalTo(true));
	}

	@Test
	void shouldReturnTrueOnIsEmptyForEmptyArray() {
		assertThat(Bytes.isEmpty(Bytes.EMPTY), equalTo(true));
	}

	@Test
	void shouldReturnTrueOnIsEmptyForNewEmptyArray() {
		assertThat(Bytes.isEmpty(new byte[] { }), equalTo(true));
	}

	@Test
	void shouldReturnFalseOnIsEmptyForNonEmptyArray() {
		byte[] a = generateByteArray(5);

		assertThat(Bytes.isEmpty(a), equalTo(false));
	}

	@Test
	void shouldReturnFalseOnIsNotEmptyForNullArray() {
		assertThat(Bytes.isNotEmpty(null), equalTo(false));
	}

	@Test
	void shouldReturnFalseOnIsNotEmptyForEmptyArray() {
		assertThat(Bytes.isNotEmpty(Bytes.EMPTY), equalTo(false));
	}

	@Test
	void shouldReturnFalseOnIsNotEmptyForNewEmptyArray() {
		assertThat(Bytes.isNotEmpty(new byte[] { }), equalTo(false));
	}

	@Test
	void shouldReturnTrueOnIsNotEmptyForNonEmptyArray() {
		byte[] a = generateByteArray(5);

		assertThat(Bytes.isNotEmpty(a), equalTo(true));
	}

	@Test
	void shouldReturnEmptyArrayWhenFromHexIsCalledWithEmptyString() {
		byte[] result = Bytes.fromHex("");

		assertThat(result, equalTo(Bytes.EMPTY));
	}

	@Test
	void shouldReturnEmptyArrayWhenFromHexIsCalledWithWhitespaceString() {
		byte[] result = Bytes.fromHex("    \n\t  ");

		assertThat(result, equalTo(Bytes.EMPTY));
	}

	@Test
	void shouldThrowExceptionWhenFromHexIsCalledWithNullString() {
		NullPointerException e = assertThrows(NullPointerException.class, () -> Bytes.fromHex(null));

		assertThat(e.getMessage(), equalTo("Hex string cannot be null"));
	}

	@Test
	void shouldThrowExceptionWhenReverseIsCalledWithNullArray() {
		NullPointerException e = assertThrows(NullPointerException.class, () -> Bytes.reverse(null));

		assertThat(e.getMessage(), equalTo("Byte array cannot be null"));
	}

	@Test
	void shouldReverseByteArray() {
		byte[] input = new byte[] { 1, 2, 3, 4, 5 };
		byte[] expected = new byte[] { 5, 4, 3, 2, 1 };

		byte[] result = Bytes.reverse(input);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldPadPKCS7() {
		byte[] input = new byte[] { 1, 2, 3, 4, 5 };
		byte padByte = 10;
		int blockSize = 16;
		byte[] expected =
				new byte[] { 1, 2, 3, 4, 5, padByte, padByte, padByte, padByte, padByte, padByte, padByte, padByte, padByte, padByte, padByte };
		byte[] result = Bytes.padPKCS7(input, blockSize);

		assertThat(result, equalTo(expected));
	}

	/**
	 * Tests for
	 * <ul>
	 * <li>{@link Bytes#fromFile(String)}</li>
	 * <li>{@link Bytes#fromFile(String, Consumer)}</li>
	 * </ul>
	 */
	@Nested
	class FromFileTests {

		/**
		 * To easily create a binary file with this content run the following command once:
		 *
		 * <pre>
		 * echo "000102030405060708090A0B0C0D0E0F" | xxd -r -p > binary-file.bin
		 * </pre>
		 */
		private static final byte[] BINARY_FILE_CONTENT = new byte[] {
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
		};

		private static final String BINARY_FILE_NAME = "binary-file.bin";
		private static final String WRONG_FILE_NAME = "non-existing-file.bin";

		@Test
		void shouldReturnBytesFromFile() {
			byte[] result = Bytes.fromFile(BINARY_FILE_NAME);

			assertThat(result, equalTo(BINARY_FILE_CONTENT));
		}

		@Test
		void shouldReturnStringFromAbsolutePathFile() {
			String currentDir = Paths.get("").toAbsolutePath().toString();
			byte[] result = Bytes.fromFile(currentDir + "/src/test/resources/" + BINARY_FILE_NAME);

			assertThat(result, equalTo(BINARY_FILE_CONTENT));
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenFromStringThrowsExceptionWhenCalledWithNameCharsetSize() {
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = e -> {
				runnable.run();
				assertThat(e, instanceOf(NoSuchFileException.class));
			};

			byte[] result = Bytes.fromFile("/" + WRONG_FILE_NAME, onError);

			assertThat(result, equalTo(Bytes.EMPTY));
			verify(runnable).run();
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenFromStringThrowsExceptionWhenCalledWithClasspathPath() {
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = e -> {
				runnable.run();
				assertThat(e, instanceOf(FileNotFoundException.class));
				assertThat(e.getMessage(), equalTo("Classpath resource not found: " + WRONG_FILE_NAME));
			};

			byte[] result = Bytes.fromFile(WRONG_FILE_NAME, onError);

			assertThat(result, equalTo(Bytes.EMPTY));
			verify(runnable).run();
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
