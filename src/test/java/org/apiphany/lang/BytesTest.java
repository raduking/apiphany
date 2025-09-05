package org.apiphany.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;

/**
 * Test class for {@link Bytes}.
 *
 * @author Radu Sebastian LAZIN
 */
class BytesTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		ReflectionException reflectionException = assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(Bytes.class));
		InvocationTargetException invocationTargetException = JavaObjects.cast(reflectionException.getCause());
		UnsupportedOperationException unsupportedOperationException = JavaObjects.cast(invocationTargetException.getCause());
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

	public static byte[] generateByteArray(final int n) {
		byte[] result = new byte[n];
		for (int i = 0; i < n; i++) {
			result[i] = (byte) (i + 1);
		}
		return result;
	}
}
