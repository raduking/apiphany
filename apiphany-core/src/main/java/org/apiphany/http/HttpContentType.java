package org.apiphany.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import org.apiphany.ApiMimeType;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Strings;
import org.morphix.lang.Nullables;
import org.morphix.lang.collections.Lists;
import org.morphix.reflection.Constructors;

/**
 * Represents an HTTP content type when the character set differs from the one already defined or when additional
 * parameters (such as boundary) are needed. This is useful for parsing HTTP headers sent from an HTTP server which
 * might decide not to follow the character sets defined in the {@link ContentType} enumeration.
 * <p>
 * TODO: add support for other parameters besides charset and boundary, version for application/* types, format for
 * text/* types, etc.
 *
 * @author Radu Sebastian LAZIN
 */
public class HttpContentType implements ApiMimeType {

	/**
	 * Name space for parameter names.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Param {

		/**
		 * Character set parameter name.
		 */
		public static final String CHARSET = "charset";

		/**
		 * Boundary parameter name.
		 */
		public static final String BOUNDARY = "boundary";

		/**
		 * Hide constructor.
		 */
		private Param() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * The content type.
	 */
	private final ContentType contentType;

	/**
	 * The character set.
	 */
	private final Charset charset;

	/**
	 * The boundary parameter for multipart types.
	 */
	private final String boundary;

	/**
	 * Constructs an HTTP content type object.
	 *
	 * @param contentType the content type
	 * @param charset the character set
	 * @param boundary the boundary parameter (for multipart types)
	 */
	public HttpContentType(final ContentType contentType, final Charset charset, final String boundary) {
		this.contentType = Objects.requireNonNull(contentType);
		this.charset = charset;
		this.boundary = boundary;
	}

	/**
	 * Constructs an HTTP content type object.
	 *
	 * @param contentType the content type
	 * @param charset the character set
	 */
	public HttpContentType(final ContentType contentType, final Charset charset) {
		this(contentType, charset, null);
	}

	/**
	 * Returns an HTTP content type object based on the given content type, character set and boundary.
	 *
	 * @param contentType the content type
	 * @param charset the character set
	 * @param boundary the boundary parameter
	 * @return an HTTP content type object
	 */
	public static HttpContentType of(final ContentType contentType, final Charset charset, final String boundary) {
		return new HttpContentType(contentType, charset, boundary);
	}

	/**
	 * Returns an HTTP content type object based on the given content type and character set.
	 *
	 * @param contentType the content type
	 * @param charset the character set
	 * @return an HTTP content type object
	 */
	public static HttpContentType of(final ContentType contentType, final Charset charset) {
		return new HttpContentType(contentType, charset);
	}

	/**
	 * Returns an HTTP content type object based on the given content type.
	 *
	 * @param contentType the content type
	 * @return an HTTP content type object
	 */
	public static HttpContentType of(final ContentType contentType) {
		return of(contentType, null, null);
	}

	/**
	 * Returns a new builder for constructing HTTP content type objects.
	 *
	 * @return a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the content type.
	 *
	 * @return the content type
	 */
	public ContentType getContentType() {
		return contentType;
	}

	/**
	 * Returns the character set.
	 *
	 * @return the character set
	 */
	public Charset getCharset() {
		return null != charset ? charset : contentType.charset();
	}

	/**
	 * Returns the boundary parameter (for multipart types).
	 *
	 * @return the boundary parameter, or null if not set
	 */
	public String getBoundary() {
		return boundary;
	}

	/**
	 * @see ApiMimeType#charset()
	 */
	@Override
	public Charset charset() {
		return getCharset();
	}

	/**
	 * Alias for {@link #getBoundary()}.
	 *
	 * @return the boundary parameter, or null if not set
	 */
	public String boundary() {
		return getBoundary();
	}

	/**
	 * @see ApiMimeType#value()
	 */
	@Override
	public String value() {
		StringBuilder sb = new StringBuilder();
		sb.append(getContentType().toString());
		Charset activeCharset = charset;
		if (null != activeCharset) {
			sb.append("; ")
					.append(Param.CHARSET)
					.append("=")
					.append(activeCharset);
		}
		String activeBoundary = getBoundary();
		if (null != activeBoundary) {
			sb.append("; ")
					.append(Param.BOUNDARY)
					.append("=")
					.append(activeBoundary);
		}
		return sb.toString();
	}

	/**
	 * Returns the canonical RFC 7231-style string representation of this HTTP content type.
	 * <ul>
	 * <li>type/subtype are lower-cased</li>
	 * <li>charset parameter is included only if not null</li>
	 * <li>boundary parameter is included only if not null</li>
	 * <li>parameter names are lower-cased</li>
	 * <li>charset value is lower-cased</li>
	 * <li>no superfluous whitespace</li>
	 * </ul>
	 *
	 * @return the canonical normalized value
	 */
	public String normalizedValue() {
		StringBuilder sb = new StringBuilder();
		sb.append(getContentType().value().toLowerCase());
		Charset activeCharset = charset;
		if (null != activeCharset) {
			sb.append("; ")
					.append(Param.CHARSET.toLowerCase())
					.append("=")
					.append(activeCharset.name().toLowerCase());
		}
		String activeBoundary = getBoundary();
		if (null != activeBoundary) {
			sb.append("; ")
					.append(Param.BOUNDARY.toLowerCase())
					.append("=")
					.append(activeBoundary);
		}
		return sb.toString();
	}

	/**
	 * @see ApiMimeType#contentType()
	 */
	@Override
	public ContentType contentType() {
		return getContentType();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Returns an HTTP content type from the given list of header values.
	 *
	 * @param headerValues the header values list
	 * @return an HTTP content type from the given list of header values
	 */
	public static HttpContentType parse(final List<String> headerValues) {
		if (Lists.isEmpty(headerValues)) {
			return null;
		}
		for (String headerValue : headerValues) {
			HttpContentType contentType = parse(headerValue);
			if (null != contentType) {
				return contentType;
			}
		}
		return null;
	}

	/**
	 * Returns an HTTP content type from the given header value.
	 * <p>
	 * This method must be called only for single header values (as opposed to multiple header values). If multiple header
	 * values are present, use {@link #parse(List)} instead. To parse multiple header values the list must be constructed
	 * first by splitting the header value on commas.
	 * <ul>
	 * <li>TODO: add proper handling of multiple header values in a single string</li>
	 * <li>TODO: add strict parsing mode which throws exceptions on invalid header values instead of returning null</li>
	 * </ul>
	 *
	 * @param headerValue the header value
	 * @return an HTTP content type from the given header value
	 */
	public static HttpContentType parse(final String headerValue) {
		if (Strings.isEmpty(headerValue)) {
			return null;
		}
		String[] parts = headerValue.split(";");
		String mimeType = parts[0].trim().toLowerCase();

		ContentType type = ContentType.fromString(mimeType, Nullables.supplyNull());
		if (null == type) {
			return null;
		}
		if (parts.length == 1) {
			return HttpContentType.of(type);
		}
		Charset charset = null;
		String boundary = null;
		for (int index = 1; index < parts.length; index++) {
			String[] param = parts[index].trim().split("=", 2);
			if (param.length == 2) {
				if (null == charset && Param.CHARSET.equalsIgnoreCase(param[0])) {
					charset = ApiMimeType.parseCharset(param[1].trim());
				} else if (null == boundary && Param.BOUNDARY.equalsIgnoreCase(param[0])) {
					boundary = param[1].trim();
				}
			}
		}
		return HttpContentType.of(type, charset, boundary);
	}

	/**
	 * Returns a new HTTP content type given the content type and the encoding. If the content type is null or blank, it
	 * defaults to {@code application/octet-stream}. If the encoding is null or blank, the character set is not set and will
	 * be determined by the content type if possible.
	 *
	 * @param type the content type
	 * @param encoding the character set
	 * @return a new HTTP content type
	 */
	public static HttpContentType from(final String type, final String encoding) {
		String contentTypeValue = Strings.isNotBlank(type) ? type.trim() : ContentType.Value.APPLICATION_OCTET_STREAM;
		ContentType contentType = ContentType.fromString(contentTypeValue);
		Charset charset = ApiMimeType.parseCharset(encoding);
		return HttpContentType.of(contentType, charset);
	}

	/**
	 * Fluent builder for {@link HttpContentType}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Builder {

		/**
		 * The content type.
		 */
		private ContentType contentType;

		/**
		 * The character set.
		 */
		private Charset charset;

		/**
		 * The boundary parameter.
		 */
		private String boundary;

		/**
		 * Private constructor, use {@link HttpContentType#builder()}.
		 */
		private Builder() {
			// use factory method
		}

		/**
		 * Sets the content type.
		 *
		 * @param contentType the content type
		 * @return this
		 */
		public Builder contentType(final ContentType contentType) {
			this.contentType = contentType;
			return this;
		}

		/**
		 * Sets the character set.
		 *
		 * @param charset the character set
		 * @return this
		 */
		public Builder charset(final Charset charset) {
			this.charset = charset;
			return this;
		}

		/**
		 * Sets the boundary parameter.
		 *
		 * @param boundary the boundary parameter
		 * @return this
		 */
		public Builder boundary(final String boundary) {
			this.boundary = boundary;
			return this;
		}

		/**
		 * Builds the HTTP content type.
		 *
		 * @return a new HTTP content type
		 */
		public HttpContentType build() {
			return new HttpContentType(contentType, charset, boundary);
		}
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof HttpContentType that) {
			return Objects.equals(this.contentType, that.contentType)
					&& Objects.equals(this.charset, that.charset)
					&& Objects.equals(this.boundary, that.boundary);
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(contentType, charset, boundary);
	}
}
