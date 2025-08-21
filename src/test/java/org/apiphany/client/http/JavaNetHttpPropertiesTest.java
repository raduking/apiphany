package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaNetHttpProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpPropertiesTest {

	private static final String ROOT = "java-net-http";

	private static final String JAVA_NET_HTTP_PROPERTIES_JSON = Strings.fromFile("/client/http/java-net-http-properties.json");

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

}
