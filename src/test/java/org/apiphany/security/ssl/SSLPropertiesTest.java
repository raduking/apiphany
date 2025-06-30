package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import javax.net.ssl.SSLHandshakeException;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.ssl.server.SimpleHttpsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SSLProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLPropertiesTest {

	private static final String SSL = "ssl";

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final int SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
	private static final String SERVER_URL = "https://localhost:" + SERVER_PORT;

	private static final SSLProperties SSL_PROPERTIES = JsonBuilder.fromJson(Strings.fromFile("/ssl-properties.json"), SSLProperties.class);
	private static final ClientProperties CLIENT_PROPERTIES = new ClientProperties();
	static {
		CLIENT_PROPERTIES.setCustomProperties(SSL, SSL_PROPERTIES);
	}

	private static final SimpleHttpsServer SERVER = new SimpleHttpsServer(SERVER_PORT, SSL_PROPERTIES);

	@AfterEach
	void tearDown() throws Exception {
		SERVER.close();
	}

	@Test
	void shouldHaveSSLAsRootProperty() {
		assertThat(SSLProperties.ROOT, equalTo(SSL));
	}

	@Test
	void shouldNotGetNameIfSSLIsNotInitialized() throws Exception {
		int port = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
		SimpleHttpsServer server = new SimpleHttpsServer(port, new SSLProperties());

		Exception exception = null;
		try (SimpleApiClient api = new SimpleApiClient("https://localhost:" + port)) {
			api.getName();
		} catch (Exception e) {
			exception = e;
		} finally {
			server.close();
		}

		assertNotNull(exception);
		assertThat(exception.getClass(), equalTo(SSLHandshakeException.class));
	}

	@Test
	void shouldReturnNameOnGetNameWithInitializedSSL() throws Exception {
		try (SimpleSSLApiClient api = new SimpleSSLApiClient()) {
			String name = api.getName();

			assertThat(name, equalTo(SimpleHttpsServer.NAME));
		}
	}

	static class SimpleApiClient extends ApiClient {

		public SimpleApiClient(final String url) {
			super(url, with(JavaNetHttpExchangeClient.class));
			setBleedExceptions(true);
		}

		public String getName() {
			return client()
					.http()
					.get()
					.path(API, "name")
					.retrieve(String.class)
					.orNull();
		}

	}

	static class SimpleSSLApiClient extends ApiClient {

		public SimpleSSLApiClient() {
			super(SERVER_URL, with(JavaNetHttpExchangeClient.class).
					properties(CLIENT_PROPERTIES));
		}

		public String getName() {
			return client()
					.http()
					.get()
					.path(API, "name")
					.retrieve(String.class)
					.orNull();
		}

	}
}
