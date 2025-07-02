package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.ssl.client.Bytes;
import org.apiphany.security.ssl.client.ClientHello;
import org.apiphany.security.ssl.client.CypherSuite;
import org.apiphany.security.ssl.client.CypherSuiteName;
import org.apiphany.security.ssl.client.MinimalTLSClient;
import org.apiphany.security.ssl.server.SimpleHttpsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link SSLProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLPropertiesTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SSLPropertiesTest.class);

	private static final String SSL = "ssl";

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final String LOCALHOST = "localhost";
	private static final String SERVER_HOST = LOCALHOST;
	private static final int SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
	private static final String SERVER_URL = "https://" + SERVER_HOST + ":" + SERVER_PORT;

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

	@Test
	void shouldPerformTLS_V_1_2_SSLHandshake() throws Exception {
		int port = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
		SSLProperties sslProperties = JsonBuilder.fromJson(Strings.fromFile("/ssl-properties.json"), SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		SimpleHttpsServer server = new SimpleHttpsServer(port, sslProperties);

		byte[] serverFinished = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port)) {
			serverFinished = client.performHandshake();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertNotNull(serverFinished);
	}

	@Test
	void shouldBuildClientHello() throws IOException {
		final byte[] byteArray = new byte[] {
				(byte) 0x16, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0xA5, (byte) 0x01, (byte) 0x00, (byte) 0x00,
				(byte) 0xA1, (byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
				(byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C,
				(byte) 0x0D, (byte) 0x0E, (byte) 0x0F, (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14,
				(byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1A, (byte) 0x1B, (byte) 0x1C,
				(byte) 0x1D, (byte) 0x1E, (byte) 0x1F, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0xCC, (byte) 0xA8,
				(byte) 0xCC, (byte) 0xA9, (byte) 0xC0, (byte) 0x2F, (byte) 0xC0, (byte) 0x30, (byte) 0xC0, (byte) 0x2B,
				(byte) 0xC0, (byte) 0x2C, (byte) 0xC0, (byte) 0x13, (byte) 0xC0, (byte) 0x09, (byte) 0xC0, (byte) 0x14,
				(byte) 0xC0, (byte) 0x0A, (byte) 0x00, (byte) 0x9C, (byte) 0x00, (byte) 0x9D, (byte) 0x00, (byte) 0x2F,
				(byte) 0x00, (byte) 0x35, (byte) 0xC0, (byte) 0x12, (byte) 0x00, (byte) 0x0A, (byte) 0x01, (byte) 0x00,
				(byte) 0x00, (byte) 0x58, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x00, (byte) 0x16,
				(byte) 0x00, (byte) 0x00, (byte) 0x13, (byte) 0x65, (byte) 0x78, (byte) 0x61, (byte) 0x6D, (byte) 0x70,
				(byte) 0x6C, (byte) 0x65, (byte) 0x2E, (byte) 0x75, (byte) 0x6C, (byte) 0x66, (byte) 0x68, (byte) 0x65,
				(byte) 0x69, (byte) 0x6D, (byte) 0x2E, (byte) 0x6E, (byte) 0x65, (byte) 0x74, (byte) 0x00, (byte) 0x05,
				(byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x0A, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x1D, (byte) 0x00,
				(byte) 0x17, (byte) 0x00, (byte) 0x18, (byte) 0x00, (byte) 0x19, (byte) 0x00, (byte) 0x0B, (byte) 0x00,
				(byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x0D, (byte) 0x00, (byte) 0x12, (byte) 0x00,
				(byte) 0x10, (byte) 0x04, (byte) 0x01, (byte) 0x04, (byte) 0x03, (byte) 0x05, (byte) 0x01, (byte) 0x05,
				(byte) 0x03, (byte) 0x06, (byte) 0x01, (byte) 0x06, (byte) 0x03, (byte) 0x02, (byte) 0x01, (byte) 0x02,
				(byte) 0x03, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x12,
				(byte) 0x00, (byte) 0x00
		};
		final CypherSuiteName[] cypherSuitesArray = {
				CypherSuiteName.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
				CypherSuiteName.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
				CypherSuiteName.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
				CypherSuiteName.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
				CypherSuiteName.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
				CypherSuiteName.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
				CypherSuiteName.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
				CypherSuiteName.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
				CypherSuiteName.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
				CypherSuiteName.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
				CypherSuiteName.TLS_RSA_WITH_AES_128_GCM_SHA256,
				CypherSuiteName.TLS_RSA_WITH_AES_256_GCM_SHA384,
				CypherSuiteName.TLS_RSA_WITH_AES_128_CBC_SHA,
				CypherSuiteName.TLS_RSA_WITH_AES_256_CBC_SHA,
				CypherSuiteName.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,
				CypherSuiteName.TLS_RSA_WITH_3DES_EDE_CBC_SHA
		};
		List<CypherSuite> cypherSuites = List.of(cypherSuitesArray).stream().map(CypherSuite::new).toList();

		ClientHello clientHello = new ClientHello(List.of("example.ulfheim.net"), cypherSuites);

		byte[] bytes = clientHello.toByteArray();

		LOGGER.info("Expected: {}", Bytes.hexString(byteArray));
		LOGGER.info("Received: {}", Bytes.hexString(bytes));
		for (int i = 0; i < bytes.length; ++i) {
			boolean valid = bytes[i] == byteArray[i];
			assertTrue(valid);
		}
	}

	@Test
	void test() {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        String[] defaultCiphers = factory.getDefaultCipherSuites();
        String[] supportedCiphers = factory.getSupportedCipherSuites();

        LOGGER.info("Default Cipher Suites:");
        for (String cipher : defaultCiphers) {
        	LOGGER.info(cipher);
        }

        LOGGER.info("\nSupported Cipher Suites:");
        for (String cipher : supportedCiphers) {
        	LOGGER.info(cipher);
        }
		assertTrue(true);
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
