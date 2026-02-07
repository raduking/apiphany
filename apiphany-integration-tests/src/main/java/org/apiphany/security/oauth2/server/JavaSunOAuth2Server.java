package org.apiphany.security.oauth2.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
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
import org.apiphany.lang.collections.Lists;
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
 * Simple OAuth2 server using Sun {@link HttpServer} and Nimbus library.
 *
 * @author Radu Sebastian LAZIN
 */
public class JavaSunOAuth2Server implements AutoCloseable {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaSunOAuth2Server.class);

	/**
	 * Default token expiration duration.
	 */
	public static final Duration DEFAULT_EXPIRES_IN = Duration.ofHours(1);

	/**
	 * Server URL.
	 */
	private final String url;

	/**
	 * Server port.
	 */
	private final int port;

	/**
	 * OAuth2 client ID.
	 */
	private final String clientId;

	/**
	 * OAuth2 client secret.
	 */
	private final String clientSecret;

	/**
	 * Underlying HTTP server.
	 */
	private final HttpServer httpServer;

	/**
	 * Constructs a new {@link JavaSunOAuth2Server} instance.
	 *
	 * @param port server port
	 * @param clientId OAuth2 client ID
	 * @param clientSecret OAuth2 client secret
	 */
	public JavaSunOAuth2Server(final int port, final String clientId, final String clientSecret) {
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

	/**
	 * Stops the server
	 */
	@Override
	public void close() throws Exception {
		httpServer.stop(0);
	}

	/**
	 * Returns the server URL.
	 *
	 * @return the server URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the server port.
	 *
	 * @return the server port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns the OAuth2 client ID.
	 *
	 * @return the client ID
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * Returns the OAuth2 client secret.
	 *
	 * @return the client secret
	 */
	public String getClientSecret() {
		return clientSecret;
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
	 * Token handler serving the /token endpoint.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class TokenHandler implements HttpHandler {

		/**
		 * Logger instance.
		 */
		private static final Logger LOGGER = LoggerFactory.getLogger(TokenHandler.class);

		/**
		 * Default read buffer size.
		 */
		private static final int DEFAULT_READ_BUFFER_SIZE = 10000;

		/**
		 * Reference to the parent server.
		 */
		private final JavaSunOAuth2Server server;

		/**
		 * Constructs a new {@link TokenHandler} instance.
		 *
		 * @param server reference to the parent server
		 */
		protected TokenHandler(final JavaSunOAuth2Server server) {
			this.server = server;
		}

		/**
		 * Handles the /token requests.
		 *
		 * @param exchange the HTTP exchange
		 */
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!HttpMethod.POST.matches(exchange.getRequestMethod())) {
				sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED);
				return;
			}
			Map<String, List<String>> params = RequestParameters.from(getBody(exchange));

			String clientId = Lists.first(params.get(OAuth2Parameter.CLIENT_ID.value()));
			String clientSecret = Lists.first(params.get(OAuth2Parameter.CLIENT_SECRET.value()));
			String grantType = Lists.first(params.get(OAuth2Parameter.GRANT_TYPE.value()));
			boolean isAuthorized = AuthorizationGrantType.CLIENT_CREDENTIALS.matches(grantType)
					&& Objects.equals(server.clientId, clientId)
					&& Objects.equals(server.clientSecret, clientSecret);
			if (!isAuthorized) {
				sendResponse(exchange, HttpStatus.UNAUTHORIZED, ErrorResponse.of("Invalid client: " + clientId));
				return;
			}
			String expiresIn = Lists.first(params.get(OAuth2Parameter.EXPIRES_IN.value()));
			Duration expiresInDuration = getExpiresIn(expiresIn);
			String accessToken = generateToken(clientId, expiresInDuration);
			if (null == accessToken) {
				sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponse.of("Cannot generate token"));
				return;
			}

			AuthenticationToken token = new AuthenticationToken();
			token.setAccessToken(accessToken);
			token.setTokenType(HttpAuthScheme.BEARER.value());
			token.setExpiresIn(expiresInDuration.toSeconds());

			sendResponse(exchange, HttpStatus.OK, token.toString());
		}

		/**
		 * Parses the 'expires_in' parameter value.
		 *
		 * @param value the 'expires_in' parameter value
		 * @return the parsed duration or default if parsing fails
		 */
		private static Duration getExpiresIn(final String value) {
			try {
				return Duration.ofSeconds(Long.valueOf(value));
			} catch (Exception e) {
				LOGGER.info("Error reading 'expires_in': {}, defaulting to: {}", value, DEFAULT_EXPIRES_IN, e);
				return DEFAULT_EXPIRES_IN;
			}
		}

		/**
		 * Reads the request body as a string.
		 *
		 * @param exchange the HTTP exchange
		 * @return the request body as a string
		 * @throws IOException if an I/O error occurs
		 */
		private static String getBody(final HttpExchange exchange) throws IOException {
			try (InputStream is = exchange.getRequestBody()) {
				return Strings.toString(is, StandardCharsets.UTF_8, DEFAULT_READ_BUFFER_SIZE);
			}
		}

		/**
		 * Generates a signed JWT token.
		 *
		 * @param clientId the client ID
		 * @param expiresIn the token expiration duration
		 * @return the generated token
		 */
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

		/**
		 * Sends an HTTP response with the specified status and response body.
		 *
		 * @param <T> the type of the response body
		 * @param exchange the HTTP exchange containing request and response data
		 * @param status the HTTP status to send
		 * @param response the response body
		 * @throws IOException if an I/O error occurs
		 */
		private static <T> void sendResponse(final HttpExchange exchange, final HttpStatus status, final T response) throws IOException {
			exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.value(), ContentType.APPLICATION_JSON.value());
			String responseString = Strings.safeToString(response);
			exchange.sendResponseHeaders(status.getCode(), responseString.length());
			OutputStream os = exchange.getResponseBody();
			os.write(responseString.getBytes(StandardCharsets.UTF_8));
			os.close();
		}

		/**
		 * Sends an HTTP response with the specified status and a default error message.
		 *
		 * @param exchange the HTTP exchange containing request and response data
		 * @param status the HTTP status to send
		 * @throws IOException if an I/O error occurs
		 */
		private static void sendResponse(final HttpExchange exchange, final HttpStatus status) throws IOException {
			sendResponse(exchange, status, ErrorResponse.of(status.message()));
		}
	}

	/**
	 * Error response.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class ErrorResponse {

		/**
		 * Error message.
		 */
		private String error;

		/**
		 * Default constructor.
		 */
		public ErrorResponse() {
			// empty
		}

		/**
		 * Returns the error message.
		 *
		 * @return the error message
		 */
		public String getError() {
			return error;
		}

		/**
		 * Sets the error message.
		 *
		 * @param error the error message
		 */
		public void setError(final String error) {
			this.error = error;
		}

		/**
		 * Returns the JSON representation of the error response.
		 *
		 * @return the JSON representation of the error response
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		/**
		 * Creates an {@link ErrorResponse} instance with the specified error message.
		 *
		 * @param error the error message
		 * @return the {@link ErrorResponse} instance
		 */
		static ErrorResponse of(final String error) {
			ErrorResponse errorResponse = new ErrorResponse();
			errorResponse.error = error;
			return errorResponse;
		}
	}
}
