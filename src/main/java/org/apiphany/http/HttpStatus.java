package org.apiphany.http;

import java.util.Map;

import org.apiphany.Status;
import org.morphix.lang.Enums;

/**
 * Enumeration of HTTP status codes.
 *
 * <p>
 * The HTTP status code type can be retrieved via {@link #type()}.
 *
 * @see <a href="https://www.iana.org/assignments/http-status-codes">HTTP Status Code Registry</a>
 * @see <a href="https://en.wikipedia.org/wiki/List_of_HTTP_status_codes">List of HTTP status codes - Wikipedia</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum HttpStatus implements Status {

	/**
	 * {@code 100 Continue}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.1">HTTP/1.1: Semantics and Content, section 6.2.1</a>
	 */
	CONTINUE(100, Type.INFORMATIONAL, "Continue"),

	/**
	 * {@code 101 Switching Protocols}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.2">HTTP/1.1: Semantics and Content, section 6.2.2</a>
	 */
	SWITCHING_PROTOCOLS(101, Type.INFORMATIONAL, "Switching Protocols"),

	/**
	 * {@code 102 Processing}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
	 */
	PROCESSING(102, Type.INFORMATIONAL, "Processing"),

	/**
	 * {@code 103 Early Hints}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc8297">An HTTP Status Code for Indicating Hints</a>
	 */
	EARLY_HINTS(103, Type.INFORMATIONAL, "Early Hints"),

	/**
	 * {@code 200 OK}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">HTTP/1.1: Semantics and Content, section 6.3.1</a>
	 */
	OK(200, Type.SUCCESSFUL, "OK"),

	/**
	 * {@code 201 Created}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.2">HTTP/1.1: Semantics and Content, section 6.3.2</a>
	 */
	CREATED(201, Type.SUCCESSFUL, "Created"),

	/**
	 * {@code 202 Accepted}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.3">HTTP/1.1: Semantics and Content, section 6.3.3</a>
	 */
	ACCEPTED(202, Type.SUCCESSFUL, "Accepted"),

	/**
	 * {@code 203 Non-Authoritative Information}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.4">HTTP/1.1: Semantics and Content, section 6.3.4</a>
	 */
	NON_AUTHORITATIVE_INFORMATION(203, Type.SUCCESSFUL, "Non-Authoritative Information"),

	/**
	 * {@code 204 No Content}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.5">HTTP/1.1: Semantics and Content, section 6.3.5</a>
	 */
	NO_CONTENT(204, Type.SUCCESSFUL, "No Content"),

	/**
	 * {@code 205 Reset Content}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.6">HTTP/1.1: Semantics and Content, section 6.3.6</a>
	 */
	RESET_CONTENT(205, Type.SUCCESSFUL, "Reset Content"),

	/**
	 * {@code 206 Partial Content}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.1">HTTP/1.1: Range Requests, section 4.1</a>
	 */
	PARTIAL_CONTENT(206, Type.SUCCESSFUL, "Partial Content"),

	/**
	 * {@code 207 Multi-Status}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
	 */
	MULTI_STATUS(207, Type.SUCCESSFUL, "Multi-Status"),

	/**
	 * {@code 208 Already Reported}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding Extensions</a>
	 */
	ALREADY_REPORTED(208, Type.SUCCESSFUL, "Already Reported"),

	/**
	 * {@code 226 IM Used}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
	 */
	IM_USED(226, Type.SUCCESSFUL, "IM Used"),

	/**
	 * {@code 300 Multiple Choices}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.1">HTTP/1.1: Semantics and Content, section 6.4.1</a>
	 */

	MULTIPLE_CHOICES(300, Type.REDIRECTION, "Multiple Choices"),

	/**
	 * {@code 301 Moved Permanently}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.2">HTTP/1.1: Semantics and Content, section 6.4.2</a>
	 */
	MOVED_PERMANENTLY(301, Type.REDIRECTION, "Moved Permanently"),

	/**
	 * {@code 302 Found}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.3">HTTP/1.1: Semantics and Content, section 6.4.3</a>
	 */
	FOUND(302, Type.REDIRECTION, "Found"),

	/**
	 * {@code 303 See Other}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.4">HTTP/1.1: Semantics and Content, section 6.4.4</a>
	 */
	SEE_OTHER(303, Type.REDIRECTION, "See Other"),

	/**
	 * {@code 304 Not Modified}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.1">HTTP/1.1: Conditional Requests, section 4.1</a>
	 */
	NOT_MODIFIED(304, Type.REDIRECTION, "Not Modified"),

	/**
	 * {@code 305 Use Proxy}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.5">HTTP/1.1: Semantics and Content, section 6.4.5</a>
	 * @deprecated due to security concerns regarding in-band configuration of a proxy
	 */
	@Deprecated
	USE_PROXY(305, Type.REDIRECTION, "Use Proxy"),

	/**
	 * {@code 307 Temporary Redirect}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.7">HTTP/1.1: Semantics and Content, section 6.4.7</a>
	 */
	TEMPORARY_REDIRECT(307, Type.REDIRECTION, "Temporary Redirect"),
	/**
	 * {@code 308 Permanent Redirect}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7238">RFC 7238</a>
	 */
	PERMANENT_REDIRECT(308, Type.REDIRECTION, "Permanent Redirect"),

	/**
	 * {@code 400 Bad Request}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">HTTP/1.1: Semantics and Content, section 6.5.1</a>
	 */
	BAD_REQUEST(400, Type.CLIENT_ERROR, "Bad Request"),

	/**
	 * {@code 401 Unauthorized}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1: Authentication, section 3.1</a>
	 */
	UNAUTHORIZED(401, Type.CLIENT_ERROR, "Unauthorized"),

	/**
	 * {@code 402 Payment Required}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.2">HTTP/1.1: Semantics and Content, section 6.5.2</a>
	 */
	PAYMENT_REQUIRED(402, Type.CLIENT_ERROR, "Payment Required"),

	/**
	 * {@code 403 Forbidden}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1: Semantics and Content, section 6.5.3</a>
	 */
	FORBIDDEN(403, Type.CLIENT_ERROR, "Forbidden"),

	/**
	 * {@code 404 Not Found}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1: Semantics and Content, section 6.5.4</a>
	 */
	NOT_FOUND(404, Type.CLIENT_ERROR, "Not Found"),

	/**
	 * {@code 405 Method Not Allowed}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">HTTP/1.1: Semantics and Content, section 6.5.5</a>
	 */
	METHOD_NOT_ALLOWED(405, Type.CLIENT_ERROR, "Method Not Allowed"),

	/**
	 * {@code 406 Not Acceptable}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.6">HTTP/1.1: Semantics and Content, section 6.5.6</a>
	 */
	NOT_ACCEPTABLE(406, Type.CLIENT_ERROR, "Not Acceptable"),

	/**
	 * {@code 407 Proxy Authentication Required}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.2">HTTP/1.1: Authentication, section 3.2</a>
	 */
	PROXY_AUTHENTICATION_REQUIRED(407, Type.CLIENT_ERROR, "Proxy Authentication Required"),

	/**
	 * {@code 408 Request Timeout}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.7">HTTP/1.1: Semantics and Content, section 6.5.7</a>
	 */
	REQUEST_TIMEOUT(408, Type.CLIENT_ERROR, "Request Timeout"),

	/**
	 * {@code 409 Conflict}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">HTTP/1.1: Semantics and Content, section 6.5.8</a>
	 */
	CONFLICT(409, Type.CLIENT_ERROR, "Conflict"),

	/**
	 * {@code 410 Gone}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.9"> HTTP/1.1: Semantics and Content, section 6.5.9</a>
	 */
	GONE(410, Type.CLIENT_ERROR, "Gone"),

	/**
	 * {@code 411 Length Required}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.10"> HTTP/1.1: Semantics and Content, section
	 * 6.5.10</a>
	 */
	LENGTH_REQUIRED(411, Type.CLIENT_ERROR, "Length Required"),

	/**
	 * {@code 412 Precondition failed}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.2"> HTTP/1.1: Conditional Requests, section 4.2</a>
	 */
	PRECONDITION_FAILED(412, Type.CLIENT_ERROR, "Precondition Failed"),

	/**
	 * {@code 413 Payload Too Large}.
	 *
	 * @since 4.1
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.11"> HTTP/1.1: Semantics and Content, section
	 * 6.5.11</a>
	 */
	PAYLOAD_TOO_LARGE(413, Type.CLIENT_ERROR, "Payload Too Large"),

	/**
	 * {@code 414 URI Too Long}.
	 *
	 * @since 4.1
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12"> HTTP/1.1: Semantics and Content, section
	 * 6.5.12</a>
	 */
	URI_TOO_LONG(414, Type.CLIENT_ERROR, "URI Too Long"),

	/**
	 * {@code 415 Unsupported Media Type}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.13"> HTTP/1.1: Semantics and Content, section
	 * 6.5.13</a>
	 */
	UNSUPPORTED_MEDIA_TYPE(415, Type.CLIENT_ERROR, "Unsupported Media Type"),

	/**
	 * {@code 416 Requested Range Not Satisfiable}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.4">HTTP/1.1: Range Requests, section 4.4</a>
	 */
	REQUESTED_RANGE_NOT_SATISFIABLE(416, Type.CLIENT_ERROR, "Requested range not satisfiable"),

	/**
	 * {@code 417 Expectation Failed}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.14"> HTTP/1.1: Semantics and Content, section
	 * 6.5.14</a>
	 */
	EXPECTATION_FAILED(417, Type.CLIENT_ERROR, "Expectation Failed"),

	/**
	 * {@code 418 I'm a teapot}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
	 */
	I_AM_A_TEAPOT(418, Type.CLIENT_ERROR, "I'm a teapot"),

	/**
	 * {@code 422 Unprocessable Entity}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
	 */
	UNPROCESSABLE_ENTITY(422, Type.CLIENT_ERROR, "Unprocessable Entity"),

	/**
	 * {@code 423 Locked}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
	 */
	LOCKED(423, Type.CLIENT_ERROR, "Locked"),

	/**
	 * {@code 424 Failed Dependency}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
	 */
	FAILED_DEPENDENCY(424, Type.CLIENT_ERROR, "Failed Dependency"),

	/**
	 * {@code 425 Too Early}.
	 *
	 * @since 5.2
	 * @see <a href="https://tools.ietf.org/html/rfc8470">RFC 8470</a>
	 */
	TOO_EARLY(425, Type.CLIENT_ERROR, "Too Early"),

	/**
	 * {@code 426 Upgrade Required}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS Within HTTP/1.1</a>
	 */
	UPGRADE_REQUIRED(426, Type.CLIENT_ERROR, "Upgrade Required"),

	/**
	 * {@code 428 Precondition Required}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status Codes</a>
	 */
	PRECONDITION_REQUIRED(428, Type.CLIENT_ERROR, "Precondition Required"),

	/**
	 * {@code 429 Too Many Requests}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status Codes</a>
	 */
	TOO_MANY_REQUESTS(429, Type.CLIENT_ERROR, "Too Many Requests"),

	/**
	 * {@code 431 Request Header Fields Too Large}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status Codes</a>
	 */
	REQUEST_HEADER_FIELDS_TOO_LARGE(431, Type.CLIENT_ERROR, "Request Header Fields Too Large"),

	/**
	 * {@code 451 Unavailable For Legal Reasons}.
	 *
	 * @see <a href="https://tools.ietf.org/html/draft-ietf-httpbis-legally-restricted-status-04"> An HTTP Status Code to
	 * Report Legal Obstacles</a>
	 * @since 4.3
	 */
	UNAVAILABLE_FOR_LEGAL_REASONS(451, Type.CLIENT_ERROR, "Unavailable For Legal Reasons"),

	/**
	 * {@code 500 Internal Server Error}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">HTTP/1.1: Semantics and Content, section 6.6.1</a>
	 */
	INTERNAL_SERVER_ERROR(500, Type.SERVER_ERROR, "Internal Server Error"),

	/**
	 * {@code 501 Not Implemented}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">HTTP/1.1: Semantics and Content, section 6.6.2</a>
	 */
	NOT_IMPLEMENTED(501, Type.SERVER_ERROR, "Not Implemented"),

	/**
	 * {@code 502 Bad Gateway}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">HTTP/1.1: Semantics and Content, section 6.6.3</a>
	 */
	BAD_GATEWAY(502, Type.SERVER_ERROR, "Bad Gateway"),

	/**
	 * {@code 503 Service Unavailable}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.4">HTTP/1.1: Semantics and Content, section 6.6.4</a>
	 */
	SERVICE_UNAVAILABLE(503, Type.SERVER_ERROR, "Service Unavailable"),

	/**
	 * {@code 504 Gateway Timeout}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.5">HTTP/1.1: Semantics and Content, section 6.6.5</a>
	 */
	GATEWAY_TIMEOUT(504, Type.SERVER_ERROR, "Gateway Timeout"),

	/**
	 * {@code 505 HTTP Version Not Supported}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.6">HTTP/1.1: Semantics and Content, section 6.6.6</a>
	 */
	HTTP_VERSION_NOT_SUPPORTED(505, Type.SERVER_ERROR, "HTTP Version not supported"),

	/**
	 * {@code 506 Variant Also Negotiates}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content Negotiation</a>
	 */
	VARIANT_ALSO_NEGOTIATES(506, Type.SERVER_ERROR, "Variant Also Negotiates"),

	/**
	 * {@code 507 Insufficient Storage}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
	 */
	INSUFFICIENT_STORAGE(507, Type.SERVER_ERROR, "Insufficient Storage"),

	/**
	 * {@code 508 Loop Detected}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding Extensions</a>
	 */
	LOOP_DETECTED(508, Type.SERVER_ERROR, "Loop Detected"),

	/**
	 * {@code 509 Bandwidth Limit Exceeded}
	 */
	BANDWIDTH_LIMIT_EXCEEDED(509, Type.SERVER_ERROR, "Bandwidth Limit Exceeded"),

	/**
	 * {@code 510 Not Extended}
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc2774#section-7">HTTP Extension Framework</a>
	 */
	NOT_EXTENDED(510, Type.SERVER_ERROR, "Not Extended"),

	/**
	 * {@code 511 Network Authentication Required}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status Codes</a>
	 */
	NETWORK_AUTHENTICATION_REQUIRED(511, Type.SERVER_ERROR, "Network Authentication Required");

	/**
	 * All the values.
	 */
	private static final HttpStatus[] VALUES = values();

	/**
	 * The name map for easy from implementation.
	 */
	private static final Map<Integer, HttpStatus> NAME_MAP = Enums.buildNameMap(VALUES, HttpStatus::value);

	/**
	 * The numeric value of the HTTP status code.
	 */
	private final int value;

	/**
	 * The type of the HTTP status code (e.g., informational, successful, redirection, client error, server error).
	 */
	private final Type type;

	/**
	 * The human-readable message associated with the HTTP status code.
	 */
	private final String message;

	/**
	 * Constructs an {@link HttpStatus} with the specified numeric value, type, and message.
	 *
	 * @param value the numeric value of the status code
	 * @param type the type of the status code
	 * @param message the human-readable message associated with the status code
	 */
	HttpStatus(final int value, final Type type, final String message) {
		this.value = value;
		this.type = type;
		this.message = message;
	}

	/**
	 * Returns the numeric value of this status code.
	 *
	 * @return the numeric value of this status code
	 */
	public int value() {
		return value;
	}

	/**
	 * Alias for {@link #value}.
	 *
	 * @return  the numeric value of this status code
	 */
	@Override
	public int getCode() {
		return value();
	}

	/**
	 * Returns the HTTP status type of this status code.
	 *
	 * @return the HTTP status type
	 */
	public Type type() {
		return this.type;
	}

	/**
	 * Returns the message associated with this status code.
	 *
	 * @return the message associated with this status code
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Checks if this status code is of the specified type.
	 *
	 * @param type the type to check against
	 * @return true if this status code is of the specified type, false otherwise
	 */
	public boolean isType(final Type type) {
		return type == type();
	}

	/**
	 * Checks if this status code is in the 1xx informational range.
	 *
	 * @return true if this status code is in the 1xx range, false otherwise
	 */
	public boolean is1xxInformational() {
		return isType(Type.INFORMATIONAL);
	}

	/**
	 * Checks if this status code is in the 2xx successful range.
	 *
	 * @return true if this status code is in the 2xx range, false otherwise
	 */
	public boolean is2xxSuccessful() {
		return isType(Type.SUCCESSFUL);
	}

	/**
	 * Checks if this status code is in the 3xx redirection range.
	 *
	 * @return true if this status code is in the 3xx range, false otherwise
	 */
	public boolean is3xxRedirection() {
		return isType(Type.REDIRECTION);
	}

	/**
	 * Checks if this status code is in the 4xx client error range.
	 *
	 * @return true if this status code is in the 4xx range, false otherwise
	 */
	public boolean is4xxClientError() {
		return isType(Type.CLIENT_ERROR);
	}

	/**
	 * Checks if this status code is in the 5xx server error range.
	 *
	 * @return true if this status code is in the 5xx range, false otherwise
	 */
	public boolean is5xxServerError() {
		return isType(Type.SERVER_ERROR);
	}

	/**
	 * Checks if this status code represents an error (2xx).
	 *
	 * @return true if this status code represents a success, false otherwise
	 */
	@Override
	public boolean isSuccess() {
		return is2xxSuccessful();
	}

	/**
	 * Checks if this status code represents an error (4xx or 5xx).
	 *
	 * @return true if this status code represents an error, false otherwise
	 */
	@Override
	public boolean isError() {
		return is4xxClientError() || is5xxServerError();
	}

	/**
	 * Returns a string representation of this status code.
	 *
	 * @return a string representation of this status code
	 */
	@Override
	public String toString() {
		return value() + " " + name();
	}

	/**
	 * Returns the {@code HttpStatus} enum constant with the specified numeric value.
	 *
	 * @param statusCode the numeric value of the enum to be returned
	 * @return the enum constant with the specified numeric value
	 * @throws IllegalArgumentException if this enum has no constant for the specified numeric value
	 */
	public static HttpStatus from(final int statusCode) {
		return Enums.from(statusCode, NAME_MAP, VALUES);
	}

	/**
	 * Enumeration of HTTP status types.
	 * <p>
	 * Retrievable via {@link HttpStatus#type()}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public enum Type {

		/**
		 * Informational status type (1xx).
		 */
		INFORMATIONAL(1),

		/**
		 * Successful status type (2xx).
		 */
		SUCCESSFUL(2),

		/**
		 * Redirection status type (3xx).
		 */
		REDIRECTION(3),

		/**
		 * Client error status type (4xx).
		 */
		CLIENT_ERROR(4),

		/**
		 * Server error status type (5xx).
		 */
		SERVER_ERROR(5);

		/**
		 * The integer value of the status type (1 for informational, 2 for successful, etc.).
		 */
		private final int value;

		/**
		 * The name map for easy from implementation.
		 */
		private static final Map<Integer, Type> NAME_MAP = Enums.buildNameMap(values(), Type::value);

		/**
		 * Constructs a {@link Type} with the specified integer value.
		 *
		 * @param value the integer value of the status type.
		 */
		Type(final int value) {
			this.value = value;
		}

		/**
		 * Returns the integer value of this status type. Ranges from 1 to 5.
		 *
		 * @return the integer value of this status type
		 */
		public int value() {
			return value;
		}

		/**
		 * Returns the {@code Type} enum constant for the supplied status code.
		 *
		 * @param statusCode the HTTP status code (potentially non-standard)
		 * @return the {@code Type} enum constant for the supplied status code
		 * @throws IllegalArgumentException if this enum has no corresponding constant
		 */
		public static Type from(final int statusCode) {
			int typeCode = statusCode / 100;
			return Enums.from(typeCode, NAME_MAP, values());
		}
	}
}
