package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.http.HttpClient.Version;
import java.util.Map;
import java.util.Objects;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link JavaNetHttpProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpPropertiesTest {

	private static final String ROOT = "java-net-http";

	private static final String JAVA_NET_HTTP_PROPERTIES_JSON = Strings.fromFile("client/http/java-net-http-properties.json");
	private static final JavaNetHttpProperties JAVA_NET_HTTP_PROPERTIES =
			JsonBuilder.fromJson(JAVA_NET_HTTP_PROPERTIES_JSON, JavaNetHttpProperties.class);

	@Test
	void shouldHaveSSLAsRootProperty() {
		assertThat(JavaNetHttpProperties.ROOT, equalTo(ROOT));
	}

	@Test
	void shouldReadJavaNetHttpPropertiesFromJson() {
		String json = Strings.removeAllWhitespace(JAVA_NET_HTTP_PROPERTIES_JSON);

		JavaNetHttpProperties javaNetHttpProperties1 = JsonBuilder.fromJson(json, JavaNetHttpProperties.class);

		assertThat(javaNetHttpProperties1, notNullValue());

		String result = javaNetHttpProperties1.toString();

		JavaNetHttpProperties javaNetHttpProperties2 = JsonBuilder.fromJson(result, JavaNetHttpProperties.class);
		String expected = javaNetHttpProperties2.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnDefaultHttpVersionForRequestIfNotSet() {
		JavaNetHttpProperties.Request request = new JavaNetHttpProperties.Request();

		assertThat(request.getHttpVersion(), equalTo(JavaNetHttpProperties.Request.Default.HTTP_VERSION));
	}

	@Test
	void shouldReturnDefaultHttpVersionForRequestIfSetWithWrongValue() {
		JavaNetHttpProperties.Request request = new JavaNetHttpProperties.Request();
		request.setVersion("INVALID_HTTP_VERSION");

		assertThat(request.getHttpVersion(), equalTo(JavaNetHttpProperties.Request.Default.HTTP_VERSION));
	}

	@Test
	void shouldReadJavaNetHttpRequestPropertiesFromJson() {
		String json = Strings.removeAllWhitespace(JAVA_NET_HTTP_PROPERTIES_JSON);

		JavaNetHttpProperties javaNetHttpProperties = JsonBuilder.fromJson(json, JavaNetHttpProperties.class);

		assertThat(javaNetHttpProperties.getRequest(), notNullValue());

		String result = javaNetHttpProperties.getRequest().toString();

		JavaNetHttpProperties.Request javaNetHttpPropertiesRequest = JsonBuilder.fromJson(result, JavaNetHttpProperties.Request.class);
		String expected = javaNetHttpPropertiesRequest.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldConvertJavaNetHttpPropertiesFromMap() {
		JsonBuilder jsonBuilder = Constructors.IgnoreAccess.newInstance(JsonBuilder.class);

		JavaNetHttpProperties javaNetHttpProperties1 = jsonBuilder.fromPropertiesMap(Map.of(
				"request", Map.of(
						"version", "HTTP/2")),
				JavaNetHttpProperties.class,
				Consumers.noConsumer());

		assertThat(javaNetHttpProperties1, notNullValue());
		assertThat(javaNetHttpProperties1.getRequest(), notNullValue());
		assertThat(javaNetHttpProperties1.getRequest().getHttpVersion(), equalTo(Version.HTTP_2));
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		JavaNetHttpProperties javaNetHttpProperties1 = JAVA_NET_HTTP_PROPERTIES;
		JavaNetHttpProperties javaNetHttpProperties2 = JsonBuilder.fromJson(JAVA_NET_HTTP_PROPERTIES_JSON, JavaNetHttpProperties.class);

		// same reference
		assertEquals(javaNetHttpProperties1, javaNetHttpProperties1);

		// different instance, same values
		assertEquals(javaNetHttpProperties1, javaNetHttpProperties2);
		assertEquals(javaNetHttpProperties2, javaNetHttpProperties1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(javaNetHttpProperties1.hashCode(), javaNetHttpProperties2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		JavaNetHttpProperties javaNetHttpProperties1 = JAVA_NET_HTTP_PROPERTIES;
		JavaNetHttpProperties javaNetHttpProperties2 = JsonBuilder.fromJson(JAVA_NET_HTTP_PROPERTIES_JSON, JavaNetHttpProperties.class);
		javaNetHttpProperties2.getRequest().setVersion("HTTP/2");

		// different objects
		assertNotEquals(javaNetHttpProperties1, javaNetHttpProperties2);
		assertThat(javaNetHttpProperties1, not(equalTo(null)));
		assertThat(javaNetHttpProperties2, not(equalTo("not-a-java-net-http-properties")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		JavaNetHttpProperties javaNetHttpProperties = JAVA_NET_HTTP_PROPERTIES;

		int expectedHash = Objects.hash(javaNetHttpProperties.getRequest());

		assertEquals(expectedHash, javaNetHttpProperties.hashCode());
	}

	@Nested
	class RequestTest {

		@Test
		void shouldEqualSameValuesAndSameReference() {
			JavaNetHttpProperties.Request request1 = JAVA_NET_HTTP_PROPERTIES.getRequest();
			JavaNetHttpProperties.Request request2 = JsonBuilder.fromJson(JAVA_NET_HTTP_PROPERTIES_JSON, JavaNetHttpProperties.class).getRequest();

			// same reference
			assertEquals(request1, request1);

			// different instance, same values
			assertEquals(request1, request2);
			assertEquals(request2, request1);

			// hashCode contract (important for coverage + correctness)
			assertEquals(request1.hashCode(), request2.hashCode());
		}

		@Test
		void shouldNotEqualIfDifferentObjects() {
			JavaNetHttpProperties.Request request1 = JAVA_NET_HTTP_PROPERTIES.getRequest();
			JavaNetHttpProperties.Request request2 = JsonBuilder.fromJson(JAVA_NET_HTTP_PROPERTIES_JSON, JavaNetHttpProperties.class).getRequest();
			request2.setVersion("HTTP/2");

			// different objects
			assertNotEquals(request1, request2);
			assertThat(request1, not(equalTo(null)));
			assertThat(request2, not(equalTo("not-a-request")));
		}

		@Test
		void shouldBuildHashcodeWithAllFields() {
			JavaNetHttpProperties.Request request = JAVA_NET_HTTP_PROPERTIES.getRequest();

			int expectedHash = Objects.hash(request.getVersion());

			assertEquals(expectedHash, request.hashCode());
		}
	}
}
