package org.apiphany.client.http;

import java.util.function.Function;
import java.util.function.Supplier;

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
	 * and writing a request body. A {@code null} mime type is not convertible.
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
	 * Generic method that converts an object to JSON.
	 *
	 * @param obj object to convert
	 * @param toJson function used to convert
	 * @param targetTypeNameSupplier the target type name supplier
	 * @return de-serialized object
	 */
	protected T from(final Object obj, final Function<Object, T> toJson, final Supplier<String> targetTypeNameSupplier) {
		if (null == obj) {
			return null;
		}
		T result = toJson.apply(obj);
		if (null == result) {
			throw converterException(targetTypeNameSupplier.get());
		}
		return result;
	}

	/**
	 * Creates an {@link ObjectConverterException} when converting to a target type name.
	 *
	 * @param targetTypeName the target type name contained in the message
	 * @return an object converter exception
	 */
	protected ObjectConverterException converterException(final String targetTypeName) {
		return converterException(targetTypeName, null);
	}

	/**
	 * Creates an {@link ObjectConverterException} when converting to a target type name.
	 *
	 * @param targetTypeName the target type name contained in the message
	 * @param cause the cause
	 * @return an object converter exception
	 */
	protected ObjectConverterException converterException(final String targetTypeName, final Throwable cause) {
		return new ObjectConverterException("Error converting JSON response to " + targetTypeName, cause);
	}

	/**
	 * Converts the given object to an instance of the specified class. This method supports conversion from JSON.
	 *
	 * @param obj the object to convert
	 * @param mimeType the mime type
	 * @param targetClass the target class to which the JSON content will be de-serialized
	 * @return the de-serialized object of type {@code T}, or {@code null} if the input object is {@code null}
	 * @throws ObjectConverterException if a non-{@code null} input cannot be converted
	 * @see ContentConverter#from(Object, ApiMimeType, Class)
	 * @see JsonBuilder#fromJson(Object, Class)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
		return from(obj, o -> JsonBuilder.fromJson(o, targetClass), targetClass::getName);
	}

	/**
	 * Converts the given object to an instance of the specified generic class. This method supports conversion from JSON.
	 *
	 * @param obj the object to convert, which must be JSON
	 * @param mimeType the mime type
	 * @param targetGenericClass the target generic class to which the JSON content will be de-serialized
	 * @return the de-serialized object of type {@code T}, or {@code null} if the input object is {@code null}
	 * @throws ObjectConverterException if a non-{@code null} input cannot be converted
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 * @see JsonBuilder#fromJson(Object, GenericClass)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
		return from(obj, o -> JsonBuilder.fromJson(o, targetGenericClass), () -> targetGenericClass.getType().getTypeName());
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
