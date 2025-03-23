package org.apiphany.client.http;

import org.apiphany.ApiMessage;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValuesChain;
import org.apiphany.http.ContentType;
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
	 * Converts the given object to a {@link String} representation.
	 *
	 * @param obj the object to convert.
	 * @param dstClass the target class, which must be {@link String}.
	 * @return the string representation of the object.
	 */
	@Override
	public String from(final Object obj, final Class<String> dstClass) {
		return Strings.safeToString(obj);
	}

	/**
	 * Converts the given object to a {@link String} representation. This method is functionally equivalent to
	 * {@link #from(Object, Class)} but supports generic types.
	 *
	 * @param obj the object to convert.
	 * @param genericDstClass the target generic class, which must be {@link String}.
	 * @return the string representation of the object.
	 */
	@Override
	public String from(final Object obj, final GenericClass<String> genericDstClass) {
		return Strings.safeToString(obj);
	}

	/**
	 * Determines whether this converter can handle the content of the given {@link ApiMessage} based on the provided
	 * headers. This converter supports content with the {@code text/plain} content type.
	 *
	 * @param <U> the type of the content in the {@link ApiMessage}.
	 * @param <V> the type of the headers.
	 * @param message the {@link ApiMessage} containing the content to convert.
	 * @param headers the headers that may influence the conversion.
	 * @return true if the content type is {@code text/plain}, false otherwise.
	 */
	@Override
	public <U, V> boolean isConvertible(final ApiMessage<U> message, final V headers, final HeaderValuesChain headerValuesChain) {
		for (String contentType : getContentTypes(headers, headerValuesChain)) {
			if (contentType.contains(ContentType.TEXT_PLAIN.getValue())) {
				return true;
			}
		}
		return false;
	}

}
