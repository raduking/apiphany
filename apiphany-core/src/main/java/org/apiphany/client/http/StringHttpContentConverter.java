package org.apiphany.client.http;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apiphany.ApiMessage;
import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Strings;
import org.morphix.reflection.GenericClass;

/**
 * A {@link ContentConverter} implementation that converts objects to {@link String} representations. This converter is
 * specifically designed to handle plain text content (e.g., {@code text/plain}). It uses
 * {@link Strings#safeToString(Object)} to safely convert objects to strings.
 *
 * @author Radu Sebastian LAZIN
 */
public class StringHttpContentConverter implements HttpContentConverter<String> {

	/**
	 * Constructs a new {@link StringHttpContentConverter}. This constructor is intentionally empty, as no special
	 * initialization is required.
	 */
	public StringHttpContentConverter() {
		// empty
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return the singleton instance
	 */
	public static StringHttpContentConverter instance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, Class)
	 */
	@Override
	public String from(final Object obj, final ApiMimeType mimeType, final Class<String> targetClass) {
		return from(obj, mimeType);
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 */
	@Override
	public String from(final Object obj, final ApiMimeType mimeType, final GenericClass<String> targetGenericClass) {
		return from(obj, mimeType);
	}

	/**
	 * @see ContentConverter#isConvertible(ApiMessage, ApiMimeType, Object, HeaderValues)
	 */
	@Override
	public <U, H> boolean isConvertible(final ApiMessage<U> message, final ApiMimeType mimeType, final H headers, final HeaderValues chain) {
		if (null != mimeType) {
			return ContentType.TEXT_PLAIN == mimeType.contentType();
		}
		return false;
	}

	/**
	 * Converts the object to {@link String}.
	 *
	 * @param obj the object to convert
	 * @param mimeType the content type of the given object
	 * @return converted object to string
	 */
	public static String from(final Object obj, final ApiMimeType mimeType) {
		if (null == obj) {
			return null;
		}
		if (obj instanceof String string) {
			return string;
		}
		Charset charset = ApiMimeType.charset(mimeType);
		return switch (obj) {
			case byte[] bytes -> new String(bytes, charset);
			case InputStream is -> Strings.toString(is, charset);
			default -> Strings.safeToString(obj);
		};
	}

	/**
	 * Singleton instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Actual singleton instance.
		 */
		private static final StringHttpContentConverter INSTANCE = new StringHttpContentConverter();
	}

}
