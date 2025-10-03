package org.apiphany.utils.security.ssl.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.tls.CipherSuite;
import org.apiphany.utils.security.ssl.SSLContextAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/**
 * HTTPS server that allows enabling legacy cipher suites such as RC4.
 * <p>
 * WARNING: This should only be used for testing purposes.
 */
public class LegacyHttpsServer implements AutoCloseable {

	private static final String PROPERTY_JDK_TLS_DISABLED_ALGORITHMS = "jdk.tls.disabledAlgorithms";

	private static final Logger LOGGER = LoggerFactory.getLogger(LegacyHttpsServer.class);

	public static final String ROUTE_API_NAME = "/api/name";

	public static final String NAME = "Mumu";

	private static final String[] LEGACY_CIPHER_SUITES = {
			CipherSuite.TLS_RSA_WITH_RC4_128_SHA.name()
	};

	private final HttpsServer httpsServer;
	private final ExecutorService executor;
	private final int port;
	private final SSLContextAdapter sslContext;

	private final String originalDisabledAlgorithms;

	public LegacyHttpsServer(final int port, final SSLProperties sslProperties, SecureRandom secureRandom) {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		// Remove RC4 from disabled algorithms
		this.originalDisabledAlgorithms = Security.getProperty(PROPERTY_JDK_TLS_DISABLED_ALGORITHMS);
		String disabled = this.originalDisabledAlgorithms;
		LOGGER.info("Disabled algorithms before: {}", disabled);
		if (disabled.contains("RC4")) {
			disabled = disabled.replace("RC4, ", "").replace("RC4", "");
			Security.setProperty(PROPERTY_JDK_TLS_DISABLED_ALGORITHMS, disabled);
			LOGGER.warn("RC4 was re-enabled for TLS (INSECURE).");
		}

		this.sslContext = new SSLContextAdapter(SSLContexts.create(sslProperties));
		this.sslContext.setSecureRandom(secureRandom);

		this.httpsServer = createHttpsServer(port, sslContext);
		this.httpsServer.createContext(ROUTE_API_NAME, new NameHandler<>(this));
		this.httpsServer.setExecutor(executor);
		this.httpsServer.start();

		this.port = port;

		LOGGER.info("Legacy TLS Server started on port: {}", port);
		LOGGER.info("Enabled ciphers: {}", Arrays.toString(LEGACY_CIPHER_SUITES));
	}

	public LegacyHttpsServer(final int port, final SSLProperties sslProperties) {
		this(port, sslProperties, new SecureRandom());
	}

	@Override
	public void close() {
		try {
			httpsServer.stop(0);
			executor.close();
		} finally {
			// Restore global security property
			if (originalDisabledAlgorithms != null) {
				Security.setProperty(PROPERTY_JDK_TLS_DISABLED_ALGORITHMS, originalDisabledAlgorithms);
				LOGGER.info("Restored {}: {}", PROPERTY_JDK_TLS_DISABLED_ALGORITHMS, originalDisabledAlgorithms);
			}
		}
	}

	public int getPort() {
		return port;
	}

	private static HttpsServer createHttpsServer(final int port, final SSLContext sslContext) {
		try {
			HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
			httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				@Override
				public void configure(HttpsParameters params) {
					params.setCipherSuites(LEGACY_CIPHER_SUITES);
					params.setProtocols(new String[] {
							SSLProtocol.TLS_1_2.value()
					});
					LOGGER.debug("HTTPS parameters: {}", JsonBuilder.toJson(params));
				}
			});
			return httpsServer;
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port, e);
		}
	}
}
