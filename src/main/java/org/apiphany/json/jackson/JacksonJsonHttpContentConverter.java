package org.apiphany.json.jackson;

import org.apiphany.ApiMessage;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.http.HttpContentConverter;
import org.apiphany.header.HeaderValuesChain;
import org.apiphany.http.ContentType;
import org.morphix.reflection.GenericClass;

/**
 * A {@link ContentConverter} implementation that converts JSON content to objects of type {@code T}. This converter
 * uses the {@link JacksonJsonBuilder} to de-serialize JSON strings into Java objects. It supports content with the
 * {@code application/json} content type.
 *
 * @param <T> the type of the object to which the JSON content will be converted.
 * @author Radu Sebastian LAZIN
 */
public class JacksonJsonHttpContentConverter<T> implements HttpContentConverter<T> {

	/**
	 * Constructs a new {@link JacksonJsonHttpContentConverter}. This constructor is intentionally empty, as no special
	 * initialization is required.
	 */
	public JacksonJsonHttpContentConverter() {
		// empty
	}

	/**
	 * Converts the given object to an instance of the specified class. This method supports conversion from JSON strings.
	 *
	 * @param obj the object to convert, which must be a JSON string.
	 * @param dstClass the target class to which the JSON content will be de-serialized.
	 * @return the de-serialized object of type {@code T}.
	 * @throws UnsupportedOperationException if the input object is not a JSON string.
	 */
	@Override
	public T from(final Object obj, final Class<T> dstClass) {
		if (obj instanceof String str) {
			return JacksonJsonBuilder.fromJson(str, dstClass);
		}
		throw new UnsupportedOperationException("Conversion from " + obj.getClass().getCanonicalName() + " is not supported.");
	}

	/**
	 * Converts the given object to an instance of the specified generic class. This method supports conversion from JSON
	 * strings.
	 *
	 * @param obj the object to convert, which must be a JSON string.
	 * @param genericDstClass the target generic class to which the JSON content will be de-serialized.
	 * @return the de-serialized object of type {@code T}.
	 * @throws UnsupportedOperationException if the input object is not a JSON string.
	 */
	@Override
	public T from(final Object obj, final GenericClass<T> genericDstClass) {
		if (obj instanceof String str) {
			return JacksonJsonBuilder.fromJson(str, genericDstClass);
		}
		throw new UnsupportedOperationException("Conversion from " + obj.getClass().getCanonicalName() + " is not supported.");
	}

	/**
	 * Determines whether this converter can handle the content of the given {@link ApiMessage} based on the provided
	 * headers. This converter supports content with the {@code application/json} content type.
	 *
	 * @param <U> the type of the content in the {@link ApiMessage}.
	 * @param <V> the type of the headers.
	 * @param message the {@link ApiMessage} containing the content to convert.
	 * @param headers the headers that may influence the conversion.
	 * @return true if the content type is {@code application/json}, false otherwise.
	 */
	@Override
	public <U, V> boolean isConvertible(final ApiMessage<U> message, final V headers, final HeaderValuesChain headerValuesChain) {
		for (String contentType : getContentTypes(headers, headerValuesChain)) {
			if (contentType.contains(ContentType.APPLICATION_JSON.value())) {
				return true;
			}
		}
		return false;
	}
}
