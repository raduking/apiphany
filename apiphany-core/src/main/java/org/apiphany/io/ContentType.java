package org.apiphany.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

import org.apiphany.ApiMimeType;
import org.apiphany.lang.Strings;
import org.morphix.lang.Enums;
import org.morphix.reflection.Constructors;

/**
 * This enum provides a set of commonly used MIME types and their associated character sets.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ContentType implements ApiMimeType {

	/**
	 * Atom XML content type.
	 */
	APPLICATION_ATOM_XML(Value.APPLICATION_ATOM_XML, StandardCharsets.UTF_8),

	/**
	 * Form URL-encoded content type.
	 */
	APPLICATION_FORM_URLENCODED(Value.APPLICATION_FORM_URLENCODED, StandardCharsets.ISO_8859_1),

	/**
	 * JSON content type.
	 */
	APPLICATION_JSON(Value.APPLICATION_JSON, StandardCharsets.UTF_8),

	/**
	 * Newline-delimited JSON content type.
	 */
	APPLICATION_NDJSON(Value.APPLICATION_NDJSON, StandardCharsets.UTF_8),

	/**
	 * Binary data (octet-stream) content type.
	 */
	APPLICATION_OCTET_STREAM(Value.APPLICATION_OCTET_STREAM),

	/**
	 * PDF content type.
	 */
	APPLICATION_PDF(Value.APPLICATION_PDF, StandardCharsets.UTF_8),

	/**
	 * SOAP XML content type.
	 */
	APPLICATION_SOAP_XML(Value.APPLICATION_SOAP_XML, StandardCharsets.UTF_8),

	/**
	 * SVG XML content type.
	 */
	APPLICATION_SVG_XML(Value.APPLICATION_SVG_XML, StandardCharsets.UTF_8),

	/**
	 * XHTML XML content type.
	 */
	APPLICATION_XHTML_XML(Value.APPLICATION_XHTML_XML, StandardCharsets.UTF_8),

	/**
	 * XML content type.
	 */
	APPLICATION_XML(Value.APPLICATION_XML, StandardCharsets.UTF_8),

	/**
	 * Problem JSON content type.
	 */
	APPLICATION_PROBLEM_JSON(Value.APPLICATION_PROBLEM_JSON, StandardCharsets.UTF_8),

	/**
	 * Problem XML content type.
	 */
	APPLICATION_PROBLEM_XML(Value.APPLICATION_PROBLEM_XML, StandardCharsets.UTF_8),

	/**
	 * RSS XML content type.
	 */
	APPLICATION_RSS_XML(Value.APPLICATION_RSS_XML, StandardCharsets.UTF_8),

	/**
	 * BMP image content type.
	 */
	IMAGE_BMP(Value.IMAGE_BMP),

	/**
	 * GIF image content type.
	 */
	IMAGE_GIF(Value.IMAGE_GIF),

	/**
	 * JPEG image content type.
	 */
	IMAGE_JPEG(Value.IMAGE_JPEG),

	/**
	 * PNG image content type.
	 */
	IMAGE_PNG(Value.IMAGE_PNG),

	/**
	 * SVG image content type.
	 */
	IMAGE_SVG(Value.IMAGE_SVG),

	/**
	 * TIFF image content type.
	 */
	IMAGE_TIFF(Value.IMAGE_TIFF),

	/**
	 * WebP image content type.
	 */
	IMAGE_WEBP(Value.IMAGE_WEBP),

	/**
	 * Content type for a full HTTP message, used primarily in the response to a TRACE request. The body contains the exact
	 * HTTP message that was received.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-8.3.1">RFC 7230, Section 8.3.1</a>
	 */
	MESSAGE_HTTP(Value.MESSAGE_HTTP),

	/**
	 * Multipart form data content type.
	 */
	MULTIPART_FORM_DATA(Value.MULTIPART_FORM_DATA, StandardCharsets.ISO_8859_1),

	/**
	 * Multipart mixed content type.
	 */
	MULTIPART_MIXED(Value.MULTIPART_MIXED, StandardCharsets.ISO_8859_1),

	/**
	 * Multipart related content type.
	 */
	MULTIPART_RELATED(Value.MULTIPART_RELATED, StandardCharsets.ISO_8859_1),

	/**
	 * HTML text content type.
	 */
	TEXT_HTML(Value.TEXT_HTML, StandardCharsets.UTF_8),

	/**
	 * Markdown text content type.
	 */
	TEXT_MARKDOWN(Value.TEXT_MARKDOWN, StandardCharsets.UTF_8),

	/**
	 * Plain text content type.
	 */
	TEXT_PLAIN(Value.TEXT_PLAIN, StandardCharsets.UTF_8),

	/**
	 * XML text content type.
	 */
	TEXT_XML(Value.TEXT_XML, StandardCharsets.UTF_8),

	/**
	 * Event stream text content type.
	 */
	TEXT_EVENT_STREAM(Value.TEXT_EVENT_STREAM, StandardCharsets.UTF_8),

	/**
	 * Wild card content type (matches any type).
	 */
	WILDCARD(Value.WILDCARD);

	/**
	 * Name space for all the {@link String} values used by this enumeration so that they can be used in annotations since
	 * they need constant expressions.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Value {

		/**
		 * Atom XML content type.
		 */
		public static final String APPLICATION_ATOM_XML = "application/atom+xml";

		/**
		 * Form URL-encoded content type.
		 */
		public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

		/**
		 * JSON content type.
		 */
		public static final String APPLICATION_JSON = "application/json";

		/**
		 * Newline-delimited JSON content type.
		 */
		public static final String APPLICATION_NDJSON = "application/x-ndjson";

		/**
		 * Binary data (octet-stream) content type.
		 */
		public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

		/**
		 * PDF content type.
		 */
		public static final String APPLICATION_PDF = "application/pdf";

		/**
		 * SOAP XML content type.
		 */
		public static final String APPLICATION_SOAP_XML = "application/soap+xml";

		/**
		 * SVG XML content type.
		 */
		public static final String APPLICATION_SVG_XML = "application/svg+xml";

		/**
		 * XHTML XML content type.
		 */
		public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

		/**
		 * XML content type.
		 */
		public static final String APPLICATION_XML = "application/xml";

		/**
		 * Problem JSON content type.
		 */
		public static final String APPLICATION_PROBLEM_JSON = "application/problem+json";

		/**
		 * Problem XML content type.
		 */
		public static final String APPLICATION_PROBLEM_XML = "application/problem+xml";

		/**
		 * RSS XML content type.
		 */
		public static final String APPLICATION_RSS_XML = "application/rss+xml";

		/**
		 * BMP image content type.
		 */
		public static final String IMAGE_BMP = "image/bmp";

		/**
		 * GIF image content type.
		 */
		public static final String IMAGE_GIF = "image/gif";

		/**
		 * JPEG image content type.
		 */
		public static final String IMAGE_JPEG = "image/jpeg";

		/**
		 * PNG image content type.
		 */
		public static final String IMAGE_PNG = "image/png";

		/**
		 * SVG image content type.
		 */
		public static final String IMAGE_SVG = "image/svg+xml";

		/**
		 * TIFF image content type.
		 */
		public static final String IMAGE_TIFF = "image/tiff";

		/**
		 * WebP image content type.
		 */
		public static final String IMAGE_WEBP = "image/webp";

		/**
		 * Content type for a full HTTP message, used primarily in the response to a TRACE request. The body contains the exact
		 * HTTP message that was received.
		 *
		 * @see <a href="https://tools.ietf.org/html/rfc7230#section-8.3.1">RFC 7230, Section 8.3.1</a>
		 */
		public static final String MESSAGE_HTTP = "message/http";

		/**
		 * Multipart form data content type.
		 */
		public static final String MULTIPART_FORM_DATA = "multipart/form-data";

		/**
		 * Multipart mixed content type.
		 */
		public static final String MULTIPART_MIXED = "multipart/mixed";

		/**
		 * Multipart related content type.
		 */
		public static final String MULTIPART_RELATED = "multipart/related";

		/**
		 * HTML text content type.
		 */
		public static final String TEXT_HTML = "text/html";

		/**
		 * Markdown text content type.
		 */
		public static final String TEXT_MARKDOWN = "text/markdown";

		/**
		 * Plain text content type.
		 */
		public static final String TEXT_PLAIN = "text/plain";

		/**
		 * XML text content type.
		 */
		public static final String TEXT_XML = "text/xml";

		/**
		 * Event stream text content type.
		 */
		public static final String TEXT_EVENT_STREAM = "text/event-stream";

		/**
		 * Wild card content type (matches any type).
		 */
		public static final String WILDCARD = "*/*";

		/**
		 * Hide constructor.
		 */
		private Value() {
			throw Constructors.unsupportedOperationException();
		}
	}

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
	 * @param value the MIME type value
	 * @param charset the character set
	 */
	ContentType(final String value, final Charset charset) {
		this.value = value;
		this.charset = charset;
	}

	/**
	 * Constructs a {@link ContentType} with a MIME type and no character set.
	 *
	 * @param value the MIME type value
	 */
	ContentType(final String value) {
		this(value, null);
	}

	/**
	 * Returns the MIME type value of this content type.
	 *
	 * @return the MIME type value
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

	/**
	 * Returns the character set associated with this content type, if applicable.
	 *
	 * @return the character set, or null if not specified
	 */
	@Override
	public Charset charset() {
		return charset;
	}

	/**
	 * @see ApiMimeType#contentType()
	 */
	@Override
	public ContentType contentType() {
		return this;
	}

	/**
	 * Returns a {@link ContentType} enum from a string representation of the MIME type.
	 *
	 * @param contentType the MIME type as a string
	 * @return the corresponding {@link ContentType} enum, or null if no match is found
	 */
	public static ContentType fromString(final String contentType) {
		return Enums.fromString(contentType, NAME_MAP, values());
	}

	/**
	 * Returns a {@link ContentType} enum from a string representation of the MIME type.
	 *
	 * @param contentType the MIME type as a string
	 * @param defaultValueSupplier the default value supplied when content type cannot be parsed
	 * @return the corresponding {@link ContentType} enum, or null if no match is found
	 */
	public static ContentType fromString(final String contentType, final Supplier<ContentType> defaultValueSupplier) {
		return Enums.from(contentType, NAME_MAP, defaultValueSupplier);
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
