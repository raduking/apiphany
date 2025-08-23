package org.apiphany.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A simple HTTP server that has all HTTP methods implemented.
 *
 * @author Radu Sebastian LAZIN
 */
public class KeyValueHttpServer implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueHttpServer.class);

	public static final String ROUTE_API_KEYS = "/api/keys";

	public static final String DEFAULT_KEY = "Mumu";
	public static final String DEFAULT_VALUE = "Cucu";

	private final HttpServer httpServer;
	private final ExecutorService executor;
	private final int port;

	private Map<String, String> map = new ConcurrentHashMap<>();

	public KeyValueHttpServer(final int port) {
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		this.httpServer = createHttpServer(port);
		this.httpServer.createContext(ROUTE_API_KEYS, new NameHandler(this));
		this.httpServer.setExecutor(executor);
		this.httpServer.start();

		this.port = port;

		this.map.put(DEFAULT_KEY, DEFAULT_VALUE);

		LOGGER.info("Server started on port: {}", port);
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

		private final KeyValueHttpServer server;

		public NameHandler(final KeyValueHttpServer server) {
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
			switch (method) {
				case GET -> handleGet(exchange);
				case PUT -> handlePut(exchange);
				case POST -> handlePost(exchange);
				case DELETE -> handleDelete(exchange);
				default -> exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
			}
		}

		private void handleGet(HttpExchange exchange) throws IOException {
            String key = getKeyFromPath(exchange);
            if (server.map.containsKey(key)) {
            	sendResponse(exchange, HttpStatus.OK, server.map.get(key));
            } else {
            	exchange.sendResponseHeaders(HttpStatus.NOT_FOUND.value(), -1);
            }
		}

		private void handlePut(HttpExchange exchange) throws IOException {
            String key = getKeyFromPath(exchange);
			String body = Strings.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
			server.map.put(key, body);
			sendResponse(exchange, HttpStatus.OK, body);
		}

		private void handlePost(HttpExchange exchange) throws IOException {
			String body = Strings.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
			String[] pair = body.split(":");
			server.map.put(pair[0], pair[1]);
			sendResponse(exchange, HttpStatus.OK, pair[1]);
		}

		private void handleDelete(HttpExchange exchange) throws IOException {
            String key = getKeyFromPath(exchange);
            String value = server.map.remove(key);
			sendResponse(exchange, HttpStatus.OK, value);
		}

		private static <T> void sendResponse(final HttpExchange exchange, final HttpStatus status, final T response) throws IOException {
			String responseString = Strings.safeToString(response);
			exchange.sendResponseHeaders(status.getCode(), responseString.length());
			OutputStream os = exchange.getResponseBody();
			os.write(responseString.getBytes(StandardCharsets.UTF_8));
			os.close();
		}

		private static String getKeyFromPath(HttpExchange exchange) {
			String fullPath = exchange.getRequestURI().getPath();
            return fullPath.substring((ROUTE_API_KEYS + "/").length());
		}

	}
}
