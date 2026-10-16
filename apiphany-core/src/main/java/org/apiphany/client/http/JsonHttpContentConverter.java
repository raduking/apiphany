package org.apiphany.client.http;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.morphix.convert.ObjectConverterException;
import org.morphix.reflection.GenericClass;

/**
 * A {@link ContentConverter} that reads and writes JSON with {@link JsonBuilder}. It handles content whose type is
 * {@code application/json}.
 * <ul>
 * <li>{@link #isConvertible} selects this converter for a response body and for a JSON request body</li>
 * <li>{@link #from(Object, ApiMimeType, Class)} reads a response</li>
 * <li>{@link #toString(Object, ApiMimeType)} writes a request body</li>
 * </ul>
 *
 * @param <T> the type of the object to which the JSON content will be converted
 *
 * @author Radu Sebastian LAZIN
 */
public class JsonHttpContentConverter<T> implements HttpContentConverter<T> {

	/**
	 * Constructs a new {@link JsonHttpContentConverter}. This constructor is intentionally empty, as no special
	 * initialization is required.
	 */
	public JsonHttpContentConverter() {
		// empty
	}

	/**
	 * Returns true when the content type is {@code application/json}. This is the check used for both reading a response
	 * and writing a request body. A null mime type is not convertible.
	 *
	 * @param <H> the type of the headers
	 *
	 * @param mimeType the content type
	 * @param headers the headers that may influence the conversion
	 * @param chain chain of header values extractor that will be used to get a specific header list
	 * @return true if the content type is {@code application/json}, false otherwise
	 * @see ContentConverter#isConvertible(ApiMimeType, Object, HeaderValues)
	 */
	@Override
	public <H> boolean isConvertible(final ApiMimeType mimeType, final H headers, final HeaderValues chain) {
		if (null != mimeType) {
			return ContentType.APPLICATION_JSON == mimeType.contentType();
		}
		return false;
	}

	/**
	 * Converts the given object to an instance of the specified class. This method supports conversion from JSON.
	 *
	 * @param obj the object to convert
	 * @param mimeType the mime type
	 * @param targetClass the target class to which the JSON content will be de-serialized
	 * @return the de-serialized object of type {@code T}
	 * @throws ObjectConverterException if a non-null input cannot be converted
	 * @see ContentConverter#from(Object, ApiMimeType, Class)
	 * @see JsonBuilder#fromJson(Object, Class)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
		T result = JsonBuilder.fromJson(obj, targetClass);
		if (null == result && null != obj) {
			throw new ObjectConverterException("Error converting JSON response to " + targetClass.getName());
		}
		return result;
	}

	/**
	 * Converts the given object to an instance of the specified generic class. This method supports conversion from JSON.
	 *
	 * @param obj the object to convert, which must be JSON
	 * @param mimeType the mime type
	 * @param targetGenericClass the target generic class to which the JSON content will be de-serialized
	 * @return the de-serialized object of type {@code T}
	 * @throws ObjectConverterException if a non-null input cannot be converted
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 * @see JsonBuilder#fromJson(Object, GenericClass)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
		T result = JsonBuilder.fromJson(obj, targetGenericClass);
		if (null == result && null != obj) {
			throw new ObjectConverterException("Error converting JSON response to " + targetGenericClass.getType().getTypeName());
		}
		return result;
	}

	/**
	 * Writes the given object as a JSON request body.
	 *
	 * @param body the request body to write
	 * @param mimeType the content type
	 * @return the JSON string
	 * @see ContentConverter#toString(Object, ApiMimeType)
	 * @see JsonBuilder#toJson(Object)
	 */
	@Override
	public String toString(final Object body, final ApiMimeType mimeType) {
		return JsonBuilder.toJson(body);
	}
}
