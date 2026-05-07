package org.apiphany.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ClientProperties.Compression}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClientPropertiesCompressionTest {

	@Test
	void shouldReadClientPropertiesWithTimeoutFromJson() {
		String clientPropertiesJson = Strings.fromFile("client/client-properties.json");
		String json = Strings.removeAllWhitespace(clientPropertiesJson);

		ClientProperties clientProperties1 = JsonBuilder.fromJson(json, ClientProperties.class);

		assertThat(clientProperties1, notNullValue());

		String result = clientProperties1.getCompression().toString();

		String stringProperties = clientProperties1.toString();
		ClientProperties clientProperties2 = JsonBuilder.fromJson(stringProperties, ClientProperties.class);

		String expected = clientProperties2.getCompression().toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReadNestedClientProperties() {
		String clientPropertiesJson = Strings.fromFile("client/client-properties.json");
		String json = Strings.removeAllWhitespace(clientPropertiesJson);

		ClientProperties clientProperties = JsonBuilder.fromJson(json, ClientProperties.class);
		ClientProperties nestedClientProperties1 = clientProperties.getClientProperties("", "some-client", ClientProperties.class);

		assertThat(nestedClientProperties1, notNullValue());

		String result = nestedClientProperties1.getCompression().toString();

		String stringProperties = clientProperties.toString();
		clientProperties = JsonBuilder.fromJson(stringProperties, ClientProperties.class);
		ClientProperties nestedClientProperties2 = clientProperties.getClientProperties("", "some-client", ClientProperties.class);

		String expected = nestedClientProperties2.getCompression().toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldDefaultIsGzipToFalse() {
		ClientProperties clientProperties = new ClientProperties();

		assertThat(clientProperties.getCompression().isGzip(), equalTo(false));
	}
}
