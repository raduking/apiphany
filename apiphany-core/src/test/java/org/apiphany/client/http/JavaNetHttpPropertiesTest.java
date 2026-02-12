package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.http.HttpClient.Version;
import java.util.Map;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
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

		assertThat(request.getHttpVersion(), equalTo(JavaNetHttpProperties.Request.DEFAULT_HTTP_VERSION));
	}

	@Test
	void shouldReturnDefaultHttpVersionForRequestIfSetWithWrongValue() {
		JavaNetHttpProperties.Request request = new JavaNetHttpProperties.Request();
		request.setVersion("INVALID_HTTP_VERSION");

		assertThat(request.getHttpVersion(), equalTo(JavaNetHttpProperties.Request.DEFAULT_HTTP_VERSION));
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
}
