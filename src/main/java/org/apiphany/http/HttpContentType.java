package org.apiphany.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import org.apiphany.ApiMimeType;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.morphix.lang.Nullables;
import org.morphix.reflection.Constructors;

/**
 * Represents a HTTP content type when the character set differs from the one already defined. This is useful for
 * parsing HTTP headers sent from an HTTP server which might decide not to follow the character sets defined in the
 * {@link ContentType} enumeration.
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
	 * Constructs an HTTP content type object.
	 *
	 * @param contentType the content type
	 * @param charset the character set
	 */
	public HttpContentType(final ContentType contentType, final Charset charset) {
		this.contentType = Objects.requireNonNull(contentType);
		this.charset = null != charset ? charset : contentType.charset();
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
		return of(contentType, null);
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
		return charset;
	}

	/**
	 * @see #charset()
	 */
	@Override
	public Charset charset() {
		return getCharset();
	}

	/**
	 * @see #value()
	 */
	@Override
	public String value() {
		StringBuilder sb = new StringBuilder();
		sb.append(getContentType().toString());
		if (null != getCharset()) {
			sb.append("; ")
					.append(Param.CHARSET)
					.append("=")
					.append(getCharset());
		}
		return sb.toString();
	}

	/**
	 * @see #contentType()
	 */
	@Override
	public ContentType contentType() {
		return getContentType();
	}

	/**
	 * @see #toString()
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Returns a HTTP content type from the given list of header values.
	 *
	 * @param headerValues the header values list
	 * @return a HTTP content type from the given list of header values
	 */
	public static HttpContentType parseHeader(final List<String> headerValues) {
		if (Lists.isEmpty(headerValues)) {
			return null;
		}
		for (String headerValue : headerValues) {
			HttpContentType contentType = parseHeaderValue(headerValue);
			if (null != contentType) {
				return contentType;
			}
		}
		return null;
	}

	/**
	 * Returns a HTTP content type from the given header value.
	 *
	 * @param headerValue the header value
	 * @return a HTTP content type from the given header value
	 */
	public static HttpContentType parseHeaderValue(final String headerValue) {
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
		int index = 0;
		while (null == charset && index++ < parts.length) {
			String[] param = parts[index].trim().split("=", 2);
			if (param.length == 2 && Param.CHARSET.equalsIgnoreCase(param[0])) {
				charset = ApiMimeType.parseCharset(param[1].trim());
			}
		}
		return HttpContentType.of(type, charset);
	}

	/**
	 * Returns a HTTP content type from the given header value. This is an alias for {@link #parseHeaderValue(String)}.
	 *
	 * @param headerValue the header value
	 * @return a HTTP content type from the given header value
	 */
	public static HttpContentType parseHeader(final String headerValue) {
		return parseHeaderValue(headerValue);
	}

	/**
	 * Returns a new HTTP content type given the content type and the encoding.
	 *
	 * @param type the content type
	 * @param encoding the character set
	 * @return a new HTTP content type
	 */
	public static HttpContentType from(final String type, final String encoding) {
		ContentType contentType = ContentType.fromString(type);
		Charset charset = ApiMimeType.parseCharset(encoding);
		return HttpContentType.of(contentType, charset);
	}

	/**
	 * @see #equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof HttpContentType that) {
			return Objects.equals(contentType, that.contentType)
					&& Objects.equals(charset, that.charset);
		}
		return false;
	}

	/**
	 * @see #hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(contentType, charset);
	}
}
