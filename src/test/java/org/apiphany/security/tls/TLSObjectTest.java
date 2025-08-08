package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Hex;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.ssl.Keys;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.ssl.server.SimpleHttpsServer;
import org.apiphany.security.tls.client.MinimalTLSClient;
import org.apiphany.security.tls.client.PseudoRandomFunction;
import org.junit.jupiter.api.AfterAll;
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
class TLSObjectTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(TLSObjectTest.class);

	private static final String SSL = "ssl";
	private static final String HMAC_SHA384 = "HmacSHA384";

	private static final String LOCALHOST = "localhost";
	private static final int SERVER_PORT = Sockets.findAvailableTcpPort();

	private static final KeyPair CLIENT_KEY_PAIR = Keys.loadKeyPairFromResources();
	private static final String SSL_PROPERTIES_JSON = Strings.fromFile("/security/ssl/ssl-properties.json");
	private static final SSLProperties SSL_PROPERTIES = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
	private static final ClientProperties CLIENT_PROPERTIES = new ClientProperties();
	static {
		CLIENT_PROPERTIES.setCustomProperties(SSL, SSL_PROPERTIES);
	}
	private static final List<CipherSuite> CIPHER_SUITES = List.of(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384);

	private static final SimpleHttpsServer SERVER = new SimpleHttpsServer(SERVER_PORT, SSL_PROPERTIES);

	@AfterAll
	static void tearDownAll() throws Exception {
		SERVER.close();
	}

	@Test
	void shouldPerformTLS12HandshakeWithSupportedCipherSuites() throws Exception {
		int port = Sockets.findAvailableTcpPort();
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		SimpleHttpsServer server = new SimpleHttpsServer(port, sslProperties);

		byte[] serverFinished = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR, CIPHER_SUITES)) {
			serverFinished = client.performHandshake();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertNotNull(serverFinished);
	}

	@ParameterizedTest
	@MethodSource("provideSupportedCipherSuites")
	void shouldPerformTLS12HandshakeWithParameterizedCipherSuites(final CipherSuite cipherSuite) throws Exception {
		int port = Sockets.findAvailableTcpPort();
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		SimpleHttpsServer server = new SimpleHttpsServer(port, sslProperties);

		byte[] serverFinished = null;
		List<CipherSuite> cipherSuites = List.of(cipherSuite);
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR, cipherSuites)) {
			serverFinished = client.performHandshake();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertNotNull(serverFinished);
	}

	private static Stream<Arguments> provideSupportedCipherSuites() {
		return Stream.of(
				Arguments.of(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384),
				Arguments.of(CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384)
		);
	}

	@Test
	void shouldPerformTLS12HandshakeWithCloseNotify() throws Exception {
		int port = Sockets.findAvailableTcpPort();
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		SimpleHttpsServer server = new SimpleHttpsServer(port, sslProperties);

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
	void shouldPerformTLS12HandshakeAndGetName() throws Exception {
		int port = Sockets.findAvailableTcpPort();
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		SimpleHttpsServer server = new SimpleHttpsServer(port, sslProperties);

		String response = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR, CIPHER_SUITES)) {
			client.performHandshake();
			response = client.get("/" + ApiClient.API + "/name");
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertThat(response, equalTo(SimpleHttpsServer.NAME));
	}

	@Disabled("Run only when OpenSSL is running on port 4433")
	@Test
	void shouldPerformTLS12HandshakeOpenSSL() throws Exception {
		int port = 4433;

		byte[] serverFinished = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR, CIPHER_SUITES)) {
			serverFinished = client.performHandshake();
			client.closeNotify();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		}
		assertNotNull(serverFinished);
	}

	@Test
	void shouldPerformTLS12HandshakeWithGoogle() throws Exception {
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
	void shouldPerformTLS12HandshakeWithWwwGoogleCom() throws Exception {
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

	@ParameterizedTest
	@MethodSource("providePRFArguments")
	void shouldApplyTheCorrectPseudoRandomFunction(final String label, final String seed, final int length, final String expected)
			throws Exception {
		byte[] secret = new byte[48];
		Arrays.fill(secret, (byte) 0x0b);

		byte[] seedBytes = seed.getBytes(StandardCharsets.US_ASCII);
		byte[] output = PseudoRandomFunction.apply(secret, label, seedBytes, length, HMAC_SHA384);

		String hexOutput = Hex.string(output).toLowerCase().trim();

		LOGGER.info("PRF Output: {}", hexOutput);
		LOGGER.info("PRF Expect: {}", expected);

		assertThat(hexOutput, equalTo(expected));
	}

	private static Stream<Arguments> providePRFArguments() {
		return Stream.of(
				Arguments.of(
						"test label",
						"test seed",
						32,
						"cc 3a 20 27 3a 70 78 6a 85 65 6d 30 c0 ad 0c 7b "
								+ "e2 0b fd 51 d5 d1 5c 43 82 25 d8 fb 6a 94 82 f1"),
				Arguments.of(
						"another label",
						"different seed",
						48,
						"e1 1d fa 82 07 7c 04 77 2f b6 4a 1a 1a e5 40 59 "
								+ "cc 3b 19 9d f6 4b 50 23 28 50 08 7f ee 74 b8 68 "
								+ "63 90 53 de bf 4e 6f ce a8 a8 a3 34 7e f2 9d 7b"),
				Arguments.of(
						"label3",
						"seed3",
						64,
						"91 56 02 f6 ba 3f 29 ae 16 6c d7 26 e9 aa e5 16 "
								+ "ec 6b e9 01 29 e5 a9 11 2f ed f3 f1 bd 09 b2 a2 "
								+ "67 a1 ee 35 f5 ab da 72 97 4e 6d 41 87 ca d1 84 "
								+ "c5 c7 73 75 4f 8e 03 61 83 1e 26 c3 09 5c f9 2a"),
				Arguments.of(
						"test label",
						"test seed",
						16,
						"cc 3a 20 27 3a 70 78 6a 85 65 6d 30 c0 ad 0c 7b"));
	}
}
