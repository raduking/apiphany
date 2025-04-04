package org.apiphany.auth.oauth2.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apiphany.auth.oauth2.AuthorizationGrantType;
import org.apiphany.http.ContentType;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Basic OAuth2 server.
 *
 * @author Radu Sebastian LAZIN
 */
public class NimbusOAuth2Server {

	private static final Logger LOGGER = LoggerFactory.getLogger(NimbusOAuth2Server.class);

	public static final int DEFAULT_PORT = 8385;
	public static final Duration DEFAULT_EXPIRES_IN = Duration.ofHours(1);

	private static final String CLIENT_SECRET = "apiphany-client-secret-more-than-32-characters";
	private static final String CLIENT_ID = "apiphany-client";

	private final String url;
	private final int port;
	private final String clientId;
	private final String clientSecret;

	public NimbusOAuth2Server() {
		this(DEFAULT_PORT);
	}

	public NimbusOAuth2Server(final int port) {
		HttpServer server = createHttpServer(port);
		server.createContext("/token", new TokenHandler(this));
		server.setExecutor(null);

		this.port = port;
		this.url = "http://localhost:" + port;
		this.clientId = CLIENT_ID;
		this.clientSecret = CLIENT_SECRET;

		server.start();
		LOGGER.info("OAuth2 server started at {}/token", url);
	}

	public String getUrl() {
		return url;
	}

	private static HttpServer createHttpServer(final int port) {
		try {
			return HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			return null;
		}
	}

	static class TokenHandler implements HttpHandler {

		private static final Logger LOGGER = LoggerFactory.getLogger(NimbusOAuth2Server.class);

		private final NimbusOAuth2Server server;

		protected TokenHandler(final NimbusOAuth2Server server) {
			this.server = server;
		}

		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!HttpMethod.POST.matches(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "{\"error\":\"method_not_allowed\"}");
				return;
			}

			// Read request body
			InputStream is = exchange.getRequestBody();
			String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

			// Parse the form data
			String[] params = body.split("&");
			Map<String, String> paramsMap = new HashMap<>();

			for (String param : params) {
				String[] pair = param.split("=");
				if (pair.length == 2) {
					String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
					paramsMap.put(pair[0], value);
				}
			}

			String clientId = paramsMap.get("client_id");
			if (Objects.equals(AuthorizationGrantType.CLIENT_CREDENTIALS.value(), paramsMap.get("grant_type"))
					&& Objects.equals(server.clientId, clientId)
					&& Objects.equals(server.clientSecret, paramsMap.get("client_secret"))) {
				String token = generateToken(clientId);
				if (null != token) {
					String response = "{\"access_token\":\"" + token
							+ "\", \"token_type\":\"Bearer\", \"expires_in\":"
							+ getExpiresIn(paramsMap.get("expires_in")).toSeconds() + "}";
					sendResponse(exchange, 200, response);
				} else {
					sendResponse(exchange, 500, "{\"error\":\"cannot_generate_token\"}");
				}
			} else {
				sendResponse(exchange, 401, "{\"error\":\"invalid_client\"}");
			}
		}

		private static Duration getExpiresIn(final String value) {
			try {
				return Duration.ofSeconds(Long.valueOf(value));
			} catch (Exception e) {
				LOGGER.info("Error reading 'expires_in':", e);
				return DEFAULT_EXPIRES_IN;
			}
		}

		private String generateToken(final String clientId) {
			try {
				JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
						.issuer("http://localhost:" + server.port)
						.subject(clientId)
						.expirationTime(new Date(new Date().getTime() + DEFAULT_EXPIRES_IN.toMillis()))
						.jwtID(UUID.randomUUID().toString())
						.build();

				byte[] secretBytes = server.clientSecret.getBytes();

				JWSSigner signer = new MACSigner(secretBytes);
				SignedJWT signedJWT = new SignedJWT(
						new JWSHeader(JWSAlgorithm.HS256),
						claimsSet);
				signedJWT.sign(signer);

				return signedJWT.serialize();
			} catch (JOSEException e) {
				LOGGER.info("Error generating token:", e);
				return null;
			}
		}

		private static void sendResponse(final HttpExchange exchange, final int statusCode, final String response) throws IOException {
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.value(), ContentType.APPLICATION_JSON.value());
			exchange.sendResponseHeaders(statusCode, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes(StandardCharsets.UTF_8));
			os.close();
		}
	}
}
