package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.ApiClient;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for JSON serialization and deserialization. These tests verify that the client correctly handles JSON
 * request and response bodies when the Content-Type header is set to application/json.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonContract extends ApiphanyContract {

	record MyDto(String name) {
		// no additional code needed
	}

	@DisplayName("JSON: The client should deserialize JSON responses when Content-Type is application/json")
	@Test
	default void shouldDeserializeJson() throws Exception {
		wiremock().stubFor(get("/json")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("""
								    {"name":"john"}
								""")));

		ApiClient api = apiClient();
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("json")
					.retrieve(MyDto.class)
					.orNull();

			assertEquals("john", result.name());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/json")));
	}

	record MyDtoWithToString(String name) {
		@Override
		public String toString() {
			return "My name is " + name;
		}
	}

	@DisplayName("JSON: The client should not serialize JSON when Content-Type is not set")
	@Test
	default void shouldNotSerializeJsonWhenHeaderIsNotSet() throws Exception {
		wiremock().stubFor(post("/json")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("json")
					.body(new MyDtoWithToString("john"))
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/json"))
				.withRequestBody(equalTo("My name is john")));
	}

	@DisplayName("JSON: The client should serialize JSON with JsonBuilder when Content-Type is application/json")
	@Test
	default void shouldSerializeJsonWithJsonBuilderWhenContentTypeIsSet() throws Exception {
		wiremock().stubFor(post("/json")
				.willReturn(aResponse()
						.withStatus(200)));

		MyDtoWithToString dto = new MyDtoWithToString("john");
		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("json")
					.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON)
					.body(dto)
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/json"))
				.withHeader("Content-Type", equalTo("application/json"))
				.withRequestBody(equalTo(JsonBuilder.toJson(dto))));
	}

	@DisplayName("JSON: The client should throw an exception when the response body is not valid JSON")
	@Test
	default void shouldFailOnInvalidJson() throws Exception {
		wiremock().stubFor(get("/bad-json")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("{ invalid json")));

		ApiClient api = apiClient();
		try (api) {
			assertThrows(Exception.class, () -> api.client()
					.http()
					.get()
					.path("bad-json")
					.retrieve(MyDto.class)
					.orRethrow());
		}

		wiremock().verify(1, getRequestedFor(urlEqualTo("/bad-json")));
	}

	@DisplayName("JSON: The client should deserialize JSON arrays")
	@Test
	default void shouldDeserializeJsonArray() throws Exception {
		wiremock().stubFor(get("/json-array")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("""
									[
										{"name":"john"},
										{"name":"jane"}
									]
								""")));

		ApiClient api = apiClient();
		try (api) {
			MyDto[] result = api.client()
					.http()
					.get()
					.path("json-array")
					.retrieve(MyDto[].class)
					.orNull();

			assertEquals(2, result.length);
			assertEquals("john", result[0].name());
			assertEquals("jane", result[1].name());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/json-array")));
	}

	@DisplayName("JSON: The client should deserialize JSON when charset is present in Content-Type")
	@Test
	default void shouldDeserializeJsonWithCharset() throws Exception {
		wiremock().stubFor(get("/json-charset")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json; charset=UTF-8")
						.withBody("""
									{"name":"john"}
								""")));

		ApiClient api = apiClient();
		try (api) {
			MyDto result = api.client()
					.http()
					.get()
					.path("json-charset")
					.retrieve(MyDto.class)
					.orNull();

			assertEquals("john", result.name());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/json-charset")));
	}

	@DisplayName("JSON: The client should not deserialize JSON when Content-Type is not application/json")
	@Test
	default void shouldNotDeserializeJsonWithoutJsonContentType() throws Exception {
		wiremock().stubFor(get("/plain-json")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("""
									{"name":"john"}
								""")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("plain-json")
					.retrieve(String.class)
					.orNull();

			assertEquals("{\"name\":\"john\"}", result.trim());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/plain-json")));
	}

	@DisplayName("JSON: The client should throw exception and not deserialize JSON when Content-Type is not application/json when requesting an entity type")
	@Test
	default void shouldThrowExceptionNotDeserializeJsonWithoutJsonContentTypeWhenRequestingEntity() throws Exception {
		wiremock().stubFor(get("/plain-json")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("""
									{"name":"john"}
								""")));

		ApiClient api = apiClient();
		try (api) {
			assertThrows(Exception.class, () -> api.client()
					.http()
					.get()
					.path("plain-json")
					.retrieve(MyDto.class)
					.orRethrow());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/plain-json")));
	}

	record NullableDto(String name, String value) {
	}

	@DisplayName("JSON: The client should deserialize null JSON fields")
	@Test
	default void shouldDeserializeNullJsonFields() throws Exception {
		wiremock().stubFor(get("/json-null")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("""
									{
										"name":"john",
										"value":null
									}
								""")));

		ApiClient api = apiClient();
		try (api) {
			NullableDto result = api.client()
					.http()
					.get()
					.path("json-null")
					.retrieve(NullableDto.class)
					.orNull();

			assertEquals("john", result.name());
			assertEquals(null, result.value());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/json-null")));
	}

	@DisplayName("JSON: Empty response body with application/json should fail deserialization")
	@Test
	default void shouldFailOnEmptyJsonBody() throws Exception {
		wiremock().stubFor(get("/empty-json")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("")));

		ApiClient api = apiClient();
		try (api) {
			assertThrows(Exception.class, () -> api.client()
					.http()
					.get()
					.path("empty-json")
					.retrieve(MyDto.class)
					.orRethrow());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/empty-json")));
	}

	@DisplayName("JSON: The client should send Accept application/json when explicitly set")
	@Test
	default void shouldSendAcceptJsonHeader() throws Exception {
		wiremock().stubFor(get("/accept-json")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("""
									{"name":"john"}
								""")));

		ApiClient api = apiClient();
		try (api) {
			MyDto result = api.client()
					.http()
					.get()
					.path("accept-json")
					.header(HttpHeader.ACCEPT, ContentType.APPLICATION_JSON)
					.retrieve(MyDto.class)
					.orNull();

			assertEquals("john", result.name());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/accept-json"))
				.withHeader("Accept", equalTo("application/json")));
	}
}
