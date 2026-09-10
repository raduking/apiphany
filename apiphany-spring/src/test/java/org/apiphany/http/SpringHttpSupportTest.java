package org.apiphany.http;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

/**
 * Tests for {@link SpringHttpSupport}.
 *
 * @author Radu Sebastian LAZIN
 */
class SpringHttpSupportTest {

	@Nested
	class GetHttpMethodTests {

		@Test
		void shouldReturnGet() {
			assertThat(SpringHttpSupport.getHttpMethod("GET"), equalTo(HttpMethod.GET));
		}

		@Test
		void shouldReturnPost() {
			assertThat(SpringHttpSupport.getHttpMethod("POST"), equalTo(HttpMethod.POST));
		}

		@Test
		void shouldReturnPut() {
			assertThat(SpringHttpSupport.getHttpMethod("PUT"), equalTo(HttpMethod.PUT));
		}

		@Test
		void shouldReturnDelete() {
			assertThat(SpringHttpSupport.getHttpMethod("DELETE"), equalTo(HttpMethod.DELETE));
		}

		@Test
		void shouldReturnPatch() {
			assertThat(SpringHttpSupport.getHttpMethod("PATCH"), equalTo(HttpMethod.PATCH));
		}

		@Test
		void shouldReturnCustomMethod() {
			assertThat(SpringHttpSupport.getHttpMethod("INVALID"), notNullValue());
		}
	}

	@Nested
	class CopyHeadersTests {

		@Test
		void shouldCopyHeadersToTarget() {
			HttpHeaders source = new HttpHeaders();
			source.set("X-Custom", "value1");
			source.set("Accept", "application/json");

			HttpHeaders target = new HttpHeaders();

			SpringHttpSupport.copyHeaders(source, target);

			assertThat(target.get("X-Custom"), equalTo(List.of("value1")));
			assertThat(target.get("Accept"), equalTo(List.of("application/json")));
		}

		@Test
		void shouldNotFailWhenSourceIsEmpty() {
			HttpHeaders source = new HttpHeaders();
			HttpHeaders target = new HttpHeaders();

			SpringHttpSupport.copyHeaders(source, target);

			assertThat(target.isEmpty(), equalTo(true));
		}

		@Test
		void shouldAppendToExistingTargetHeaders() {
			HttpHeaders source = new HttpHeaders();
			source.set("X-New", "new-value");

			HttpHeaders target = new HttpHeaders();
			target.set("X-Existing", "existing-value");

			SpringHttpSupport.copyHeaders(source, target);

			assertThat(target.get("X-Existing"), equalTo(List.of("existing-value")));
			assertThat(target.get("X-New"), equalTo(List.of("new-value")));
		}
	}

	@Nested
	class CreateHttpEntityTests {

		@Test
		void shouldCreateGenericHttpEntity() {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> entity = SpringHttpSupport.createHttpEntity("body", headers);

			assertThat(entity.getBody(), equalTo("body"));
			assertThat(entity.getHeaders().getContentType(), equalTo(MediaType.APPLICATION_JSON));
		}

		@Test
		void shouldCreateInputStreamHttpEntity() {
			InputStream inputStream = new ByteArrayInputStream("stream content".getBytes());

			HttpEntity<InputStreamResource> entity = SpringHttpSupport.createHttpEntity(inputStream, new HttpHeaders());

			assertThat(entity, notNullValue());
			assertThat(entity.getBody(), notNullValue());
		}

		@Test
		void shouldCreateFileHttpEntity() throws Exception {
			Path tempPath = Files.createTempFile("spring-test", ".txt");
			tempPath.toFile().deleteOnExit();
			Files.writeString(tempPath, "file content");

			HttpEntity<FileSystemResource> entity = SpringHttpSupport.createHttpEntity(tempPath.toFile(), new HttpHeaders());

			assertThat(entity, notNullValue());
			assertThat(entity.getBody(), notNullValue());
		}
	}

	@Nested
	class GetContentTypeTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnContentTypeFromResponse() {
			ClientHttpResponse response = mock(ClientHttpResponse.class);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			when(response.getHeaders()).thenReturn(headers);

			MediaType contentType = SpringHttpSupport.getContentType(response);

			assertThat(contentType, equalTo(MediaType.APPLICATION_JSON));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnOctetStreamWhenNoContentType() {
			ClientHttpResponse response = mock(ClientHttpResponse.class);
			when(response.getHeaders()).thenReturn(new HttpHeaders());

			MediaType contentType = SpringHttpSupport.getContentType(response);

			assertThat(contentType, equalTo(MediaType.APPLICATION_OCTET_STREAM));
		}
	}

	@Nested
	class RequestFactoryTests {

		@Test
		void shouldReturnNullRequestFactoryForDefaultRestClientBuilder() {
			assertThat(SpringHttpSupport.getRequestFactory(RestClient.builder()), equalTo(null));
		}

		@Test
		void shouldReturnConfiguredRequestFactoryFromRestClientBuilder() {
			ClientHttpRequestFactory requestFactory = mock(ClientHttpRequestFactory.class);
			RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);

			assertThat(SpringHttpSupport.getRequestFactory(builder), equalTo(requestFactory));
		}

		@Test
		void shouldReturnNullRequestFactoryWhenRestClientBuilderIsNull() {
			assertThat(SpringHttpSupport.getRequestFactory((RestClient.Builder) null), equalTo(null));
		}

		@Test
		void shouldReturnFalseWhenRestTemplateBuilderHasNoExplicitRequestFactory() {
			assertThat(SpringHttpSupport.hasRequestFactory(new RestTemplateBuilder()), equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenRestTemplateBuilderHasExplicitRequestFactory() {
			ClientHttpRequestFactory requestFactory = mock(ClientHttpRequestFactory.class);
			RestTemplateBuilder builder = new RestTemplateBuilder().requestFactory(() -> requestFactory);

			assertThat(SpringHttpSupport.hasRequestFactory(builder), equalTo(true));
		}

		@Test
		void shouldReturnFalseWhenRestTemplateBuilderIsNull() {
			assertThat(SpringHttpSupport.hasRequestFactory(null), equalTo(false));
		}

		@Test
		void shouldReturnNullRequestFactoryForDefaultRestTemplateBuilder() {
			assertThat(SpringHttpSupport.getRequestFactory(new RestTemplateBuilder()), equalTo(null));
		}

		@Test
		void shouldReturnConfiguredRequestFactoryFromRestTemplateBuilder() {
			ClientHttpRequestFactory requestFactory = mock(ClientHttpRequestFactory.class);
			RestTemplateBuilder builder = new RestTemplateBuilder().requestFactory(() -> requestFactory);

			assertThat(SpringHttpSupport.getRequestFactory(builder), equalTo(requestFactory));
		}

		@Test
		void shouldReturnNullRequestFactoryWhenRestTemplateBuilderIsNull() {
			assertThat(SpringHttpSupport.getRequestFactory((RestTemplateBuilder) null), equalTo(null));
		}
	}

	@Test
	void shouldPreventInstantiation() {
		assertDefaultConstructorThrows(SpringHttpSupport.class);
	}
}
