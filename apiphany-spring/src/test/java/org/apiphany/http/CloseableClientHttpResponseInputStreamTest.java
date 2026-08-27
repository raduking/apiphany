package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Tests for {@link CloseableClientHttpResponseInputStream}.
 *
 * @author Radu Sebastian LAZIN
 */
class CloseableClientHttpResponseInputStreamTest {

	private static final byte[] DATA = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };

	@Nested
	class FactoryMethodTests {

		@Test
		@SuppressWarnings("resource")
		void shouldCreateFromValidResponse() throws Exception {
			ClientHttpResponse response = mockResponse(DATA);

			CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(response);

			assertNotNull(stream);
			stream.close();
		}

		@Test
		void shouldThrowOnNullResponse() {
			assertThrows(NullPointerException.class, () -> CloseableClientHttpResponseInputStream.of(null));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldThrowOnNullBody() throws Exception {
			ClientHttpResponse response = mock(ClientHttpResponse.class);
			when(response.getBody()).thenReturn(null);

			assertThrows(NullPointerException.class, () -> CloseableClientHttpResponseInputStream.of(response));
		}
	}

	@Nested
	class ReadTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReadSingleByte() throws Exception {
			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(DATA))) {
				assertThat(stream.read(), equalTo(0x01));
				assertThat(stream.read(), equalTo(0x02));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReadByteArray() throws Exception {
			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(DATA))) {
				byte[] buf = new byte[DATA.length];
				int read = stream.read(buf);

				assertThat(read, equalTo(DATA.length));
				assertArrayEquals(DATA, buf);
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReadByteArrayWithOffset() throws Exception {
			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(DATA))) {
				byte[] buf = new byte[5];
				int read = stream.read(buf, 2, 3);

				assertThat(read, equalTo(3));
				assertThat(buf[0], equalTo((byte) 0));
				assertThat(buf[1], equalTo((byte) 0));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReadAllBytes() throws Exception {
			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(DATA))) {
				byte[] result = stream.readAllBytes();

				assertArrayEquals(DATA, result);
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReadNBytes() throws Exception {
			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(DATA))) {
				byte[] result = stream.readNBytes(3);

				assertThat(result.length, equalTo(3));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReadNBytesIntoBuffer() throws Exception {
			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(DATA))) {
				byte[] buf = new byte[10];
				int read = stream.readNBytes(buf, 0, 3);

				assertThat(read, equalTo(3));
			}
		}
	}

	@Nested
	class SkipAndAvailableTests {

		@Test
		@SuppressWarnings("resource")
		void shouldSkipBytes() throws Exception {
			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(DATA))) {
				long skipped = stream.skip(2);

				assertThat(skipped, equalTo(2L));
				assertThat(stream.read(), equalTo(0x03));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnAvailableBytes() throws Exception {
			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(DATA))) {
				assertThat(stream.available(), equalTo(DATA.length));
			}
		}
	}

	@Nested
	class CloseTests {

		@Test
		@SuppressWarnings("resource")
		void shouldCloseResponse() throws Exception {
			ClientHttpResponse response = mockResponse(DATA);
			CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(response);

			stream.close();

			verify(response).close();
		}
	}

	@Nested
	class EndToEndTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReadCompleteContent() throws Exception {
			byte[] testData = "Hello, World!".getBytes();

			try (CloseableClientHttpResponseInputStream stream = CloseableClientHttpResponseInputStream.of(mockResponse(testData))) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int b;
				while ((b = stream.read()) != -1) {
					out.write(b);
				}

				assertArrayEquals(testData, out.toByteArray());
			}
		}
	}

	@SuppressWarnings("resource")
	private static ClientHttpResponse mockResponse(final byte[] data) throws Exception {
		ClientHttpResponse response = mock(ClientHttpResponse.class);
		when(response.getBody()).thenReturn(new ByteArrayInputStream(data));
		return response;
	}
}
