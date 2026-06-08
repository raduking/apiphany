package org.apiphany.security.http;

import java.util.Set;

import org.apiphany.http.DeFactoHeader;
import org.apiphany.http.HttpHeader;
import org.apiphany.lang.Strings;

/**
 * Common HTTP-sensitive defaults. These can be used for redaction, secure logging or any other purpose where
 * identifying common sensitive header and parameter names is necessary.
 * <p>
 * Contains default header and parameter names that are commonly used to transport credentials, authentication tokens,
 * session identifiers, or other sensitive values.
 *
 * @author Radu Sebastian LAZIN
 */
public class DefaultHttpSensitivity implements HttpSensitivity { // NOSONAR - singleton intended

	/**
	 * A predefined set of common HTTP-sensitive header names that should be treated with special care.
	 */
	private static final Set<String> HEADERS = Set.of(
			HttpHeader.Name.AUTHORIZATION,
			HttpHeader.Name.PROXY_AUTHORIZATION,
			HttpHeader.Name.COOKIE,
			HttpHeader.Name.SET_COOKIE,
			HttpHeader.Name.SET_COOKIE2,
			HttpHeader.Name.WWW_AUTHENTICATE,
			DeFactoHeader.Name.API_KEY,
			DeFactoHeader.Name.X_API_KEY,
			DeFactoHeader.Name.X_AUTH_TOKEN,
			DeFactoHeader.Name.X_AUTHORIZATION);

	/**
	 * A predefined set of common HTTP-sensitive query parameter names that should be treated with special care.
	 */
	private static final Set<String> PARAMS = Set.of(
			"token",
			"access_token",
			"refresh_token",
			"api_key",
			"apikey",
			"code",
			"client_secret",
			"password");

	/**
	 * @see HttpSensitivity#isSensitiveHeader(String)
	 */
	@Override
	public boolean isSensitiveHeader(final String headerName) {
		return Strings.containsIgnoreCase(headerName, HEADERS);
	}

	/**
	 * @see HttpSensitivity#isSensitiveParam(String)
	 */
	@Override
	public boolean isSensitiveParam(final String paramName) {
		return Strings.containsIgnoreCase(paramName, PARAMS);
	}

	/**
	 * Returns the singleton instance of {@link DefaultHttpSensitivity}.
	 *
	 * @return the singleton instance of {@link DefaultHttpSensitivity}
	 */
	public static DefaultHttpSensitivity instance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Private constructor.
	 */
	private DefaultHttpSensitivity() {
		// empty
	}

	/**
	 * Instance holder for the singleton instance of {@link DefaultHttpSensitivity}. This ensures that the instance is
	 * created lazily and in a thread-safe manner without the need for synchronization.
	 *
	 * @see DefaultHttpSensitivity#instance()
	 */
	private static class InstanceHolder {

		/**
		 * The singleton instance of {@link DefaultHttpSensitivity}.
		 */
		private static final DefaultHttpSensitivity INSTANCE = new DefaultHttpSensitivity();
	}
}
