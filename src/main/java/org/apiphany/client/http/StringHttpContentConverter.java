package org.apiphany.client.http;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apiphany.ApiMessage;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValuesChain;
import org.apiphany.http.ContentType;
import org.apiphany.http.ResolvedContentType;
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
	 * @see #from(Object, ResolvedContentType, Class)
	 */
	@Override
	public String from(final Object obj, final ResolvedContentType resolvedContentType, final Class<String> dstClass) {
		return from(obj, resolvedContentType);
	}

	/**
	 * @see #from(Object, ResolvedContentType, GenericClass)
	 */
	@Override
	public String from(final Object obj, final ResolvedContentType resolvedContentType, final GenericClass<String> genericDstClass) {
		return Strings.safeToString(obj);
	}

	/**
	 * @see #isConvertible(ApiMessage, ResolvedContentType, Object, HeaderValuesChain)
	 */
	@Override
	public <U, H> boolean isConvertible(final ApiMessage<U> message, final ResolvedContentType resolvedContentType, final H headers,
			final HeaderValuesChain headerValuesChain) {
		if (null != resolvedContentType) {
			return ContentType.TEXT_PLAIN == resolvedContentType.contentType();
		}
		return false;
	}

	/**
	 * Converts the object to {@link String}.
	 *
	 * @param obj the object to convert
	 * @param resolvedContentType the content type of the given object
	 * @return converted object to string
	 */
	public static String from(final Object obj, final ResolvedContentType resolvedContentType) {
		if (obj instanceof String string) {
			return string;
		}
		Charset charset = ResolvedContentType.charset(resolvedContentType);
		if (obj instanceof byte[] bytes) {
			return new String(bytes, charset);
		}
		if (obj instanceof InputStream is) {
			return Strings.toString(is, charset);
		}
		return Strings.safeToString(obj);
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
