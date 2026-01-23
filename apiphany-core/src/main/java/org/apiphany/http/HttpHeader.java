package org.apiphany.http;

import java.util.Map;
import java.util.Objects;

import org.morphix.lang.Enums;

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
	ACCEPT("Accept"),

	/**
	 * The HTTP {@code Accept-Charset} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.3">Section 5.3.3 of RFC 7231</a>
	 */
	ACCEPT_CHARSET("Accept-Charset"),

	/**
	 * The HTTP {@code Accept-Encoding} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.4">Section 5.3.4 of RFC 7231</a>
	 */
	ACCEPT_ENCODING("Accept-Encoding"),

	/**
	 * The HTTP {@code Accept-Language} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">Section 5.3.5 of RFC 7231</a>
	 */
	ACCEPT_LANGUAGE("Accept-Language"),

	/**
	 * The HTTP {@code Accept-Patch} header field name.
	 *
	 * @since 5.3.6
	 * @see <a href="https://tools.ietf.org/html/rfc5789#section-3.1">Section 3.1 of RFC 5789</a>
	 */
	ACCEPT_PATCH("Accept-Patch"),

	/**
	 * The HTTP {@code Accept-Ranges} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-2.3">Section 5.3.5 of RFC 7233</a>
	 */
	ACCEPT_RANGES("Accept-Ranges"),

	/**
	 * The CORS {@code Access-Control-Allow-Credentials} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),

	/**
	 * The CORS {@code Access-Control-Allow-Headers} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),

	/**
	 * The CORS {@code Access-Control-Allow-Methods} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),

	/**
	 * The CORS {@code Access-Control-Allow-Origin} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),

	/**
	 * The CORS {@code Access-Control-Expose-Headers} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),

	/**
	 * The CORS {@code Access-Control-Max-Age} response header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),

	/**
	 * The CORS {@code Access-Control-Request-Headers} request header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers"),

	/**
	 * The CORS {@code Access-Control-Request-Method} request header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),

	/**
	 * The HTTP {@code Age} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.1">Section 5.1 of RFC 7234</a>
	 */
	AGE("Age"),

	/**
	 * The HTTP {@code Alt-Svc} header field name. Used to list alternative services for this origin, such as HTTP/3
	 * endpoints.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7838">RFC 7838</a>
	 */
	ALT_SVC("Alt-Svc"),

	/**
	 * The HTTP {@code Allow} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.1">Section 7.4.1 of RFC 7231</a>
	 */
	ALLOW("Allow"),

	/**
	 * The HTTP {@code Authorization} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.2">Section 4.2 of RFC 7235</a>
	 */
	AUTHORIZATION("Authorization"),

	/**
	 * The HTTP {@code Cache-Control} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.2">Section 5.2 of RFC 7234</a>
	 */
	CACHE_CONTROL("Cache-Control"),

	/**
	 * The HTTP {@code Connection} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.1">Section 6.1 of RFC 7230</a>
	 */
	CONNECTION("Connection"),

	/**
	 * The HTTP {@code Content-Encoding} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.2.2">Section 3.1.2.2 of RFC 7231</a>
	 */
	CONTENT_ENCODING("Content-Encoding"),

	/**
	 * The HTTP {@code Content-Disposition} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6266">RFC 6266</a>
	 */
	CONTENT_DISPOSITION("Content-Disposition"),

	/**
	 * The HTTP {@code Content-Language} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section 3.1.3.2 of RFC 7231</a>
	 */
	CONTENT_LANGUAGE("Content-Language"),

	/**
	 * The HTTP {@code Content-Length} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.2">Section 3.3.2 of RFC 7230</a>
	 */
	CONTENT_LENGTH("Content-Length"),

	/**
	 * The HTTP {@code Content-Location} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.4.2">Section 3.1.4.2 of RFC 7231</a>
	 */
	CONTENT_LOCATION("Content-Location"),

	/**
	 * The HTTP {@code Content-Range} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.2">Section 4.2 of RFC 7233</a>
	 */
	CONTENT_RANGE("Content-Range"),

	/**
	 * The HTTP {@code Content-Security-Policy} header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/CSP3/#csp-header">Content Security Policy Level 3, Section 4.1</a>
	 */
	CONTENT_SECURITY_POLICY("Content-Security-Policy"),

	/**
	 * The HTTP {@code Content-Security-Policy-Report-Only} header field name.
	 *
	 * @see <a href="https://www.w3.org/TR/CSP3/#cspro-header">Content Security Policy Level 3, Section 4.2</a>
	 */
	CONTENT_SECURITY_POLICY_REPORT_ONLY("Content-Security-Policy-Report-Only"),

	/**
	 * The HTTP {@code Content-Type} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section 3.1.1.5 of RFC 7231</a>
	 */
	CONTENT_TYPE("Content-Type"),

	/**
	 * The HTTP {@code Cookie} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.3.4">Section 4.3.4 of RFC 2109</a>
	 */
	COOKIE("Cookie"),

	/**
	 * The HTTP {@code Date} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.2">Section 7.1.1.2 of RFC 7231</a>
	 */
	DATE("Date"),

	/**
	 * The HTTP {@code ETag} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC 7232</a>
	 */
	ETAG("ETag"),

	/**
	 * The HTTP {@code Expect} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.1">Section 5.1.1 of RFC 7231</a>
	 */
	EXPECT("Expect"),

	/**
	 * The HTTP {@code Expires} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.3">Section 5.3 of RFC 7234</a>
	 */
	EXPIRES("Expires"),

	/**
	 * The HTTP {@code From} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.1">Section 5.5.1 of RFC 7231</a>
	 */
	FROM("From"),

	/**
	 * The HTTP {@code Host} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.4">Section 5.4 of RFC 7230</a>
	 */
	HOST("Host"),

	/**
	 * The HTTP {@code If-Match} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.1">Section 3.1 of RFC 7232</a>
	 */
	IF_MATCH("If-Match"),

	/**
	 * The HTTP {@code If-Modified-Since} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.3">Section 3.3 of RFC 7232</a>
	 */
	IF_MODIFIED_SINCE("If-Modified-Since"),

	/**
	 * The HTTP {@code If-None-Match} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.2">Section 3.2 of RFC 7232</a>
	 */
	IF_NONE_MATCH("If-None-Match"),

	/**
	 * The HTTP {@code If-Range} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.2">Section 3.2 of RFC 7233</a>
	 */
	IF_RANGE("If-Range"),

	/**
	 * The HTTP {@code If-Unmodified-Since} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.4">Section 3.4 of RFC 7232</a>
	 */
	IF_UNMODIFIED_SINCE("If-Unmodified-Since"),

	/**
	 * The HTTP {@code Last-Modified} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.2">Section 2.2 of RFC 7232</a>
	 */
	LAST_MODIFIED("Last-Modified"),

	/**
	 * The HTTP {@code Link} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc5988">RFC 5988</a>
	 */
	LINK("Link"),

	/**
	 * The HTTP {@code Location} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.2">Section 7.1.2 of RFC 7231</a>
	 */
	LOCATION("Location"),

	/**
	 * The HTTP {@code Max-Forwards} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.2">Section 5.1.2 of RFC 7231</a>
	 */
	MAX_FORWARDS("Max-Forwards"),

	/**
	 * The HTTP {@code Origin} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6454">RFC 6454</a>
	 */
	ORIGIN("Origin"),

	/**
	 * The HTTP {@code Pragma} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.4">Section 5.4 of RFC 7234</a>
	 */
	PRAGMA("Pragma"),

	/**
	 * The HTTP {@code Proxy-Authenticate} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.3">Section 4.3 of RFC 7235</a>
	 */
	PROXY_AUTHENTICATE("Proxy-Authenticate"),

	/**
	 * The HTTP {@code Proxy-Authorization} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.4">Section 4.4 of RFC 7235</a>
	 */
	PROXY_AUTHORIZATION("Proxy-Authorization"),

	/**
	 * The HTTP {@code Range} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.1">Section 3.1 of RFC 7233</a>
	 */
	RANGE("Range"),

	/**
	 * The HTTP {@code Referer} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.2">Section 5.5.2 of RFC 7231</a>
	 */
	REFERER("Referer"),

	/**
	 * The HTTP {@code Retry-After} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.3">Section 7.1.3 of RFC 7231</a>
	 */
	RETRY_AFTER("Retry-After"),

	/**
	 * The HTTP {@code Server} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.2">Section 7.4.2 of RFC 7231</a>
	 */
	SERVER("Server"),

	/**
	 * The HTTP {@code Set-Cookie} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.2.2">Section 4.2.2 of RFC 2109</a>
	 */
	SET_COOKIE("Set-Cookie"),

	/**
	 * The HTTP {@code Set-Cookie2} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2965">RFC 2965</a>
	 */
	SET_COOKIE2("Set-Cookie2"),

	/**
	 * The HTTP {@code TE} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.3">Section 4.3 of RFC 7230</a>
	 */
	TE("TE"),

	/**
	 * The HTTP {@code Trailer} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.4">Section 4.4 of RFC 7230</a>
	 */
	TRAILER("Trailer"),

	/**
	 * The HTTP {@code Transfer-Encoding} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.1">Section 3.3.1 of RFC 7230</a>
	 */
	TRANSFER_ENCODING("Transfer-Encoding"),

	/**
	 * The HTTP {@code Upgrade} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.7">Section 6.7 of RFC 7230</a>
	 */
	UPGRADE("Upgrade"),

	/**
	 * The HTTP {@code User-Agent} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.3">Section 5.5.3 of RFC 7231</a>
	 */
	USER_AGENT("User-Agent"),

	/**
	 * The HTTP {@code Vary} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.4">Section 7.1.4 of RFC 7231</a>
	 */
	VARY("Vary"),

	/**
	 * The HTTP {@code Via} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.7.1">Section 5.7.1 of RFC 7230</a>
	 */
	VIA("Via"),

	/**
	 * The HTTP {@code Warning} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.5">Section 5.5 of RFC 7234</a>
	 */
	WARNING("Warning"),

	/**
	 * The HTTP {@code WWW-Authenticate} header field name.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.1">Section 4.1 of RFC 7235</a>
	 */
	WWW_AUTHENTICATE("WWW-Authenticate"),

	/**
	 * The de facto standard {@code X-Frame-Options} header field name. Used to indicate whether a browser should be allowed
	 * to render a page in a {@code <frame>}, {@code <iframe>}, {@code <embed>}, or {@code <object>}.
	 *
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options">MDN Web Docs</a>
	 * @see <a href="https://tools.ietf.org/html/rfc7034">RFC 7034 (This RFC describes the header but does not establish it
	 * as a standard)</a>
	 */
	X_FRAME_OPTIONS("X-Frame-Options"),

	/**
	 * The non-standard {@code X-XSS-Protection} header field name. Used to control the XSS filter built into older
	 * browsers.
	 *
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-XSS-Protection">MDN Web Docs</a>
	 * @deprecated This header is deprecated and should be disabled in favor of using Content-Security-Policy. Modern
	 * browsers have removed support for this header.
	 */
	@Deprecated
	X_XSS_PROTECTION("X-XSS-Protection");

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, HttpHeader> NAME_MAP = Enums.buildNameMap(values(), header -> header.toString().toLowerCase());

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
