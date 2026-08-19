package org.apiphany.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link BinarySerializers}.
 *
 * @author Radu Sebastian LAZIN
 */
class BinarySerializersTest {

	@Nested
	class ToByteArrayTests {

		@Test
		void shouldReturnEmptyArrayForEmptyList() {
			List<BytesWrapper> list = Collections.emptyList();

			byte[] result = BinarySerializers.toByteArray(list);

			assertArrayEquals(new byte[0], result);
		}

		@Test
		void shouldReturnSingleElementBytes() {
			byte[] bytes = new byte[] { 1, 2, 3 };
			BytesWrapper wrapper = new BytesWrapper(bytes);
			List<BytesWrapper> list = List.of(wrapper);

			byte[] result = BinarySerializers.toByteArray(list);

			assertArrayEquals(bytes, result);
		}

		@Test
		void shouldConcatenateMultipleElements() {
			BytesWrapper w1 = new BytesWrapper(new byte[] { 1, 2 });
			BytesWrapper w2 = new BytesWrapper(new byte[] { 3, 4, 5 });
			BytesWrapper w3 = new BytesWrapper(new byte[] { 6 });
			List<BytesWrapper> list = List.of(w1, w2, w3);

			byte[] result = BinarySerializers.toByteArray(list);

			assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6 }, result);
		}

		@Test
		void shouldWorkWithDifferentBinaryRepresentableTypes() {
			UInt8 u8 = new UInt8((byte) 0x0A);
			List<UInt8> list = List.of(u8);

			byte[] result = BinarySerializers.toByteArray(list);

			assertThat(result.length, equalTo(u8.sizeOf()));
			assertArrayEquals(new byte[] { 0x0A }, result);
		}

		@Test
		void shouldPreAllocateBufferUsingSizeOf() {
			BytesWrapper w1 = new BytesWrapper(new byte[] { 10, 20, 30 });
			BytesWrapper w2 = new BytesWrapper(new byte[] { 40, 50 });
			List<BytesWrapper> list = List.of(w1, w2);

			byte[] result = BinarySerializers.toByteArray(list);

			assertThat(result.length, equalTo(5));
		}

		@Test
		void shouldHandleSingleElementListWithMutableInput() {
			byte[] original = new byte[] { 7, 8, 9 };
			BytesWrapper wrapper = new BytesWrapper(original);
			List<BytesWrapper> list = new ArrayList<>();
			list.add(wrapper);

			byte[] result = BinarySerializers.toByteArray(list);

			assertArrayEquals(original, result);
		}
	}
}
