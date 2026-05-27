package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.RequestParameters;
import org.apiphany.http.Multipart;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for form-URL-encoded and multipart form-data request bodies.
 *
 * @author Radu Sebastian LAZIN
 */
public interface BodyContract extends ApiphanyContract {

	@DisplayName("Body: The client should send form URL-encoded body with correct Content-Type")
	@Test
	default void shouldSendFormUrlEncodedBody() throws Exception {
		wiremock().stubFor(post("/form")
				.willReturn(aResponse()
						.withStatus(200)));

		Map<String, List<String>> params = Map.of("key1", List.of("value1"), "key2", List.of("value2"));

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("form")
					.form(params)
					.retrieve();
		}

		String expectedBody = RequestParameters.asString(RequestParameters.encode(params));

		wiremock().verify(postRequestedFor(urlPathEqualTo("/form"))
				.withHeader("Content-Type", equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED))
				.withRequestBody(equalTo(expectedBody)));
	}

	@DisplayName("Body: The client should URL-encode special characters in form body")
	@Test
	default void shouldSendFormUrlEncodedBodyWithSpecialCharacters() throws Exception {
		wiremock().stubFor(post("/form-encode")
				.willReturn(aResponse()
						.withStatus(200)));

		Map<String, List<String>> params = new LinkedHashMap<>();
		params.put("name", List.of("hello world"));
		params.put("email", List.of("a@b.com"));

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("form-encode")
					.form(params)
					.retrieve();
		}

		String expectedBody = RequestParameters.asString(RequestParameters.encode(params));

		wiremock().verify(postRequestedFor(urlPathEqualTo("/form-encode"))
				.withHeader("Content-Type", equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED))
				.withRequestBody(equalTo(expectedBody)));
	}

	@DisplayName("Body: The client should send empty form body for empty params")
	@Test
	default void shouldSendFormUrlEncodedBodyWithEmptyParams() throws Exception {
		wiremock().stubFor(post("/form-empty")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("form-empty")
					.form(Map.of())
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlPathEqualTo("/form-empty"))
				.withHeader("Content-Type", equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED)));
	}

	@DisplayName("Body: The client should send multipart form-data with text fields")
	@Test
	default void shouldSendMultipartFormDataWithFields() throws Exception {
		wiremock().stubFor(post("/multipart")
				.willReturn(aResponse()
						.withStatus(200)));

		String boundary = "----testBoundaryFields";
		Multipart.Body body = Multipart.Body.builder()
				.boundary(boundary)
				.field("name", "John")
				.field("age", "30")
				.build();

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("multipart")
					.multipart(body)
					.retrieve();
		}

		String expectedBody = new String(body.toByteArray(), StandardCharsets.US_ASCII);
		wiremock().verify(postRequestedFor(urlPathEqualTo("/multipart"))
				.withHeader("Content-Type", equalTo("multipart/form-data; boundary=" + boundary))
				.withRequestBody(equalTo(expectedBody)));
	}

	@DisplayName("Body: The client should send multipart form-data with file upload")
	@Test
	default void shouldSendMultipartFormDataWithFile() throws Exception {
		wiremock().stubFor(post("/multipart-file")
				.willReturn(aResponse()
						.withStatus(200)));

		String boundary = "----testBoundaryFile";
		byte[] fileData = "file content".getBytes(StandardCharsets.UTF_8);
		Multipart.Body body = Multipart.Body.builder()
				.boundary(boundary)
				.field("description", "profile picture")
				.file("avatar", "photo.jpg", "image/jpeg", fileData)
				.build();

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("multipart-file")
					.multipart(body)
					.retrieve();
		}

		String expectedBody = new String(body.toByteArray(), StandardCharsets.US_ASCII);
		wiremock().verify(postRequestedFor(urlPathEqualTo("/multipart-file"))
				.withHeader("Content-Type", equalTo("multipart/form-data; boundary=" + boundary))
				.withRequestBody(equalTo(expectedBody)));
	}

	@DisplayName("Body: The multipart Content-Type boundary should match the boundary in the body")
	@Test
	default void shouldMatchMultipartBoundaryInContentType() throws Exception {
		wiremock().stubFor(post("/multipart-boundary")
				.willReturn(aResponse()
						.withStatus(200)));

		String boundary = "----customBoundary123";
		Multipart.Body body = Multipart.Body.builder()
				.boundary(boundary)
				.field("test", "value")
				.build();

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("multipart-boundary")
					.multipart(body)
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlPathEqualTo("/multipart-boundary"))
				.withHeader("Content-Type", equalTo(body.getContentTypeValue()))
				.withRequestBody(containing("--" + boundary)));
	}
}
