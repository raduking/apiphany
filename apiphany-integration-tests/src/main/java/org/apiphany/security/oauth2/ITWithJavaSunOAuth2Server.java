package org.apiphany.security.oauth2;

import org.apiphany.security.JwtTokenValidator;
import org.apiphany.security.oauth2.server.JavaSunHttpServer;
import org.apiphany.security.oauth2.server.JavaSunOAuth2Server;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for Integration Tests that use a {@link JavaSunOAuth2Server}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(JavaSunOAuth2ITExtension.class)
public abstract class ITWithJavaSunOAuth2Server {

	/**
	 * Default constructor.
	 */
	protected ITWithJavaSunOAuth2Server() {
		// empty
	}

	/**
	 * Returns the OAuth2 server instance.
	 *
	 * @return the OAuth2 server instance
	 */
	protected static JavaSunOAuth2Server oAuth2Server() {
		return JavaSunOAuth2ITExtension.oauth2Server();
	}

	/**
	 * Returns the protected API server instance.
	 *
	 * @return the protected API server instance
	 */
	protected static JavaSunHttpServer apiServer() {
		return JavaSunOAuth2ITExtension.apiServer();
	}

	/**
	 * Returns the JWT token validator.
	 *
	 * @return the JWT token validator
	 */
	protected static JwtTokenValidator tokenValidator() {
		return JavaSunOAuth2ITExtension.tokenValidator();
	}
}
