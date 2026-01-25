package org.apiphany.security.oath2;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apiphany.net.Sockets;
import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.oauth2.server.JavaSunHttpServer;
import org.apiphany.security.oauth2.server.JavaSunOAuth2Server;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JavaSunOAuth2ITExtension implements BeforeAllCallback, AfterAllCallback {

	private static final AtomicBoolean VERIFIED = new AtomicBoolean(false);

	static final String LOCALHOST = "localhost";

	static final String CLIENT_SECRET = "apiphany-client-secret-more-than-32-characters";
	static final String CLIENT_ID = "apiphany-client";

	static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	static final int OAUTH_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
	static final int API_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	static final JavaSunOAuth2Server OAUTH2_SERVER = new JavaSunOAuth2Server(OAUTH_SERVER_PORT, CLIENT_ID, CLIENT_SECRET);
	static final JwtTokenValidator JWT_TOKEN_VALIDATOR = new JwtTokenValidator(CLIENT_ID, CLIENT_SECRET, OAUTH2_SERVER.getUrl());
	static final JavaSunHttpServer API_SERVER = new JavaSunHttpServer(API_SERVER_PORT, JWT_TOKEN_VALIDATOR);

	@Override
	public void beforeAll(final ExtensionContext context) throws Exception {
		if (VERIFIED.compareAndSet(false, true)) {
			boolean oauthServerStarted = Sockets.canConnectTo(LOCALHOST, OAUTH_SERVER_PORT, PORT_CHECK_TIMEOUT);
			boolean apiServerStarted = Sockets.canConnectTo(LOCALHOST, API_SERVER_PORT, PORT_CHECK_TIMEOUT);
			if (!oauthServerStarted) {
				throw new IllegalStateException("OAuth2 server failed to start on port: " + OAUTH_SERVER_PORT);
			}
			if (!apiServerStarted) {
				throw new IllegalStateException("API server failed to start on port: " + API_SERVER_PORT);
			}
		}
	}

	@Override
	public void afterAll(final ExtensionContext context) throws Exception {
		// only close when the root context is shutting down
		if (context.getParent().isEmpty()) {
			OAUTH2_SERVER.close();
			API_SERVER.close();
		}
	}
}
