package org.apiphany.json.jackson3;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.http.JsonHttpContentConverter;
import org.morphix.reflection.GenericClass;

/**
 * A {@link ContentConverter} implementation that converts JSON content to objects of type {@code T}. This converter
 * uses the {@link Jackson3JsonBuilder} to de-serialize JSON strings into Java objects. It supports content with the
 * {@code application/json} content type.
 *
 * @param <T> the type of the object to which the JSON content will be converted.
 *
 * @author Radu Sebastian LAZIN
 */
public class Jackson3JsonHttpContentConverter<T> extends JsonHttpContentConverter<T> {

	/**
	 * Constructs a new {@link Jackson3JsonHttpContentConverter}. This constructor is intentionally empty, as no special
	 * initialization is required.
	 */
	public Jackson3JsonHttpContentConverter() {
		// empty
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, Class)
	 * @see Jackson3JsonBuilder#fromJson(Object, Class)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
		return from(obj, o -> Jackson3JsonBuilder.fromJson(o, targetClass), targetClass::getName);
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 * @see Jackson3JsonBuilder#fromJson(Object, GenericClass)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
		return from(obj, o -> Jackson3JsonBuilder.fromJson(o, targetGenericClass), () -> targetGenericClass.getType().getTypeName());
	}

	/**
	 * @see ContentConverter#toString(Object, ApiMimeType)
	 * @see Jackson3JsonBuilder#toJson(Object)
	 */
	@Override
	public String toString(final Object body, final ApiMimeType mimeType) {
		return Jackson3JsonBuilder.toJson(body);
	}
}
