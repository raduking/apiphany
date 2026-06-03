package org.apiphany.http;

import java.util.Map;
import java.util.Objects;

import org.apiphany.header.HeaderName;
import org.morphix.lang.Enums;
import org.morphix.lang.function.ToStringFunction;
import org.morphix.reflection.Constructors;

/**
 * De facto standard HTTP headers used for common purposes across different frameworks and platforms.
 * <p>
 * This class provides type-safe access to widely adopted header names that are not officially standardized but are
 * commonly used in practice. Examples include API keys, authentication tokens, and custom headers that have become de
 * facto standards in the industry.
 * <p>
 * TODO: maybe add a HeaderRole enum to categorize them (e.g., CREDENTIAL, CONTEXT, SECURITY_POLICY, TRACING, OTHER).
 *
 * @see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>
 * @see HttpHeader
 * @see TracingHeader
 *
 * @author Radu Sebastian LAZIN
 */
public enum DeFactoHeader implements HeaderName {

	/**
	 * The {@code Api-Key} header is commonly used to pass an API key for authentication and authorization purposes.
	 */
	API_KEY(Name.API_KEY),

	/**
	 * The {@code Correlation-Id} header is used to correlate requests across different services and components in a
	 * distributed system.
	 */
	CORRELATION_ID(Name.CORRELATION_ID),

	/**
	 * The {@code Request-Id} header is used to uniquely identify a single request, often for tracing and debugging
	 * purposes.
	 */
	REQUEST_ID(Name.REQUEST_ID),

	/**
	 * The {@code X-API-Key} header is an alternative to {@code Api-Key} and is also commonly used for API key
	 * authentication.
	 */
	X_API_KEY(Name.X_API_KEY),

	/**
	 * The {@code X-Auth-Token} header is commonly used to pass an authentication token, such as a JWT or session token, for
	 * securing API requests.
	 */
	X_AUTH_TOKEN(Name.X_AUTH_TOKEN),

	/**
	 * The {@code X-Authorization} header is an alternative to {@code Authorization} and is sometimes used in custom
	 * authentication schemes.
	 */
	X_AUTHORIZATION(Name.X_AUTHORIZATION),

	/**
	 * The de facto standard {@code X-Content-Type-Options} header field name. Used to prevent MIME type sniffing by
	 * instructing browsers to follow the declared content type.
	 *
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Content-Type-Options">MDN Web Docs</a>
	 * @see <a href="https://tools.ietf.org/html/rfc7034">RFC 7034 (This RFC describes the header but does not establish it
	 * as a standard)</a>
	 */
	X_CONTENT_TYPE_OPTIONS(Name.X_CONTENT_TYPE_OPTIONS),

	/**
	 * The de facto standard {@code X-Frame-Options} header field name. Used to indicate whether a browser should be allowed
	 * to render a page in a {@code <frame>}, {@code <iframe>}, {@code <embed>}, or {@code <object>}.
	 *
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options">MDN Web Docs</a>
	 * @see <a href="https://tools.ietf.org/html/rfc7034">RFC 7034 (This RFC describes the header but does not establish it
	 * as a standard)</a>
	 */
	X_FRAME_OPTIONS(Name.X_FRAME_OPTIONS),

	/**
	 * The {@code X-Request-ID} header is an alternative to {@code Request-Id} and is also commonly used to uniquely
	 * identify a single request for tracing and debugging purposes.
	 */
	X_REQUEST_ID(Name.X_REQUEST_ID),

	/**
	 * The non-standard {@code X-XSS-Protection} header field name. Used to control the XSS filter built into older
	 * browsers.
	 *
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-XSS-Protection">MDN Web Docs</a>
	 * @deprecated This header is deprecated and should be disabled in favor of using Content-Security-Policy. Modern
	 * browsers have removed support for this header.
	 */
	@Deprecated
	X_XSS_PROTECTION(Name.X_XSS_PROTECTION); // NOSONAR

	/**
	 * Namespace for the header name values.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Name {

		/**
		 * The non-standard but widely used HTTP {@code Api-Key} header name.
		 */
		public static final String API_KEY = "Api-Key";

		/**
		 * The non-standard but widely used HTTP {@code Correlation-Id} header name.
		 */
		public static final String CORRELATION_ID = "Correlation-Id";

		/**
		 * The non-standard but widely used HTTP {@code Request-Id} header name.
		 */
		public static final String REQUEST_ID = "Request-Id";

		/**
		 * The non-standard but widely used HTTP {@code X-API-Key} header name.
		 */
		public static final String X_API_KEY = "X-API-Key";

		/**
		 * The non-standard but widely used HTTP {@code X-Auth-Token} header name.
		 */
		public static final String X_AUTH_TOKEN = "X-Auth-Token";

		/**
		 * The non-standard but widely used HTTP {@code X-Authorization} header name.
		 */
		public static final String X_AUTHORIZATION = "X-Authorization";

		/**
		 * The de facto standard {@code X-Content-Type-Options} header field name. Used to prevent MIME type sniffing by
		 * instructing browsers to follow the declared content type.
		 *
		 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Content-Type-Options">MDN Web Docs</a>
		 * @see <a href="https://tools.ietf.org/html/rfc7034">RFC 7034 (This RFC describes the header but does not establish it
		 * as a standard)</a>
		 */
		public static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

		/**
		 * The de facto standard {@code X-Frame-Options} header field name. Used to indicate whether a browser should be allowed
		 * to render a page in a {@code <frame>}, {@code <iframe>}, {@code <embed>}, or {@code <object>}.
		 *
		 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options">MDN Web Docs</a>
		 * @see <a href="https://tools.ietf.org/html/rfc7034">RFC 7034 (This RFC describes the header but does not establish it
		 * as a standard)</a>
		 */
		public static final String X_FRAME_OPTIONS = "X-Frame-Options";

		/**
		 * The non-standard but widely used HTTP {@code X-Request-Id} header name.
		 */
		public static final String X_REQUEST_ID = "X-Request-Id";

		/**
		 * The non-standard {@code X-XSS-Protection} header field name. Used to control the XSS filter built into older
		 * browsers.
		 *
		 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-XSS-Protection">MDN Web Docs</a>
		 * @deprecated This header is deprecated and should be disabled in favor of using Content-Security-Policy. Modern
		 * browsers have removed support for this header.
		 */
		@Deprecated // NOSONAR
		public static final String X_XSS_PROTECTION = "X-XSS-Protection"; // NOSONAR

		/**
		 * Private constructor to prevent instantiation.
		 */
		private Name() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, DeFactoHeader> NAME_MAP = Enums.buildNameMap(values(), ToStringFunction.toLowerCase());

	/**
	 * The {@link String} value.
	 */
	private final String value;

	/**
	 * Constructor.
	 *
	 * @param value string value
	 */
	DeFactoHeader(final String value) {
		this.value = Objects.requireNonNull(value, "header value cannot be null");
	}

	/**
	 * Returns a {@link DeFactoHeader} enum from a {@link String}.
	 *
	 * @param header HTTP header as string
	 * @return a HTTP header enum
	 */
	public static DeFactoHeader fromString(final String header) {
		return Enums.fromString(Objects.requireNonNull(header).toLowerCase(), NAME_MAP, values());
	}

	/**
	 * Returns the string value.
	 *
	 * @return the string value
	 */
	@Override
	public String value() {
		return value;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return value();
	}
}
