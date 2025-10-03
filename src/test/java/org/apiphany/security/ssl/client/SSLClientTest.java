package org.apiphany.security.ssl.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.net.ssl.SSLHandshakeException;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.HttpException;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.utils.security.ssl.server.BasicHttpsServer;
import org.apiphany.utils.security.ssl.server.NameHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SSLProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLClientTest {

	private static final String SSL = "ssl";

	private static final String LOCALHOST = "localhost";
	private static final String SERVER_HOST = LOCALHOST;
	private static final int SERVER_PORT = Sockets.findAvailableTcpPort();
	private static final String SERVER_URL = "https://" + SERVER_HOST + ":" + SERVER_PORT;

	private static final String SSL_PROPERTIES_JSON = Strings.fromFile("/security/ssl/ssl-properties.json");
	private static final SSLProperties SSL_PROPERTIES = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
	private static final ClientProperties CLIENT_PROPERTIES = new ClientProperties();
	static {
		CLIENT_PROPERTIES.setCustomProperties(SSL, SSL_PROPERTIES);
	}

	private static final BasicHttpsServer SERVER = new BasicHttpsServer(SERVER_PORT, SSL_PROPERTIES);

	@AfterAll
	static void tearDownAll() throws Exception {
		SERVER.close();
	}

	@Test
	void shouldReturnNameOnGetNameWithInitializedSSL() throws Exception {
		try (SimpleSSLApiClient api = new SimpleSSLApiClient()) {
			String name = api.getName();

			assertThat(name, equalTo(NameHandler.NAME));
		}
	}

	@Test
	void shouldNotGetNameIfSSLIsNotInitialized() throws Exception {
		int port = Sockets.findAvailableTcpPort();
		BasicHttpsServer server = new BasicHttpsServer(port, new SSLProperties());

		Exception exception = null;
		try (SimpleApiClient api = new SimpleApiClient("https://localhost:" + port)) {
			api.getName();
		} catch (Exception e) {
			exception = e;
		} finally {
			server.close();
		}

		assertNotNull(exception);
		assertThat(exception.getClass(), equalTo(HttpException.class));
		Throwable cause = exception.getCause();
		assertThat(cause.getClass(), equalTo(SSLHandshakeException.class));
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
			super(SERVER_URL, with(JavaNetHttpExchangeClient.class).properties(CLIENT_PROPERTIES));
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
