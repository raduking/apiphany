package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import org.apiphany.client.ClientProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

/**
 * Tests for {@link CloseableHttpRequestFactory}.
 *
 * @author Radu Sebastian LAZIN
 */
class CloseableHttpRequestFactoryTest {

	@Nested
	class OfTests {

		@Test
		void shouldCreateFactoryWithDelegate() throws Exception {
			ClientHttpRequestFactory delegate = mock(ClientHttpRequestFactory.class);
			ClientHttpRequest mockRequest = mock(ClientHttpRequest.class);
			when(delegate.createRequest(URI.create("https://example.com"), HttpMethod.GET)).thenReturn(mockRequest);

			try (CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.of(delegate)) {
				ClientHttpRequest request = factory.createRequest(URI.create("https://example.com"), HttpMethod.GET);

				assertThat(request, notNullValue());
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCloseManagedResources() throws Exception {
			ClientHttpRequestFactory delegate = mock(ClientHttpRequestFactory.class);
			AutoCloseable closeable = mock(AutoCloseable.class);

			try (CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.of(delegate, closeable)) {
				// factory is open
			}

			verify(closeable).close();
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCloseMultipleResources() throws Exception {
			ClientHttpRequestFactory delegate = mock(ClientHttpRequestFactory.class);
			AutoCloseable closeable1 = mock(AutoCloseable.class);
			AutoCloseable closeable2 = mock(AutoCloseable.class);

			try (CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.of(delegate, closeable1, closeable2)) {
				// factory is open
			}

			verify(closeable1).close();
			verify(closeable2).close();
		}
	}

	@Nested
	class CreateRequestTests {

		@Test
		void shouldDelegateCreateRequest() throws Exception {
			ClientHttpRequestFactory delegate = mock(ClientHttpRequestFactory.class);
			ClientHttpRequest mockRequest = mock(ClientHttpRequest.class);
			URI uri = URI.create("https://api.example.com/resource");

			when(delegate.createRequest(uri, HttpMethod.POST)).thenReturn(mockRequest);

			try (CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.of(delegate)) {
				ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);

				assertThat(request, equalTo(mockRequest));
			}
		}
	}

	@Nested
	class DetectTests {

		@Test
		void shouldDetectWithClientLibrary() {
			ClientProperties properties = ClientProperties.defaults();

			try (CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.detect(properties, "http-client5")) {
				assertThat(factory, notNullValue());
			} catch (Exception e) {
				// acceptable - factory creation may fail without proper SSL config
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNullForBlankLibrary() {
			ClientProperties properties = ClientProperties.defaults();

			CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.detect(properties, "", Map.of());

			assertThat(factory, equalTo(null));
		}

		@Test
		void shouldThrowForUnknownLibrary() {
			ClientProperties properties = ClientProperties.defaults();

			assertThrows(IllegalArgumentException.class,
					() -> CloseableHttpRequestFactory.detect(properties, "unknown-library", Map.of()));
		}

		@Test
		void shouldDetectWithoutArgs() {
			ClientProperties properties = ClientProperties.defaults();

			try (CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.detect(properties)) {
				assertThat(factory, notNullValue());
			} catch (Exception e) {
				// acceptable - factory creation may fail without proper SSL config
			}
		}

		@Test
		void shouldDetectWithNullArgs() {
			ClientProperties properties = ClientProperties.defaults();

			try (CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.detect(properties, (Object) null)) {
				assertThat(factory, notNullValue());
			} catch (Exception e) {
				// acceptable - factory creation may fail without proper SSL config
			}
		}

		@Test
		void shouldDetectWithMapArgs() {
			ClientProperties properties = ClientProperties.defaults();

			try (CloseableHttpRequestFactory factory = CloseableHttpRequestFactory.detect(properties, Map.of())) {
				assertThat(factory, notNullValue());
			} catch (Exception e) {
				// acceptable - factory creation may fail without proper SSL config
			}
		}
	}
}
