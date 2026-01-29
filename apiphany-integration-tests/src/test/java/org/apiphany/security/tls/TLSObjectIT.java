package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.security.KeyPair;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import org.apiphany.ApiClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.KeyPairs;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.ssl.server.BasicHttpsServer;
import org.apiphany.security.ssl.server.NameHandler;
import org.apiphany.security.tls.client.MinimalTLSClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link TLSObject} hierarchy.
 *
 * @author Radu Sebastian LAZIN
 */
class TLSObjectIT {

	private static final Logger LOGGER = LoggerFactory.getLogger(TLSObjectIT.class);

	private static final String LOCALHOST = "localhost";
	private static final Duration DEBUG_SOCKET_TIMEOUT = Duration.ofMinutes(3);

	private static final KeyPair CLIENT_KEY_PAIR = KeyPairs.loadKeyPairFromResources();
	private static final String SSL_PROPERTIES_JSON = Strings.fromFile("security/ssl/ssl-properties.json");
	private static final List<CipherSuite> CIPHER_SUITES = List.of(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384);

	private static Stream<Arguments> provideSupportedCipherSuites() {
		return Stream.of(
				Arguments.of(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384),
				Arguments.of(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256));
	}

	@ParameterizedTest
	@MethodSource("provideSupportedCipherSuites")
	void shouldPerformTLS12HandshakeAndGetName(final CipherSuite cipherSuite) throws Exception {
		int port = Sockets.findAvailableTcpPort();
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);

		BasicHttpsServer server = new BasicHttpsServer(port, sslProperties);

		String response = null;
		List<CipherSuite> cipherSuites = List.of(cipherSuite);
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR, cipherSuites)) {
			byte[] serverFinished = client.performHandshake();
			assertNotNull(serverFinished);
			response = client.get("/" + ApiClient.API + "/name");
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertThat(response, equalTo(NameHandler.NAME));
	}

	@Disabled("This test is here to debug errors, the same is tested in shouldPerformTLS12HandshakeAndGetName")
	@Test
	void shouldPerformTLS12HandshakeWithECDHERSAAES256GCMSHA384() throws Exception {
		int port = Sockets.findAvailableTcpPort();
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);

		TLSLoggingProvider.install();
		BasicHttpsServer server = new BasicHttpsServer(port, sslProperties);

		String response = null;
		List<CipherSuite> cipherSuites = List.of(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384);
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, DEBUG_SOCKET_TIMEOUT, CLIENT_KEY_PAIR, cipherSuites)) {
			byte[] serverFinished = client.performHandshake();
			assertNotNull(serverFinished);
			response = client.get("/" + ApiClient.API + "/name");
		} finally {
			server.close();
		}
		assertThat(response, equalTo(NameHandler.NAME));
	}

	@Test
	void shouldPerformTLS12HandshakeWithCloseNotify() throws Exception {
		int port = Sockets.findAvailableTcpPort();
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		BasicHttpsServer server = new BasicHttpsServer(port, sslProperties);

		byte[] closeNotify = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR, CIPHER_SUITES)) {
			client.performHandshake();
			closeNotify = client.closeNotify();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertNotNull(closeNotify);
	}

	@Test
	void shouldPerformTLS12HandshakeWithECDHERSAAES256GCMSHA384WithOpenSSL() {
		// assumes OpenSSL is running on port 4433 you can run it with the command described in the keystore-generation.md file.
		// TODO: move this to an integration test module
		int port = 4433;
		assumeTrue(Sockets.canConnectTo(LOCALHOST, port), LOCALHOST + ":" + port + " is unreachable, skipping test.");

		byte[] serverFinished = null;
		List<CipherSuite> cipherSuites = List.of(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384);
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR, cipherSuites)) {
			serverFinished = client.performHandshake();
			client.closeNotify();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		}
		assertNotNull(serverFinished);
	}

	@Test
	void shouldPerformTLS12HandshakeWithAES128CBCSHAWithOpenSSL() throws Exception {
		// assumes OpenSSL is running on port 4433 you can run it with the command described in the keystore-generation.md file.
		// TODO: move this to an integration test module
		int port = 4433;
		assumeTrue(Sockets.canConnectTo(LOCALHOST, port), LOCALHOST + ":" + port + " is unreachable, skipping test.");

		byte[] serverFinished = null;
		List<CipherSuite> cipherSuites = List.of(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR, cipherSuites)) {
			serverFinished = client.performHandshake();
		}
		assertNotNull(serverFinished);
	}

	@Test
	void shouldPerformTLS12HandshakeWithGoogle() {
		int port = 443;
		String host = "google.com";
		assumeTrue(Sockets.canConnectTo(host, port), host + " is unreachable, skipping test.");

		String response = null;
		try (MinimalTLSClient client = new MinimalTLSClient(host, port, CLIENT_KEY_PAIR, CIPHER_SUITES)) {
			client.performHandshake();
			response = client.get("/");
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		}
		assertNotNull(response);
	}

	@Test
	void shouldPerformTLS12HandshakeWithWwwGoogleCom() {
		assumeTrue("true".equals(System.getProperty("test.tls.chunked")));
		int port = 443;
		String host = "www.google.com";
		assumeTrue(Sockets.canConnectTo(host, port), host + " is unreachable, skipping test.");

		String response = null;
		try (MinimalTLSClient client = new MinimalTLSClient(host, port, CLIENT_KEY_PAIR, CIPHER_SUITES)) {
			client.performHandshake();
			response = client.get("/");
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		}
		LOGGER.debug("Full response:\n{}", response);
		assertNotNull(response);
	}
}
