package org.apiphany.http;

import java.util.Map;
import java.util.Objects;

import org.morphix.lang.Enums;
import org.morphix.lang.function.ToStringFunction;
import org.morphix.reflection.Constructors;

/**
 * RFC 7231 HTTP headers.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum HttpHeader {

	/**
	 * The HTTP {@code Accept} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">Section 5.3.2 of RFC 7231</a>
	 */
	ACCEPT(Name.ACCEPT),

	/**
	 * The HTTP {@code Accept-Charset} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.3">Section 5.3.3 of RFC 7231</a>
	 */
	ACCEPT_CHARSET(Name.ACCEPT_CHARSET),

	/**
	 * The HTTP {@code Accept-Encoding} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.4">Section 5.3.4 of RFC 7231</a>
	 */
	ACCEPT_ENCODING(Name.ACCEPT_ENCODING),

	/**
	 * The HTTP {@code Accept-Language} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">Section 5.3.5 of RFC 7231</a>
	 */
	ACCEPT_LANGUAGE(Name.ACCEPT_LANGUAGE),

	/**
	 * The HTTP {@code Accept-Patch} header field name.
	 *
	 * @since 5.3.6
	 * @see <a href="https://tools.ietf.org/html/rfc5789#section-3.1">Section 3.1 of RFC 5789</a>
	 */
	ACCEPT_PATCH(Name.ACCEPT_PATCH),

	/**
	 * The HTTP {@code Accept-Ranges} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-2.3">Section 5.3.5 of RFC 7233</a>
	 */
	ACCEPT_RANGES(Name.ACCEPT_RANGES),

	/**
	 * The CORS {@code Access-Control-Allow-Credentials} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_ALLOW_CREDENTIALS(Name.ACCESS_CONTROL_ALLOW_CREDENTIALS),

	/**
	 * The CORS {@code Access-Control-Allow-Headers} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_ALLOW_HEADERS(Name.ACCESS_CONTROL_ALLOW_HEADERS),

	/**
	 * The CORS {@code Access-Control-Allow-Methods} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_ALLOW_METHODS(Name.ACCESS_CONTROL_ALLOW_METHODS),

	/**
	 * The CORS {@code Access-Control-Allow-Origin} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_ALLOW_ORIGIN(Name.ACCESS_CONTROL_ALLOW_ORIGIN),

	/**
	 * The CORS {@code Access-Control-Expose-Headers} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_EXPOSE_HEADERS(Name.ACCESS_CONTROL_EXPOSE_HEADERS),

	/**
	 * The CORS {@code Access-Control-Max-Age} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_MAX_AGE(Name.ACCESS_CONTROL_MAX_AGE),

	/**
	 * The CORS {@code Access-Control-Request-Headers} request header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_REQUEST_HEADERS(Name.ACCESS_CONTROL_REQUEST_HEADERS),

	/**
	 * The CORS {@code Access-Control-Request-Method} request header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_REQUEST_METHOD(Name.ACCESS_CONTROL_REQUEST_METHOD),

	/**
	 * The HTTP {@code Age} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.1">Section 5.1 of RFC 7234</a>
	 */
	AGE(Name.AGE),

	/**
	 * The HTTP {@code Alt-Svc} header field name. Used to list alternative services for this origin, such as HTTP/3
	 * endpoints.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7838">RFC 7838</a>
	 */
	ALT_SVC(Name.ALT_SVC),

	/**
	 * The HTTP {@code Allow} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.1">Section 7.4.1 of RFC 7231</a>
	 */
	ALLOW(Name.ALLOW),

	/**
	 * The HTTP {@code Authorization} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.2">Section 4.2 of RFC 7235</a>
	 */
	AUTHORIZATION(Name.AUTHORIZATION),

	/**
	 * The HTTP {@code Cache-Control} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.2">Section 5.2 of RFC 7234</a>
	 */
	CACHE_CONTROL(Name.CACHE_CONTROL),

	/**
	 * The HTTP {@code Connection} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.1">Section 6.1 of RFC 7230</a>
	 */
	CONNECTION(Name.CONNECTION),

	/**
	 * The HTTP {@code Content-Encoding} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.2.2">Section 3.1.2.2 of RFC 7231</a>
	 */
	CONTENT_ENCODING(Name.CONTENT_ENCODING),

	/**
	 * The HTTP {@code Content-Disposition} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6266">RFC 6266</a>
	 */
	CONTENT_DISPOSITION(Name.CONTENT_DISPOSITION),

	/**
	 * The HTTP {@code Content-Language} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section 3.1.3.2 of RFC 7231</a>
	 */
	CONTENT_LANGUAGE(Name.CONTENT_LANGUAGE),

	/**
	 * The HTTP {@code Content-Length} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.2">Section 3.3.2 of RFC 7230</a>
	 */
	CONTENT_LENGTH(Name.CONTENT_LENGTH),

	/**
	 * The HTTP {@code Content-Location} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.4.2">Section 3.1.4.2 of RFC 7231</a>
	 */
	CONTENT_LOCATION(Name.CONTENT_LOCATION),

	/**
	 * The HTTP {@code Content-Range} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.2">Section 4.2 of RFC 7233</a>
	 */
	CONTENT_RANGE(Name.CONTENT_RANGE),

	/**
	 * The HTTP {@code Content-Security-Policy} header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/CSP3/#csp-header">Content Security Policy Level 3, Section 4.1</a>
	 */
	CONTENT_SECURITY_POLICY(Name.CONTENT_SECURITY_POLICY),

	/**
	 * The HTTP {@code Content-Security-Policy-Report-Only} header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/CSP3/#cspro-header">Content Security Policy Level 3, Section 4.2</a>
	 */
	CONTENT_SECURITY_POLICY_REPORT_ONLY(Name.CONTENT_SECURITY_POLICY_REPORT_ONLY),

	/**
	 * The HTTP {@code Content-Type} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section 3.1.1.5 of RFC 7231</a>
	 */
	CONTENT_TYPE(Name.CONTENT_TYPE),

	/**
	 * The HTTP {@code Cookie} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.3.4">Section 4.3.4 of RFC 2109</a>
	 */
	COOKIE(Name.COOKIE),

	/**
	 * The HTTP {@code Date} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.2">Section 7.1.1.2 of RFC 7231</a>
	 */
	DATE(Name.DATE),

	/**
	 * The HTTP {@code ETag} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC 7232</a>
	 */
	ETAG(Name.ETAG),

	/**
	 * The HTTP {@code Expect} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.1">Section 5.1.1 of RFC 7231</a>
	 */
	EXPECT(Name.EXPECT),

	/**
	 * The HTTP {@code Expires} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.3">Section 5.3 of RFC 7234</a>
	 */
	EXPIRES(Name.EXPIRES),

	/**
	 * The HTTP {@code From} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.1">Section 5.5.1 of RFC 7231</a>
	 */
	FROM(Name.FROM),

	/**
	 * The HTTP {@code Host} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.4">Section 5.4 of RFC 7230</a>
	 */
	HOST(Name.HOST),

	/**
	 * The HTTP {@code If-Match} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.1">Section 3.1 of RFC 7232</a>
	 */
	IF_MATCH(Name.IF_MATCH),

	/**
	 * The HTTP {@code If-Modified-Since} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.3">Section 3.3 of RFC 7232</a>
	 */
	IF_MODIFIED_SINCE(Name.IF_MODIFIED_SINCE),

	/**
	 * The HTTP {@code If-None-Match} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.2">Section 3.2 of RFC 7232</a>
	 */
	IF_NONE_MATCH(Name.IF_NONE_MATCH),

	/**
	 * The HTTP {@code If-Range} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.2">Section 3.2 of RFC 7233</a>
	 */
	IF_RANGE(Name.IF_RANGE),

	/**
	 * The HTTP {@code If-Unmodified-Since} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.4">Section 3.4 of RFC 7232</a>
	 */
	IF_UNMODIFIED_SINCE(Name.IF_UNMODIFIED_SINCE),

	/**
	 * The HTTP {@code Last-Modified} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.2">Section 2.2 of RFC 7232</a>
	 */
	LAST_MODIFIED(Name.LAST_MODIFIED),

	/**
	 * The HTTP {@code Link} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc5988">RFC 5988</a>
	 */
	LINK(Name.LINK),

	/**
	 * The HTTP {@code Location} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.2">Section 7.1.2 of RFC 7231</a>
	 */
	LOCATION(Name.LOCATION),

	/**
	 * The HTTP {@code Max-Forwards} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.2">Section 5.1.2 of RFC 7231</a>
	 */
	MAX_FORWARDS(Name.MAX_FORWARDS),

	/**
	 * The HTTP {@code Origin} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6454">RFC 6454</a>
	 */
	ORIGIN(Name.ORIGIN),

	/**
	 * The HTTP {@code Pragma} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.4">Section 5.4 of RFC 7234</a>
	 */
	PRAGMA(Name.PRAGMA),

	/**
	 * The HTTP {@code Proxy-Authenticate} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.3">Section 4.3 of RFC 7235</a>
	 */
	PROXY_AUTHENTICATE(Name.PROXY_AUTHENTICATE),

	/**
	 * The HTTP {@code Proxy-Authorization} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.4">Section 4.4 of RFC 7235</a>
	 */
	PROXY_AUTHORIZATION(Name.PROXY_AUTHORIZATION),

	/**
	 * The HTTP {@code Range} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.1">Section 3.1 of RFC 7233</a>
	 */
	RANGE(Name.RANGE),

	/**
	 * The HTTP {@code Referer} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.2">Section 5.5.2 of RFC 7231</a>
	 */
	REFERER(Name.REFERER),

	/**
	 * The HTTP {@code Retry-After} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.3">Section 7.1.3 of RFC 7231</a>
	 */
	RETRY_AFTER(Name.RETRY_AFTER),

	/**
	 * The HTTP {@code Server} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.2">Section 7.4.2 of RFC 7231</a>
	 */
	SERVER(Name.SERVER),

	/**
	 * The HTTP {@code Set-Cookie} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.2.2">Section 4.2.2 of RFC 2109</a>
	 */
	SET_COOKIE(Name.SET_COOKIE),

	/**
	 * The HTTP {@code Set-Cookie2} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2965">RFC 2965</a>
	 */
	SET_COOKIE2(Name.SET_COOKIE2),

	/**
	 * The HTTP {@code TE} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.3">Section 4.3 of RFC 7230</a>
	 */
	TE(Name.TE),

	/**
	 * The HTTP {@code Trailer} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.4">Section 4.4 of RFC 7230</a>
	 */
	TRAILER(Name.TRAILER),

	/**
	 * The HTTP {@code Transfer-Encoding} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.1">Section 3.3.1 of RFC 7230</a>
	 */
	TRANSFER_ENCODING(Name.TRANSFER_ENCODING),

	/**
	 * The HTTP {@code Upgrade} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.7">Section 6.7 of RFC 7230</a>
	 */
	UPGRADE(Name.UPGRADE),

	/**
	 * The HTTP {@code User-Agent} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.3">Section 5.5.3 of RFC 7231</a>
	 */
	USER_AGENT(Name.USER_AGENT),

	/**
	 * The HTTP {@code Vary} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.4">Section 7.1.4 of RFC 7231</a>
	 */
	VARY(Name.VARY),

	/**
	 * The HTTP {@code Via} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.7.1">Section 5.7.1 of RFC 7230</a>
	 */
	VIA(Name.VIA),

	/**
	 * The HTTP {@code Warning} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.5">Section 5.5 of RFC 7234</a>
	 */
	WARNING(Name.WARNING),

	/**
	 * The HTTP {@code WWW-Authenticate} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.1">Section 4.1 of RFC 7235</a>
	 */
	WWW_AUTHENTICATE(Name.WWW_AUTHENTICATE),

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
	 * Namespace for the standard HTTP header field names.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Name {

		/**
		 * The HTTP {@code Accept} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">Section 5.3.2 of RFC 7231</a>
		 */
		public static final String ACCEPT = "Accept";

		/**
		 * The HTTP {@code Accept-Charset} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.3">Section 5.3.3 of RFC 7231</a>
		 */
		public static final String ACCEPT_CHARSET = "Accept-Charset";

		/**
		 * The HTTP {@code Accept-Encoding} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.4">Section 5.3.4 of RFC 7231</a>
		 */
		public static final String ACCEPT_ENCODING = "Accept-Encoding";

		/**
		 * The HTTP {@code Accept-Language} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">Section 5.3.5 of RFC 7231</a>
		 */
		public static final String ACCEPT_LANGUAGE = "Accept-Language";

		/**
		 * The HTTP {@code Accept-Patch} header field name.
		 *
		 * @since 5.3.6
		 * @see <a href="https://tools.ietf.org/html/rfc5789#section-3.1">Section 3.1 of RFC 5789</a>
		 */
		public static final String ACCEPT_PATCH = "Accept-Patch";

		/**
		 * The HTTP {@code Accept-Ranges} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7233#section-2.3">Section 5.3.5 of RFC 7233</a>
		 */
		public static final String ACCEPT_RANGES = "Accept-Ranges";

		/**
		 * The CORS {@code Access-Control-Allow-Credentials} response header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
		 */
		public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

		/**
		 * The CORS {@code Access-Control-Allow-Headers} response header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
		 */
		public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

		/**
		 * The CORS {@code Access-Control-Allow-Methods} response header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
		 */
		public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

		/**
		 * The CORS {@code Access-Control-Allow-Origin} response header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
		 */
		public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

		/**
		 * The CORS {@code Access-Control-Expose-Headers} response header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
		 */
		public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

		/**
		 * The CORS {@code Access-Control-Max-Age} response header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
		 */
		public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

		/**
		 * The CORS {@code Access-Control-Request-Headers} request header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
		 */
		public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

		/**
		 * The CORS {@code Access-Control-Request-Method} request header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
		 */
		public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

		/**
		 * The HTTP {@code Age} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.1">Section 5.1 of RFC 7234</a>
		 */
		public static final String AGE = "Age";

		/**
		 * The HTTP {@code Alt-Svc} header field name. Used to list alternative services for this origin, such as HTTP/3
		 * endpoints.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7838">RFC 7838</a>
		 */
		public static final String ALT_SVC = "Alt-Svc";

		/**
		 * The HTTP {@code Allow} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.1">Section 7.4.1 of RFC 7231</a>
		 */
		public static final String ALLOW = "Allow";

		/**
		 * The HTTP {@code Authorization} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.2">Section 4.2 of RFC 7235</a>
		 */
		public static final String AUTHORIZATION = "Authorization";

		/**
		 * The HTTP {@code Cache-Control} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.2">Section 5.2 of RFC 7234</a>
		 */
		public static final String CACHE_CONTROL = "Cache-Control";

		/**
		 * The HTTP {@code Connection} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.1">Section 6.1 of RFC 7230</a>
		 */
		public static final String CONNECTION = "Connection";

		/**
		 * The HTTP {@code Content-Encoding} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.2.2">Section 3.1.2.2 of RFC 7231</a>
		 */
		public static final String CONTENT_ENCODING = "Content-Encoding";

		/**
		 * The HTTP {@code Content-Disposition} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc6266">RFC 6266</a>
		 */
		public static final String CONTENT_DISPOSITION = "Content-Disposition";

		/**
		 * The HTTP {@code Content-Language} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section 3.1.3.2 of RFC 7231</a>
		 */
		public static final String CONTENT_LANGUAGE = "Content-Language";

		/**
		 * The HTTP {@code Content-Length} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.2">Section 3.3.2 of RFC 7230</a>
		 */
		public static final String CONTENT_LENGTH = "Content-Length";

		/**
		 * The HTTP {@code Content-Location} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.4.2">Section 3.1.4.2 of RFC 7231</a>
		 */
		public static final String CONTENT_LOCATION = "Content-Location";

		/**
		 * The HTTP {@code Content-Range} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.2">Section 4.2 of RFC 7233</a>
		 */
		public static final String CONTENT_RANGE = "Content-Range";

		/**
		 * The HTTP {@code Content-Security-Policy} header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/CSP3/#csp-header">Content Security Policy Level 3, Section 4.1</a>
		 */
		public static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";

		/**
		 * The HTTP {@code Content-Security-Policy-Report-Only} header field name.
		 *
		 * @see <a href="https://www.w3.org/TR/CSP3/#cspro-header">Content Security Policy Level 3, Section 4.2</a>
		 */
		public static final String CONTENT_SECURITY_POLICY_REPORT_ONLY = "Content-Security-Policy-Report-Only";

		/**
		 * The HTTP {@code Content-Type} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section 3.1.1.5 of RFC 7231</a>
		 */
		public static final String CONTENT_TYPE = "Content-Type";

		/**
		 * The HTTP {@code Cookie} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.3.4">Section 4.3.4 of RFC 2109</a>
		 */
		public static final String COOKIE = "Cookie";

		/**
		 * The HTTP {@code Date} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.2">Section 7.1.1.2 of RFC 7231</a>
		 */
		public static final String DATE = "Date";

		/**
		 * The HTTP {@code ETag} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC 7232</a>
		 */
		public static final String ETAG = "ETag";

		/**
		 * The HTTP {@code Expect} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.1">Section 5.1.1 of RFC 7231</a>
		 */
		public static final String EXPECT = "Expect";

		/**
		 * The HTTP {@code Expires} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.3">Section 5.3 of RFC 7234</a>
		 */
		public static final String EXPIRES = "Expires";

		/**
		 * The HTTP {@code From} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.1">Section 5.5.1 of RFC 7231</a>
		 */
		public static final String FROM = "From";

		/**
		 * The HTTP {@code Host} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.4">Section 5.4 of RFC 7230</a>
		 */
		public static final String HOST = "Host";

		/**
		 * The HTTP {@code If-Match} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.1">Section 3.1 of RFC 7232</a>
		 */
		public static final String IF_MATCH = "If-Match";

		/**
		 * The HTTP {@code If-Modified-Since} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.3">Section 3.3 of RFC 7232</a>
		 */
		public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

		/**
		 * The HTTP {@code If-None-Match} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.2">Section 3.2 of RFC 7232</a>
		 */
		public static final String IF_NONE_MATCH = "If-None-Match";

		/**
		 * The HTTP {@code If-Range} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.2">Section 3.2 of RFC 7233</a>
		 */
		public static final String IF_RANGE = "If-Range";

		/**
		 * The HTTP {@code If-Unmodified-Since} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.4">Section 3.4 of RFC 7232</a>
		 */
		public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

		/**
		 * The HTTP {@code Last-Modified} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.2">Section 2.2 of RFC 7232</a>
		 */
		public static final String LAST_MODIFIED = "Last-Modified";

		/**
		 * The HTTP {@code Link} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc5988">RFC 5988</a>
		 */
		public static final String LINK = "Link";

		/**
		 * The HTTP {@code Location} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.2">Section 7.1.2 of RFC 7231</a>
		 */
		public static final String LOCATION = "Location";

		/**
		 * The HTTP {@code Max-Forwards} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.2">Section 5.1.2 of RFC 7231</a>
		 */
		public static final String MAX_FORWARDS = "Max-Forwards";

		/**
		 * The HTTP {@code Origin} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc6454">RFC 6454</a>
		 */
		public static final String ORIGIN = "Origin";

		/**
		 * The HTTP {@code Pragma} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.4">Section 5.4 of RFC 7234</a>
		 */
		public static final String PRAGMA = "Pragma";

		/**
		 * The HTTP {@code Proxy-Authenticate} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.3">Section 4.3 of RFC 7235</a>
		 */
		public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

		/**
		 * The HTTP {@code Proxy-Authorization} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.4">Section 4.4 of RFC 7235</a>
		 */
		public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

		/**
		 * The HTTP {@code Range} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.1">Section 3.1 of RFC 7233</a>
		 */
		public static final String RANGE = "Range";

		/**
		 * The HTTP {@code Referer} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.2">Section 5.5.2 of RFC 7231</a>
		 */
		public static final String REFERER = "Referer";

		/**
		 * The HTTP {@code Retry-After} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.3">Section 7.1.3 of RFC 7231</a>
		 */
		public static final String RETRY_AFTER = "Retry-After";

		/**
		 * The HTTP {@code Server} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.2">Section 7.4.2 of RFC 7231</a>
		 */
		public static final String SERVER = "Server";

		/**
		 * The HTTP {@code Set-Cookie} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.2.2">Section 4.2.2 of RFC 2109</a>
		 */
		public static final String SET_COOKIE = "Set-Cookie";

		/**
		 * The HTTP {@code Set-Cookie2} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc2965">RFC 2965</a>
		 */
		public static final String SET_COOKIE2 = "Set-Cookie2";

		/**
		 * The HTTP {@code TE} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.3">Section 4.3 of RFC 7230</a>
		 */
		public static final String TE = "TE";

		/**
		 * The HTTP {@code Trailer} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.4">Section 4.4 of RFC 7230</a>
		 */
		public static final String TRAILER = "Trailer";

		/**
		 * The HTTP {@code Transfer-Encoding} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.1">Section 3.3.1 of RFC 7230</a>
		 */
		public static final String TRANSFER_ENCODING = "Transfer-Encoding";

		/**
		 * The HTTP {@code Upgrade} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.7">Section 6.7 of RFC 7230</a>
		 */
		public static final String UPGRADE = "Upgrade";

		/**
		 * The HTTP {@code User-Agent} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.3">Section 5.5.3 of RFC 7231</a>
		 */
		public static final String USER_AGENT = "User-Agent";

		/**
		 * The HTTP {@code Vary} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.4">Section 7.1.4 of RFC 7231</a>
		 */
		public static final String VARY = "Vary";

		/**
		 * The HTTP {@code Via} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.7.1">Section 5.7.1 of RFC 7230</a>
		 */
		public static final String VIA = "Via";

		/**
		 * The HTTP {@code Warning} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.5">Section 5.5 of RFC 7234</a>
		 */
		public static final String WARNING = "Warning";

		/**
		 * The HTTP {@code WWW-Authenticate} header field name.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.1">Section 4.1 of RFC 7235</a>
		 */
		public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

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
	private static final Map<String, HttpHeader> NAME_MAP = Enums.buildNameMap(values(), ToStringFunction.toLowerCase());

	/**
	 * The {@link String} value.
	 */
	private final String value;

	/**
	 * Constructor.
	 *
	 * @param value string value
	 */
	HttpHeader(final String value) {
		this.value = Objects.requireNonNull(value, "header value cannot be null");
	}

	/**
	 * Returns a {@link HttpHeader} enum from a {@link String}.
	 *
	 * @param header HTTP header as string
	 * @return a HTTP header enum
	 */
	public static HttpHeader fromString(final String header) {
		return Enums.fromString(Objects.requireNonNull(header).toLowerCase(), NAME_MAP, values());
	}

	/**
	 * Returns true if the given string matches the enum value ignoring the case, false otherwise. The HTTP headers are
	 * case-insensitive.
	 *
	 * @param header header as string to match
	 * @return true if the given string matches the enum value ignoring the case, false otherwise.
	 */
	public boolean matches(final String header) {
		return value().equalsIgnoreCase(header);
	}

	/**
	 * Returns the string value.
	 *
	 * @return the string value
	 */
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
