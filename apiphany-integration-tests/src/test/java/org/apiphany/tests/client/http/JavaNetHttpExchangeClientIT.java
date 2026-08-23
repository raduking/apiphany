package org.apiphany.tests.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.client.KeyValueApiClient;
import org.apiphany.http.server.KeyValueHttpServer;
import org.apiphany.io.ContentType;
import org.apiphany.net.Sockets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaNetHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpExchangeClientIT {

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final int API_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	private static final ClientProperties CLIENT_PROPERTIES = new ClientProperties();

	private static final KeyValueHttpServer API_SERVER = new KeyValueHttpServer(API_SERVER_PORT);
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

	@Test
	void shouldReturnHeaders() {
		String value = API_CLIENT.get(KeyValueHttpServer.DEFAULT_KEY);
		Map<String, List<String>> headers = API_CLIENT.head(KeyValueHttpServer.DEFAULT_KEY);

		HttpContentType expectedContentType = HttpContentType.of(ContentType.TEXT_PLAIN, StandardCharsets.UTF_8);

		assertThat(Headers.get(HttpHeader.CONTENT_LENGTH, headers), equalTo(List.of(String.valueOf(value.length()))));
		assertThat(Headers.get(HttpHeader.CONTENT_TYPE, headers), equalTo(List.of(expectedContentType.value())));
	}

	@Test
	void shouldReturnOptions() {
		Map<String, List<String>> headers = API_CLIENT.options();

		assertThat(Headers.get(HttpHeader.ALLOW, headers), equalTo(List.of(KeyValueHttpServer.ALLOW_HEADER_VALUE)));
		assertThat(Headers.get(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, headers), equalTo(List.of("*")));
		assertThat(Headers.get(HttpHeader.ACCESS_CONTROL_ALLOW_METHODS, headers), equalTo(List.of(KeyValueHttpServer.ALLOW_HEADER_VALUE)));
		assertThat(Headers.get(HttpHeader.ACCESS_CONTROL_ALLOW_HEADERS, headers), equalTo(List.of(HttpHeader.CONTENT_TYPE.value())));
	}

	@Test
	void shouldReturnMapWhenGettingAllValues() {
		Map<String, String> values = API_CLIENT.getAll();

		assertThat(values, notNullValue());
	}

	@Test
	void shouldReturnTrace() {
		ApiResponse<String> response = API_CLIENT.trace();

		Map<String, List<String>> headers = response.getHeaders();
		assertThat(headers.get(HttpHeader.CONTENT_TYPE.value()), equalTo(List.of(ContentType.MESSAGE_HTTP.value())));

		String body = response.getBody();
		assertThat(body, startsWith(HttpMethod.TRACE.value()));
	}
}
