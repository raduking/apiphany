package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.net.Sockets;
import org.apiphany.server.KeyValueHttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaNetHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpExchangeClientTest {

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final int API_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	private static final KeyValueHttpServer API_SERVER = new KeyValueHttpServer(API_SERVER_PORT);

	private static final ClientProperties CLIENT_PROPERTIES = new ClientProperties();
	private static final SimpleApiClient API_CLIENT = new SimpleApiClient(CLIENT_PROPERTIES);

	private static final String NEW_KEY = "Bubu";
	private static final String NEW_VALUE_1 = "Juju";
	private static final String NEW_VALUE_2 = "Pupu";

	@AfterAll
	static void cleanup() throws Exception {
		API_CLIENT.close();
		API_SERVER.close();
	}

	@Test
	void shouldReturnDefaultValueOnGetDefaultKey() {
		String value = API_CLIENT.get(KeyValueHttpServer.DEFAULT_KEY);

		assertThat(value, equalTo(KeyValueHttpServer.DEFAULT_VALUE));
	}

	@Test
	void shouldPerformCRUD() {
		String value = API_CLIENT.add(NEW_KEY, NEW_VALUE_1);
		assertThat(value, equalTo(NEW_VALUE_1));

		value = API_CLIENT.set(NEW_KEY, NEW_VALUE_2);
		assertThat(value, equalTo(NEW_VALUE_2));

		value = API_CLIENT.get(NEW_KEY);
		assertThat(value, equalTo(NEW_VALUE_2));

		value = API_CLIENT.delete(NEW_KEY);
		assertThat(value, equalTo(NEW_VALUE_2));
	}

	static class SimpleApiClient extends ApiClient {

		protected SimpleApiClient(final ClientProperties properties) {
			super("http://localhost:" + API_SERVER_PORT, ExchangeClient.builder()
					.client(JavaNetHttpExchangeClient.class)
					.properties(properties));
		}

		public String get(final String key) {
			return client()
					.http()
					.get()
					.path(API, "keys", key)
					.retrieve(String.class)
					.orNull();
		}

		public String set(final String key, final String value) {
			return client()
					.http()
					.put()
					.path(API, "keys", key)
					.body(value)
					.retrieve(String.class)
					.orNull();
		}

		public String add(final String key, final String value) {
			return client()
					.http()
					.post()
					.path(API, "keys")
					.body(key + ":" + value)
					.retrieve(String.class)
					.orNull();
		}

		public String delete(final String key) {
			return client()
					.http()
					.delete()
					.path(API, "keys", key)
					.retrieve(String.class)
					.orNull();
		}
	}

}
