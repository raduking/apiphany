package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestClientException;

/**
 * Tests for {@link HttpEntityRequestCallback}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpEntityRequestCallbackTest {

	@Nested
	class NullBodyTests {

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		void shouldCopyHeadersWhenBodyIsNull() throws Exception {
			HttpHeaders entityHeaders = new HttpHeaders();
			entityHeaders.setContentType(MediaType.APPLICATION_JSON);
			entityHeaders.set("X-Custom", "value");

			HttpEntity<?> entity = new HttpEntity<>(null, entityHeaders);
			HttpEntityRequestCallback callback = new HttpEntityRequestCallback(entity, List.of());

			ClientHttpRequest request = mock(ClientHttpRequest.class);
			HttpHeaders requestHeaders = new HttpHeaders();
			when(request.getHeaders()).thenReturn(requestHeaders);

			callback.doWithRequest(request);

			assertThat(requestHeaders.getContentType(), equalTo(MediaType.APPLICATION_JSON));
			assertThat(requestHeaders.get("X-Custom"), equalTo(List.of("value")));
		}
	}

	@Nested
	class ConverterFoundTests {

		@Test
		@SuppressWarnings("unchecked")
		void shouldWriteBodyWithMatchingConverter() throws Exception {
			HttpHeaders entityHeaders = new HttpHeaders();
			entityHeaders.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>("test body", entityHeaders);

			HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
			when(converter.canWrite(String.class, MediaType.APPLICATION_JSON)).thenReturn(true);

			HttpEntityRequestCallback<String> callback = new HttpEntityRequestCallback<>(entity, List.of(converter));

			ClientHttpRequest request = mock(ClientHttpRequest.class);
			HttpHeaders requestHeaders = new HttpHeaders();
			when(request.getHeaders()).thenReturn(requestHeaders);

			callback.doWithRequest(request);

			verify(converter).write("test body", MediaType.APPLICATION_JSON, request);
			assertThat(requestHeaders.getContentType(), equalTo(MediaType.APPLICATION_JSON));
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldWriteBodyWithNullContentType() throws Exception {
			HttpEntity<String> entity = new HttpEntity<>("body");

			HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
			when(converter.canWrite(String.class, null)).thenReturn(true);

			HttpEntityRequestCallback<String> callback = new HttpEntityRequestCallback<>(entity, List.of(converter));

			ClientHttpRequest request = mock(ClientHttpRequest.class);
			HttpHeaders requestHeaders = new HttpHeaders();
			when(request.getHeaders()).thenReturn(requestHeaders);

			callback.doWithRequest(request);

			verify(converter).write(eq("body"), isNull(), eq(request));
		}
	}

	@Nested
	class NoConverterTests {

		@Test
		@SuppressWarnings("unchecked")
		void shouldThrowWhenNoConverterFound() {
			HttpHeaders entityHeaders = new HttpHeaders();
			entityHeaders.setContentType(MediaType.APPLICATION_XML);
			HttpEntity<String> entity = new HttpEntity<>("body", entityHeaders);

			HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
			when(converter.canWrite(String.class, MediaType.APPLICATION_XML)).thenReturn(false);

			HttpEntityRequestCallback<String> callback = new HttpEntityRequestCallback<>(entity, List.of(converter));

			ClientHttpRequest request = mock(ClientHttpRequest.class);
			when(request.getHeaders()).thenReturn(new HttpHeaders());

			RestClientException ex = assertThrows(RestClientException.class, () -> callback.doWithRequest(request));

			assertThat(ex.getMessage(), equalTo("No HttpMessageConverter for java.lang.String and content type \"application/xml\""));
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldThrowWithoutContentTypeInMessage() {
			HttpEntity<String> entity = new HttpEntity<>("body");

			HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
			when(converter.canWrite(String.class, null)).thenReturn(false);

			HttpEntityRequestCallback<String> callback = new HttpEntityRequestCallback<>(entity, List.of(converter));

			ClientHttpRequest request = mock(ClientHttpRequest.class);
			when(request.getHeaders()).thenReturn(new HttpHeaders());

			RestClientException ex = assertThrows(RestClientException.class, () -> callback.doWithRequest(request));

			assertThat(ex.getMessage(), equalTo("No HttpMessageConverter for java.lang.String"));
		}
	}

	@Nested
	class HeaderTests {

		@Test
		@SuppressWarnings("unchecked")
		void shouldClearExistingRequestHeaders() throws Exception {
			HttpEntity<String> entity = new HttpEntity<>("body");

			HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
			when(converter.canWrite(String.class, null)).thenReturn(true);

			HttpEntityRequestCallback<String> callback = new HttpEntityRequestCallback<>(entity, List.of(converter));

			ClientHttpRequest request = mock(ClientHttpRequest.class);
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set("X-PreExisting", "old");
			when(request.getHeaders()).thenReturn(requestHeaders);

			callback.doWithRequest(request);

			assertThat(requestHeaders.containsKey("X-PreExisting"), equalTo(false));
		}
	}

	@Nested
	class ConstructorTests {

		@Test
		@SuppressWarnings({ })
		void shouldHandleNullConverterList() {
			HttpEntity<String> entity = new HttpEntity<>("body");
			HttpEntityRequestCallback<String> callback = new HttpEntityRequestCallback<>(entity, null);

			ClientHttpRequest request = mock(ClientHttpRequest.class);
			when(request.getHeaders()).thenReturn(new HttpHeaders());

			assertThrows(RestClientException.class, () -> callback.doWithRequest(request));
		}
	}
}
