package org.apiphany.tests.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Duration;
import java.util.Map;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.client.KeyValueApiClient;
import org.apiphany.http.server.GZipKeyValueHttpServer;
import org.apiphany.http.server.KeyValueHttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaNetHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpExchangeClientGZipIT {

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);

	private static final ClientProperties CLIENT_PROPERTIES = new ClientProperties();

	private static final GZipKeyValueHttpServer API_SERVER = new GZipKeyValueHttpServer(PORT_CHECK_TIMEOUT);
	private static final KeyValueApiClient API_CLIENT = new KeyValueApiClient(API_SERVER.getPort(), CLIENT_PROPERTIES);

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

	@Test
	void shouldReturnMapWhenGettingAllValues() {
		Map<String, String> values = API_CLIENT.getAll();

		assertThat(values, notNullValue());
	}
}
