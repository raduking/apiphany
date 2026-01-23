package org.apiphany.utils.security.ssl.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.collections.Lists;
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
 *
 * @author Radu Sebastian LAZIN
 */
public class LegacyHttpsServer implements AutoCloseable {

	private static final String PROPERTY_JDK_TLS_DISABLED_ALGORITHMS = "jdk.tls.disabledAlgorithms";

	private static final Logger LOGGER = LoggerFactory.getLogger(LegacyHttpsServer.class);

	public static final String ROUTE_API_NAME = "/api/name";

	public static final String NAME = "Mumu";

	public static final List<CipherSuite> SUPPORTED_CIPHER_SUITES = List.of(
			CipherSuite.TLS_RSA_WITH_RC4_128_SHA,
			CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
			CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256);

	private static final String[] LEGACY_CIPHER_SUITES = SUPPORTED_CIPHER_SUITES.stream().map(CipherSuite::name).toArray(String[]::new);

	private static final String[] SSL_PROTOCOLS = {
			SSLProtocol.TLS_1_2.value()
	};

	private static final List<String> LEGACY_ALGORITHMS = List.of(
			"RC4",
			"TLS_RSA_*");

	private final HttpsServer httpsServer;
	private final ExecutorService executor;
	private final int port;
	private final SSLContextAdapter sslContext;

	private final String originalDisabledAlgorithms;

	public LegacyHttpsServer(final int port, final SSLProperties sslProperties, final SecureRandom secureRandom) {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		// remove RC4 from disabled algorithms
		this.originalDisabledAlgorithms = Security.getProperty(PROPERTY_JDK_TLS_DISABLED_ALGORITHMS);
		String disabled = this.originalDisabledAlgorithms;
		LOGGER.info("Disabled algorithms before: {}", disabled);

		List<String> disabledAlgorithms = new ArrayList<>();
		for (String legacyAlgorithm : LEGACY_ALGORITHMS) {
			if (disabled.contains(legacyAlgorithm)) {
				disabled = disabled.replace(legacyAlgorithm + ", ", "").replace(legacyAlgorithm, "");
				LOGGER.warn("'{}' was re-enabled for TLS (INSECURE).", legacyAlgorithm);
				disabledAlgorithms.add(legacyAlgorithm);
			}
		}
		if (Lists.isNotEmpty(disabledAlgorithms)) {
			Security.setProperty(PROPERTY_JDK_TLS_DISABLED_ALGORITHMS, disabled);
		}
		LOGGER.info("Disabled algorithms after: {}", disabled);

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
			// restore global security property
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
				public void configure(final HttpsParameters params) {
					SSLParameters sslParameters = new SSLParameters(LEGACY_CIPHER_SUITES, SSL_PROTOCOLS);
					params.setSSLParameters(sslParameters);
					LOGGER.debug("HTTPS parameters: {}", JsonBuilder.toJson(params));
				}
			});
			return httpsServer;
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port, e);
		}
	}
}
