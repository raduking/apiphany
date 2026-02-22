package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CloseableHttpResponseInputStreamTest {

	@Mock
	private ClassicHttpResponse mockResponse;

	@Mock
	private HttpEntity mockEntity;

	@Test
	void shouldCreateInputStreamFromResponse() throws Exception {
		byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
		InputStream testInputStream = new ByteArrayInputStream(testData);

		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(testInputStream);

		CloseableHttpResponseInputStream stream = CloseableHttpResponseInputStream.of(mockResponse);

		assertThat(stream, is(notNullValue()));
		byte[] result = stream.readAllBytes();
		assertThat(result, is(equalTo(testData)));
	}

	@Test
	void shouldThrowExceptionWhenResponseIsNull() {
		Exception exception = assertThrows(NullPointerException.class,
				() -> CloseableHttpResponseInputStream.of(null));
		assertThat(exception.getMessage(), is("response cannot be null"));
	}

	@Test
	void shouldThrowExceptionWhenEntityIsNull() {
		when(mockResponse.getEntity()).thenReturn(null);

		Exception exception = assertThrows(NullPointerException.class,
				() -> CloseableHttpResponseInputStream.of(mockResponse));
		assertThat(exception.getMessage(), is("response entity cannot be null"));
	}

	@Test
	void shouldThrowExceptionWhenEntityContentIsNull() throws Exception {
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(null);

		Exception exception = assertThrows(NullPointerException.class,
				() -> CloseableHttpResponseInputStream.of(mockResponse));
		assertThat(exception.getMessage(), is("entity content cannot be null")); // Assuming this is the error message from ThrowingSupplier
	}

	@Test
	void shouldDelegateReadOperations() throws Exception {
		byte[] testData = "test".getBytes(StandardCharsets.UTF_8);
		InputStream testInputStream = new ByteArrayInputStream(testData);

		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(testInputStream);

		CloseableHttpResponseInputStream stream = CloseableHttpResponseInputStream.of(mockResponse);

		assertThat(stream.read(), is(equalTo((int) 't')));

		byte[] buffer = new byte[3];
		int bytesRead = stream.read(buffer);
		assertThat(bytesRead, is(3));
		assertThat(new String(buffer), is("est"));

		byte[] singleBuffer = new byte[1];
		assertThat(stream.read(singleBuffer), is(-1)); // End of stream
	}

	@Test
	void shouldDelegateReadAllBytes() throws Exception {
		byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
		InputStream testInputStream = new ByteArrayInputStream(testData);

		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(testInputStream);

		CloseableHttpResponseInputStream stream = CloseableHttpResponseInputStream.of(mockResponse);

		byte[] result = stream.readAllBytes();
		assertThat(result, is(equalTo(testData)));
	}

	@Test
	void shouldDelegateReadNBytes() throws Exception {
		byte[] testData = "test data".getBytes(StandardCharsets.UTF_8);
		InputStream testInputStream = new ByteArrayInputStream(testData);

		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(testInputStream);

		CloseableHttpResponseInputStream stream = CloseableHttpResponseInputStream.of(mockResponse);

		byte[] result = stream.readNBytes(4);
		assertThat(new String(result), is("test"));

		byte[] buffer = new byte[10];
		int bytesRead = stream.readNBytes(buffer, 0, 5);
		assertThat(bytesRead, is(5));
		assertThat(new String(buffer, 0, bytesRead), is(" data"));
	}

	@Test
	void shouldCloseResponseWhenStreamIsClosed() throws Exception {
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(new byte[0]));

		CloseableHttpResponseInputStream stream = CloseableHttpResponseInputStream.of(mockResponse);

		stream.close();

		verify(mockResponse).close();
	}

	@Test
	void shouldPropagateIOExceptionOnClose() throws Exception {
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(new byte[0]));
		doThrow(new IOException("Failed to close")).when(mockResponse).close();

		CloseableHttpResponseInputStream stream = CloseableHttpResponseInputStream.of(mockResponse);

		assertThrows(IOException.class, stream::close);
	}

	@Test
	void shouldImplementHashCodeAndEquals() throws Exception {
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(new byte[0]));

		CloseableHttpResponseInputStream stream1 = CloseableHttpResponseInputStream.of(mockResponse);
		CloseableHttpResponseInputStream stream2 = CloseableHttpResponseInputStream.of(mockResponse);

		assertThat(stream1.equals(stream1), is(true));
		assertThat(stream1.equals(null), is(false));
		assertThat(stream1.equals(new Object()), is(false));

		// Using the same mock response, they should be equal
		assertThat(stream1.equals(stream2), is(true));
		assertThat(stream1.hashCode(), is(equalTo(stream2.hashCode())));
	}

	@Test
	void shouldNotBeEqualWhenResponsesAreDifferent() throws Exception {
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(new byte[0]));

		ClassicHttpResponse anotherMockResponse = mock(ClassicHttpResponse.class);
		HttpEntity anotherMockEntity = mock(HttpEntity.class);
		when(anotherMockResponse.getEntity()).thenReturn(anotherMockEntity);
		when(anotherMockEntity.getContent()).thenReturn(new ByteArrayInputStream(new byte[0]));

		CloseableHttpResponseInputStream stream1 = CloseableHttpResponseInputStream.of(mockResponse);
		CloseableHttpResponseInputStream stream2 = CloseableHttpResponseInputStream.of(anotherMockResponse);

		assertThat(stream1.equals(stream2), is(false));
	}
}
