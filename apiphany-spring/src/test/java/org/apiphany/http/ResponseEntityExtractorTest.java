package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.UnknownContentTypeException;

/**
 * Tests for {@link ResponseEntityExtractor}.
 *
 * @author Radu Sebastian LAZIN
 */
class ResponseEntityExtractorTest {

	@Nested
	class InputStreamTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnCloseableInputStreamForInputStreamType() throws Exception {
			byte[] data = "hello".getBytes();
			ClientHttpResponse response = mockResponse(data);

			ResponseEntityExtractor<InputStream> extractor = new ResponseEntityExtractor<>(InputStream.class, List.of());
			ResponseEntity<InputStream> result = extractor.extractData(response);

			assertThat(result.getBody(), instanceOf(CloseableClientHttpResponseInputStream.class));
			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
			result.getBody().close();
		}
	}

	@Nested
	class ByteArrayTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReadByteArrayResponseBody() throws Exception {
			byte[] data = { 0x01, 0x02, 0x03 };
			ClientHttpResponse response = mockResponse(data);

			ResponseEntityExtractor<byte[]> extractor = new ResponseEntityExtractor<>(byte[].class, List.of());
			ResponseEntity<byte[]> result = extractor.extractData(response);

			assertArrayEquals(data, result.getBody());
			assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldThrowOnOversizedByteArray() {
			Executable supplier = () -> {
				byte[] hugeData = new byte[100];
				ClientHttpResponse response = mockResponse(hugeData);

				ResponseEntityExtractor<byte[]> extractor = new ResponseEntityExtractor<>(byte[].class, List.of(), 10);
				extractor.extractData(response);
			};
			assertThrows(RestClientException.class, supplier);
		}
	}

	@Nested
	class ConverterTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldUseMatchingConverter() throws Exception {
			byte[] data = "converted".getBytes();
			ClientHttpResponse response = mockResponse(data);

			HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
			when(converter.canRead(String.class, MediaType.TEXT_PLAIN)).thenReturn(true);
			when(converter.read(String.class, response)).thenReturn("converted body");

			ResponseEntityExtractor<String> extractor = new ResponseEntityExtractor<>(String.class, List.of(converter));
			ResponseEntity<String> result = extractor.extractData(response);

			assertThat(result.getBody(), equalTo("converted body"));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldThrowWhenNoConverterFound() throws Exception {
			byte[] data = "data".getBytes();
			ClientHttpResponse response = mockResponse(data);

			HttpMessageConverter<Boolean> converter = mock(HttpMessageConverter.class);
			when(converter.canRead(String.class, MediaType.TEXT_PLAIN)).thenReturn(false);

			ResponseEntityExtractor<String> extractor = new ResponseEntityExtractor<>(String.class, List.of(converter));

			assertThrows(UnknownContentTypeException.class, () -> extractor.extractData(response));
		}
	}

	@Nested
	class ErrorTests {

		@Test
		@SuppressWarnings("resource")
		void shouldWrapIOExceptionInRestClientException() {
			Executable executable = () -> {
				ClientHttpResponse response = mock(ClientHttpResponse.class);
				when(response.getBody()).thenThrow(new IOException("read error"));
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.TEXT_PLAIN);
				when(response.getHeaders()).thenReturn(headers);
				when(response.getStatusCode()).thenReturn(HttpStatus.OK);

				ResponseEntityExtractor<byte[]> extractor = new ResponseEntityExtractor<>(byte[].class, List.of());
				extractor.extractData(response);
			};
			assertThrows(RestClientException.class, executable);
		}

		@Test
		void shouldThrowOnNullResponseClass() {
			assertThrows(NullPointerException.class,
					() -> new ResponseEntityExtractor<>(null, List.of()));
		}
	}

	@Nested
	class ExtractDataTests {

		@Test
		@SuppressWarnings("resource")
		void shouldBuildResponseEntityWithCorrectStatus() throws Exception {
			byte[] data = "ok".getBytes();
			ClientHttpResponse response = mockResponse(data, HttpStatus.CREATED);

			ResponseEntityExtractor<byte[]> extractor = new ResponseEntityExtractor<>(byte[].class, List.of());
			ResponseEntity<byte[]> result = extractor.extractData(response);

			assertThat(result.getStatusCode(), equalTo(HttpStatus.CREATED));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldIncludeResponseHeaders() throws Exception {
			byte[] data = "ok".getBytes();
			ClientHttpResponse response = mockResponse(data);
			response.getHeaders().set("X-Custom", "value");

			ResponseEntityExtractor<byte[]> extractor = new ResponseEntityExtractor<>(byte[].class, List.of());
			ResponseEntity<byte[]> result = extractor.extractData(response);

			assertThat(result.getHeaders().get("X-Custom"), equalTo(List.of("value")));
		}
	}

	@SuppressWarnings("resource")
	private static ClientHttpResponse mockResponse(final byte[] body, final HttpStatus status) throws Exception {
		ClientHttpResponse response = mock(ClientHttpResponse.class);
		when(response.getBody()).thenReturn(new ByteArrayInputStream(body));
		when(response.getStatusCode()).thenReturn(status);
		when(response.getStatusText()).thenReturn(status.getReasonPhrase());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		when(response.getHeaders()).thenReturn(headers);
		return response;
	}

	private static ClientHttpResponse mockResponse(final byte[] body) throws Exception {
		return mockResponse(body, HttpStatus.OK);
	}
}
