package org.apiphany.security.oauth2.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A simple HTTP server that has only one endpoint validated with OAuth2 with tokens generated from
 * {@link SimpleOAuth2Server} if a validator was provided.
 *
 * @author Radu Sebastian LAZIN
 */
public class SimpleHttpApiServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpApiServer.class);

	public static final String NAME = "Mumu";

	private final JwtTokenValidator tokenValidator;
	private final HttpServer httpServer;
	private final int port;

	public SimpleHttpApiServer(final int port, final JwtTokenValidator tokenValidator) {
		this.httpServer = createHttpServer(port);
		this.httpServer.createContext("/api/name", new NameHandler(this));
		this.httpServer.start();
		this.port = port;
		this.tokenValidator = tokenValidator;

		LOGGER.info("Server started on port: {}", port);
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

		private final SimpleHttpApiServer server;

		public NameHandler(final SimpleHttpApiServer server) {
			this.server = server;
		}

		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (HttpMethod.GET.matches(exchange.getRequestMethod())) {
				if (!isAuthorized(exchange)) {
					return;
				}
				sendResponse(exchange, HttpStatus.OK, SimpleHttpApiServer.NAME);
			} else {
				exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
			}
		}

		private boolean isAuthorized(final HttpExchange exchange) throws IOException {
			if (null == server.tokenValidator) {
				return true;
			}
			List<String> authorizationHeaderValues = MapHeaderValues.getInstance().get(HttpHeader.AUTHORIZATION, exchange.getRequestHeaders());
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
