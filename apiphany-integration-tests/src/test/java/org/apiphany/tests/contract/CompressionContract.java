package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;

import org.apiphany.ApiClient;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.deflate.Deflate;
import org.apiphany.io.gzip.GZip;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for response compression handling in the ApiClient. These tests verify that the client correctly
 * handles gzip and deflate compressed responses based on the Content-Encoding header.
 *
 * @author Radu Sebastian LAZIN
 */
public interface CompressionContract extends ApiphanyContract {

	@DisplayName("Compression: The client should automatically decode gzip responses when Content-Encoding is set to gzip")
	@Test
	default void shouldDecodeGzipAutomatically() throws Exception {
		byte[] gzipped = GZip.compress("hello");

		wiremock().stubFor(get("/gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody(gzipped)));

		ApiClient api = apiClient();
		try (api) {
			var result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, ContentEncoding.GZIP)
					.path("gzip")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/gzip"))
				.withHeader("Accept-Encoding", containing("gzip")));
	}

	@DisplayName("Compression: The client should handle empty gzip responses without throwing an exception")
	@Test
	default void shouldHandleEmptyGzipResponse() throws Exception {
		wiremock().stubFor(get("/empty-gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody(new byte[0])));

		ApiClient api = apiClient();
		try (api) {
			byte[] result = api.client()
					.http()
					.get()
					.path("empty-gzip")
					.retrieve(byte[].class)
					.orNull();

			assertNotNull(result);
			assertEquals(0, result.length);
		}
	}

	@DisplayName("Compression: The client should automatically decode responses with multiple encodings in the correct order")
	@Test
	default void shouldDecodeGzipThenDeflateAutomatically() throws Exception {
		byte[] gzipped = GZip.compress("hello".getBytes(StandardCharsets.UTF_8));
		byte[] deflateThenGzip = Deflate.compress(gzipped);

		wiremock().stubFor(get("/gzip-deflate")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip, deflate")
						.withBody(deflateThenGzip)));

		ApiClient api = apiClient();
		try (api) {
			var result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
					.path("gzip-deflate")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/gzip-deflate"))
				.withHeader("Accept-Encoding", containing("gzip"))
				.withHeader("Accept-Encoding", containing("deflate")));
	}

	@DisplayName("Compression: The client should not fail when Content-Encoding is set to gzip but the body is not actually gzipped")
	@Test
	default void shouldNotFailWhenBodyIsNotActuallyGzipped() throws Exception {
		wiremock().stubFor(get("/fake-gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody("hello")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, ContentEncoding.GZIP)
					.path("fake-gzip")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/fake-gzip")));
	}

	@DisplayName("Compression: The client should be able to parse Content-Encoding headers with spaces between encodings and still decompress the body correctly")
	@Test
	default void shouldParseContentEncodingWithSpacesAndDecompressCorrectly() throws Exception {
		byte[] gzipped = GZip.compress("hello".getBytes(StandardCharsets.UTF_8));
		byte[] body = Deflate.compress(gzipped);

		wiremock().stubFor(get("/encoding-spaces")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip ,  deflate")
						.withBody(body)));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
					.path("encoding-spaces")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}
	}

	@DisplayName("Compression: The client should ignore unknown encodings in the Content-Encoding header and still decompress the body with the known encodings")
	@Test
	default void shouldIgnoreUnknownEncodingAndStillDecompressCorrectly() throws Exception {
		byte[] body = GZip.compress("hello".getBytes(StandardCharsets.UTF_8));

		wiremock().stubFor(get("/unknown-encoding")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip, weird")
						.withBody(body)));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, "gzip")
					.path("unknown-encoding")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}
	}

	@DisplayName("Compression: The client should not automatically decompress the body more than once even if Content-Encoding is set to gzip only once but the body is actually gzipped multiple times")
	@Test
	default void shouldNotAutoDecompressDoubleGzip() throws Exception {
		byte[] onceGzipped = GZip.compress("hello");
		byte[] doubleGzipped = GZip.compress(onceGzipped);

		wiremock().stubFor(get("/double-gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody(doubleGzipped)));

		ApiClient api = apiClient();
		try (api) {
			byte[] result = api.client()
					.http()
					.get()
					.path("double-gzip")
					.header(HttpHeader.ACCEPT_ENCODING, ContentEncoding.GZIP)
					.retrieve(byte[].class)
					.orNull();

			// the content should still be a gzip-ped byte array
			assertNotNull(result);
			// must NOT auto-decode twice, so the result is not the original "hello"
			assertNotEquals("hello", new String(result, Strings.DEFAULT_CHARSET));

			byte[] decompressedTwice = GZip.decompress(result);
			assertEquals("hello", new String(decompressedTwice, Strings.DEFAULT_CHARSET));
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/double-gzip"))
				.withHeader("Accept-Encoding", containing("gzip")));
	}
}
