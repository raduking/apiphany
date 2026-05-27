package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.RequestParameters;
import org.apiphany.http.Multipart;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Bytes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for form-URL-encoded and multipart form-data request bodies.
 *
 * @author Radu Sebastian LAZIN
 */
public interface BodyContract extends ApiphanyContract {

	@DisplayName("Basic: String request bodies should be encoded as UTF-8 by default")
	@Test
	default void shouldEncodeRequestBodyAsUtf8() throws Exception {
		wiremock().stubFor(post("/utf8")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("utf8")
					.body("é")
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/utf8"))
				.withRequestBody(equalTo("é")));
	}

	@DisplayName("Basic: The client should send binary request bodies unchanged")
	@Test
	default void shouldSendBinaryBody() throws Exception {
		wiremock().stubFor(post("/binary")
				.willReturn(aResponse()
						.withStatus(200)));

		byte[] body = new byte[] { 0x00, 0x01, 0x02 };

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("binary")
					.body(body)
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/binary"))
				.withRequestBody(equalTo(new String(body))));
	}

	@DisplayName("Body: sends application/x-www-form-urlencoded body")
	@Test
	default void shouldSendFormUrlEncodedBody() throws Exception {
		wiremock().stubFor(post("/form")
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
					.path("form")
					.form(params)
					.retrieve();
		}

		String expectedBody = RequestParameters.asString(RequestParameters.encode(params));

		wiremock().verify(postRequestedFor(urlPathEqualTo("/form"))
				.withHeader("Content-Type", equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED))
				.withRequestBody(equalTo(expectedBody)));
	}

	@DisplayName("Body: sends empty form body for empty params")
	@Test
	default void shouldSendFormUrlEncodedBodyWithEmptyParams() throws Exception {
		wiremock().stubFor(post("/form-empty")
				.willReturn(aResponse()
						.withStatus(200)));

		Map<String, List<String>> params = Map.of();

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("form-empty")
					.form(params)
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlPathEqualTo("/form-empty"))
				.withHeader("Content-Type", equalTo(ContentType.Value.APPLICATION_FORM_URLENCODED))
				.withRequestBody(binaryEqualTo(Bytes.EMPTY)));
	}

	@DisplayName("Body: sends multipart request with correct Content-Type and boundary")
	@Test
	default void shouldSendMultipartFormData() throws Exception {
		wiremock().stubFor(post("/multipart").willReturn(aResponse().withStatus(200)));

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

		wiremock().verify(postRequestedFor(urlPathEqualTo("/multipart"))
				.withHeader("Content-Type",
						equalTo("multipart/form-data; boundary=" + boundary))
				.withRequestBody(equalTo(body.toString(StandardCharsets.UTF_8))));
	}

	@DisplayName("Body: multipart boundary in header matches body delimiter")
	@Test
	default void shouldMatchMultipartBoundaryInContentType() throws Exception {
		wiremock().stubFor(post("/multipart-boundary")
				.willReturn(aResponse().withStatus(200)));

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
				.withHeader("Content-Type",
						equalTo(body.getContentTypeValue()))
				.withRequestBody(containing("--" + boundary)));
	}

	@DisplayName("Body: sends multipart form-data with Unicode content without modification")
	@Test
	default void shouldSendMultipartFormDataWithUnicode() throws Exception {
		wiremock().stubFor(post("/multipart-unicode")
				.willReturn(aResponse()
						.withStatus(200)));

		String boundary = "----unicodeBoundary";

		Multipart.Body body = Multipart.Body.builder()
				.boundary(boundary)
				.field("name", "Ștefan")
				.field("city", "Cluj-Napoca")
				.build();

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("multipart-unicode")
					.multipart(body)
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlPathEqualTo("/multipart-unicode"))
				.withHeader("Content-Type", equalTo(body.getContentTypeValue()))
				.withRequestBody(binaryEqualTo(body.toByteArray())));
	}
}
