package org.apiphany.http;

import java.nio.charset.Charset;
import java.util.List;

import org.apiphany.lang.collections.Lists;

/**
 * Represents a resolved content type when the character set differs from the one already defined.
 *
 * @param contentType the content type
 * @param charset the resolved character set
 *
 * @author Radu Sebastian LAZIN
 */
public record ResolvedContentType(ContentType contentType, Charset charset) {

	/**
	 * Returns the resolved character set.
	 *
	 * @return the resolved character set
	 */
	public Charset resolvedCharset() {
		return null != charset ? charset : contentType.getCharset();
	}

	/**
	 * Returns a resolved content type from the given list of header values.
	 *
	 * @param headerValues the header values list
	 * @return a resolved content type from the given list of header values
	 */
	public static ResolvedContentType parseHeader(final List<String> headerValues) {
		if (Lists.isEmpty(headerValues)) {
			return null;
		}
		for (String headerValue : headerValues) {
			ResolvedContentType contentType = parseHeader(headerValue);
			if (null != contentType) {
				return contentType;
			}
		}
		return null;
	}

	/**
	 * Returns a resolved content type from the given header value.
	 *
	 * @param headerValue the header value
	 * @return a resolved content type from the given header value
	 */
	public static ResolvedContentType parseHeader(final String headerValue) {
	    if (headerValue == null) {
			return null;
		}
	    String[] parts = headerValue.split(";");
	    String mimeType = parts[0].trim().toLowerCase();

	    ContentType type = null;
	    try {
	    	type = ContentType.fromString(mimeType);
	    } catch (IllegalArgumentException e) {
	    	return null;
	    }
	    Charset charset = null;

	    if (parts.length > 1) {
	        for (int i = 1; i < parts.length; i++) {
	            String[] param = parts[i].trim().split("=", 2);
	            if (param.length == 2 && "charset".equalsIgnoreCase(param[0])) {
	                try {
	                    charset = Charset.forName(param[1].trim());
	                } catch (Exception ignored) {
	                	// continue
	                }
	            }
	        }
	    }
	    return new ResolvedContentType(type, charset);
	}
}
