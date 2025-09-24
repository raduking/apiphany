package org.apiphany.security.ssl.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.ssl.SSLContextAdapter;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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
			"TLS_RSA_WITH_RC4_128_SHA"
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
		this.httpsServer.createContext(ROUTE_API_NAME, new NameHandler(this));
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
					// Override the SSL parameters to allow legacy ciphers
					SSLContext c = getSSLContext();
					SSLEngine engine = c.createSSLEngine();
					engine.setEnabledCipherSuites(LEGACY_CIPHER_SUITES);
					params.setCipherSuites(LEGACY_CIPHER_SUITES);
					String[] protocols = new String[] { "TLSv1.2" };
					params.setProtocols(protocols);
					// params.setNeedClientAuth(false);

					LOGGER.debug("HTTPS parameters: {}", JsonBuilder.toJson(params));
				}
			});
			return httpsServer;
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port, e);
		}
	}

	static class NameHandler implements HttpHandler {

		@SuppressWarnings("unused")
		private final LegacyHttpsServer server;

		public NameHandler(final LegacyHttpsServer server) {
			this.server = server;
		}

		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (HttpMethod.GET.matches(exchange.getRequestMethod())) {
				sendResponse(exchange, HttpStatus.OK, LegacyHttpsServer.NAME);
			} else {
				exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
			}
		}

		private static <T> void sendResponse(final HttpExchange exchange, final HttpStatus status, final T response) throws IOException {
			String responseString = Strings.safeToString(response);
			exchange.sendResponseHeaders(status.getCode(), responseString.length());
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(responseString.getBytes(StandardCharsets.UTF_8));
			}
		}
	}
}
