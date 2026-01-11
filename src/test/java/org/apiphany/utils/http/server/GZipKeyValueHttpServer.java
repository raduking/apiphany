package org.apiphany.utils.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Strings;
import org.apiphany.lang.gzip.GZip;
import org.morphix.lang.Nullables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * GZIP-aware key-value HTTP server:
 * <ul>
 * <li>decompresses requests only if client sends "Content-Encoding: gzip"</li>
 * <li>always compresses GET responses with GZIP</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class GZipKeyValueHttpServer implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(GZipKeyValueHttpServer.class);

	private final HttpServer server;
	private final ExecutorService executor;
	private final int port;
	private final Map<String, String> map = new ConcurrentHashMap<>();

	public static final String ROUTE_API_KEYS = "/api/keys";

	public static final String DEFAULT_KEY = "Mumu";
	public static final String DEFAULT_VALUE = "Cucu";

	private static final int NO_BODY = -1;

	public GZipKeyValueHttpServer(final int port) {
		this.port = port;
		this.executor = Executors.newVirtualThreadPerTaskExecutor();

		this.server = createServer(port);
		this.server.createContext(ROUTE_API_KEYS, new GZipKeysHandler());
		this.server.setExecutor(executor);
		this.server.start();

		map.put(DEFAULT_KEY, DEFAULT_VALUE);

		LOGGER.info("GZip-aware server started on port {}", port);
	}

	private static HttpServer createServer(final int port) {
		try {
			return HttpServer.create(new java.net.InetSocketAddress(port), 0);
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port);
		}
	}

	@Override
	public void close() {
		server.stop(0);
		executor.shutdown();
	}

	public int getPort() {
		return port;
	}

	class GZipKeysHandler implements HttpHandler {

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
				case PATCH -> handlePatch(exchange);
				default -> exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
			}
		}

		private void handleGet(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			String response = null == key ? null : map.getOrDefault(key, null);

			if (response == null) {
				exchange.sendResponseHeaders(HttpStatus.NOT_FOUND.value(), NO_BODY);
				return;
			}
			sendResponse(exchange, HttpStatus.OK, response);
		}

		private void handlePut(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			String body = readRequestBody(exchange);
			map.put(key, body);

			sendResponse(exchange, HttpStatus.OK, body);
		}

		private void handlePost(final HttpExchange exchange) throws IOException {
			String body = readRequestBody(exchange);

			String[] pair = body.split(":", 2);
			map.put(pair[0], pair[1]);

			sendResponse(exchange, HttpStatus.OK, pair[1]);
		}

		private void handleDelete(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			String value = map.remove(key);

			sendResponse(exchange, HttpStatus.OK, value);
		}

		private void handlePatch(final HttpExchange exchange) throws IOException {
			String key = getKeyFromPath(exchange);
			String body = readRequestBody(exchange);

			String value = map.get(key);
			String newValue = value + body;
			map.put(key, newValue);

			sendResponse(exchange, HttpStatus.OK, newValue);
		}

		@SuppressWarnings("resource")
		private static String readRequestBody(final HttpExchange exchange) throws IOException {
			InputStream is = exchange.getRequestBody();

			String encoding = exchange.getRequestHeaders().getFirst(HttpHeader.CONTENT_ENCODING.value());
			if (null != encoding) {
				ContentEncoding contentEncoding = ContentEncoding.fromString(encoding, Nullables.supplyNull());
				if (ContentEncoding.GZIP == contentEncoding) {
					return GZip.decompressToString(is);
				}
			}
			return Strings.toString(is, StandardCharsets.UTF_8);
		}

		private static <T> void sendResponse(final HttpExchange exchange, final HttpStatus status, final String response) throws IOException {
			byte[] compressed = GZip.compress(response);
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_ENCODING.value(), ContentEncoding.GZIP.value());

			HttpContentType contentType = HttpContentType.of(ContentType.TEXT_PLAIN, StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.value(), contentType.value());

			exchange.sendResponseHeaders(status.getCode(), compressed.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(compressed);
			}
		}

		private static String getKeyFromPath(final HttpExchange exchange) {
			String path = exchange.getRequestURI().getPath();
			if (ROUTE_API_KEYS.equals(path)) {
				return null;
			}
			return path.substring((ROUTE_API_KEYS + "/").length());
		}
	}
}
