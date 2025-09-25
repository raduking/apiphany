package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.security.KeyPair;
import java.time.Duration;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitTimeout;
import org.apiphany.net.Sockets;
import org.apiphany.security.ssl.Keys;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.ssl.server.LegacyHttpsServer;
import org.apiphany.security.tls.client.MinimalTLSClient;
import org.apiphany.utils.ForkedJvmExtension;
import org.apiphany.utils.ForkedJvmTest;
import org.apiphany.utils.ForkedLegacyHttpsServerRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link TLSObject} hierarchy with legacy cipher suite.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(ForkedJvmExtension.class)
class TLSObjectLegacyCipherTest {

	private static final String SSL_PROPERTIES_JSON_FILE = "/security/ssl/ssl-properties.json";

	private static final Logger LOGGER = LoggerFactory.getLogger(TLSObjectLegacyCipherTest.class);

	private static final String LOCALHOST = "localhost";
	private static final Duration DEBUG_SOCKET_TIMEOUT = Duration.ofMinutes(3);

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
	void shouldPerformTLS12HandshakeWithRC4128SHA() throws Exception {
		int port = Sockets.findAvailableTcpPort();

		KeyPair clientKeyPair = Keys.loadKeyPairFromResources();
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
	void shouldPerformTLS12HandshakeWithRC4128SHAWithResetSSL() throws Exception {
		int port = Sockets.findAvailableTcpPort();

		String[] cmd = new String[] {
				"java",
				"--add-opens", "java.base/sun.security.ssl=ALL-UNNAMED",
				"--add-opens", "java.base/javax.net.ssl=ALL-UNNAMED",
				"-cp", System.getProperty("java.class.path"),
				ForkedLegacyHttpsServerRunner.class.getName(),
				String.valueOf(port),
				SSL_PROPERTIES_JSON_FILE
		};
		Process serverProcess = new ProcessBuilder(cmd)
				.redirectErrorStream(true)
				.start();

		Thread loggingThread = Thread.ofVirtual().start(() -> {
			try (InputStream is = serverProcess.getInputStream()) {
				is.transferTo(System.out);
			} catch (Exception e) {
				LOGGER.error("Error logging", e);
			}
		});

		Retry retry = Retry.of(WaitTimeout.of(DEBUG_SOCKET_TIMEOUT, Duration.ofMillis(200)));
		boolean canConnect = retry.when(() -> Sockets.canConnectTo(LOCALHOST, port), Boolean::booleanValue);

		byte[] serverFinished = null;
		try {
			if (canConnect) {
				KeyPair clientKeyPair = Keys.loadKeyPairFromResources();
				List<CipherSuite> cipherSuites = List.of(CipherSuite.TLS_RSA_WITH_RC4_128_SHA);
				try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, DEBUG_SOCKET_TIMEOUT, clientKeyPair, cipherSuites)) {
					serverFinished = client.performHandshake();
				}
			} else {
				throw new IllegalStateException("Cannot connect to server: " + LOCALHOST + ":" + port);
			}
		} finally {
			serverProcess.destroy();
			if (null != loggingThread) {
				loggingThread.join();
			}
		}

		assertNotNull(serverFinished);
	}
}
