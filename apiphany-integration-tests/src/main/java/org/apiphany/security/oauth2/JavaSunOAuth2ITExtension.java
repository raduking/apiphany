package org.apiphany.security.oauth2;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apiphany.net.Sockets;
import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.oauth2.server.JavaSunHttpServer;
import org.apiphany.security.oauth2.server.JavaSunOAuth2Server;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit extension that manages the lifecycle of a {@link JavaSunOAuth2Server} and a protected API server using
 * {@link JavaSunHttpServer} for integration tests.
 *
 * @author Radu Sebastian LAZIN
 */
public class JavaSunOAuth2ITExtension implements BeforeAllCallback, AfterAllCallback {

	/**
	 * Indicates whether the servers have been verified as started.
	 */
	private static final AtomicBoolean VERIFIED = new AtomicBoolean(false);

	/**
	 * Localhost constant.
	 */
	private static final String LOCALHOST = "localhost";

	/**
	 * Client secret constant.
	 */
	public static final String CLIENT_SECRET = "apiphany-client-secret-more-than-32-characters";

	/**
	 * Client ID constant.
	 */
	public static final String CLIENT_ID = "apiphany-client";

	/**
	 * Port check timeout duration.
	 */
	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);

	/**
	 * OAuth2 server port.
	 */
	public static final int OAUTH2_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	/**
	 * API server port.
	 */
	public static final int API_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	/**
	 * OAuth2 server instance.
	 */
	public static final JavaSunOAuth2Server OAUTH2_SERVER = new JavaSunOAuth2Server(OAUTH2_SERVER_PORT, CLIENT_ID, CLIENT_SECRET);

	/**
	 * JWT token validator instance.
	 */
	public static final JwtTokenValidator JWT_TOKEN_VALIDATOR = new JwtTokenValidator(CLIENT_ID, CLIENT_SECRET, OAUTH2_SERVER.getUrl());

	/**
	 * API server instance.
	 */
	public static final JavaSunHttpServer API_SERVER = new JavaSunHttpServer(API_SERVER_PORT, JWT_TOKEN_VALIDATOR);

	/**
	 * Default constructor.
	 */
	public JavaSunOAuth2ITExtension() {
		// empty
	}

	/**
	 * @see BeforeAllCallback#beforeAll(ExtensionContext)
	 */
	@Override
	public void beforeAll(final ExtensionContext context) throws Exception {
		if (VERIFIED.compareAndSet(false, true)) {
			boolean oauthServerStarted = Sockets.canConnectTo(LOCALHOST, OAUTH2_SERVER_PORT, PORT_CHECK_TIMEOUT);
			boolean apiServerStarted = Sockets.canConnectTo(LOCALHOST, API_SERVER_PORT, PORT_CHECK_TIMEOUT);
			if (!oauthServerStarted) {
				throw new IllegalStateException("OAuth2 server failed to start on port: " + OAUTH2_SERVER_PORT);
			}
			if (!apiServerStarted) {
				throw new IllegalStateException("API server failed to start on port: " + API_SERVER_PORT);
			}
		}
	}

	/**
	 * @see AfterAllCallback#afterAll(ExtensionContext)
	 */
	@Override
	public void afterAll(final ExtensionContext context) throws Exception {
		// only close when the root context is shutting down
		if (context.getParent().isEmpty()) {
			OAUTH2_SERVER.close();
			API_SERVER.close();
		}
	}

	/**
	 * Returns the OAuth2 server instance.
	 *
	 * @return the OAuth2 server
	 */
	public static JavaSunOAuth2Server oauth2Server() {
		return OAUTH2_SERVER;
	}

	/**
	 * Returns the API server instance.
	 *
	 * @return the API server
	 */
	public static JavaSunHttpServer apiServer() {
		return API_SERVER;
	}

	/**
	 * Returns the JWT token validator instance.
	 *
	 * @return the JWT token validator
	 */
	public static JwtTokenValidator tokenValidator() {
		return JWT_TOKEN_VALIDATOR;
	}

	/**
	 * Returns the client ID.
	 *
	 * @return the client ID
	 */
	public static String clientId() {
		return CLIENT_ID;
	}

	/**
	 * Returns the client secret.
	 *
	 * @return the client secret
	 */
	public static String clientSecret() {
		return CLIENT_SECRET;
	}
}
