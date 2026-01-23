package org.apiphany.utils.security.ssl.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class NameHandler<T> implements HttpHandler {

	public static final String NAME = "Mumu";

	@SuppressWarnings("unused")
	private final T server;

	public NameHandler(final T server) {
		this.server = server;
	}

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		if (HttpMethod.GET.matches(exchange.getRequestMethod())) {
			sendResponse(exchange, HttpStatus.OK, NAME);
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
