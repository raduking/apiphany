package org.apiphany.security.ssl.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler for the name endpoint that returns a fixed name.
 *
 * @param <T> the type of the server
 *
 * @author Radu Sebastian LAZIN
 */
public class NameHandler<T> implements HttpHandler {

	/**
	 * Name returned by the name endpoint.
	 */
	public static final String NAME = "Mumu";

	/**
	 * Reference to the server.
	 */
	@SuppressWarnings("unused")
	private final T server;

	/**
	 * Constructor initializing the handler with a reference to the server.
	 *
	 * @param server the server instance
	 */
	public NameHandler(final T server) {
		this.server = server;
	}

	/**
	 * Handles HTTP exchanges for the name endpoint.
	 *
	 * @param exchange the HTTP exchange
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		if (HttpMethod.GET.matches(exchange.getRequestMethod())) {
			sendResponse(exchange, HttpStatus.OK, NAME);
		} else {
			exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
		}
	}

	/**
	 * Sends a response to the client.
	 *
	 * @param <T> the type of the response
	 * @param exchange the HTTP exchange
	 * @param status the HTTP status
	 * @param response the response body
	 * @throws IOException if an I/O error occurs
	 */
	private static <T> void sendResponse(final HttpExchange exchange, final HttpStatus status, final T response) throws IOException {
		String responseString = Strings.safeToString(response);
		exchange.sendResponseHeaders(status.getCode(), responseString.length());
		OutputStream os = exchange.getResponseBody();
		os.write(responseString.getBytes(StandardCharsets.UTF_8));
		os.close();
	}
}
