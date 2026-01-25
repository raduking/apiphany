package org.apiphany.security.oath2;

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

	protected static JavaSunOAuth2Server oAuth2Server() {
		return JavaSunOAuth2ITExtension.OAUTH2_SERVER;
	}

	protected static JavaSunHttpServer apiServer() {
		return JavaSunOAuth2ITExtension.API_SERVER;
	}

	protected static JwtTokenValidator tokenValidator() {
		return JavaSunOAuth2ITExtension.JWT_TOKEN_VALIDATOR;
	}
}
