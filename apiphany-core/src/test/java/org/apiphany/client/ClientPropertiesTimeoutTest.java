package org.apiphany.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ClientProperties.Timeout}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClientPropertiesTimeoutTest {

	private static final int CONNECT_TIMEOUT = 666;
	private static final int CONNECTION_REQUEST_TIMEOUT = 667;
	private static final int SOCKET_TIMEOUT = 668;
	private static final int REQUEST_TIMEOUT = 669;

	@Test
	void shouldReadClientPropertiesWithTimeoutFromJson() {
		String clientPropertiesJson = Strings.fromFile("client/client-properties.json");
		String json = Strings.removeAllWhitespace(clientPropertiesJson);

		ClientProperties clientProperties1 = JsonBuilder.fromJson(json, ClientProperties.class);

		assertThat(clientProperties1, notNullValue());

		String result = clientProperties1.getTimeout().toString();

		ClientProperties clientProperties2 = JsonBuilder.fromJson(result, ClientProperties.class);
		String expected = clientProperties2.getTimeout().toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReadNestedClientProperties() {
		String clientPropertiesJson = Strings.fromFile("client/client-properties.json");
		String json = Strings.removeAllWhitespace(clientPropertiesJson);

		ClientProperties clientProperties = JsonBuilder.fromJson(json, ClientProperties.class);
		ClientProperties nestedClientProperties1 = clientProperties.getClientProperties("", "some-client", ClientProperties.class);

		assertThat(nestedClientProperties1, notNullValue());

		String result = nestedClientProperties1.getTimeout().toString();

		ClientProperties nestedClientProperties2 = JsonBuilder.fromJson(result, ClientProperties.class);
		String expected = nestedClientProperties2.getTimeout().toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldBuildTimeoutWithItsBuilderClass() {
		ClientProperties.Timeout timeout = ClientProperties.Timeout.Builder.custom()
				.connect(CONNECT_TIMEOUT)
				.connectionRequest(CONNECTION_REQUEST_TIMEOUT)
				.socket(SOCKET_TIMEOUT)
				.request(REQUEST_TIMEOUT)
				.build(ClientProperties.Timeout.class);

		assertThat(timeout.getConnect().toMillis(), equalTo((long) CONNECT_TIMEOUT));
		assertThat(timeout.getConnectionRequest().toMillis(), equalTo((long) CONNECTION_REQUEST_TIMEOUT));
		assertThat(timeout.getSocket().toMillis(), equalTo((long) SOCKET_TIMEOUT));
	}
}
