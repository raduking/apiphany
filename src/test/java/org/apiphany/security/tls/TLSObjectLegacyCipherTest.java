package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyPair;
import java.time.Duration;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.ssl.Keys;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.ssl.server.LegacyHttpsServer;
import org.apiphany.security.tls.client.MinimalTLSClient;
import org.apiphany.utils.ForkedJvmExtension;
import org.apiphany.utils.ForkedJvmTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test class for {@link TLSObject} hierarchy with legacy cipher suite.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(ForkedJvmExtension.class)
class TLSObjectLegacyCipherTest {

	private static final String LOCALHOST = "localhost";
	private static final Duration DEBUG_SOCKET_TIMEOUT = Duration.ofMinutes(3);

	private static final KeyPair CLIENT_KEY_PAIR = Keys.loadKeyPairFromResources();
	private static final String SSL_PROPERTIES_JSON = Strings.fromFile("/security/ssl/ssl-properties.json");

	@Test
	@ForkedJvmTest(
		jvmArgs = {
				"-Xmx64m",
				"-Xms64m",
				"-XX:+UseSerialGC",
				"-XX:+TieredCompilation",
				"-XX:TieredStopAtLevel=1",
				"--add-opens", "java.base/javax.net.ssl=ALL-UNNAMED",
				"--add-opens", "java.base/javax.crypto=ALL-UNNAMED",
				"--add-opens", "java.base/sun.security.internal.spec=ALL-UNNAMED",
				"--add-opens", "java.base/com.sun.crypto.provider=ALL-UNNAMED",
				"--add-opens", "jdk.httpserver/sun.net.httpserver=ALL-UNNAMED",
				"--add-opens", "java.base/sun.security.ssl=ALL-UNNAMED" })
	void shouldPerformTLS12HandshakeWithRC4128SHA() throws Exception {
		int port = Sockets.findAvailableTcpPort();
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);

		TLSLoggingProvider.install();
		LegacyHttpsServer server = new LegacyHttpsServer(port, sslProperties);

		byte[] serverFinished = null;
		List<CipherSuite> cipherSuites = List.of(CipherSuite.TLS_RSA_WITH_RC4_128_SHA);
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, DEBUG_SOCKET_TIMEOUT, CLIENT_KEY_PAIR, cipherSuites)) {
			serverFinished = client.performHandshake();
		} finally {
			server.close();
		}
		assertNotNull(serverFinished);
	}
}
