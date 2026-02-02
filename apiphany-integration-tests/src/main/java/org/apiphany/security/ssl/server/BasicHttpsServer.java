package org.apiphany.security.ssl.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLContextAdapter;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/**
 * A simple HTTPS server that has only one endpoint.
 * <p>
 * This server provides a single route: {@code /api/name} which returns the string {@code "Mumu"}.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicHttpsServer implements AutoCloseable {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicHttpsServer.class);

	/**
	 * API route for name endpoint.
	 */
	public static final String ROUTE_API_NAME = "/api/name";

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
	 * Constructor to create and start the HTTPS server on the specified port with the given SSL properties and secure
	 * random.
	 *
	 * @param port the port number on which the server will listen
	 * @param sslProperties the SSL properties for configuring the SSL context
	 * @param secureRandom the secure random instance for SSL context
	 */
	public BasicHttpsServer(final int port, final SSLProperties sslProperties, final SecureRandom secureRandom) {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		this.sslContext = new SSLContextAdapter(SSLContexts.create(sslProperties));
		this.sslContext.setSecureRandom(secureRandom);

		this.httpsServer = createHttpsServer(port, sslContext);
		this.httpsServer.createContext(ROUTE_API_NAME, new NameHandler<>(this));
		this.httpsServer.setExecutor(executor);
		this.httpsServer.start();

		this.port = port;

		LOGGER.info("Server started on port: {}", port);
	}

	/**
	 * Constructor to create and start the HTTPS server on the specified port with the given SSL properties.
	 *
	 * @param port the port number on which the server will listen
	 * @param sslProperties the SSL properties for configuring the SSL context
	 */
	public BasicHttpsServer(final int port, final SSLProperties sslProperties) {
		this(port, sslProperties, new SecureRandom());
	}

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		httpsServer.stop(0);
		executor.close();
	}

	/**
	 * Gets the SSL context used by the server.
	 *
	 * @return the SSL context
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
					super.configure(params);
					log(params);
				}
			});
			return httpsServer;
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port);
		}
	}

	/**
	 * Logs the HTTPS parameters.
	 *
	 * @param params the HTTPS parameters
	 */
	public static void log(final HttpsParameters params) {
		try {
			String httpParameters = JsonBuilder.toJson(params);
			LOGGER.debug("HTTPS parameters: {}", httpParameters);
		} catch (Exception e) {
			LOGGER.debug("Module not open. Add --add-opens jdk.httpserver/sun.net.httpserver=ALL-UNNAMED", e);
		}
	}
}
