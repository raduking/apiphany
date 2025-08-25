package org.apiphany;

import java.nio.charset.Charset;

import org.apiphany.io.ContentType;
import org.apiphany.lang.Strings;

/**
 * Interface describing an API mime type.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ApiMimeType {

	/**
	 * Returns the mime type string value.
	 *
	 * @return the mime type string value
	 */
	String value();

	/**
	 * Returns the character set associated with this mime type, can be {@code null}.
	 *
	 * @return the character set associated with this mime type
	 */
	Charset charset();

	/**
	 * Returns the content type associated with this mime type.
	 *
	 * @return the content type associated with this mime type
	 */
	ContentType contentType();

	/**
	 * Returns the character set from an API mime type, or {@link Strings#DEFAULT_CHARSET} if the character set cannot be
	 * extracted or the API mime type is {@code null}.
	 *
	 * @param mimiType the mime type
	 * @return the character set from a mime type
	 */
	public static Charset charset(final ApiMimeType mimiType) {
		if (null == mimiType) {
			return Strings.DEFAULT_CHARSET;
		}
		Charset charset = mimiType.charset();
		if (null == charset) {
			return Strings.DEFAULT_CHARSET;
		}
		return charset;
	}

	/**
	 * Parses a character set and returns it if successfully parsed, {@code null} otherwise.
	 *
	 * @param charset character set string
	 * @return a character set
	 */
	public static Charset parseCharset(final String charset) {
		try {
			return Charset.forName(charset);
		} catch (Exception e) {
			return null;
		}
	}
}
