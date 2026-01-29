package org.apiphany.security.ssl.server;

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
import org.apiphany.security.ssl.SSLContextAdapter;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.tls.CipherSuite;
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

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LegacyHttpsServer.class);

	/**
	 * Security property for disabled TLS algorithms.
	 */
	private static final String PROPERTY_JDK_TLS_DISABLED_ALGORITHMS = "jdk.tls.disabledAlgorithms";

	/**
	 * API route for name endpoint.
	 */
	public static final String ROUTE_API_NAME = "/api/name";

	/**
	 * Supported legacy cipher suites.
	 */
	@SuppressWarnings("deprecation")
	public static final List<CipherSuite> SUPPORTED_CIPHER_SUITES = List.of(
			CipherSuite.TLS_RSA_WITH_RC4_128_SHA,
			CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
			CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256);

	/**
	 * Legacy cipher suites as string array.
	 */
	private static final String[] LEGACY_CIPHER_SUITES = SUPPORTED_CIPHER_SUITES.stream().map(CipherSuite::name).toArray(String[]::new);

	/**
	 * Supported SSL protocols.
	 */
	private static final String[] SSL_PROTOCOLS = {
			SSLProtocol.TLS_1_2.value()
	};

	/**
	 * Legacy algorithms to be re-enabled.
	 */
	private static final List<String> LEGACY_ALGORITHMS = List.of(
			"RC4",
			"TLS_RSA_*");

	/**
	 * Underlying HTTPS server.
	 */
	private final HttpsServer httpsServer;

	/**
	 * Executor service for handling requests.
	 */
	private final ExecutorService executor;

	/**
	 * Port on which the server is running.
	 */
	private final int port;

	/**
	 * SSL context for the server.
	 */
	private final SSLContextAdapter sslContext;

	/**
	 * Original disabled algorithms before modification.
	 */
	private final String originalDisabledAlgorithms;

	/**
	 * Constructor to create and start the HTTPS server on the specified port with the given SSL properties and secure
	 * random.
	 *
	 * @param port the port number on which the server will listen
	 * @param sslProperties the SSL properties for configuring the SSL context
	 * @param secureRandom the secure random instance for SSL context
	 */
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

	/**
	 * Constructor to create and start the HTTPS server on the specified port with the given SSL properties.
	 *
	 * @param port the port number on which the server will listen
	 * @param sslProperties the SSL properties for configuring the SSL context
	 */
	public LegacyHttpsServer(final int port, final SSLProperties sslProperties) {
		this(port, sslProperties, new SecureRandom());
	}

	/**
	 * @see AutoCloseable#close()
	 */
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

	/**
	 * Returns the port on which the server is running.
	 *
	 * @return the port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Creates an HTTPS server on the specified port with the given SSL context.
	 *
	 * @param port the port number
	 * @param sslContext the SSL context
	 * @return the created HTTPS server
	 */
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
