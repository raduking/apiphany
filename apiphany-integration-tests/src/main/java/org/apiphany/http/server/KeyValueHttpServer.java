package org.apiphany.http.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A simple HTTP server that has all HTTP methods implemented for a key-value store accessible under the
 * {@code /api/keys} route.
 *
 * @author Radu Sebastian LAZIN
 */
public class KeyValueHttpServer implements AutoCloseable {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueHttpServer.class);

	/**
	 * API route for keys.
	 */
	public static final String ROUTE_API_KEYS = "/api/keys";

	/**
	 * Default key.
	 */
	public static final String DEFAULT_KEY = "Mumu";

	/**
	 * Default value.
	 */
	public static final String DEFAULT_VALUE = "Cucu";

	/**
	 * List of allowed HTTP methods.
	 */
	public static final List<HttpMethod> ALLOW = List.of(
			HttpMethod.GET,
			HttpMethod.PUT,
			HttpMethod.POST,
			HttpMethod.DELETE,
			HttpMethod.PATCH,
			HttpMethod.HEAD,
			HttpMethod.CONNECT,
			HttpMethod.OPTIONS,
			HttpMethod.TRACE);

	/**
	 * Comma-separated string of allowed HTTP methods for the Allow header.
	 */
	public static final String ALLOW_HEADER_VALUE = String.join(", ", ALLOW.stream().map(HttpMethod::toString).toList());

	/**
	 * Constant indicating no body in the response.
	 */
	private static final int NO_BODY = -1;

	/**
	 * The underlying HTTP server.
	 */
	private final HttpServer httpServer;

	/**
	 * Executor service for handling requests.
	 */
	private final ExecutorService executor;

	/**
	 * The port on which the server is running.
	 */
	private final int port;

	/**
	 * The in-memory key-value store.
	 */
	private final Map<String, String> map = new ConcurrentHashMap<>();

	/**
	 * Constructs and starts the key-value HTTP server on the specified port.
	 *
	 * @param port the port number
	 */
	public KeyValueHttpServer(final int port) {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		this.httpServer = createHttpServer(port);
		this.httpServer.createContext(ROUTE_API_KEYS, new KeysHandler(this));
		this.httpServer.setExecutor(executor);
		this.httpServer.start();

		this.port = port;

		this.map.put(DEFAULT_KEY, DEFAULT_VALUE);

		LOGGER.info("Server started on port: {}", port);
	}

	/**
	 * Stops the server and releases resources.
	 */
	@Override
	public void close() throws Exception {
		httpServer.stop(0);
		executor.close();
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
	 * Creates an HTTP server bound to the specified port.
	 *
	 * @param port the port number
	 * @return the created HTTP server
	 */
	private static HttpServer createHttpServer(final int port) {
		try {
			return HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port);
		}
	}

	/**
	 * Handler for managing key-value pairs via HTTP methods.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class KeysHandler implements HttpHandler {

		/**
		 * Reference to the parent server.
		 */
		private final KeyValueHttpServer server;

		/**
		 * Constructs a KeysHandler with a reference to the parent server.
		 *
		 * @param server the parent KeyValueHttpServer
		 */
		public KeysHandler(final KeyValueHttpServer server) {
			this.server = server;
		}

		/**
		 * Handles incoming HTTP requests and routes them to the appropriate method handler.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			HttpMethod method;
			try {
				method = HttpMethod.fromString(exchange.getRequestMethod());
			} catch (IllegalArgumentException e) {
				exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), NO_BODY);
				return;
			}
			switch (method) {
				case GET -> handleGet(exchange);
				case PUT -> handlePut(exchange);
				case POST -> handlePost(exchange);
				case DELETE -> handleDelete(exchange);
				case PATCH -> handlePatch(exchange);
				case HEAD -> handleHead(exchange);
				case CONNECT -> handleConnect(exchange);
				case OPTIONS -> handleOptions(exchange);
				case TRACE -> handleTrace(exchange);
				default -> exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), NO_BODY);
			}
		}

		/**
		 * Handles GET requests to retrieve values by key or all key-value pairs.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private void handleGet(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			if (null == key) {
				String json = JsonBuilder.toJson(server.map);
				HttpContentType contentType = HttpContentType.of(ContentType.APPLICATION_JSON, StandardCharsets.UTF_8);
				exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.value(), contentType.value());
				sendResponse(exchange, HttpStatus.OK, json);
			} else if (server.map.containsKey(key)) {
				sendResponse(exchange, HttpStatus.OK, server.map.get(key));
			} else {
				exchange.sendResponseHeaders(HttpStatus.NOT_FOUND.value(), NO_BODY);
			}
		}

		/**
		 * Handles PUT requests to add or update a key-value pair.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private void handlePut(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			@SuppressWarnings("resource")
			String body = Strings.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
			server.map.put(key, body);
			sendResponse(exchange, HttpStatus.OK, body);
		}

		/**
		 * Handles POST requests to add a new key-value pair.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private void handlePost(final HttpExchange exchange) throws IOException {
			@SuppressWarnings("resource")
			String body = Strings.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
			String[] pair = body.split(":");
			server.map.put(pair[0], pair[1]);
			sendResponse(exchange, HttpStatus.OK, pair[1]);
		}

		/**
		 * Handles DELETE requests to remove a key-value pair by key.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private void handleDelete(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			String value = server.map.remove(key);
			sendResponse(exchange, HttpStatus.OK, value);
		}

		/**
		 * Handles PATCH requests to append data to an existing value by key.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private void handlePatch(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			@SuppressWarnings("resource")
			String body = Strings.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
			String value = server.map.get(key);
			String newValue = value + body;
			server.map.put(key, newValue);
			sendResponse(exchange, HttpStatus.OK, newValue);
		}

		/**
		 * Handles HEAD requests to retrieve metadata about a value by key.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private void handleHead(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			if (server.map.containsKey(key)) {
				HttpContentType contentType = HttpContentType.of(ContentType.TEXT_PLAIN, StandardCharsets.UTF_8);
				exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.value(), contentType.value());
				String value = server.map.get(key);
				int length = value.getBytes(StandardCharsets.UTF_8).length;
				exchange.getResponseHeaders().set(HttpHeader.CONTENT_LENGTH.value(), String.valueOf(length));
				exchange.sendResponseHeaders(HttpStatus.OK.value(), NO_BODY);
			} else {
				exchange.sendResponseHeaders(HttpStatus.NOT_FOUND.value(), NO_BODY);
			}
		}

		/**
		 * Handles CONNECT requests to acknowledge the connection.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private static void handleConnect(final HttpExchange exchange) throws IOException {
			exchange.sendResponseHeaders(HttpStatus.OK.value(), NO_BODY);
			try (OutputStream os = exchange.getResponseBody()) {
				// no body for CONNECT, just acknowledge the connection
			}
		}

		/**
		 * Handles OPTIONS requests to provide allowed HTTP methods and CORS headers.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private static void handleOptions(final HttpExchange exchange) throws IOException {
			// set CORS headers to allow requests from any origin and the methods the server supports
			exchange.getResponseHeaders().set(HttpHeader.ALLOW.value(), ALLOW_HEADER_VALUE);
			exchange.getResponseHeaders().set(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN.value(), "*");
			exchange.getResponseHeaders().set(HttpHeader.ACCESS_CONTROL_ALLOW_METHODS.value(), ALLOW_HEADER_VALUE);
			exchange.getResponseHeaders().set(HttpHeader.ACCESS_CONTROL_ALLOW_HEADERS.value(), HttpHeader.CONTENT_TYPE.value());

			exchange.sendResponseHeaders(HttpStatus.OK.value(), NO_BODY);
		}

		/**
		 * Handles TRACE requests to echo back the received request.
		 *
		 * @param exchange the HTTP exchange object
		 * @throws IOException if an I/O error occurs
		 */
		private static void handleTrace(final HttpExchange exchange) throws IOException {
			// reconstruct the request to echo back
			StringBuilder requestBuilder = new StringBuilder();
			requestBuilder.append(exchange.getRequestMethod()).append(" ")
					.append(exchange.getRequestURI()).append(" ")
					.append(exchange.getProtocol()).append("\r\n");
			exchange.getRequestHeaders().forEach((key, values) -> {
				for (String value : values) {
					requestBuilder.append(key).append(": ").append(value).append("\r\n");
				}
			});
			requestBuilder.append("\r\n");
			String responseBody = requestBuilder.toString();

			// Set the Content-Type as specified by HTTP for TRACE
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.value(), ContentType.MESSAGE_HTTP.value());
			sendResponse(exchange, HttpStatus.OK, responseBody);
		}

		/**
		 * Sends a response with the specified status and body.
		 *
		 * @param <T> the type of the response body
		 * @param exchange the HTTP exchange object
		 * @param status the HTTP status to send
		 * @param response the response body
		 * @throws IOException if an I/O error occurs
		 */
		private static <T> void sendResponse(final HttpExchange exchange, final HttpStatus status, final T response) throws IOException {
			String responseString = Strings.safeToString(response);
			exchange.sendResponseHeaders(status.getCode(), responseString.length());
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(responseString.getBytes(StandardCharsets.UTF_8));
			}
		}

		/**
		 * Extracts the key from the request path.
		 *
		 * @param exchange the HTTP exchange object
		 * @return the extracted key, or null if no key is specified
		 */
		private static String getKeyFromPath(final HttpExchange exchange) {
			String fullPath = exchange.getRequestURI().getPath();
			if (ROUTE_API_KEYS.equals(fullPath)) {
				return null;
			}
			return fullPath.substring((ROUTE_API_KEYS + "/").length());
		}
	}
}
