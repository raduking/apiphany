package org.apiphany.security.ssl.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

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
 * A simple HTTPS server that has only one endpoint.
 * <p>
 * This server provides a single route: {@code /api/name} which returns the string {@code "Mumu"}.
 *
 * @author Radu Sebastian LAZIN
 */
public class SimpleHttpsServer implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpsServer.class);

	public static final String ROUTE_API_NAME = "/api/name";

	public static final String NAME = "Mumu";

	private final HttpsServer httpsServer;
	private final ExecutorService executor;
	private final int port;
	private final SSLContextAdapter sslContext;

	public SimpleHttpsServer(final int port, final SSLProperties sslProperties, SecureRandom secureRandom) {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		this.sslContext = new SSLContextAdapter(SSLContexts.create(sslProperties));
		this.sslContext.setSecureRandom(secureRandom);

		this.httpsServer = createHttpsServer(port, sslContext);
		this.httpsServer.createContext(ROUTE_API_NAME, new NameHandler(this));
		this.httpsServer.setExecutor(executor);
		this.httpsServer.start();

		this.port = port;

		LOGGER.info("Server started on port: {}", port);
	}

	public SimpleHttpsServer(final int port, final SSLProperties sslProperties) {
		this(port, sslProperties, new SecureRandom());
	}

	@Override
	public void close() throws Exception {
		httpsServer.stop(0);
		executor.close();
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
					super.configure(params);
					log(params);
				}
			});
			return httpsServer;
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port);
		}
	}

	public static void log(HttpsParameters params) {
		try {
			LOGGER.debug("HTTPS parameters: {}", JsonBuilder.toJson(params));
		} catch (Exception e) {
			LOGGER.debug("Module not open. Add --add-opens jdk.httpserver/sun.net.httpserver=ALL-UNNAMED", e);
		}
	}

	static class NameHandler implements HttpHandler {

		@SuppressWarnings("unused")
		private final SimpleHttpsServer server;

		public NameHandler(final SimpleHttpsServer server) {
			this.server = server;
		}

		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (HttpMethod.GET.matches(exchange.getRequestMethod())) {
				sendResponse(exchange, HttpStatus.OK, SimpleHttpsServer.NAME);
			} else {
				exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
			}
		}

		private static <T> void sendResponse(final HttpExchange exchange, final HttpStatus status, final T response) throws IOException {
			String responseString = Strings.safeToString(response);
			exchange.sendResponseHeaders(status.getCode(), responseString.length());
			OutputStream os = exchange.getResponseBody();
			os.write(responseString.getBytes(StandardCharsets.UTF_8));
			os.close();
		}

	}
}
