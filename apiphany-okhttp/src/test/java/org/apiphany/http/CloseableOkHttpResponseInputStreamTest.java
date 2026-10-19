package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Test class for {@link CloseableOkHttpResponseInputStream}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class CloseableOkHttpResponseInputStreamTest {

	@Mock
	private Response mockResponse;

	@Mock
	private ResponseBody mockResponseBody;

	@Test
	@SuppressWarnings("resource")
	void shouldCreateInputStreamFromResponse() throws Exception {
		byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
		InputStream testInputStream = new ByteArrayInputStream(testData);

		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(testInputStream).when(mockResponseBody).byteStream();

		CloseableOkHttpResponseInputStream stream = CloseableOkHttpResponseInputStream.of(mockResponse);

		assertThat(stream, is(notNullValue()));
		byte[] result = stream.readAllBytes();
		assertThat(result, is(equalTo(testData)));
	}

	@Test
	void shouldThrowExceptionWhenResponseIsNull() {
		Exception exception = assertThrows(NullPointerException.class,
				() -> CloseableOkHttpResponseInputStream.of(null));
		assertThat(exception.getMessage(), is("response cannot be null"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowExceptionWhenBodyIsNull() {
		doReturn(null).when(mockResponse).body();

		Exception exception = assertThrows(NullPointerException.class,
				() -> CloseableOkHttpResponseInputStream.of(mockResponse));
		assertThat(exception.getMessage(), is("response body cannot be null"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowExceptionWhenBodyContentIsNull() throws Exception {
		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(null).when(mockResponseBody).byteStream();

		Exception exception = assertThrows(NullPointerException.class,
				() -> CloseableOkHttpResponseInputStream.of(mockResponse));
		assertThat(exception.getMessage(), is("body content cannot be null"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDelegateReadOperations() throws Exception {
		byte[] testData = "test".getBytes(StandardCharsets.UTF_8);
		InputStream testInputStream = new ByteArrayInputStream(testData);

		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(testInputStream).when(mockResponseBody).byteStream();

		CloseableOkHttpResponseInputStream stream = CloseableOkHttpResponseInputStream.of(mockResponse);

		assertThat(stream.read(), is(equalTo((int) 't')));

		byte[] buffer = new byte[3];
		int bytesRead = stream.read(buffer);
		assertThat(bytesRead, is(3));
		assertThat(new String(buffer), is("est"));

		byte[] singleBuffer = new byte[1];
		assertThat(stream.read(singleBuffer), is(-1)); // End of stream
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDelegateReadAllBytes() throws Exception {
		byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
		InputStream testInputStream = new ByteArrayInputStream(testData);

		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(testInputStream).when(mockResponseBody).byteStream();

		CloseableOkHttpResponseInputStream stream = CloseableOkHttpResponseInputStream.of(mockResponse);

		byte[] result = stream.readAllBytes();
		assertThat(result, is(equalTo(testData)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDelegateReadNBytes() throws Exception {
		byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
		InputStream testInputStream = new ByteArrayInputStream(testData);

		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(testInputStream).when(mockResponseBody).byteStream();

		CloseableOkHttpResponseInputStream stream = CloseableOkHttpResponseInputStream.of(mockResponse);

		byte[] result = stream.readNBytes(4);
		assertThat(new String(result), is("test"));

		byte[] buffer = new byte[10];
		int bytesRead = stream.readNBytes(buffer, 0, 5);
		assertThat(bytesRead, is(5));
		assertThat(new String(buffer, 0, bytesRead), is(" data"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCloseResponseWhenStreamIsClosed() throws Exception {
		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(new ByteArrayInputStream(new byte[0])).when(mockResponseBody).byteStream();

		CloseableOkHttpResponseInputStream stream = CloseableOkHttpResponseInputStream.of(mockResponse);

		stream.close();

		verify(mockResponse).close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldPropagateIOExceptionOnClose() throws Exception {
		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(new ByteArrayInputStream(new byte[0])).when(mockResponseBody).byteStream();
		doThrow(new RuntimeException("Failed to close")).when(mockResponse).close();

		CloseableOkHttpResponseInputStream stream = CloseableOkHttpResponseInputStream.of(mockResponse);

		assertThrows(RuntimeException.class, stream::close);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldImplementHashCodeAndEquals() throws Exception {
		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(new ByteArrayInputStream(new byte[0])).when(mockResponseBody).byteStream();

		CloseableOkHttpResponseInputStream stream1 = CloseableOkHttpResponseInputStream.of(mockResponse);
		CloseableOkHttpResponseInputStream stream2 = CloseableOkHttpResponseInputStream.of(mockResponse);

		assertThat(stream1.equals(stream1), is(true));
		assertThat(stream1.equals(null), is(false));
		assertThat(stream1.equals(new Object()), is(false));

		// Using the same mock response, they should be equal
		assertThat(stream1.equals(stream2), is(true));
		assertThat(stream1.hashCode(), is(equalTo(stream2.hashCode())));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotBeEqualWhenResponsesAreDifferent() throws Exception {
		doReturn(mockResponseBody).when(mockResponse).body();
		doReturn(new ByteArrayInputStream(new byte[0])).when(mockResponseBody).byteStream();

		Response anotherMockResponse = mock(Response.class);
		ResponseBody anotherMockResponseBody = mock(ResponseBody.class);
		doReturn(anotherMockResponseBody).when(anotherMockResponse).body();
		doReturn(new ByteArrayInputStream(new byte[0])).when(anotherMockResponseBody).byteStream();

		CloseableOkHttpResponseInputStream stream1 = CloseableOkHttpResponseInputStream.of(mockResponse);
		CloseableOkHttpResponseInputStream stream2 = CloseableOkHttpResponseInputStream.of(anotherMockResponse);

		assertThat(stream1.equals(stream2), is(false));
	}
}
