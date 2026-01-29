package org.apiphany.security.oauth2.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apiphany.header.Headers;
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
 * A simple immutable HTTP server using Sun {@link HttpServer} that has only one endpoint validated with OAuth2 with
 * tokens generated from {@link JavaSunOAuth2Server} if a validator was provided.
 * <p>
 * This server provides a single route: {@code /api/name} which returns the string {@code "Mumu"}.
 *
 * @author Radu Sebastian LAZIN
 */
public class JavaSunHttpServer implements AutoCloseable {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaSunHttpServer.class);

	/**
	 * API route for name retrieval.
	 */
	public static final String ROUTE_API_NAME = "/api/name";

	/**
	 * Name returned by the {@code /api/name} endpoint.
	 */
	public static final String NAME = "Mumu";

	/**
	 * JWT token validator.
	 */
	private final JwtTokenValidator tokenValidator;

	/**
	 * Underlying HTTP server.
	 */
	private final HttpServer httpServer;

	/**
	 * Executor service for handling requests.
	 */
	private final ExecutorService executor;

	/**
	 * Port on which the server is running.
	 */
	private final int port;

	/**
	 * Constructs a new {@link JavaSunHttpServer} instance.
	 *
	 * @param port the port on which the server will run
	 * @param tokenValidator the JWT token validator for securing endpoints; if null, no validation is performed
	 */
	public JavaSunHttpServer(final int port, final JwtTokenValidator tokenValidator) {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		this.httpServer = createHttpServer(port);
		this.httpServer.createContext(ROUTE_API_NAME, new NameHandler(this));
		this.httpServer.setExecutor(executor);
		this.httpServer.start();

		this.port = port;
		this.tokenValidator = tokenValidator;

		LOGGER.info("Server started on port: {}", port);
	}

	/**
	 * Constructs a new {@link JavaSunHttpServer} instance without token validation.
	 *
	 * @param port the port on which the server will run
	 */
	public JavaSunHttpServer(final int port) {
		this(port, null);
	}

	/**
	 * Closes the server and releases resources.
	 */
	@Override
	public void close() throws Exception {
		httpServer.stop(0);
		executor.close();
	}

	/**
	 * Returns the port on which the server is running.
	 *
	 * @return the server port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Creates and configures an {@link HttpServer} instance.
	 *
	 * @param port the port on which the server will run
	 * @return the configured {@link HttpServer} instance
	 */
	private static HttpServer createHttpServer(final int port) {
		try {
			return HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port);
		}
	}

	/**
	 * Handler for the {@code /api/name} endpoint.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class NameHandler implements HttpHandler {

		/**
		 * Reference to the parent server.
		 */
		private final JavaSunHttpServer server;

		/**
		 * Constructs a new {@link NameHandler} instance.
		 *
		 * @param server the parent server
		 */
		public NameHandler(final JavaSunHttpServer server) {
			this.server = server;
		}

		/**
		 * Handles incoming HTTP requests.
		 *
		 * @param exchange the HTTP exchange containing request and response data
		 * @throws IOException if an I/O error occurs
		 */
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

		/**
		 * Handles GET requests to the endpoint.
		 *
		 * @param exchange the HTTP exchange containing request and response data
		 * @throws IOException if an I/O error occurs
		 */
		private static void handleGet(final HttpExchange exchange) throws IOException {
			sendResponse(exchange, HttpStatus.OK, JavaSunHttpServer.NAME);
		}

		/**
		 * Checks if the request is authorized using the token validator.
		 *
		 * @param exchange the HTTP exchange containing request and response data
		 * @return true if authorized, false otherwise
		 * @throws IOException if an I/O error occurs
		 */
		private boolean isAuthorized(final HttpExchange exchange) throws IOException {
			if (null == server.tokenValidator) {
				return true;
			}
			List<String> authorizationHeaderValues = Headers.get(HttpHeader.AUTHORIZATION, exchange.getRequestHeaders());
			if (Lists.isEmpty(authorizationHeaderValues)) {
				return sendResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing " + HttpHeader.AUTHORIZATION + " header.");
			}
			int authHeadersSize = authorizationHeaderValues.size();
			if (authorizationHeaderValues.size() > 1) {
				return sendResponse(exchange, HttpStatus.UNAUTHORIZED,
						"Only one " + HttpHeader.AUTHORIZATION + " header value accepted, got " + authHeadersSize + ".");
			}
			String authorizationHeaderValue = Lists.first(authorizationHeaderValues);
			String[] pair = authorizationHeaderValue.split(" ");
			if (pair.length != 2) {
				return sendResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid " + HttpHeader.AUTHORIZATION + " header value.");
			}
			try {
				HttpAuthScheme.fromString(pair[0]);
			} catch (IllegalArgumentException e) {
				return sendResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid authorization scheme.");
			}
			String token = pair[1];
			try {
				server.tokenValidator.validateToken(token);
				return true;
			} catch (Exception e) {
				return sendResponse(exchange, HttpStatus.UNAUTHORIZED, e.getMessage());
			}
		}

		/**
		 * Sends an HTTP response with the specified status and response body.
		 *
		 * @param <T> the type of the response body
		 * @param exchange the HTTP exchange containing request and response data
		 * @param status the HTTP status to send
		 * @param response the response body
		 * @return true if the status is 2xx successful, false otherwise
		 * @throws IOException if an I/O error occurs
		 */
		private static <T> boolean sendResponse(final HttpExchange exchange, final HttpStatus status, final T response) throws IOException {
			String responseString = Strings.safeToString(response);
			exchange.sendResponseHeaders(status.getCode(), responseString.length());
			OutputStream os = exchange.getResponseBody();
			os.write(responseString.getBytes(StandardCharsets.UTF_8));
			os.close();
			return status.is2xxSuccessful();
		}
	}
}
