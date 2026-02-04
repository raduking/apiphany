package org.apiphany.json.jackson2;

import org.apiphany.ApiMessage;
import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.http.HttpContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.io.ContentType;
import org.morphix.convert.ObjectConverterException;
import org.morphix.reflection.GenericClass;

/**
 * A {@link ContentConverter} implementation that converts JSON content to objects of type {@code T}. This converter
 * uses the {@link Jackson2JsonBuilder} to de-serialize JSON strings into Java objects. It supports content with the
 * {@code application/json} content type.
 *
 * @param <T> the type of the object to which the JSON content will be converted.
 *
 * @author Radu Sebastian LAZIN
 */
public class Jackson2JsonHttpContentConverter<T> implements HttpContentConverter<T> {

	/**
	 * Constructs a new {@link Jackson2JsonHttpContentConverter}. This constructor is intentionally empty, as no special
	 * initialization is required.
	 */
	public Jackson2JsonHttpContentConverter() {
		// empty
	}

	/**
	 * Converts the given object to an instance of the specified class. This method supports conversion from JSON strings.
	 *
	 * @param obj the object to convert
	 * @param mimeType the mime type
	 * @param targetClass the target class to which the JSON content will be de-serialized
	 * @return the de-serialized object of type {@code T}
	 * @throws UnsupportedOperationException if the input object is not a JSON string
	 * @see ContentConverter#from(Object, ApiMimeType, Class)
	 * @see Jackson2JsonBuilder#fromJson(Object, Class)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
		T result = Jackson2JsonBuilder.fromJson(obj, targetClass);
		if (null == result && null != obj) {
			throw new ObjectConverterException("Error converting JSON response to " + targetClass.getName());
		}
		return result;
	}

	/**
	 * Converts the given object to an instance of the specified generic class. This method supports conversion from JSON
	 * strings.
	 *
	 * @param obj the object to convert, which must be a JSON string
	 * @param targetGenericClass the target generic class to which the JSON content will be de-serialized
	 * @return the de-serialized object of type {@code T}
	 * @throws UnsupportedOperationException if the input object is not a JSON
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 * @see Jackson2JsonBuilder#fromJson(Object, GenericClass)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
		T result = Jackson2JsonBuilder.fromJson(obj, targetGenericClass);
		if (null == result && null != obj) {
			throw new ObjectConverterException("Error converting JSON response to " + targetGenericClass.getType().getTypeName());
		}
		return result;
	}

	/**
	 * Determines whether this converter can handle the content of the given {@link ApiMessage} based on the provided
	 * headers. This converter supports content with the {@code application/json} content type.
	 *
	 * @param <U> the type of the content in the {@link ApiMessage}
	 * @param <V> the type of the headers
	 *
	 * @param message the {@link ApiMessage} containing the content to convert
	 * @param mimeType the content type
	 * @param headers the headers that may influence the conversion
	 * @param chain chain of header values extractor that will be used to get a specific header list
	 * @return true if the content type is {@code application/json}, false otherwise
	 * @see ContentConverter#isConvertible(ApiMessage, ApiMimeType, Object, HeaderValues)
	 */
	@Override
	public <U, V> boolean isConvertible(final ApiMessage<U> message, final ApiMimeType mimeType, final V headers, final HeaderValues chain) {
		if (null != mimeType) {
			return ContentType.APPLICATION_JSON == mimeType.contentType();
		}
		return false;
	}
}
