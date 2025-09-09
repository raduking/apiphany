package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apiphany.ApiClient;
import org.apiphany.ApiClientFluentAdapter;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.io.ContentType;
import org.apiphany.net.Sockets;
import org.apiphany.server.KeyValueHttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

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

	private static final byte[] BYTES = new byte[] { 0x01, 0x02, 0x03 };

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

		assertThat(Headers.get(HttpHeader.CONTENT_LENGTH, headers), equalTo(List.of(String.valueOf(value.length()))));
		assertThat(Headers.get(HttpHeader.CONTENT_TYPE, headers), equalTo(List.of("text/plain; charset=utf-8")));
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

	@Test
	void shouldConvertByteArrayToByteArrayBodyPublisher() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(API_CLIENT)
				.body(BYTES);

		BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

		assertThat(bodyPublisher.contentLength(), equalTo((long) BYTES.length));

		TestBodySubscriber subscriber = new TestBodySubscriber();
		bodyPublisher.subscribe(subscriber);
		subscriber.awaitCompletion();

		assertTrue(subscriber.isCompleted());
		assertNull(subscriber.getError());
		assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
	}

	@Test
	void shouldConvertInputStreamToInputStreamBodyPublisher() {
		ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(API_CLIENT)
				.body(bis);

		BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

		TestBodySubscriber subscriber = new TestBodySubscriber();
		bodyPublisher.subscribe(subscriber);
		subscriber.awaitCompletion();

		assertTrue(subscriber.isCompleted());
		assertNull(subscriber.getError());
		assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
	}

	@Test
	void shouldConvertInputStreamSupplierToInputStreamBodyPublisher() {
		ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);
		Supplier<? extends InputStream> supplier = () -> bis;

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(API_CLIENT)
				.body(supplier);

		BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

		TestBodySubscriber subscriber = new TestBodySubscriber();
		bodyPublisher.subscribe(subscriber);
		subscriber.awaitCompletion();

		assertTrue(subscriber.isCompleted());
		assertNull(subscriber.getError());
		assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
	}

	@Test
	void shouldConvertStringToStringBodyPublisher() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(API_CLIENT)
				.body(NEW_VALUE_1);

		BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

		assertThat(bodyPublisher.contentLength(), equalTo((long) NEW_VALUE_1.length()));

		TestBodySubscriber subscriber = new TestBodySubscriber();
		bodyPublisher.subscribe(subscriber);
		subscriber.awaitCompletion();

		assertTrue(subscriber.isCompleted());
		assertNull(subscriber.getError());
		assertThat(NEW_VALUE_1.getBytes(StandardCharsets.UTF_8), equalTo(subscriber.getReceivedBytes()));
	}

	static class SimpleApiClient extends ApiClient {

		private static final String KEYS = "keys";

		private static final GenericClass<Map<String, String>> MAP_TYPE = typeObject();

		protected SimpleApiClient(final ClientProperties properties) {
			super("http://localhost:" + API_SERVER.getPort(),
					with(JavaNetHttpExchangeClient.class)
							.properties(properties));
		}

		public String get(final String key) {
			return client()
					.http()
					.get()
					.path(API, KEYS, key)
					.retrieve(String.class)
					.orNull();
		}

		public Map<String, String> getAll() {
			return client()
					.http()
					.get()
					.path(API, KEYS)
					.retrieve(MAP_TYPE)
					.orNull();
		}

		public String set(final String key, final String value) {
			return client()
					.http()
					.put()
					.path(API, KEYS, key)
					.body(value)
					.retrieve(String.class)
					.orNull();
		}

		public String add(final String key, final String value) {
			return client()
					.http()
					.post()
					.path(API, KEYS)
					.body(key + ":" + value)
					.retrieve(String.class)
					.orNull();
		}

		public String delete(final String key) {
			return client()
					.http()
					.delete()
					.path(API, KEYS, key)
					.retrieve(String.class)
					.orNull();
		}

		public String append(final String key, final String appended) {
			return client()
					.http()
					.patch()
					.path(API, KEYS, key)
					.body(appended)
					.retrieve(String.class)
					.orNull();
		}

		public Map<String, List<String>> head(final String key) {
			return client()
					.http()
					.head()
					.path(API, KEYS, key)
					.retrieve()
					.getHeaders();
		}

		public Map<String, List<String>> options() {
			return client()
					.http()
					.options()
					.path(API, KEYS)
					.retrieve()
					.getHeaders();
		}

		public ApiResponse<String> trace() {
			return client()
					.http()
					.trace()
					.path(API, KEYS)
					.retrieve(String.class);
		}
	}
}
