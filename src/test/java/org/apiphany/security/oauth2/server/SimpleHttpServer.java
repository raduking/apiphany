package org.apiphany.security.oauth2.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.apiphany.security.JwtTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A simple HTTP server that has only one endpoint validated with OAuth2 with tokens generated from
 * {@link SimpleOAuth2Server} if a validator was provided.
 * <p>
 * This server provides a single route: {@code /api/name} which returns the string {@code "Mumu"}.
 *
 * @author Radu Sebastian LAZIN
 */
public class SimpleHttpServer implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpServer.class);

	public static final String ROUTE_API_NAME = "/api/name";

	public static final String NAME = "Mumu";

	private final JwtTokenValidator tokenValidator;
	private final HttpServer httpServer;
	private final ExecutorService executor;
	private final int port;

	public SimpleHttpServer(final int port, final JwtTokenValidator tokenValidator) {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		this.httpServer = createHttpServer(port);
		this.httpServer.createContext(ROUTE_API_NAME, new NameHandler(this));
		this.httpServer.setExecutor(executor);
		this.httpServer.start();

		this.port = port;
		this.tokenValidator = tokenValidator;

		LOGGER.info("Server started on port: {}", port);
	}

	public SimpleHttpServer(final int port) {
		this(port, null);
	}

	@Override
	public void close() throws Exception {
		httpServer.stop(0);
		executor.close();
	}

	public int getPort() {
		return port;
	}

	private static HttpServer createHttpServer(final int port) {
		try {
			return HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port);
		}
	}

	static class NameHandler implements HttpHandler {

		private final SimpleHttpServer server;

		public NameHandler(final SimpleHttpServer server) {
			this.server = server;
		}

		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			HttpMethod method;
			try {
				method = HttpMethod.fromString(exchange.getRequestMethod());
			} catch (IllegalArgumentException e) {
				exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
				return;
			}
			if (!isAuthorized(exchange)) {
				return;
			}
			switch (method) {
				case GET -> handleGet(exchange);
				default -> exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
			}
		}

		private static void handleGet(HttpExchange exchange) throws IOException {
			sendResponse(exchange, HttpStatus.OK, SimpleHttpServer.NAME);
		}

		private boolean isAuthorized(final HttpExchange exchange) throws IOException {
			if (null == server.tokenValidator) {
				return true;
			}
			List<String> authorizationHeaderValues = MapHeaderValues.get(HttpHeader.AUTHORIZATION, exchange.getRequestHeaders());
			if (Lists.isEmpty(authorizationHeaderValues)) {
				sendResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing " + HttpHeader.AUTHORIZATION + " header.");
				return false;
			}
			if (authorizationHeaderValues.size() > 1) {
				sendResponse(exchange, HttpStatus.UNAUTHORIZED, "Only one " + HttpHeader.AUTHORIZATION + " header value accepted.");
				return false;
			}
			String authorizationHeaderValue = Lists.first(authorizationHeaderValues);
			String[] pair = authorizationHeaderValue.split(" ");
			if (pair.length != 2) {
				sendResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid " + HttpHeader.AUTHORIZATION + " header value.");
				return false;
			}
			try {
				HttpAuthScheme.fromString(pair[0]);
			} catch (IllegalArgumentException e) {
				sendResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid authorization scheme.");
				return false;
			}
			String token = pair[1];
			try {
				server.tokenValidator.validateToken(token);
				return true;
			} catch (Exception e) {
				sendResponse(exchange, HttpStatus.UNAUTHORIZED, e.getMessage());
				return false;
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
