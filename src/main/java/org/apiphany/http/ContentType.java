package org.apiphany.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apiphany.lang.Strings;
import org.morphix.lang.Enums;

/**
 * Represents the values for the {@link HttpHeader#CONTENT_TYPE} header. This enum provides a set of commonly used MIME
 * types and their associated character sets.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ContentType {

	/**
	 * Atom XML content type.
	 */
	APPLICATION_ATOM_XML("application/atom+xml", StandardCharsets.UTF_8),

	/**
	 * Form URL-encoded content type.
	 */
	APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded", StandardCharsets.ISO_8859_1),

	/**
	 * JSON content type.
	 */
	APPLICATION_JSON("application/json", StandardCharsets.UTF_8),

	/**
	 * Newline-delimited JSON content type.
	 */
	APPLICATION_NDJSON("application/x-ndjson", StandardCharsets.UTF_8),

	/**
	 * Binary data (octet-stream) content type.
	 */
	APPLICATION_OCTET_STREAM("application/octet-stream"),

	/**
	 * PDF content type.
	 */
	APPLICATION_PDF("application/pdf", StandardCharsets.UTF_8),

	/**
	 * SOAP XML content type.
	 */
	APPLICATION_SOAP_XML("application/soap+xml", StandardCharsets.UTF_8),

	/**
	 * SVG XML content type.
	 */
	APPLICATION_SVG_XML("application/svg+xml", StandardCharsets.UTF_8),

	/**
	 * XHTML XML content type.
	 */
	APPLICATION_XHTML_XML("application/xhtml+xml", StandardCharsets.UTF_8),

	/**
	 * XML content type.
	 */
	APPLICATION_XML("application/xml", StandardCharsets.UTF_8),

	/**
	 * Problem JSON content type.
	 */
	APPLICATION_PROBLEM_JSON("application/problem+json", StandardCharsets.UTF_8),

	/**
	 * Problem XML content type.
	 */
	APPLICATION_PROBLEM_XML("application/problem+xml", StandardCharsets.UTF_8),

	/**
	 * RSS XML content type.
	 */
	APPLICATION_RSS_XML("application/rss+xml", StandardCharsets.UTF_8),

	/**
	 * BMP image content type.
	 */
	IMAGE_BMP("image/bmp"),

	/**
	 * GIF image content type.
	 */
	IMAGE_GIF("image/gif"),

	/**
	 * JPEG image content type.
	 */
	IMAGE_JPEG("image/jpeg"),

	/**
	 * PNG image content type.
	 */
	IMAGE_PNG("image/png"),

	/**
	 * SVG image content type.
	 */
	IMAGE_SVG("image/svg+xml"),

	/**
	 * TIFF image content type.
	 */
	IMAGE_TIFF("image/tiff"),

	/**
	 * WebP image content type.
	 */
	IMAGE_WEBP("image/webp"),

	/**
	 * Content type for a full HTTP message, used primarily in the response to a TRACE request. The body contains the exact
	 * HTTP message that was received.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-8.3.1">RFC 7230, Section 8.3.1</a>
	 */
	MESSAGE_HTTP("message/http"),

	/**
	 * Multipart form data content type.
	 */
	MULTIPART_FORM_DATA("multipart/form-data", StandardCharsets.ISO_8859_1),

	/**
	 * Multipart mixed content type.
	 */
	MULTIPART_MIXED("multipart/mixed", StandardCharsets.ISO_8859_1),

	/**
	 * Multipart related content type.
	 */
	MULTIPART_RELATED("multipart/related", StandardCharsets.ISO_8859_1),

	/**
	 * HTML text content type.
	 */
	TEXT_HTML("text/html", StandardCharsets.UTF_8),

	/**
	 * Markdown text content type.
	 */
	TEXT_MARKDOWN("text/markdown", StandardCharsets.UTF_8),

	/**
	 * Plain text content type.
	 */
	TEXT_PLAIN("text/plain", StandardCharsets.UTF_8),

	/**
	 * XML text content type.
	 */
	TEXT_XML("text/xml", StandardCharsets.UTF_8),

	/**
	 * Event stream text content type.
	 */
	TEXT_EVENT_STREAM("text/event-stream", StandardCharsets.UTF_8),

	/**
	 * Wild card content type (matches any type).
	 */
	WILDCARD("*/*");

	/**
	 * A map of content type names to their corresponding enum values for easy lookup.
	 */
	private static final Map<String, ContentType> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The MIME type value of the content type.
	 */
	private final String value;

	/**
	 * The character set associated with the content type, if applicable.
	 */
	private final Charset charset;

	/**
	 * Constructs a {@link ContentType} with a MIME type and a character set.
	 *
	 * @param value the MIME type value.
	 * @param charset the character set.
	 */
	ContentType(final String value, final Charset charset) {
		this.value = value;
		this.charset = charset;
	}

	/**
	 * Constructs a {@link ContentType} with a MIME type and no character set.
	 *
	 * @param value the MIME type value.
	 */
	ContentType(final String value) {
		this(value, null);
	}

	/**
	 * Returns the MIME type value of this content type.
	 *
	 * @return the MIME type value.
	 */
	public String value() {
		return value;
	}

	/**
	 * @see #toString()
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Returns the character set associated with this content type, if applicable.
	 *
	 * @return the character set, or null if not specified.
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Returns a {@link ContentType} enum from a string representation of the MIME type.
	 *
	 * @param contentType the MIME type as a string.
	 * @return the corresponding {@link ContentType} enum, or null if no match is found.
	 */
	public static ContentType fromString(final String contentType) {
		return Enums.fromString(contentType, NAME_MAP, values());
	}

	/**
	 * Returns true if this content type is contained in the given content type, false otherwise.
	 *
	 * @param contentType content type
	 * @return true if this content type is contained in the given content type, false otherwise
	 */
	public boolean in(final String contentType) {
		return Strings.safe(contentType).contains(value);
	}
}
