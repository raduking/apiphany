package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import org.apiphany.client.ClientProperties;
import org.apiphany.net.Sockets;
import org.apiphany.utils.http.client.KeyValueApiClient;
import org.apiphany.utils.http.server.GZipKeyValueHttpServer;
import org.apiphany.utils.http.server.KeyValueHttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaNetHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpExchangeClientGZipTest {

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final int API_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	private static final ClientProperties CLIENT_PROPERTIES = new ClientProperties();

	private static final GZipKeyValueHttpServer API_SERVER = new GZipKeyValueHttpServer(API_SERVER_PORT);
	private static final KeyValueApiClient API_CLIENT = new KeyValueApiClient("http://localhost:" + API_SERVER_PORT, CLIENT_PROPERTIES);

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

		value = API_CLIENT.append(NEW_KEY, NEW_VALUE_1);
		assertThat(value, equalTo(NEW_VALUE_2 + NEW_VALUE_1));

		value = API_CLIENT.delete(NEW_KEY);
		assertThat(value, equalTo(NEW_VALUE_2 + NEW_VALUE_1));
	}

}
