package org.apiphany.security.oauth2.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apiphany.RequestParameters;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.oauth2.AuthorizationGrantType;
import org.apiphany.security.oauth2.OAuth2Parameter;
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
 * Simple OAuth2 server using Nimbus library.
 *
 * @author Radu Sebastian LAZIN
 */
public class SimpleOAuth2Server implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleOAuth2Server.class);

	public static final Duration DEFAULT_EXPIRES_IN = Duration.ofHours(1);

	private final String url;
	private final int port;
	private final String clientId;
	private final String clientSecret;

	private HttpServer httpServer;

	public SimpleOAuth2Server(final int port, final String clientId, final String clientSecret) {
		this.httpServer = createHttpServer(port);
		this.httpServer.createContext("/token", new TokenHandler(this));
		this.httpServer.setExecutor(null);

		this.port = port;
		this.url = "http://localhost:" + port;
		this.clientId = clientId;
		this.clientSecret = clientSecret;

		httpServer.start();
		LOGGER.info("OAuth2 server started at {}/token", url);
	}

	@Override
	public void close() throws Exception {
		httpServer.stop(0);
	}

	public String getUrl() {
		return url;
	}

	public int getPort() {
		return port;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	private static HttpServer createHttpServer(final int port) {
		try {
			return HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			throw new IllegalStateException("Server cannot be created on port: " + port);
		}
	}

	/**
	 * Token handler serving the /token endpoint.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class TokenHandler implements HttpHandler {

		private static final Logger LOGGER = LoggerFactory.getLogger(SimpleOAuth2Server.class);

		private static final int DEFAULT_READ_BUFFER_SIZE = 10000;

		private final SimpleOAuth2Server server;

		protected TokenHandler(final SimpleOAuth2Server server) {
			this.server = server;
		}

		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!HttpMethod.POST.matches(exchange.getRequestMethod())) {
				sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED);
				return;
			}
			Map<String, String> params = RequestParameters.from(getBody(exchange));

			String clientId = params.get(OAuth2Parameter.CLIENT_ID.value());
			String clientSecret = params.get(OAuth2Parameter.CLIENT_SECRET.value());
			boolean isAuthorized = AuthorizationGrantType.CLIENT_CREDENTIALS.matches(params.get(OAuth2Parameter.GRANT_TYPE.value()))
					&& Objects.equals(server.clientId, clientId)
					&& Objects.equals(server.clientSecret, clientSecret);
			if (!isAuthorized) {
				sendResponse(exchange, HttpStatus.UNAUTHORIZED, ErrorResponse.of("Invalid client: " + clientId));
				return;
			}
			Duration expiresIn = getExpiresIn(params.get(OAuth2Parameter.EXPIRES_IN.value()));
			String accessToken = generateToken(clientId, expiresIn);
			if (null == accessToken) {
				sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponse.of("Cannot generate token"));
				return;
			}

			AuthenticationToken token = new AuthenticationToken();
			token.setAccessToken(accessToken);
			token.setTokenType(HttpAuthScheme.BEARER.value());
			token.setExpiresIn(expiresIn.toSeconds());

			sendResponse(exchange, HttpStatus.OK, token.toString());
		}

		private static Duration getExpiresIn(final String value) {
			try {
				return Duration.ofSeconds(Long.valueOf(value));
			} catch (Exception e) {
				LOGGER.info("Error reading 'expires_in': " + value + ", defaulting to: " + DEFAULT_EXPIRES_IN, e);
				return DEFAULT_EXPIRES_IN;
			}
		}

		private static String getBody(final HttpExchange exchange) throws IOException {
			try (InputStream is = exchange.getRequestBody()) {
				return Strings.toString(is, StandardCharsets.UTF_8, DEFAULT_READ_BUFFER_SIZE);
			}
		}

		private String generateToken(final String clientId, final Duration expiresIn) {
			try {
				JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
						.issuer("http://localhost:" + server.port)
						.subject(clientId)
						.expirationTime(new Date(new Date().getTime() + expiresIn.toMillis()))
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

		private static <T> void sendResponse(final HttpExchange exchange, final HttpStatus status, final T response) throws IOException {
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.value(), ContentType.APPLICATION_JSON.value());
			String responseString = Strings.safeToString(response);
			exchange.sendResponseHeaders(status.getCode(), responseString.length());
			OutputStream os = exchange.getResponseBody();
			os.write(responseString.getBytes(StandardCharsets.UTF_8));
			os.close();
		}

		private static void sendResponse(final HttpExchange exchange, final HttpStatus status) throws IOException {
			sendResponse(exchange, status, ErrorResponse.of(status.getMessage()));
		}
	}

	/**
	 * Error response.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class ErrorResponse {

		private String error;

		public String getError() {
			return error;
		}

		public void setError(final String error) {
			this.error = error;
		}

		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		static ErrorResponse of(final String error) {
			ErrorResponse errorResponse = new ErrorResponse();
			errorResponse.error = error;
			return errorResponse;
		}
	}
}
