package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyPair;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Pair;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.KeyPairs;
import org.apiphany.security.ssl.ForkedHttpsServerRunner;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.ssl.server.LegacyHttpsServer;
import org.apiphany.security.tls.client.MinimalTLSClient;
import org.apiphany.test.fork.ForkedJvmExtension;
import org.apiphany.test.fork.ForkedJvmTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link TLSObject} hierarchy with legacy cipher suite.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(ForkedJvmExtension.class)
class TLSObjectLegacyCipherIT {

	private static final String SSL_PROPERTIES_JSON_FILE = "security/ssl/ssl-properties.json";

	private static final Duration DEBUG_SOCKET_TIMEOUT = Duration.ofMinutes(3);
	private static final KeyPair CLIENT_KEY_PAIR = KeyPairs.loadKeyPairFromResources();

	private static final String LOCALHOST = "localhost";
	private static final int PORT = Sockets.findAvailableTcpPort();

	private static Pair<Process, Thread> serverInfo;

	@BeforeAll
	static void setUpAll() throws Exception {
		serverInfo = ForkedHttpsServerRunner.start(LegacyHttpsServer.class, SSL_PROPERTIES_JSON_FILE, LOCALHOST, PORT, DEBUG_SOCKET_TIMEOUT, false);
	}

	@AfterAll
	static void tearDownAll() throws Exception {
		ForkedHttpsServerRunner.stop(serverInfo);
	}

	@Test
	@ForkedJvmTest(
		jvmArgs = {
				"-Xshare:off",
				"-Xmx64m",
				"-Xms64m",
				"-XX:+UseSerialGC",
				"-XX:+TieredCompilation",
				"-XX:TieredStopAtLevel=1",
				"-Djava.security.egd=file:/dev/urandom",
				"--add-opens", "java.base/javax.net.ssl=ALL-UNNAMED",
				"--add-opens", "java.base/javax.crypto=ALL-UNNAMED",
				"--add-opens", "java.base/sun.security.internal.spec=ALL-UNNAMED",
				"--add-opens", "java.base/com.sun.crypto.provider=ALL-UNNAMED",
				"--add-opens", "jdk.httpserver/sun.net.httpserver=ALL-UNNAMED",
				"--add-opens", "java.base/sun.security.ssl=ALL-UNNAMED" })
	@SuppressWarnings("deprecation")
	void shouldPerformTLS12HandshakeWithRC4128SHA() throws Exception {
		int port = Sockets.findAvailableTcpPort();

		KeyPair clientKeyPair = KeyPairs.loadKeyPairFromResources();
		String sslPropertiesJson = Strings.fromFile(SSL_PROPERTIES_JSON_FILE);

		SSLProperties sslProperties = JsonBuilder.fromJson(sslPropertiesJson, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);

		TLSLoggingProvider.install();
		LegacyHttpsServer server = new LegacyHttpsServer(port, sslProperties);

		byte[] serverFinished = null;
		List<CipherSuite> cipherSuites = List.of(CipherSuite.TLS_RSA_WITH_RC4_128_SHA);
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, DEBUG_SOCKET_TIMEOUT, clientKeyPair, cipherSuites)) {
			serverFinished = client.performHandshake();
		} finally {
			server.close();
		}
		assertNotNull(serverFinished);
	}

	@Test
	@SuppressWarnings("deprecation")
	@Disabled("This test is here to debug errors, the same is tested in shouldPerformTLS12HandshakeWithUnsupportedCipherSuitesWithResetSSL")
	void shouldPerformTLS12HandshakeWithRC4128SHAWithResetSSL() throws Exception {
		byte[] serverFinished =
				ForkedHttpsServerRunner.on(LegacyHttpsServer.class, SSL_PROPERTIES_JSON_FILE, LOCALHOST, DEBUG_SOCKET_TIMEOUT, true, (host, port) -> {
					List<CipherSuite> cipherSuites = List.of(CipherSuite.TLS_RSA_WITH_RC4_128_SHA);
					try (MinimalTLSClient client = new MinimalTLSClient(host, port, DEBUG_SOCKET_TIMEOUT, CLIENT_KEY_PAIR, cipherSuites)) {
						return client.performHandshake();
					}
				});

		assertNotNull(serverFinished);
	}

	@SuppressWarnings("deprecation")
	private static Stream<Arguments> provideUnsupportedCipherSuites() {
		return Stream.of(
				Arguments.of(CipherSuite.TLS_RSA_WITH_RC4_128_SHA),
				Arguments.of(CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384),
				Arguments.of(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA),
				Arguments.of(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256));
	}

	@ParameterizedTest
	@MethodSource("provideUnsupportedCipherSuites")
	void shouldPerformTLS12HandshakeWithUnsupportedCipherSuitesWithResetSSL(final CipherSuite cipherSuite) throws Exception {
		byte[] serverFinished = null;
		List<CipherSuite> cipherSuites = List.of(cipherSuite);
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, PORT, DEBUG_SOCKET_TIMEOUT, CLIENT_KEY_PAIR, cipherSuites)) {
			serverFinished = client.performHandshake();
		}

		assertNotNull(serverFinished);
	}
}
